/**
 * Constant discrete step interpolation
 */

package phonon.puppet.math

public object ConstantInterpolation: Interpolation {
    override public fun interpolate(v1: Double, v2: Double, t: Double): Double {
        if ( t < 1.0 ) {
            return v1
        }
        else {
            return v2
        }
    }
}