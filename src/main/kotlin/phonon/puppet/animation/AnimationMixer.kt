/**
 * Play and mix together different animation tracks.
 * Manager for multiple AnimationAction.
 * To animate an actor it should have a Skeleton + AnimationMixer.
 */

package phonon.puppet.animation

import phonon.puppet.objects.Bone
import phonon.puppet.math.*

public class AnimationMixer() {

    // active animation actions
    val playing: HashMap<String, AnimationAction> = hashMapOf()
    
    public fun play(name: String, weight: Double) {
        // get animation track from name
        AnimationTrack.get(name)?.let { track -> 
            this.playing.put(name, AnimationAction(track, 0, weight))
        }
    }

    /**
     * Stop playing animation with name, if it exists
     */
    public fun stop(name: String) {
        this.playing.remove(name)
    }

    /**
     * Stops playing all animations
     */
    public fun stopAll() {
        this.playing.clear()
    }

    /**
     * Update to next tick in each track
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