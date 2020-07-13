/**
 * Test for Matrix4f
 */
package phonon.puppet.math.test

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import phonon.puppet.math.*

public class Matrif4fTest {

    @Test
    fun transpose() {

        val mat1 = Matrix4f.of(
            0f, 1f, 2f, 3f,
            4f, 5f, 6f, 7f,
            8f, 9f, 10f, 11f,
            12f, 13f, 14f, 15f
        )

        val mat2 = Matrix4f.of(
            0f, 4f, 8f, 12f,
            1f, 5f, 9f, 13f,
            2f, 6f, 10f, 14f,
            3f, 7f, 11f, 15f
        )

        val mat3 = mat1.clone()
        val mat4 = mat2.clone()

        assertEquals(mat1, mat2.transpose())
        assertEquals(mat3.transpose(), mat4)
    }

    // tests determinant and inverse
    @Test
    fun inverse() {
        // upper tri matrix, so
        // det = 1 * 2 * 3 * 4 = 24
        val mat1 = Matrix4f.of(
            1f, 1f, 1f, 1f,
            0f, 2f, 2f, 2f,
            0f, 0f, 3f, 3f,
            0f, 0f, 0f, 4f
        )
        
        val det = mat1.det()
        assertTrue(Math.abs(det - 24f) < 1e-6)

        // get inverse, check that multiplying returns identity
        val inv = mat1.clone().inv(mat1)
        val invExpected = Matrix4f.of(
            1f, -0.5f, 0f, 0f,
            0f, 0.5f, -1f/3f, 0f,
            0f, 0f, 1f/3f, -0.25f,
            0f, 0f, 0f, 0.25f
        )

        for ( i in 0..15 ) {
            assertTrue(Math.abs(inv[i] - invExpected[i]) < 1e-6)
        }
        
        // verify multiplying gives identity
        val identity1 = Matrix4f.zero().multiplyMatrices(inv, mat1)
        val identity2 = Matrix4f.identity()
        for ( i in 0..15 ) {
            assertTrue(Math.abs(identity1[i] - identity2[i]) < 1e-6)
        }
    }
}