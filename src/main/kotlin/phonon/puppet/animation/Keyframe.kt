/**
 * Keyframe point in an animation track.
 */

package phonon.puppet.animation

import phonon.puppet.math.Interpolation

public data class Keyframe<T>(
    val tick: Int,
    val value: T,
    val interpolation: Interpolation
)