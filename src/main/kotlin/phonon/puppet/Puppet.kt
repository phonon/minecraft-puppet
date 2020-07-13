/*
 * Puppet engine + API
 */

package phonon.puppet

import java.util.EnumMap
import java.util.UUID
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.logging.Logger
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.ArmorStand
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.EntityEquipment
import org.bukkit.util.Vector
import org.bukkit.util.EulerAngle 
import org.bukkit.scheduler.BukkitTask
import phonon.puppet.math.*
import phonon.puppet.objects.*
import phonon.puppet.animation.AnimationTrack
import phonon.puppet.resourcepack.Resource
import phonon.puppet.serdes.Serdes
import phonon.puppet.utils.file.listFilesInDir

public object Puppet {

    // version
    public val version: String = "0.0.0"

    // minecraft plugin variables
    internal var plugin: JavaPlugin? = null
    internal var logger: Logger? = null

    // engine
    private var _task: BukkitTask? = null
    public var isRunning: Boolean = false

    // actors
    internal val actors: LinkedHashMap<UUID, Actor> = LinkedHashMap()
    
    // all renderable meshes
    internal val renderable: ArrayList<Mesh> = arrayListOf()
    
    // players posing actors by body movement
    internal val playerPosingActor: HashMap<Player, Actor> = hashMapOf()

    // initialization:
    // - set links to plugin variables
    public fun initialize(plugin: JavaPlugin) {
        Puppet.plugin = plugin
        Puppet.logger = plugin.getLogger()
        Puppet.loadResources(false)
    }

    /**
     * Loads resource data (models, bones, animations, etc...) from
     * plugin data directory and generates resource pack
     * needed by client.
     * 
     * @param cleanExisting delete existing data in Mesh, Skeleton, AnimationTrack
     */
    public fun loadResources(cleanExisting: Boolean = true) {
        if ( cleanExisting ) {
            Mesh.clear()
            Skeleton.clear()
            AnimationTrack.clear()
        }

        Resource.initialize()

        // load models directory + get list of animation files
        // and generates custom resource pack
        val (customModelData, skeletons, animations) = Resource.load()

        // load mesh CustomModelData
        Mesh.loadCustomModelData(customModelData)

        for ( skeleton in skeletons ) {
            Skeleton.save(skeleton)
        }

        for ( animTrack in animations ) {
            AnimationTrack.save(animTrack)
        }        
    }

    // add renderable object
    public fun addRenderable(mesh: Mesh) {
        Puppet.renderable.add(mesh)
    }

    // remove renderable object
    public fun removeRenderable(mesh: Mesh) {
        Puppet.renderable.remove(mesh)
    }

    public fun createMesh(type: String, location: Vector3f) {
        val actor = Actor.create("mesh")

        val mesh = Mesh.Builder()
            .name("mesh")
            .model(type)
            .position(0.0, 0.0, 0.0)
            .rotation(0.0, 0.0, 0.0)
            .build()
        
        actor.add(mesh)
        actor.position.copy(location)
        actor.updateTransform()
        Puppet.actors.put(actor.uuid, actor)
    }

    // create actor from skeleton
    public fun createActor(type: String, location: Vector3f): Result<Actor> {
        // create skeleton if it exists
        val skeleton: Skeleton? = Skeleton.create(type)
        
        if ( skeleton === null ) {
            return Result.failure(Exception("Skeleton does not exist"))
        }

        val actor = Actor.create("actor")

        // create parallel mesh hierarchy
        // create meshes as children of bones in skeleton
        fun linkMeshToBone(bone: Bone) {
            // first iterate children
            for ( child in bone.children ) {
                if ( child is Bone ) {
                    linkMeshToBone(child)
                }
            }

            // create mesh and link to bone (after finishing children)
            val meshName = "${type}.${bone.name}"
            val customModelData = Mesh.get(meshName)
            if ( customModelData !== null ) {

                // local position relative to actor is bone's world position
                val boneWorldPosition = bone.worldMatrix.getTranslation()

                val mesh = Mesh.Builder()
                    .name(meshName)
                    .customModelData(customModelData)
                    .position(boneWorldPosition.x, boneWorldPosition.y, boneWorldPosition.z)
                    .build()
                
                // link to actor
                actor.add(mesh)

                // link to bone
                bone.mesh = mesh
            }
        }

        linkMeshToBone(skeleton.root)

        actor.skeleton = skeleton
        actor.position.copy(location)
        actor.updateTransform()
        Puppet.actors.put(actor.uuid, actor)

        return Result.success(actor)
    }

    /**
     * TODO
     */
    public fun createActorAtEntity() {
        // TODO
    }

    /**
     * TODO
     */
    public fun createActorAtLocation() {
        // TODO
    }

    public fun toggleArmorStands(obj: GraphNode, visible: Boolean) {
        if ( obj is Mesh ) {
            obj.armorStand.setVisible(visible)
        }

        for ( child in obj.children ) {
            Puppet.toggleArmorStands(child, visible)
        }
    }

    // run render loop every tick
    public fun startEngine() {
        if ( Puppet._task === null ) {
            Puppet._task = Bukkit.getScheduler().runTaskTimer(Puppet.plugin!!, object: Runnable {
                override fun run() {
                    Puppet.render()
                }
            }, 0, 0)

            Puppet.logger!!.info("Running render loop")
            Puppet.isRunning = true
        }
        else {
            System.err.println("Puppet already running render loop")
        }
    }

    // stop running render loop
    public fun stopEngine() {
        if ( Puppet._task !== null ) {
            Puppet._task!!.cancel()
            Puppet._task = null
            Puppet.logger!!.info("Stopped render loop")
            Puppet.isRunning = false
        }
        else {
            System.err.println("Puppet already running render loop")
        }
    }

    // run one render iteration
    public fun stepEngine() {
        render()
    }

    // render function
    public fun render() {
        // update actors
        for ( actor in Puppet.actors.values ) {
            actor.update()
        }

        // update mesh armor stands
        for ( mesh in Puppet.renderable ) {
            mesh.render()
        }
    }

}