/**
 * Interface for interpolation functions
 */

package phonon.puppet.math


interface Interpolation {
    public fun interpolate(v1: Double, v2: Double, t: Double): Double

    companion object {
        // references to interpolation functions
        val CONSTANT: Interpolation = ConstantInterpolation
        val LINEAR: Interpolation = LinearInterpolation

        // return interpolation function from string name
        public fun get(name: String): Interpolation {
            return when ( name.toLowerCase() ) {
                "const",
                "constant" -> ConstantInterpolation

                "linear" -> LinearInterpolation
                
                else -> LinearInterpolation
            }
        }
    }
}