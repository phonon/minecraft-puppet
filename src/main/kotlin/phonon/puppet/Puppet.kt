/*
 * Puppet engine + API
 */

package phonon.puppet

import java.util.EnumMap
import java.util.UUID
import java.util.logging.Logger
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.Location
import org.bukkit.ChatColor
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.EntityType
import org.bukkit.scheduler.BukkitTask
import org.bukkit.command.CommandSender
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

    // link actor name -> actor for access
    // name should be unique, but must be enforced by writer
    public val actors: LinkedHashMap<String, Actor> = LinkedHashMap()

    // actors stored by UUID
    public val actorsById: LinkedHashMap<UUID, Actor> = LinkedHashMap()

    // map minecraft entity -> actor object
    public val entityToActor: HashMap<Entity, Actor> = hashMapOf()
    
    // all renderable meshes
    public val renderable: ArrayList<Mesh> = arrayListOf()

    // players posing actors by body movement
    public val playerPosingActor: HashMap<Player, Actor> = hashMapOf()

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
            Actor.clear()
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
            
            // also create actor prototypes assuming each skeleton
            // is associated with a mesh group with same name
            val actorType = ActorPrototype(skeleton.name, skeleton.name)
            Actor.save(skeleton.name, actorType)
        }

        for ( animTrack in animations ) {
            AnimationTrack.save(animTrack)
        }
    }

    /**
     * Print engine info to target.
     * 
     * @param target target Bukkit player or console CommandSender
     */
    public fun printInfo(target: CommandSender) {
        Message.print(target, "${ChatColor.BOLD}Puppet Animation Engine v${Puppet.version}")
        Message.print(target, "Library:")
        Message.print(target, "- Models: ${Mesh.library.size}")
        Message.print(target, "- Skeletons: ${Skeleton.library.size}")
        Message.print(target, "- Animations: ${AnimationTrack.library.size}")
    }

    // =====================================
    // Actor commands
    // =====================================

    /**
     * Create actor with single mesh model.
     * 
     * @param meshType custom model name
     * @param location Bukkit location to create the actor at
     * @return result containing created actor or exception with failure case 
     */
    public fun createMeshAtLocation(meshType: String, location: Location): Result<Actor> {
        if ( !Mesh.has(meshType) ) {
            return Result.failure(Exception("Mesh type does not exist"))
        }

        val actorName = Puppet.generateActorName(meshType)

        val actor = Actor.Builder()
            .name(actorName)
            .position(location.x, location.y, location.z)
            .build()

        val mesh = Mesh.Builder()
            .name("mesh")
            .model(meshType)
            .position(0.0, 0.0, 0.0)
            .rotation(0.0, 0.0, 0.0)
            .build()
        
        actor.add(mesh)
        actor.updateTransform()
        actor.render()

        Puppet.registerActor(actor)

        return Result.success(actor)
    }

    /**
     * Create actor from ActorPrototype type string at given location.
     * 
     * @param type actor prototype name (get types using Actor.types())
     * @param location Bukkit location to create the actor at
     * @return result containing created actor or exception with failure case 
     */
    public fun createActorAtLocation(type: String, location: Location): Result<Actor> {
        // check if actor prototype exists
        val actorType = Actor.get(type)
        if ( actorType === null ) {
            return Result.failure(Exception("Skeleton does not exist"))
        }

        val (modelName, skeletonName) = actorType

        // create skeleton if it exists
        val skeleton: Skeleton? = Skeleton.create(skeletonName)
        if ( skeleton === null ) {
            return Result.failure(Exception("Skeleton does not exist"))
        }

        // get actor name
        val actorName = Puppet.generateActorName(type)

        // create actor
        val actor = Actor.Builder()
            .name(actorName)
            .skeleton(skeleton)
            .position(location.x, location.y, location.z)
            .build()

        // build meshes
        // 1. attach mesh directly as child of actor (for global transforms)
        // 2. attach mesh to its bone
        fun linkMeshToBone(bone: Bone) {
            // first iterate children
            for ( child in bone.children ) {
                if ( child is Bone ) {
                    linkMeshToBone(child)
                }
            }

            // create mesh and link to bone (after finishing children)
            val meshName = "${modelName}.${bone.name}"
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

        actor.updateTransform()
        actor.render()

        Puppet.registerActor(actor)

        return Result.success(actor)
    }

    /**
     * Register actor with Puppet engine
     * 
     * @param actor actor to add to engine
     */
    public fun registerActor(actor: Actor) {
        Puppet.actors.put(actor.name, actor)
        Puppet.actorsById.put(actor.uuid, actor)

        // process actor tree
        actor.traverse({ obj -> 
            if ( obj is Mesh ) {
                // add mesh links
                Puppet.renderable.add(obj)
                Puppet.entityToActor.put(obj.armorStand, actor)
            }
        })
    }

    /**
     * Remove actor and cleanup its components.
     * 
     * @param actor actor to destroy
     */
    public fun destroyActor(actor: Actor) {
        // manually process actor tree instead of using actor.destroy()
        actor.traverse({ obj -> 
            if ( obj is Mesh ) {
                // remove armor stand from world
                obj.armorStand.remove()

                // remove mesh links
                Puppet.renderable.remove(obj)
                Puppet.entityToActor.remove(obj.armorStand, actor)
            }
        })
        
        Puppet.actors.remove(actor.name)
        Puppet.actorsById.remove(actor.uuid)

        // remove player posing actor reference
        for ( (player, posingActor) in Puppet.playerPosingActor.entries.toList() ) {
            if ( posingActor === actor ) {
                Puppet.playerPosingActor.remove(player)
                break
            }
        }
    }

    /**
     * Remove all actors currently in engine
     */
    public fun destroyAllActors() {
        val actors = Puppet.actors.values.toList()
        for ( a in actors ) {
            Puppet.destroyActor(a)
        }
    }

    /**
     * Returns list of actor names currently in game.
     * 
     * @return list of actor names currently in game
     */
    public fun getActorNames(): List<String> {
        return Puppet.actors.keys.toList()
    }

    /**
     * Return actor from name if it exists, otherwise null.
     * 
     * @param name name of actor
     * @return actor from name if it exists, otherwise null
     */
    public fun getActor(name: String): Actor? {
        return Puppet.actors.get(name)
    }

    /**
     * Return actor from UUID if it exists, otherwise null.
     * 
     * @param uuid UUID of actor
     * @return actor from UUID if it exists, otherwise null
     */
    public fun getActorById(uuid: UUID): Actor? {
        return Puppet.actorsById.get(uuid)
    }

    /**
     * Return actor associated with an Entity.
     * 
     * @param entity entity (should be an ArmorStand)
     * @return actor from entity if it exists, otherwise null
     */
    public fun getActorFromEntity(entity: Entity): Actor? {
        return Puppet.entityToActor.get(entity)
    }

    /**
     * Get first actor player is currently looking at.
     * Uses raycast to check entities player is viewing.
     * 
     * @param player player source
     * @param maxDistance max distance of raycast
     * @return first actor player is looking at, null if there is none
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

    /**
     * Toggle visibility of armor stands for a TransformGraphNode
     * object and all its children (affects Mesh type objects).
     * 
     * @param obj object in a transform graph
     * @param visible true to set ArmorStands visible
     */
    public fun toggleArmorStands(obj: TransformGraphNode, visible: Boolean) {
        obj.traverse({
            if ( obj is Mesh ) {
                obj.armorStand.setVisible(visible)
            }
        })
    }

    /**
     * Generate unique actor name based on input type string,
     * in format "typeN", where N is an integer. It will
     * generate in order N = 0, 1, ... until the first unused
     * number is found, i.e. "actor0", "actor1", ...
     * 
     * @param type actor type name
     * @return unique actor name from type
     */
    public fun generateActorName(type: String): String {
        var i = 0
        var name = "${type}${i}"
        while ( Puppet.actors.contains(name) ) {
            i = i + 1
            name = "${type}${i}"
        }
        return name
    }

    /**
     * Mark mesh object as renderable, which means it will
     * run its render() function on every engine update tick.
     * Only affects a single mesh object and not its children.
     * 
     * @param mesh mesh object
     */
    public fun addRenderable(mesh: Mesh) {
        Puppet.renderable.add(mesh)
    }

    /**
     * Remove mesh object from renderables. It will no longer
     * run its render() update during engine ticks.
     * Only affects a single mesh object and not its children.
     * 
     * @param mesh mesh object
     */
    public fun removeRenderable(mesh: Mesh) {
        Puppet.renderable.remove(mesh)
    }

    // =====================================
    // Engine commands
    // =====================================

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