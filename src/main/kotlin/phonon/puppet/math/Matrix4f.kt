/**
 * Matrix 4x4 Float32
 * Backed by a FloatArray in column-major format
 * 
 * Vector multiplication with column major (method used):
 *                 [ a0 a4 a8  a01 ] [ x ]
 * [4x4] x [4x1] = [ a1 a5 a9  a02 ] [ y ]
 *                 [ a2 a6 a10 a03 ] [ z ]
 *                 [ a3 a7 a00 a15 ] [ w ]
 * 
 * Vector multiplication with row major:
 *                 [ a0  a1  a2  a3  ] [ x ]
 * [4x4] x [4x1] = [ a4  a5  a6  a7  ] [ y ]
 *                 [ a8  a9  a10 a00 ] [ z ]
 *                 [ a01 a02 a03 a15 ] [ w ]
 */

package phonon.puppet.math

// =====================================
// column-major matrix element array index accessors
// =====================================
public const val m00: Int = 0 // XX
public const val m10: Int = 1 // YX
public const val m20: Int = 2 // ZX
public const val m30: Int = 3 // WX

public const val m01: Int = 4 // XY
public const val m11: Int = 5 // YY
public const val m21: Int = 6 // ZY
public const val m31: Int = 7 // WY

public const val m02: Int = 8 // XZ
public const val m12: Int = 9 // YZ
public const val m22: Int = 10 // ZZ
public const val m32: Int = 11 // WZ

public const val m03: Int = 12 // XW
public const val m13: Int = 13 // YW
public const val m23: Int = 14 // ZW
public const val m33: Int = 15 // WW

public class Matrix4f() {
    // elements initialized to 0
    val elements: FloatArray = FloatArray(16)

    companion object {

        // return all zeros matrix
        public fun zero(): Matrix4f {
            return Matrix4f()
        }

        // return identity matrix
        public fun identity(): Matrix4f {
            val mat4 = Matrix4f()
            mat4.elements[m00] = 1f
            mat4.elements[m11] = 1f
            mat4.elements[m22] = 1f
            mat4.elements[m33] = 1f
            return mat4
        }

        // return all ones matrix
        public fun one(): Matrix4f {
            val mat4 = Matrix4f()
            mat4.elements[m00] = 1f
            mat4.elements[m01] = 1f
            mat4.elements[m02] = 1f
            mat4.elements[m03] = 1f
            mat4.elements[m10] = 1f
            mat4.elements[m11] = 1f
            mat4.elements[m12] = 1f
            mat4.elements[m13] = 1f
            mat4.elements[m20] = 1f
            mat4.elements[m21] = 1f
            mat4.elements[m22] = 1f
            mat4.elements[m23] = 1f
            mat4.elements[m30] = 1f
            mat4.elements[m31] = 1f
            mat4.elements[m32] = 1f
            mat4.elements[m33] = 1f
            return mat4
        }

        // return matrix filled with value
        public fun fill(v: Float): Matrix4f {
            val mat4 = Matrix4f()
            mat4.elements[m00] = v
            mat4.elements[m01] = v
            mat4.elements[m02] = v
            mat4.elements[m03] = v
            mat4.elements[m10] = v
            mat4.elements[m11] = v
            mat4.elements[m12] = v
            mat4.elements[m13] = v
            mat4.elements[m20] = v
            mat4.elements[m21] = v
            mat4.elements[m22] = v
            mat4.elements[m23] = v
            mat4.elements[m30] = v
            mat4.elements[m31] = v
            mat4.elements[m32] = v
            mat4.elements[m33] = v
            return mat4
        }

        // return Matrix4 from input written as ROW MAJOR
        // (array elements same ordering as input)
        public fun of(
            a0: Float,
            a1: Float,
            a2: Float,
            a3: Float,
            a4: Float,
            a5: Float,
            a6: Float,
            a7: Float,
            a8: Float,
            a9: Float,
            a10: Float,
            a11: Float,
            a12: Float,
            a13: Float, 
            a14: Float,
            a15: Float
        ): Matrix4f {
            val mat4 = Matrix4f()
            mat4.elements[m00] = a0
            mat4.elements[m01] = a1
            mat4.elements[m02] = a2
            mat4.elements[m03] = a3
            
            mat4.elements[m10] = a4
            mat4.elements[m11] = a5
            mat4.elements[m12] = a6
            mat4.elements[m13] = a7
            
            mat4.elements[m20] = a8
            mat4.elements[m21] = a9
            mat4.elements[m22] = a10
            mat4.elements[m23] = a11
            
            mat4.elements[m30] = a12
            mat4.elements[m31] = a13
            mat4.elements[m32] = a14
            mat4.elements[m33] = a15

            return mat4
        }

        // from list of float numbers in row-major format
        // assumes list size >= 16
        public fun fromList(e: List<Float>): Matrix4f {
            return Matrix4f.of(
                e[0], e[1], e[2], e[3],
                e[4], e[5], e[6], e[7],
                e[8], e[9], e[10], e[11],
                e[12], e[13], e[14], e[15]
            )
        }

        // create new rotation matrix from euler
        public fun fromEuler(euler: Euler): Matrix4f {
            val mat4 = Matrix4f.zero()
            return mat4.rotationFromEuler(euler)
        }

        // create new rotation matrix from euler
        public fun fromQuaternion(q: Quaternion): Matrix4f {
            val mat4 = Matrix4f.zero()
            return mat4.makeRotationFromQuaternion(q)
        }

    }

    // implement array index: mat4[index]
    operator fun get(i: Int): Float {
        return this.elements[i]
    }

    operator fun set(i: Int, v: Float) {
        this.elements[i] = v
    }

    override fun equals(other: Any?): Boolean {
        if ( this === other ) {
            return true
        }
        if ( other?.javaClass != javaClass ) {
            return false
        }

        other as Matrix4f

        return (
            this.elements[0] == other.elements[0] &&
            this.elements[1] == other.elements[1] &&
            this.elements[2] == other.elements[2] &&
            this.elements[3] == other.elements[3] &&
            this.elements[4] == other.elements[4] &&
            this.elements[5] == other.elements[5] &&
            this.elements[6] == other.elements[6] &&
            this.elements[7] == other.elements[7] &&
            this.elements[8] == other.elements[8] &&
            this.elements[9] == other.elements[9] &&
            this.elements[10] == other.elements[10] &&
            this.elements[11] == other.elements[11] &&
            this.elements[12] == other.elements[12] &&
            this.elements[13] == other.elements[13] &&
            this.elements[14] == other.elements[14] &&
            this.elements[15] == other.elements[15]
        )
    }

    override fun hashCode(): Int{
        return this.elements.hashCode()
    }

    override fun toString(): String {
        val e = this.elements
        return "Matrix4f(${e[0]}, ${e[1]}, ${e[2]}, ${e[3]},\n ${e[4]}, ${e[5]}, ${e[6]}, ${e[7]},\n ${e[8]}, ${e[9]}, ${e[10]}, ${e[11]},\n ${e[12]}, ${e[13]}, ${e[14]}, ${e[15]})"
    }

    public fun clone(): Matrix4f {
        val e = this.elements
        return Matrix4f.of(
            e[m00], e[m01], e[m02], e[m03],
            e[m10], e[m11], e[m12], e[m13],
            e[m20], e[m21], e[m22], e[m23],
            e[m30], e[m31], e[m32], e[m33]
        )
    }

    public fun copy(other: Matrix4f): Matrix4f {
        this.elements[0] = other.elements[0]
        this.elements[1] = other.elements[1]
        this.elements[2] = other.elements[2]
        this.elements[3] = other.elements[3]
        this.elements[4] = other.elements[4]
        this.elements[5] = other.elements[5]
        this.elements[6] = other.elements[6]
        this.elements[7] = other.elements[7]
        this.elements[8] = other.elements[8]
        this.elements[9] = other.elements[9]
        this.elements[10] = other.elements[10]
        this.elements[11] = other.elements[11]
        this.elements[12] = other.elements[12]
        this.elements[13] = other.elements[13]
        this.elements[14] = other.elements[14]
        this.elements[15] = other.elements[15]
        return this
    }

    // sets elements given in ROW MAJOR format
    public fun set(
        a0: Float,
        a1: Float,
        a2: Float,
        a3: Float,
        a4: Float,
        a5: Float,
        a6: Float,
        a7: Float,
        a8: Float,
        a9: Float,
        a10: Float,
        a11: Float,
        a12: Float,
        a13: Float, 
        a14: Float,
        a15: Float
    ): Matrix4f {
        this.elements[m00] = a0
        this.elements[m01] = a1
        this.elements[m02] = a2
        this.elements[m03] = a3
        
        this.elements[m10] = a4
        this.elements[m11] = a5
        this.elements[m12] = a6
        this.elements[m13] = a7
        
        this.elements[m20] = a8
        this.elements[m21] = a9
        this.elements[m22] = a10
        this.elements[m23] = a11
        
        this.elements[m30] = a12
        this.elements[m31] = a13
        this.elements[m32] = a14
        this.elements[m33] = a15

        return this
    }

    public fun transpose(): Matrix4f {
        var tmp: Float
        val e = this.elements
        
        tmp = e[m10]; e[m10] = e[m01]; e[m01] = tmp;
        tmp = e[m20]; e[m20] = e[m02]; e[m02] = tmp;
        tmp = e[m30]; e[m30] = e[m03]; e[m03] = tmp;
        
        tmp = e[m21]; e[m21] = e[m12]; e[m12] = tmp;
        tmp = e[m31]; e[m31] = e[m13]; e[m13] = tmp;
        
        tmp = e[m32]; e[m32] = e[m23]; e[m23] = tmp;

        return this
    }

    // http://www.euclideanspace.com/maths/algebra/matrix/functions/inverse/fourD/index.htm
    public fun det(): Double {
        val e = this.elements

        val e00 = e[m00]; val e01 = e[m01]; val e02 = e[m02]; val e03 = e[m03];
        val e10 = e[m10]; val e11 = e[m11]; val e12 = e[m12]; val e13 = e[m13];
        val e20 = e[m20]; val e21 = e[m21]; val e22 = e[m22]; val e23 = e[m23];
        val e30 = e[m30]; val e31 = e[m31]; val e32 = e[m32]; val e33 = e[m33];

        return (
            e30 * (
                + e03 * e12 * e21
                - e02 * e13 * e21
                - e03 * e11 * e22
                + e01 * e13 * e22
                + e02 * e11 * e23
                - e01 * e12 * e23
            ) +
            e31 * (
                + e00 * e12 * e23
                - e00 * e13 * e22
                + e03 * e10 * e22
                - e02 * e10 * e23
                + e02 * e13 * e20
                - e03 * e12 * e20
            ) +
            e32 * (
                + e00 * e13 * e21
                - e00 * e11 * e23
                - e03 * e10 * e21
                + e01 * e10 * e23
                + e03 * e11 * e20
                - e01 * e13 * e20
            ) +
            e33 * (
                - e02 * e11 * e20
                - e00 * e12 * e21
                + e00 * e11 * e22
                + e02 * e10 * e21
                - e01 * e10 * e22
                + e01 * e12 * e20
            )
        ).toDouble()
    }

    // http://www.euclideanspace.com/maths/algebra/matrix/functions/inverse/fourD/index.htm
    public fun inv(m: Matrix4f): Matrix4f {

        val te = this.elements
        val e = m.elements

        val e00 = e[m00]; val e01 = e[m01]; val e02 = e[m02]; val e03 = e[m03];
        val e10 = e[m10]; val e11 = e[m11]; val e12 = e[m12]; val e13 = e[m13];
        val e20 = e[m20]; val e21 = e[m21]; val e22 = e[m22]; val e23 = e[m23];
        val e30 = e[m30]; val e31 = e[m31]; val e32 = e[m32]; val e33 = e[m33];

        val t11 = e12 * e23 * e31 - e13 * e22 * e31 + e13 * e21 * e32 - e11 * e23 * e32 - e12 * e21 * e33 + e11 * e22 * e33
        val t12 = e03 * e22 * e31 - e02 * e23 * e31 - e03 * e21 * e32 + e01 * e23 * e32 + e02 * e21 * e33 - e01 * e22 * e33
        val t13 = e02 * e13 * e31 - e03 * e12 * e31 + e03 * e11 * e32 - e01 * e13 * e32 - e02 * e11 * e33 + e01 * e12 * e33
        val t14 = e03 * e12 * e21 - e02 * e13 * e21 - e03 * e11 * e22 + e01 * e13 * e22 + e02 * e11 * e23 - e01 * e12 * e23

        val det = e00 * t11 + e10 * t12 + e20 * t13 + e30 * t14;

        // non-invertible matrix
        if ( det == 0f ) {
            return this.set(
                0f, 0f, 0f, 0f,
                0f, 0f, 0f, 0f,
                0f, 0f, 0f, 0f,
                0f, 0f, 0f, 0f
            )
        }

        val detInv = 1f / det

        te[m00] = t11 * detInv
        te[m10] = ( e13 * e22 * e30 - e12 * e23 * e30 - e13 * e20 * e32 + e10 * e23 * e32 + e12 * e20 * e33 - e10 * e22 * e33 ) * detInv
        te[m20] = ( e11 * e23 * e30 - e13 * e21 * e30 + e13 * e20 * e31 - e10 * e23 * e31 - e11 * e20 * e33 + e10 * e21 * e33 ) * detInv
        te[m30] = ( e12 * e21 * e30 - e11 * e22 * e30 - e12 * e20 * e31 + e10 * e22 * e31 + e11 * e20 * e32 - e10 * e21 * e32 ) * detInv

        te[m01] = t12 * detInv
        te[m11] = ( e02 * e23 * e30 - e03 * e22 * e30 + e03 * e20 * e32 - e00 * e23 * e32 - e02 * e20 * e33 + e00 * e22 * e33 ) * detInv
        te[m21] = ( e03 * e21 * e30 - e01 * e23 * e30 - e03 * e20 * e31 + e00 * e23 * e31 + e01 * e20 * e33 - e00 * e21 * e33 ) * detInv
        te[m31] = ( e01 * e22 * e30 - e02 * e21 * e30 + e02 * e20 * e31 - e00 * e22 * e31 - e01 * e20 * e32 + e00 * e21 * e32 ) * detInv

        te[m02] = t13 * detInv
        te[m12] = ( e03 * e12 * e30 - e02 * e13 * e30 - e03 * e10 * e32 + e00 * e13 * e32 + e02 * e10 * e33 - e00 * e12 * e33 ) * detInv
        te[m22] = ( e01 * e13 * e30 - e03 * e11 * e30 + e03 * e10 * e31 - e00 * e13 * e31 - e01 * e10 * e33 + e00 * e11 * e33 ) * detInv
        te[m32] = ( e02 * e11 * e30 - e01 * e12 * e30 - e02 * e10 * e31 + e00 * e12 * e31 + e01 * e10 * e32 - e00 * e11 * e32 ) * detInv

        te[m03] = t14 * detInv
        te[m13] = ( e02 * e13 * e20 - e03 * e12 * e20 + e03 * e10 * e22 - e00 * e13 * e22 - e02 * e10 * e23 + e00 * e12 * e23 ) * detInv
        te[m23] = ( e03 * e11 * e20 - e01 * e13 * e20 - e03 * e10 * e21 + e00 * e13 * e21 + e01 * e10 * e23 - e00 * e11 * e23 ) * detInv
        te[m33] = ( e01 * e12 * e20 - e02 * e11 * e20 + e02 * e10 * e21 - e00 * e12 * e21 - e01 * e10 * e22 + e00 * e11 * e22 ) * detInv

        return this

    }

    public fun multiplyMatrices(a: Matrix4f, b: Matrix4f): Matrix4f {

        val ae = a.elements
        val be = b.elements

        val a00 = ae[m00]; val a01 = ae[m01]; val a02 = ae[m02]; val a03 = ae[m03];
        val a10 = ae[m10]; val a11 = ae[m11]; val a12 = ae[m12]; val a13 = ae[m13];
        val a20 = ae[m20]; val a21 = ae[m21]; val a22 = ae[m22]; val a23 = ae[m23];
        val a30 = ae[m30]; val a31 = ae[m31]; val a32 = ae[m32]; val a33 = ae[m33];

        val b00 = be[m00]; val b01 = be[m01]; val b02 = be[m02]; val b03 = be[m03];
        val b10 = be[m10]; val b11 = be[m11]; val b12 = be[m12]; val b13 = be[m13];
        val b20 = be[m20]; val b21 = be[m21]; val b22 = be[m22]; val b23 = be[m23];
        val b30 = be[m30]; val b31 = be[m31]; val b32 = be[m32]; val b33 = be[m33];

        val e = this.elements

        e[m00] = a00 * b00 + a01 * b10 + a02 * b20 + a03 * b30
        e[m01] = a00 * b01 + a01 * b11 + a02 * b21 + a03 * b31
        e[m02] = a00 * b02 + a01 * b12 + a02 * b22 + a03 * b32
        e[m03] = a00 * b03 + a01 * b13 + a02 * b23 + a03 * b33

        e[m10] = a10 * b00 + a11 * b10 + a12 * b20 + a13 * b30
        e[m11] = a10 * b01 + a11 * b11 + a12 * b21 + a13 * b31
        e[m12] = a10 * b02 + a11 * b12 + a12 * b22 + a13 * b32
        e[m13] = a10 * b03 + a11 * b13 + a12 * b23 + a13 * b33

        e[m20] = a20 * b00 + a21 * b10 + a22 * b20 + a23 * b30
        e[m21] = a20 * b01 + a21 * b11 + a22 * b21 + a23 * b31
        e[m22] = a20 * b02 + a21 * b12 + a22 * b22 + a23 * b32
        e[m23] = a20 * b03 + a21 * b13 + a22 * b23 + a23 * b33

        e[m30] = a30 * b00 + a31 * b10 + a32 * b20 + a33 * b30
        e[m31] = a30 * b01 + a31 * b11 + a32 * b21 + a33 * b31
        e[m32] = a30 * b02 + a31 * b12 + a32 * b22 + a33 * b32
        e[m33] = a30 * b03 + a31 * b13 + a32 * b23 + a33 * b33

        return this
    }

    public fun multiply(m: Matrix4f): Matrix4f {
        return this.multiplyMatrices(this, m)
    }

    public fun premultiply(m: Matrix4f): Matrix4f {
        return this.multiplyMatrices(m, this)
    }

    public fun multiplyScalar(s: Float): Matrix4f {
        val e = this.elements
        e[m00] *= s; e[m01] *= s; e[m02] *= s; e[m03] *= s;
        e[m10] *= s; e[m11] *= s; e[m12] *= s; e[m13] *= s;
        e[m20] *= s; e[m21] *= s; e[m22] *= s; e[m23] *= s;
        e[m30] *= s; e[m31] *= s; e[m32] *= s; e[m33] *= s;
        return this
    }

    // returns only rotation matrix component
    public fun extractRotation(m: Matrix4f ): Matrix4f {
        val e = this.elements;
        var me = m.elements;

        val invScaleX = 1f / Math.sqrt((me[m00]*me[m00] + me[m10]*me[m10] + me[m20]*me[m20]).toDouble()).toFloat()
        val invScaleY = 1f / Math.sqrt((me[m01]*me[m01] + me[m11]*me[m11] + me[m21]*me[m21]).toDouble()).toFloat()
        val invScaleZ = 1f / Math.sqrt((me[m02]*me[m02] + me[m12]*me[m12] + me[m22]*me[m22]).toDouble()).toFloat()

        e[m00] = me[m00] * invScaleX
        e[m10] = me[m10] * invScaleX
        e[m20] = me[m20] * invScaleX
        e[m30] = 0f

        e[m01] = me[m01] * invScaleY
        e[m11] = me[m11] * invScaleY
        e[m21] = me[m21] * invScaleY
        e[m31] = 0f

        e[m02] = me[m02] * invScaleZ
        e[m12] = me[m12] * invScaleZ
        e[m22] = me[m22] * invScaleZ
        e[m32] = 0f

        e[m03] = 0f
        e[m13] = 0f
        e[m23] = 0f
        e[m33] = 1f

        return this
    }

    // set matrix to a rotation matrix from euler angles
    public fun rotationFromEuler(euler: Euler): Matrix4f {
        val el = this.elements

        val x = euler.x
        val y = euler.y
        val z = euler.z

        val a: Float = Math.cos(x.toDouble()).toFloat()
        val b: Float = Math.sin(x.toDouble()).toFloat()

        val c: Float = Math.cos(y.toDouble()).toFloat()
        val d: Float = Math.sin(y.toDouble()).toFloat()

        val e: Float = Math.cos(z.toDouble()).toFloat()
        val f: Float = Math.sin(z.toDouble()).toFloat()

        if ( euler.order == Euler.XYZ ) {
            val ae = a * e
            val af = a * f
            val be = b * e
            val bf = b * f

            el[m00] = c * e
            el[m01] = -c * f
            el[m02] = d

            el[m10] = af + be * d
            el[m11] = ae - bf * d
            el[m12] = -b * c

            el[m20] = bf - ae * d
            el[m21] = be + af * d
            el[m22] = a * c

        }
        else if ( euler.order == Euler.YXZ ) {
            val ce = c * e
            val cf = c * f
            val de = d * e
            val df = d * f

            el[m00] = ce + df * b
            el[m01] = de * b - cf
            el[m02] = a * d

            el[m10] = a * f
            el[m11] = a * e
            el[m12] = -b

            el[m20] = cf * b - de
            el[m21] = df + ce * b
            el[m22] = a * c
        }
        else if ( euler.order == Euler.ZXY ) {
            val ce = c * e
            val cf = c * f
            val de = d * e
            val df = d * f

            el[m00] = ce - df * b
            el[m01] = -a * f
            el[m02] = de + cf * b

            el[m10] = cf + de * b
            el[m11] = a * e
            el[m12] = df - ce * b

            el[m20] = -a * d
            el[m21] = b
            el[m22] = a * c
        }
        else if ( euler.order == Euler.ZYX ) {
            val ae = a * e
            val af = a * f
            val be = b * e
            val bf = b * f

            el[m00] = c * e
            el[m01] = be * d - af
            el[m02] = ae * d + bf

            el[m10] = c * f
            el[m11] = bf * d + ae
            el[m12] = af * d - be

            el[m20] = -d
            el[m21] = b * c
            el[m22] = a * c

        }
        else if ( euler.order == Euler.YZX ) {

            val ac = a * c
            val ad = a * d
            val bc = b * c
            val bd = b * d

            el[m00] = c * e
            el[m01] = bd - ac * f
            el[m02] = bc * f + ad

            el[m10] = f
            el[m11] = a * e
            el[m12] = -b * e

            el[m20] = -d * e
            el[m21] = ad * f + bc
            el[m22] = ac - bd * f

        }
        else if ( euler.order == Euler.XZY ) {
            val ac = a * c
            val ad = a * d
            val bc = b * c
            val bd = b * d

            el[m00] = c * e
            el[m01] = -f
            el[m02] = d * e

            el[m10] = ac * f + bd
            el[m11] = a * e
            el[m12] = ad * f - bc

            el[m20] = bc * f - ad
            el[m21] = b * e
            el[m22] = bd * f + ac
        }

        el[m30] = 0f
        el[m31] = 0f
        el[m32] = 0f

        el[m03] = 0f
        el[m13] = 0f
        el[m23] = 0f
        el[m33] = 1f

        return this
    }

    // set to rotation matrix from quaternion
    public fun makeRotationFromQuaternion(q: Quaternion): Matrix4f {
        return this.compose(Vector3f.ZERO, q)
    }
    
    // rotation matrix, looking from eye towards center oriented by up vector
    public fun lookAt(eye: Vector3f, target: Vector3f, up: Vector3f): Matrix4f {

        val vx = Vector3f.zero()
        val vy = Vector3f.zero()
        val vz = Vector3f.zero()
        val e = this.elements

        vz.subVectors(eye, target)

        // eye and target are in the same position
        val EPSILON = 0.00000001
        if ( Math.abs(vz.lengthSquared()) < EPSILON ) {
            vz.z = 1f
        }

        vz.normalize()
        vx.crossVectors(up, vz)

        if ( Math.abs(vx.lengthSquared()) < EPSILON ) {

            // up and z are parallel
            if ( Math.abs((up.z - 1f).toDouble()) < EPSILON ) {
                vz.x += 0.000001f
            }
            else {
                vz.z += 0.000001f
            }

            vz.normalize()
            vx.crossVectors( up, vz )
        }

        vx.normalize()
        vy.crossVectors(vz, vx)

        e[m00] = vx.x; e[m01] = vy.x; e[m02] = vz.x;
        e[m10] = vx.y; e[m11] = vy.y; e[m12] = vz.y;
        e[m20] = vx.z; e[m21] = vy.z; e[m22] = vz.z;

        return this
    }

    public fun getTranslation(): Vector3f {
        return Vector3f(this.elements[m03], this.elements[m13], this.elements[m23])
    }

    public fun setPosition(v: Vector3f): Matrix4f {
        this.elements[m03] = v.x
        this.elements[m13] = v.y
        this.elements[m23] = v.z
        return this
    }

    public fun setTranslation(x: Float, y: Float, z:Float): Matrix4f {
        this.elements[m03] = x
        this.elements[m13] = y
        this.elements[m23] = z
        return this
    }

    public fun scale(v: Vector3f): Matrix4f {
        val e = this.elements
        val x = v.x
        val y = v.y
        val z = v.z

        e[m00] *= x; e[m01] *= y; e[m02] *= z;
        e[m10] *= x; e[m11] *= y; e[m12] *= z;
        e[m20] *= x; e[m21] *= y; e[m22] *= z;
        e[m30] *= x; e[m31] *= y; e[m32] *= z;

        return this
    }

    public fun getMaxScaleOnAxis(): Double {
        val e = this.elements

        val scaleXSq = (e[m00] * e[m00] + e[m10] * e[m10] + e[m20] * e[m20]).toDouble()
        val scaleYSq = (e[m01] * e[m01] + e[m11] * e[m11] + e[m21] * e[m21]).toDouble()
        val scaleZSq = (e[m02] * e[m02] + e[m12] * e[m12] + e[m22] * e[m22]).toDouble()

        return Math.sqrt( Math.max(scaleXSq, Math.max(scaleYSq, scaleZSq)) )
    }

    public fun makeTranslation(x: Double, y: Double, z: Double): Matrix4f {
        return this.set(
            1f, 0f, 0f, x.toFloat(),
            0f, 1f, 0f, y.toFloat(),
            0f, 0f, 1f, z.toFloat(),
            0f, 0f, 0f, 1f
        )
    }

    public fun makeRotationX(theta: Double): Matrix4f {
        val c = Math.cos(theta).toFloat()
        val s = Math.sin(theta).toFloat()
        return this.set(
            1f, 0f, 0f, 0f,
            0f,  c, -s, 0f,
            0f,  s,  c, 0f,
            0f, 0f, 0f, 1f
        )
    }
    
    public fun makeRotationY(theta: Double): Matrix4f {
        val c = Math.cos(theta).toFloat()
        val s = Math.sin(theta).toFloat()
        return this.set(
             c, 0f,  s, 0f,
            0f, 1f, 0f, 0f,
            -s, 0f,  c, 0f,
            0f, 0f, 0f, 1f
        )
    }

    public fun makeRotationZ(theta: Double): Matrix4f {
        val c = Math.cos(theta).toFloat()
        val s = Math.sin(theta).toFloat()
        return this.set(
            c, -s,  0f, 0f,
            s,  c,  0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        )
    }

    // http://www.gamedev.net/reference/articles/article1199.asp
    public fun makeRotationAxis(axis: Vector3f, angle: Double): Matrix4f {
        val c = Math.cos(angle).toFloat()
        val s = Math.sin(angle).toFloat()
        val t = 1f - c

        val x = axis.x
        val y = axis.y
        val z = axis.z

        val tx = t * x
        val ty = t * y

        return this.set(
            tx * x + c,     tx * y - s * z, tx * z + s * y, 0f,
            tx * y + s * z, ty * y + c,     ty * z - s * x, 0f,
            tx * z - s * y, ty * z + s * x, t * z * z + c,  0f,
            0f, 0f, 0f, 1f
        )
    }

    public fun makeScale(x: Double, y: Double, z: Double): Matrix4f {
        val xf = x.toFloat()
        val yf = y.toFloat()
        val zf = z.toFloat()
        return this.set(
            xf, 0f, 0f, 0f,
            0f, yf, 0f, 0f,
            0f, 0f, zf, 0f,
            0f, 0f, 0f, 1f
        )
    }

    public fun makeShear(x: Double, y: Double, z: Double): Matrix4f {
        val xf = x.toFloat()
        val yf = y.toFloat()
        val zf = z.toFloat()
        return this.set(
            1f, yf, zf, 0f,
            xf, 1f, zf, 0f,
            xf, yf, 1f, 0f,
            0f, 0f, 0f, 1f
        )
    }

    /**
     * In minecraft, we are unable to scale models ingame
     * So compose functions will only use a position and
     * quaternion (rotation). The full position/rotation/scale
     * compositions are commented out in bottom in case needed
     * in future.
     */
    // set matrix to standard transform matrix with translate, rotate, scale
    public fun compose(position: Vector3f, quaternion: Quaternion): Matrix4f {

        val e = this.elements

        val x = quaternion.x
        val y = quaternion.y
        val z = quaternion.z
        val w = quaternion.w
        
        val x2 = x + x
        val y2 = y + y
        val z2 = z + z

        val xx = x * x2; val xy = x * y2; val xz = x * z2;
        val yy = y * y2; val yz = y * z2; val zz = z * z2;
        val wx = w * x2; val wy = w * y2; val wz = w * z2;

        e[m00] = ( 1f - ( yy + zz ) )
        e[m10] = ( xy + wz )
        e[m20] = ( xz - wy )
        e[m30] = 0f

        e[m01] = ( xy - wz )
        e[m11] = ( 1f - ( xx + zz ) )
        e[m21] = ( yz + wx )
        e[m31] = 0f

        e[m02] = ( xz + wy )
        e[m12] = ( yz - wx )
        e[m22] = ( 1f - ( xx + yy ) )
        e[m32] = 0f

        e[m03] = position.x
        e[m13] = position.y
        e[m23] = position.z
        e[m33] = 1f

        return this
    }

    // assume only position and rotation, no scale
    public fun decompose(position: Vector3f, quaternion: Quaternion): Matrix4f {

        val e = this.elements

        // set position component
        position.x = e[m03]
        position.y = e[m13]
        position.z = e[m23]

        // if determinant is negative, we need to invert a component
        val det = this.det()
        if ( det < 0.0 ) {
            val matTemp = this.clone()
            matTemp.elements[m00] = -matTemp.elements[m00]
            matTemp.elements[m10] = -matTemp.elements[m10]
            matTemp.elements[m20] = -matTemp.elements[m20]

            quaternion.setFromRotationMatrix(matTemp)
        }
        else {
            quaternion.setFromRotationMatrix(this)
        }

        return this
    }

    /*
    // set matrix to standard transform matrix with translate, rotate, scale
    public fun compose(position: Vector3f, quaternion: Quaternion, scale: Vector3f): Matrix4f {

        val e = this.elements

        val x = quaternion.x
        val y = quaternion.y
        val z = quaternion.z
        val w = quaternion.w
        
        val x2 = x + x
        val y2 = y + y
        val z2 = z + z

        val xx = x * x2; val xy = x * y2; val xz = x * z2;
        val yy = y * y2; val yz = y * z2; val zz = z * z2;
        val wx = w * x2; val wy = w * y2; val wz = w * z2;

        val sx = scale.x
        val sy = scale.y
        val sz = scale.z

        e[m00] = ( 1f - ( yy + zz ) ) * sx
        e[m10] = ( xy + wz ) * sx
        e[m20] = ( xz - wy ) * sx
        e[m30] = 0f

        e[m01] = ( xy - wz ) * sy
        e[m11] = ( 1f - ( xx + zz ) ) * sy
        e[m21] = ( yz + wx ) * sy
        e[m31] = 0f

        e[m02] = ( xz + wy ) * sz
        e[m12] = ( yz - wx ) * sz
        e[m22] = ( 1f - ( xx + yy ) ) * sz
        e[m32] = 0f

        e[m03] = position.x
        e[m13] = position.y
        e[m23] = position.z
        e[m33] = 1f

        return this
    }

    public fun decompose(position: Vector3f, quaternion: Quaternion, scale: Vector3f): Matrix4f {

        val e = this.elements

        var sx = Math.sqrt((e[m00]*e[m00] + e[m10]*e[m10] + e[m20]*e[m20]).toDouble()).toFloat()
        val sy = Math.sqrt((e[m01]*e[m01] + e[m11]*e[m11] + e[m21]*e[m21]).toDouble()).toFloat()
        val sz = Math.sqrt((e[m02]*e[m02] + e[m12]*e[m12] + e[m22]*e[m22]).toDouble()).toFloat()

        // if determine is negative, we need to invert one scale
        val det = this.det()
        if ( det < 0 ) {
            sx = -sx
        }

        position.x = e[m03]
        position.y = e[m13]
        position.z = e[m23]

        // remove scale to isolate rotation
        val matTemp = this.clone()

        val invSx = 1f / sx
        val invSy = 1f / sy
        val invSz = 1f / sz

        matTemp.elements[m00] *= invSx
        matTemp.elements[m10] *= invSx
        matTemp.elements[m20] *= invSx

        matTemp.elements[m01] *= invSy
        matTemp.elements[m11] *= invSy
        matTemp.elements[m21] *= invSy

        matTemp.elements[m02] *= invSz
        matTemp.elements[m12] *= invSz
        matTemp.elements[m22] *= invSz

        quaternion.setFromRotationMatrix(matTemp)

        scale.x = sx
        scale.y = sy
        scale.z = sz

        return this
    }
    */
}

