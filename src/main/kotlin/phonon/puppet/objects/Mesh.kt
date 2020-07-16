/**
 * Wrapper around a 3D armor stand model
 * 
 * Also contains companion object manager for managing
 * the model library that maps String "Name" -> CustomModelData
 */

package phonon.puppet.objects

import java.util.UUID
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.util.EulerAngle 
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.inventory.ItemStack
import phonon.puppet.Puppet
import phonon.puppet.Message
import phonon.puppet.math.*

// experimentally measured length of armorstand head part offset
const val ARMOR_STAND_OFFSET: Float = 0.253731f

// scaling factor in minecraft .json model units -> minecraft world block units
// 1. block models size 16 -> scale 1/16
// 2. armor stand head scaling is 0.625
const val MODEL_SCALE: Float = 0.0390625f

// fixed vertical offset for armor stand location
const val MODEL_VERTICAL_OFFSET: Float = 1.43f

// material used for custom models
val MATERIAL_CUSTOM_MODEL = Material.BONE

public class Mesh(
    val name: String,
    var world: World,
    val model: String, // custom model name
    var customModelData: Int = 0, // entity CustomModelData id
    initialPosition: Vector3f,
    initialRotation: Euler,
    initialMatrix: Matrix4f? = null  // takes priority over initial position, rotation
): TransformGraphNode {

    // scene graph
    override var parent: TransformGraphNode? = null
    override val children: ArrayList<TransformGraphNode> = arrayListOf()

    // transform
    override val matrix: Matrix4f = Matrix4f.identity()
    override val worldMatrix: Matrix4f = Matrix4f.identity()
    override val position: Vector3f = Vector3f.zero()
    override val rotation: Euler = Euler.zero()
    override val quaternion: Quaternion = Quaternion.new()

    // dirty flag
    override val needsUpdate: Boolean = false

    // identifier
    val uuid: UUID = UUID.randomUUID()

    // armor stand to render models
    val armorStand: ArmorStand

    // buffer for writing armorstand location
    private val _locationBuffer: Location    

    // buffer for extracting world space rotation
    private val _rotationBuffer: Euler = Euler.zero()

    // buffer for writing rotation to armor stand
    private val _rotationBuffer1: EulerAngle = EulerAngle(0.0, 0.0, 0.0)

    init {
        // initialize transform
        if ( initialMatrix !== null ) {
            this.matrix.copy(initialMatrix)
            initialMatrix.decompose(this.position, this.quaternion)
            this.rotation.setFromQuaternion(this.quaternion, this.rotation.order)
        }
        else {
            this.position.copy(initialPosition)
            this.rotation.copy(initialRotation)
            this.quaternion.setFromEuler(initialRotation)
            this.matrix.compose(position, quaternion)
        }
        
        // load custom model data if name specified instead
        if ( customModelData == 0 ) {
            val customModelData = Mesh.get(this.model)
            if ( customModelData !== null ) {
                this.customModelData = customModelData
            }
        }

        // create armor stand
        val loc = Location(world, position.x.toDouble(), position.y.toDouble(), position.z.toDouble())
        val stand = world.spawn(loc, ArmorStand::class.java)
        stand.setGravity(false)
        stand.setSilent(true)
        // stand.setMarker(true)
        stand.setBasePlate(false)
        stand.setArms(false)
        stand.setVisible(false) // todo when ready
        
        // set armor stand initial model
        val itemStack = ItemStack(MATERIAL_CUSTOM_MODEL, 1)
        val meta = itemStack.getItemMeta()!!
        meta.setCustomModelData(this.customModelData)
        itemStack.setItemMeta(meta)

        val equipment = stand.getEquipment()!!
        equipment.setHelmet(itemStack)

        // save references
        this.armorStand = stand
        this._locationBuffer = loc // re-use location as buffer

        // perform initial render to set armorstand state
        this.render()
    }
    
    class Builder() {
        var _world = Bukkit.getWorlds()[0]
        var _name: String = ""
        var _model: String = ""
        var _customModelData: Int = 0
        val _position = Vector3f.zero()
        val _rotation = Euler.zero()
        var _matrix: Matrix4f? = null

        fun world(w: World) = apply { this._world = w }
        fun name(s: String) = apply { this._name = s }
        fun model(s: String) = apply { this._model = s }
        fun customModelData(id: Int) = apply { this._customModelData = id }
        fun position(x: Float, y: Float, z: Float) = apply { this._position.set(x, y, z) }
        fun position(x: Double, y: Double, z: Double) = apply { this._position.set(x, y, z) }
        fun rotation(x: Float, y: Float, z: Float) = apply { this._rotation.set(x, y, z, this._rotation.order) }
        fun rotation(x: Double, y: Double, z: Double) = apply { this._rotation.set(x, y, z, this._rotation.order) }
        fun matrix(m: Matrix4f) = apply { this._matrix = m }
        
        fun build() = Mesh(this._name, this._world, this._model, this._customModelData, this._position, this._rotation, this._matrix)
    }

    /**
     * Cleanup armor stands, cleanup tree
     */
    override public fun destroy() {
        for ( child in this.children ) {
            child.destroy()
        }

        this.armorStand.remove()
    }

    // update world transform
    override public fun updateTransform() {
        // update local transform
        this.matrix.compose(this.position, this.quaternion)

        // update world transform
        val parent = this.parent
        if ( parent !== null ) {
            this.worldMatrix.multiplyMatrices(parent.worldMatrix, this.matrix)
        }
        else {
            this.worldMatrix.copy(this.matrix)
        }

        // update children
        for ( child in this.children ) {
            child.updateTransform()
        }
    }

    /**
     * Render state into attached armor stand.
     * Must convert rotation extrinsic euler (x, y, z) -> intrinsic (pitch, yaw, roll)
     * 
     * Minecraft armor stand head rotation order: R = Rx Ry Rz
     * First z-rotation, then y-, then x- rotations applied
     *
     * R is our world-space rotation matrix, we must solve for Rx, Ry, Rz
     */
    public fun render() {
        // rotate the normally upright armor stand offset vector
        val armorStandOffset = Vector3f(0f, ARMOR_STAND_OFFSET, 0f).applyRotationMatrix4(this.worldMatrix)

        // extract position components directly from world matrix
        // and remove offset caused by armorstand head extension
        this._locationBuffer.x = (this.worldMatrix[m03] - armorStandOffset.x).toDouble()
        this._locationBuffer.y = (this.worldMatrix[m13] - armorStandOffset.y - MODEL_VERTICAL_OFFSET).toDouble()
        this._locationBuffer.z = (this.worldMatrix[m23] - armorStandOffset.z).toDouble()
        
        // get local euler angle
        this._rotationBuffer.setFromRotationMatrix(this.worldMatrix, Euler.ZYX)

        // create minecraft-format euler angle buffer (immutable)
        val rotationBufferOut = EulerAngle(
            this._rotationBuffer.x.toDouble(),
            -this._rotationBuffer.y.toDouble(),
            -this._rotationBuffer.z.toDouble()
        )

        // apply transform
        val stand = this.armorStand
        stand.teleport(this._locationBuffer)
        stand.setHeadPose(rotationBufferOut)
    }


    // Mesh static functions for managing model library
    companion object {
        // map name of model -> custom model data index
        public val library: HashMap<String, Int> = hashMapOf()

        // list of all models
        public var libraryList: List<String> = listOf()

        /**
         * Load list of models and map name to index order in list.
         * NOTE: offset index + 1 because CustomModelData starts at Integer = 1
         * in puppet format
         */
        fun loadCustomModelData(models: List<String>) {
            // clear current data
            Mesh.library.clear()

            for ( (i, name) in models.withIndex() ) {
                Mesh.library.set(name, i+1)
            }

            Mesh.libraryList = models
        }

        /**
         * Delete data in library
         */
        public fun clear() {
            Mesh.library.clear()
            Mesh.libraryList = listOf()
        }

        /**
         * Return if library has given model type
         * @param type name of model type
         */
        fun has(type: String): Boolean {
            return Mesh.library.contains(type)
        }

        /**
         * Return custom model data index
         * @param type model name
         */
        fun get(name: String): Int? {
            return Mesh.library.get(name)
        }

        /**
         * Print list of library custom models
         * @param p target to print messages to
         */
        fun print(p: CommandSender) {
            for ( (i, name) in Mesh.libraryList.withIndex() ) {
                Message.print(p, "[${i+1}]: ${name}")
            }
        }
    }
}