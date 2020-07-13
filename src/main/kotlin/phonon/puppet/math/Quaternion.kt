/**
 * Quaternion Float32
 */

package phonon.puppet.math

// utility function to clamp v in [min, max]
private fun clamp(v: Double, min: Double, max: Double): Double {
    return Math.max(min, Math.min(max, v))
}

public data class Quaternion(
    var x: Float,
    var y: Float,
    var z: Float,
    var w: Float
) {

    companion object {

        // return quaternion of all zeros
        public fun zero(): Quaternion {
            return Quaternion(0f, 0f, 0f, 0f)
        }

        // default rest pose quaternion, no rotation
        public fun new(): Quaternion {
            return Quaternion(0f, 0f, 0f, 1f)
        }

        public fun fromEuler(euler: Euler): Quaternion {
            val q = Quaternion(0f, 0f, 0f, 0f)
            return q.setFromEuler(euler)
        }

        public fun fromAxisAngle(axis: Vector3f, angle: Double): Quaternion {
            val q = Quaternion(0f, 0f, 0f, 0f)
            return q.setFromAxisAngle(axis, angle)
        }

        public fun fromRotationMatrix(mat4: Matrix4f): Quaternion {
            val q = Quaternion(0f, 0f, 0f, 0f)
            return q.setFromRotationMatrix(mat4)
        }
    }

    // implement array index: quat[index]
    operator fun get(i: Int): Float {
        when ( i ) {
            0 -> return this.x
            1 -> return this.y
            2 -> return this.z
            3 -> return this.w
            else -> throw ArrayIndexOutOfBoundsException(i)
        }
    }

    operator fun set(i: Int, v: Float) {
        when ( i ) {
            0 -> this.x = v
            1 -> this.y = v
            2 -> this.z = v
            3 -> this.w = v
            else -> throw ArrayIndexOutOfBoundsException(i)
        }
    }

    public fun clone(): Quaternion {
        return Quaternion(this.x, this.y, this.z, this.w )
    }

    public fun copy(other: Quaternion): Quaternion {
        this.x = other.x
        this.y = other.y
        this.z = other.z
        this.w = other.w
        return this
    }

    public fun set(x: Double, y: Double, z: Double, w: Double): Quaternion {
        this.x = x.toFloat()
        this.y = y.toFloat()
        this.z = z.toFloat()
        this.w = w.toFloat()
        return this
    }

    public fun set(x: Float, y: Float, z: Float, w: Float): Quaternion {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        return this
    }

    // http://www.mathworks.com/matlabcentral/fileexchange/20696-function-to-convert-between-dcm-euler-angles-quaternions-and-euler-vectors/content/SpinCalc.m
    public fun setFromEuler(euler: Euler): Quaternion {
        val x = euler.x.toDouble()
        val y = euler.y.toDouble()
        val z = euler.z.toDouble()

        val c1 = Math.cos( x / 2.0 ).toFloat()
        val c2 = Math.cos( y / 2.0 ).toFloat()
        val c3 = Math.cos( z / 2.0 ).toFloat()

        val s1 = Math.sin( x / 2.0 ).toFloat()
        val s2 = Math.sin( y / 2.0 ).toFloat()
        val s3 = Math.sin( z / 2.0 ).toFloat()

        when ( euler.order ) {
            EulerOrder.XYZ -> {
                this.x = s1 * c2 * c3 + c1 * s2 * s3
                this.y = c1 * s2 * c3 - s1 * c2 * s3
                this.z = c1 * c2 * s3 + s1 * s2 * c3
                this.w = c1 * c2 * c3 - s1 * s2 * s3
            }

            EulerOrder.YXZ -> {
                this.x = s1 * c2 * c3 + c1 * s2 * s3
                this.y = c1 * s2 * c3 - s1 * c2 * s3
                this.z = c1 * c2 * s3 - s1 * s2 * c3
                this.w = c1 * c2 * c3 + s1 * s2 * s3
            }

            EulerOrder.ZXY -> {
                this.x = s1 * c2 * c3 - c1 * s2 * s3
                this.y = c1 * s2 * c3 + s1 * c2 * s3
                this.z = c1 * c2 * s3 + s1 * s2 * c3
                this.w = c1 * c2 * c3 - s1 * s2 * s3
            }

            EulerOrder.ZYX -> {
                this.x = s1 * c2 * c3 - c1 * s2 * s3
                this.y = c1 * s2 * c3 + s1 * c2 * s3
                this.z = c1 * c2 * s3 - s1 * s2 * c3
                this.w = c1 * c2 * c3 + s1 * s2 * s3
            }

            EulerOrder.YZX -> {
                this.x = s1 * c2 * c3 + c1 * s2 * s3
                this.y = c1 * s2 * c3 + s1 * c2 * s3
                this.z = c1 * c2 * s3 - s1 * s2 * c3
                this.w = c1 * c2 * c3 - s1 * s2 * s3
            }

            EulerOrder.XZY -> {
                this.x = s1 * c2 * c3 - c1 * s2 * s3
                this.y = c1 * s2 * c3 - s1 * c2 * s3
                this.z = c1 * c2 * s3 + s1 * s2 * c3
                this.w = c1 * c2 * c3 + s1 * s2 * s3
            }
        }

        return this
    }

    // http://www.euclideanspace.com/maths/geometry/rotations/conversions/angleToQuaternion/index.htm
    // assumes axis is normalized
    public fun setFromAxisAngle(axis: Vector3f, angle: Double): Quaternion {
        val halfAngle = angle / 2.0
        val s = Math.sin( halfAngle ).toFloat()

        this.x = axis.x * s
        this.y = axis.y * s
        this.z = axis.z * s
        this.w = Math.cos( halfAngle ).toFloat()

        return this
    }

    // http://www.euclideanspace.com/maths/geometry/rotations/conversions/matrixToQuaternion/index.htm
    public fun setFromRotationMatrix(mat4: Matrix4f): Quaternion {

        // assumes the upper 3x3 of mat4 is unscaled pure rotation
        val e = mat4.elements
        val e00 = e[m00]; val e01 = e[m01]; val e02 = e[m02];
        val e10 = e[m10]; val e11 = e[m11]; val e12 = e[m12];
        val e20 = e[m20]; val e21 = e[m21]; val e22 = e[m22];

        val trace = (e00 + e11 + e22).toDouble()

        if ( trace > 0.0 ) {
            val s = 0.5f / Math.sqrt( trace + 1.0 ).toFloat()

            this.w = 0.25f / s
            this.x = ( e21 - e12 ) * s
            this.y = ( e02 - e20 ) * s
            this.z = ( e10 - e01 ) * s
        }
        else if ( e00 > e11 && e00 > e22 ) {
            val s = 2.0f * Math.sqrt( (1f + e00 - e11 - e22).toDouble() ).toFloat()

            this.w = ( e21 - e12 ) / s
            this.x = 0.25f * s
            this.y = ( e01 + e10 ) / s
            this.z = ( e02 + e20 ) / s
        }
        else if ( e11 > e22 ) {
            val s = 2.0f * Math.sqrt( (1f + e11 - e00 - e22).toDouble() ).toFloat()

            this.w = ( e02 - e20 ) / s
            this.x = ( e01 + e10 ) / s
            this.y = 0.25f * s
            this.z = ( e12 + e21 ) / s
        }
        else {
            val s = 2.0f * Math.sqrt( (1f + e22 - e00 - e11).toDouble() ).toFloat()

            this.w = ( e10 - e01 ) / s
            this.x = ( e02 + e20 ) / s
            this.y = ( e12 + e21 ) / s
            this.z = 0.25f * s
        }

        return this
    }

    // assumes direction vectors vFrom and vTo are normalized
    public fun setFromUnitVectors(vFrom: Vector3f, vTo: Vector3f): Quaternion {
        var r: Double = vFrom.dot( vTo ) + 1.0
        val EPSILON: Double = 0.000001

        if ( r < EPSILON ) {
            if ( Math.abs( vFrom.x.toDouble() ) > Math.abs( vFrom.z.toDouble() ) ) {
                this.x = -vFrom.y
                this.y = vFrom.x
                this.z = 0f
                this.w = 0f
            }
            else {
                this.x = 0f
                this.y = -vFrom.z
                this.z = vFrom.y
                this.w = 0f
            }
        }
        else { // cross product vFrom x vTo
            this.x = vFrom.y * vTo.z - vFrom.z * vTo.y
            this.y = vFrom.z * vTo.x - vFrom.x * vTo.z
            this.z = vFrom.x * vTo.y - vFrom.y * vTo.x
            this.w = r.toFloat()
        }

        return this.normalize()
    }

    // http://www.euclideanspace.com/maths/algebra/realNormedAlgebra/quaternions/code/index.htm
    public fun multiplyQuaternions(a: Quaternion, b: Quaternion): Quaternion {
        val qax = a.x
        val qay = a.y
        val qaz = a.z
        val qaw = a.w

        val qbx = b.x
        val qby = b.y
        val qbz = b.z
        val qbw = b.w

        this.x = qax * qbw + qaw * qbx + qay * qbz - qaz * qby
        this.y = qay * qbw + qaw * qby + qaz * qbx - qax * qbz
        this.z = qaz * qbw + qaw * qbz + qax * qby - qay * qbx
        this.w = qaw * qbw - qax * qbx - qay * qby - qaz * qbz

        return this
    }

    public fun multiply(q: Quaternion): Quaternion {
        return this.multiplyQuaternions(this, q )
    }

    public fun premultiply(q: Quaternion): Quaternion {
        return this.multiplyQuaternions(q, this )
    }

    public fun conjugate(): Quaternion {
        this.x *= -1f
        this.y *= -1f
        this.z *= -1f
        return this
    }

    // quaternion is assumed to have unit length
    public fun inverse(): Quaternion {
        return this.conjugate()
    }

    public fun dot(q: Quaternion ): Double {
        return (this.x * q.x + this.y * q.y + this.z * q.z + this.w * q.w).toDouble()
    }

    public fun lengthSquared(): Double {
        return (this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w).toDouble()
    }

    public fun length(): Double {
        return Math.sqrt( (this.x * this.x + this.y * this.y + this.z * this.z + this.w * this.w).toDouble() )
    }

    public fun normalize(): Quaternion {
        val len = this.length()

        if ( len == 0.0 ) {
            this.x = 0f
            this.y = 0f
            this.z = 0f
            this.w = 1f
        }
        else {
            val invLen = (1.0 / len).toFloat()

            this.x = this.x * invLen
            this.y = this.y * invLen
            this.z = this.z * invLen
            this.w = this.w * invLen
        }

        return this
    }

    public fun angleTo(q: Quaternion): Double {
        return 2.0 * Math.acos( Math.abs( clamp( this.dot( q ), -1.0, 1.0 ) ) )
    }

    // slerp between this q and qb with factor t in [0, 1]
    // http://www.euclideanspace.com/maths/algebra/realNormedAlgebra/quaternions/slerp/
    public fun slerp(qb: Quaternion, t: Double): Quaternion {

        if ( t <= 0.0 ) {
            return this
        }

        if ( t >= 1.0 ) {
            return this.copy( qb )
        }

        val x = this.x
        val y = this.y
        val z = this.z
        val w = this.w

        var cosHalfTheta = (w * qb.w + x * qb.x + y * qb.y + z * qb.z).toDouble()

        if ( cosHalfTheta < 0f ) {
            this.w = -qb.w
            this.x = -qb.x
            this.y = -qb.y
            this.z = -qb.z

            cosHalfTheta = -cosHalfTheta
        }
        else {
            this.copy( qb )
        }

        if ( cosHalfTheta >= 1.0 ) {
            this.w = w
            this.x = x
            this.y = y
            this.z = z
            return this
        }

        val sqrSinHalfTheta = 1.0 - cosHalfTheta * cosHalfTheta;

        val EPSILON = 0.00000001
        if ( sqrSinHalfTheta <= EPSILON ) {
            val s = (1.0 - t).toFloat()
            val tf = t.toFloat()

            this.w = s * w + tf * this.w
            this.x = s * x + tf * this.x
            this.y = s * y + tf * this.y
            this.z = s * z + tf * this.z

            return this.normalize()
        }

        val sinHalfTheta = Math.sqrt( sqrSinHalfTheta )
        val halfTheta = Math.atan2( sinHalfTheta, cosHalfTheta )
        val ratioA = (Math.sin( ( 1.0 - t ) * halfTheta ) / sinHalfTheta).toFloat()
        val ratioB = (Math.sin( t * halfTheta ) / sinHalfTheta).toFloat()

        this.w = ( w * ratioA + this.w * ratioB )
        this.x = ( x * ratioA + this.x * ratioB )
        this.y = ( y * ratioA + this.y * ratioB )
        this.z = ( z * ratioA + this.z * ratioB )

        return this
    }
    
    // performs slerp between quaternions qa and qb then copies into this
    public fun slerp(qa: Quaternion, qb: Quaternion, t: Double): Quaternion {
        return this.copy(qa).slerp(qb, t)
    }

    // rotate towards target q
    // step in radians
    public fun rotateTowards(q: Quaternion, step: Double): Quaternion {

        val angle = this.angleTo(q)
        
        val EPSILON = 0.00001
        if ( angle < EPSILON ) {
            return this.copy(q)
        }

        val t = Math.min( 1.0, step / angle );

        return this.slerp(q, t)
    }

    public fun fromFloatArray(array: FloatArray, offset: Int): Quaternion {
        this.x = array[offset]
        this.y = array[offset + 1]
        this.z = array[offset + 2]
        this.w = array[offset + 3]
        return this
    }

    public fun toFloatArray(): FloatArray {
        val array: FloatArray = FloatArray(4)
        array[0] = this.x
        array[1] = this.y
        array[2] = this.z
        array[3] = this.w
        return array
    }

    public fun toFloatArray(array: FloatArray, offset: Int): FloatArray {
        array[offset] = this.x
        array[offset + 1] = this.y
        array[offset + 2] = this.z
        array[offset + 3] = this.w
        return array
    }

}