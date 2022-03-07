/**
 * Test for Euler and its interoperability
 */

package phonon.puppet.math.test

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import phonon.puppet.math.*


public class EulerTest {
    
    // needed to handle floating point rounding errors
    private fun assertRoughlyEqual(a: Float, b: Float, epsilon: Double, message: String? = null) {
        assertTrue(Math.abs(a.toDouble() - b.toDouble()) < epsilon, message)
    }

    private val PI_2: Float = (Math.PI / 2.0).toFloat()
    private val PI_4: Float = (Math.PI / 4.0).toFloat()

    @Test
    fun convertMatrixAndBack1() {
        val EPSILON = 0.000001

        val euler = Euler(PI_4, PI_4, 0f, Euler.XYZ)
        
        // check rotation matrix
        val mat4 = Matrix4f.fromEuler(euler)
        val mat4Expected = Matrix4f.of(
            0.7071068f, 0.0000f,     0.7071068f, 0f,
            0.5000f,    0.7071068f, -0.5000f,    0f,
            -0.500f,    0.7071068f,  0.5000f,    0f,
            0.000f,     0.0000f,     0.0000f,    1f
        )
        for (i in 0..15) {
            assertRoughlyEqual(mat4[i], mat4Expected[i], EPSILON)
        }

        val euler2 = Euler.fromRotationMatrix(mat4, euler.order)
        for (i in 0..2) {
            assertRoughlyEqual(euler[i], euler2[i], EPSILON, "Mismatch: ${euler}, ${euler2}")
        }
    }
    
    @Test
    fun convertMatrixAndBack2() {
        val EPSILON = 0.000001

        // equivalent euler rotation [PI/4, 0.0, -PI/4]
        val euler = Euler(PI_4, 0f, -PI_4, Euler.XYZ)
        
        // check rotation matrix
        val mat4 = Matrix4f.fromEuler(euler)
        val mat4Expected = Matrix4f.of(
             0.7071068f, 0.7071068f,  0.0000f,    0f,
            -0.5000f,    0.5000f,    -0.7071068f, 0f,
            -0.5000f,    0.5000f,     0.7071068f, 0f,
             0.000f,     0.0000f,     0.0000f,    1f
        )
        for (i in 0..15) {
            assertRoughlyEqual(mat4[i], mat4Expected[i], EPSILON)
        }

        val euler2 = Euler.fromRotationMatrix(mat4, euler.order)
        for (i in 0..2) {
            assertRoughlyEqual(euler[i], euler2[i], EPSILON, "Mismatch: ${euler}, ${euler2}")
        }
    }
    
    @Test
    fun convertMatrixAndBack3() {
        val EPSILON = 0.000001

        // equivalent euler rotation [PI/2, PI/2, 0]
        val euler = Euler(PI_2, PI_2, 0f, Euler.XYZ)
        
        // check rotation matrix
        val mat4 = Matrix4f.fromEuler(euler)
        val mat4Expected = Matrix4f.of(
             0f, 0f,  1f, 0f,
             1f, 0f, -0f, 0f,
            -0f, 1f,  0f, 0f,
             0f, 0f,  0f, 1f
        )
        for (i in 0..15) {
            assertRoughlyEqual(mat4[i], mat4Expected[i], EPSILON)
        }

        val euler2 = Euler.fromRotationMatrix(mat4, euler.order)
        for (i in 0..2) {
            assertRoughlyEqual(euler[i], euler2[i], EPSILON, "Mismatch: ${euler}, ${euler2}")
        }
    }

}