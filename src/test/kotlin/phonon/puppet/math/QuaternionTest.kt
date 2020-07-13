/**
 * Test for Quaternion and its interoperability
 * with Matrix4f, Euler
 */

package phonon.puppet.math.test

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import phonon.puppet.math.*

// needed to handle floating point rounding errors
private fun assertRoughlyEqual(a: Float, b: Float, epsilon: Double, message: String? = null) {
    assertTrue(Math.abs(a.toDouble() - b.toDouble()) < epsilon, message)
}

private val PI_2: Float = (Math.PI / 2.0).toFloat()
private val PI_4: Float = (Math.PI / 4.0).toFloat()

public class QuaternionTest {

    @Test
    fun convertMatrixAndBack1() {
        val EPSILON = 0.000001

        // equivalent euler rotation [PI/4, PI/4, 0.0]
        val q = Quaternion(0.3535534f, 0.3535534f, 0.1464466f, 0.8535534f)
        
        // check rotation matrix
        val mat4 = Matrix4f.zero().makeRotationFromQuaternion(q)
        val mat4Expected = Matrix4f.of(
            0.7071068f, 0.0000f,     0.7071068f, 0f,
            0.5000f,    0.7071068f, -0.5000f,    0f,
            -0.500f,    0.7071068f,  0.5000f,    0f,
            0.000f,     0.0000f,     0.0000f,    1f
        )
        for (i in 0..15) {
            assertRoughlyEqual(mat4[i], mat4Expected[i], EPSILON)
        }

        val q2 = Quaternion.fromRotationMatrix(mat4)
        for (i in 0..3) {
            assertRoughlyEqual(q[i], q2[i], EPSILON, "Mismatch: ${q}, ${q2}")
        }
    }
    
    @Test
    fun convertMatrixAndBack2() {
        val EPSILON = 0.000001

        // equivalent euler rotation [PI/4, 0.0, -PI/4]
        val q = Quaternion(0.3535534f, 0.1464466f, -0.3535534f, 0.8535534f)
        
        // check rotation matrix
        val mat4 = Matrix4f.zero().makeRotationFromQuaternion(q)
        val mat4Expected = Matrix4f.of(
             0.7071068f, 0.7071068f,  0.0000f,    0f,
            -0.5000f,    0.5000f,    -0.7071068f, 0f,
            -0.5000f,    0.5000f,     0.7071068f, 0f,
             0.000f,     0.0000f,     0.0000f,    1f
        )
        for (i in 0..15) {
            assertRoughlyEqual(mat4[i], mat4Expected[i], EPSILON)
        }

        val q2 = Quaternion.fromRotationMatrix(mat4)
        for (i in 0..3) {
            assertRoughlyEqual(q[i], q2[i], EPSILON, "Mismatch: ${q}, ${q2}")
        }
    }

    // test pi/4 rotations
    @Test
    fun convertEulerAndBack1() {
        val EPSILON = 0.000001

        val euler = Euler(PI_4, PI_4, 0f, Euler.XYZ)
        val q = Quaternion.fromEuler(euler)
        val back = Euler.fromQuaternion(q, euler.order)
        
        for (i in 0..2) {
            assertRoughlyEqual(euler[i], back[i], EPSILON, "Mismatch: ${euler}, ${back}")
        }
    }

    // test pi/4 rotations
    @Test
    fun convertEulerAndBack2() {
        val EPSILON = 0.000001
        
        val euler = Euler(PI_4, 0f, -PI_4, Euler.XYZ)
        val q = Quaternion.fromEuler(euler)
        val back = Euler.fromQuaternion(q, euler.order)
        
        for (i in 0..2) {
            assertRoughlyEqual(euler[i], back[i], EPSILON, "Mismatch: ${euler}, ${back}")
        }
    }

    // test arbitrary rotation
    @Test
    fun convertEulerAndBack3() {
        val EPSILON = 0.000001
        
        val euler = Euler(0.4f, 0.2f, -0.000001f, Euler.XYZ)
        val q = Quaternion.fromEuler(euler)
        val back = Euler.fromQuaternion(q, euler.order)

        for (i in 0..2) {
            assertRoughlyEqual(euler[i], back[i], EPSILON, "Mismatch: ${euler}, ${back}")
        }
    }

    // test single-axis 90 deg rotations
    // can have errors due to floating point rounding problems
    // around 90 deg singularity
    @Test
    fun convertEulerAndBack4() {
        val EPSILON = 0.000001

        val euler = Euler(PI_2, 0f, 0f, Euler.XYZ)
        val q = Quaternion.fromEuler(euler)

        // should be q = [ 0.7071068, 0, 0, 0.7071068 ]
        assertRoughlyEqual(q.x, 0.7071068f, EPSILON)
        assertRoughlyEqual(q.y, 0.0f, EPSILON)
        assertRoughlyEqual(q.z, 0.0f, EPSILON)
        assertRoughlyEqual(q.w, 0.7071068f, EPSILON)

        val back = Euler.fromQuaternion(q, euler.order)

        for (i in 0..2) {
            assertRoughlyEqual(euler[i], back[i], EPSILON, "Mismatch: ${euler}, ${back}")
        }
    }

}