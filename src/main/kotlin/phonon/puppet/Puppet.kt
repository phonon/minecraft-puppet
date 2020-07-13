/*
 * Puppet engine + API
 */

package phonon.puppet

import java.util.EnumMap
import java.util.UUID
import java.util.logging.Logger
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.EntityType
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.Location
import org.bukkit.scheduler.BukkitTask
import phonon.puppet.math.*
import phonon.puppet.objects.*
import phonon.puppet.animation.AnimationTrack
import phonon.puppet.resourcepack.Resource

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
    
    // map minecraft entity -> actor object
    internal val entityToMesh: HashMap<Entity, Actor> = hashMapOf()
    
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

    /**
     * Add renderable mesh object
     */
    public fun addRenderable(mesh: Mesh) {
        Puppet.renderable.add(mesh)
    }

    /**
     * Remove renderable mesh object
     */
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

    /**
     * Create actor from skeleton
     */
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

                // link armor stand -> actor
                Puppet.entityToMesh.put(mesh.armorStand, actor)

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

    /**
     * Return actor associated with an Entity
     * @param entity entity (should be an ArmorStand)
     */
    public fun getActorFromEntity(entity: Entity): Actor? {
        return Puppet.entityToMesh.get(entity)
    }

    /**
     * Get first actor player is currently looking at.
     * Uses raycast to check entities player is viewing.
     * @param player player source
     * @param maxDistance max distance of raycast
     */
    public fun getActorPlayerIsLookingAt(player: Player, maxDistance: Double = 10.0): Actor? {
        val start = player.getEyeLocation()
        val direction = start.direction
        val raySize: Double = 0.0
        val filter = { e: Entity -> e.getType() == EntityType.ARMOR_STAND }

        val raytraceResult = player.world.rayTraceEntities(start, direction, maxDistance, raySize, filter)
        val entityHit = raytraceResult?.getHitEntity()
        println(entityHit)

        if ( entityHit !== null ) {
            return Puppet.getActorFromEntity(entityHit)
        } else {
            return null
        }
    }

    public fun toggleArmorStands(obj: GraphNode, visible: Boolean) {
        if ( obj is Mesh ) {
            obj.armorStand.setVisible(visible)
        }

        for ( child in obj.children ) {
            Puppet.toggleArmorStands(child, visible)
        }
    }

    /**
     * Starts render loop engine, updates/renders models every tick.
     */
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

    /**
     * Stop render loop engine.
     */
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

    /**
     * Run one render iteration.
     */
    public fun stepEngine() {
        render()
    }

    /**
     * Transform/animation update + render all actors/meshes.
     */
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