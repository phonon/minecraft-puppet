/**
 * Linear interpolation
 */

package phonon.puppet.math

public object LinearInterpolation: Interpolation {
    override public fun interpolate(v1: Double, v2: Double, t: Double): Double {
        if ( t <= 0.0 ) {
            return v1
        }
        else if ( t >= 1.0 ) {
            return v2
        }
        else {
            return v1 * (1.0 - t) + v2 * t
        }
    }
}