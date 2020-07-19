/**
 * Play and mix together different animation tracks.
 * Manager for multiple AnimationAction.
 * To animate an actor it should have a Skeleton + AnimationMixer.
 * TODO: animation weights + running multiple animations
 */

package phonon.puppet.animation

import phonon.puppet.objects.Bone
import phonon.puppet.math.*

public class AnimationMixer() {

    // active animation actions
    val playing: HashMap<String, AnimationAction> = hashMapOf()
    
    // flag to update animations (true - update, false - no update, paused state)
    var enabled: Boolean = true

    /**
     * Enable/disable running update ticks. Use `enable(false)`
     * to disable update loop, freezing animations without
     * removing them from the object.
     * 
     * @param flag enable/disable animations
     */
    public fun enable(flag: Boolean) {
        this.enabled = flag
    }

    /**
     * Make actor play an animation. If animation is already
     * playing on this actor, this will only update the animation
     * mixing weight.
     * 
     * @param name animation name
     * @param weight strength of animation weighting
     */
    public fun play(name: String, weight: Double) {
        // get animation track from name
        AnimationTrack.get(name)?.let { track -> 
            this.playing.put(name, AnimationAction(track, 0, weight))
        }
    }

    /**
     * Stop playing animation name, if it exists.
     * Does not check if animation exists or is being played.
     * 
     * @param name animation name to stop
     */
    public fun stop(name: String) {
        this.playing.remove(name)
    }

    /**
     * Stops playing all animations.
     */
    public fun stopAll() {
        this.playing.clear()
    }

    /**
     * Resets all animations playing to their
     * initial tick. Sets each AnimationAction tick = 0.
     */
    public fun restart() {
        for ( anim in this.playing.values ) {
            anim.tick = 0
        }
    }

    /**
     * Linearly interpolates between playing two animations
     * over input ticks period. TODO.
     * 
     * @param oldAnim
     * @param oldWeight
     * @param newAnim
     * @param newWeight
     * @param ticks 
     */
    public fun crossfade(oldAnim: String, oldWeight: Double, newAnim: String, newWeight: Double, ticks: Long) {
        // TODO
    }

    /**
     * Normalizes weighting on all animation tracks so their
     * weights add to 1.0.
     */
    public fun normalizeAnimationWeights() {
        var sum = 0.0
        for ( anim in this.playing.values ) {
            sum += anim.weight
        }

        for ( anim in this.playing.values ) {
            anim.weight = anim.weight / sum
        }
    }
    
    /**
     * Update to next tick in each track.
     * This does not check if AnimationMixer itself is enabled,
     * instead client should run the `if ( animation.enabled )` check.
     * This way, the update() command can be used to
     * step animations even when not enabled.
     */
    public fun update() {
        for ( action in this.playing.values ) {
            if ( action.enabled ) {
                action.update()
            }
        }
    }

    /**
     * Write bone transform into position, quaternion buffers.
     * 
     * @param bones iterator of bone objects
     */
    public fun writeBoneTransforms(bones: Iterable<Bone>) {
        for ( action in this.playing.values ) {
            if ( action.bonesNeedUpdate ) {
                for ( bone in bones ) {
                    val result = action.writeIntoPositionQuaternion(bone.name, bone.position, bone.quaternion)
                    if ( result == true ) {
                        // need to update bone since the position, quaternion written are
                        // local changes relative to bind pose
                        // TODO: make this cleaner
                        // TODO: animation weights
                        bone.position.addVectors(bone.bindPosition, bone.position)
                        bone.quaternion.premultiply(bone.bindQuaternion)
                        bone.rotation.setFromQuaternion(bone.quaternion, bone.rotation.order)
                    }
                }
                action.bonesNeedUpdate = false
            }
        }
    }
}