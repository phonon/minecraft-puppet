/**
 * Handles indexing and tracking state for an AnimationTrack
 */

package phonon.puppet.animation

import phonon.puppet.math.*

public class AnimationAction(
    val track: AnimationTrack,
    var tick: Int = 0,       // current track point in time
    var weight: Double = 0.0 // animation strength (from [0,1])
) {
    // flag that this action is running
    var enabled: Boolean = true

    // optimization flag that bones need to be updated
    var bonesNeedUpdate: Boolean = false

    /**
     * Write into position, quaternion objects using boneName 
     * transform track. Uses the transform on current keyframe tick
     * tracked by this AnimationAction object.
     */
    public fun writeIntoPositionQuaternion(boneName: String, position: Vector3f, quaternion: Quaternion): Boolean {
        return this.track.writeIntoPositionQuaternion(boneName, tick, position, quaternion)
    }

    /**
     * Update tick and track data index
     */
    public fun update() {
        this.tick = if ( this.tick >= this.track.length-1 ) {
            0
        } else {
            this.tick + 1
        }
        
        this.bonesNeedUpdate = true
    }
}
