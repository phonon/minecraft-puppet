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

private val WORLD_DEFAULT = Bukkit.getWorlds().get(0)
private val LOCATION_DEFAULT = Location(WORLD_DEFAULT, 0.0, 0.0, 0.0)

public class Actor(
    val name: String = "",
    val id: Int = 0
): GraphNode {

    // scene graph
    override var parent: GraphNode? = null
    override val children: ArrayList<GraphNode> = arrayListOf()

    // transform
    override val matrix: Matrix4f = Matrix4f.identity()
    override val worldMatrix: Matrix4f = Matrix4f.identity()
    override val position: Vector3f = Vector3f.zero()
    override val rotation: Euler = Euler.zero()
    override val quaternion: Quaternion = Quaternion.new()

    // dirty flag
    override val needsUpdate: Boolean = false

    // uuid identifier
    public val uuid: UUID = UUID.randomUUID()

    // associated skeleton (independent transform hierarchy)
    public var skeleton: Skeleton? = null

    // animation mixer
    public val animation: AnimationMixer = AnimationMixer()

    init {

    }

    // implement transform
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
        animation.update()

        // write animations to meshes
        val skeleton = this.skeleton
        if ( skeleton !== null ) {
            animation.writeBoneTransforms(skeleton.bones.values)
            skeleton.update()
        }
    }
    
    /**
     * Make actor play an animation. If animation is already
     * playing on this actor, this will only update the animation
     * mixing weight.
     * 
     * @param name animation name
     * @param weight strength of animation weighting
     */
    public fun playAnimation(name: String, weight: Double = 1.0) {
        this.animation.play(name, weight)
    }

    /**
     * Make actor stop playing an animation. If input
     * is empty or null, this will stop all animations.
     * 
     * @param name animation name to stop (null to stop all)
     */
    public fun stopAnimation(name: String? = null) {
        if ( name === null ) {
            this.animation.stopAll()
            return
        }

        this.animation.stop(name)
    }

    /**
     * Linearly interpolates between playing two animations
     * over input ticks period.
     * 
     * @param oldAnim
     * @param oldWeight
     * @param newAnim
     * @param newWeight
     * @param ticks 
     */
    public fun crossfadeAnimation(oldAnim: String, oldWeight: Double, newAnim: String, newWeight: Double, ticks: Long) {
        
    }

    /**
     * Get list of names of active animations this actor is playing.
     */
    public fun currentAnimations(): List<String> {
        return listOf()
    }


    /**
     * Static manager methods
     */
    companion object {

        // counter for generating actor ids
        private var idCounter: Int = 0
        
        // link actor id -> actor for access
        public val actors: HashMap<Int, Actor> = hashMapOf()

        // create actor with automatic integer id number
        public fun create(name: String): Actor {
            val id = Actor.idCounter
            Actor.idCounter += 1

            val actor = Actor(name, id)
            Actor.actors.put(actor.id, actor)

            return actor
        }

        // return actor from integer
        public fun get(id: Int): Actor? {
            return Actor.actors.get(id)
        }
    }
}
