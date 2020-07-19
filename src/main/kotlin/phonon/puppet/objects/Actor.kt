/**
 * Actor
 * 
 * High level container for a visual 3D object composed of:
 * - Mesh graph for actual models
 * - Skeleton bone graph for manipulating model
 * - Animation mixer for playing animations
 */

package phonon.puppet.objects

import java.util.Random
import java.util.EnumSet
import java.util.EnumMap
import java.util.UUID
import org.bukkit.Bukkit
import org.bukkit.Location
import phonon.puppet.math.*
import phonon.puppet.objects.*
import phonon.puppet.animation.AnimationMixer

/**
 * Declaration of model, skeleton, required to make actor.
 * Can allow mixing of different compatible models + skeletons.
 */
public data class ActorPrototype(
    val modelName: String,
    val skeletonName: String
)

/**
 * Main object for an interactive model with animations.
 * Acts as glue between skeleton, meshes, and animations.
 */
public class Actor(
    public val name: String,
    public val uuid: UUID,
    public var skeleton: Skeleton? = null, // associated skeleton
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

    // animation mixer
    public val animation: AnimationMixer = AnimationMixer()

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
    }

    class Builder() {
        var _name: String = ""
        var _uuid: UUID = UUID.randomUUID()
        val _position = Vector3f.zero()
        val _rotation = Euler.zero()
        var _matrix: Matrix4f? = null
        var _skeleton: Skeleton? = null

        fun name(s: String) = apply { this._name = s }
        fun position(x: Float, y: Float, z: Float) = apply { this._position.set(x, y, z) }
        fun position(x: Double, y: Double, z: Double) = apply { this._position.set(x, y, z) }
        fun rotation(x: Float, y: Float, z: Float) = apply { this._rotation.set(x, y, z, this._rotation.order) }
        fun rotation(x: Double, y: Double, z: Double) = apply { this._rotation.set(x, y, z, this._rotation.order) }
        fun matrix(m: Matrix4f) = apply { this._matrix = m }
        fun skeleton(s: Skeleton) = apply { this._skeleton = s }

        fun build() = Actor(this._name, this._uuid, this._skeleton, this._position, this._rotation, this._matrix)
    }

    /**
     * Use UUID for hashcode
     */
    override public fun hashCode(): Int {
        return this.uuid.hashCode()
    }

    /**
     * Cleanup self and tree
     */
    override public fun destroy() {
        for ( child in this.children ) {
            child.destroy()
        }
    }

    /**
     * Implement transform update
     */
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
     * Render loop update function
     */
    public fun update() {
        // update tree from actor transform
        this.updateTransform()

        // update animations
        if ( animation.enabled ) {
            animation.update()
        }

        // write animations to meshes
        val skeleton = this.skeleton
        if ( skeleton !== null ) {
            animation.writeBoneTransforms(skeleton.bones.values)
            skeleton.update()
        }
    }
    
    /**
     * Render this actor's mesh objects.
     */
    public fun render() {
        this.traverse({ obj -> 
            if ( obj is Mesh ) {
                obj.render()
            }
        })
    }

    /**
     * Static manager methods
     */
    companion object {

        // actor type library
        public val library: HashMap<String, ActorPrototype> = hashMapOf()

        /**
         * Save actor prototype with given name into library.
         * Will overwrite existing keys.
         */
        public fun save(name: String, actorType: ActorPrototype) {
            Actor.library.put(name, actorType)
        }

        /**
         * Clear library map
         */
        public fun clear() {
            Actor.library.clear()
        }

        /**
         * Return actor prototype from type name.
         */
        public fun get(type: String): ActorPrototype? {
            return Actor.library.get(type)
        }

        /**
         * Get list of actor prototype keys in library.
         */
        public fun types(): List<String> {
            return Actor.library.keys.toList()
        }
    }
}
