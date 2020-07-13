/**
 * Vector3 Float32
 */

package phonon.puppet.math

import org.bukkit.Location

public data class Vector3f(
    var x: Float,
    var y: Float,
    var z: Float
) {
    companion object {

        // re-usable constant vectors
        // (must not be mutated by any client)
        public val ZERO: Vector3f = Vector3f(0f, 0f, 0f)
        public val ONE: Vector3f = Vector3f(1f, 1f, 1f)

        // return new zero vector
        public fun zero(): Vector3f {
            return Vector3f(0.0f, 0.0f, 0.0f)
        }

        // return new one vector
        public fun one(): Vector3f {
            return Vector3f(1.0f, 1.0f, 1.0f)
        }

        // return new x-axis unit vector
        public fun x(): Vector3f {
            return Vector3f(1.0f, 0.0f, 0.0f)
        }
        
        // return new y-axis unit vector
        public fun y(): Vector3f {
            return Vector3f(0.0f, 1.0f, 0.0f)
        }
        
        // return new z-axis unit vector
        public fun z(): Vector3f {
            return Vector3f(0.0f, 0.0f, 1.0f)
        }

        // from bukkit location
        public fun fromLocation(loc: Location): Vector3f {
            return Vector3f(loc.x.toFloat(), loc.y.toFloat(), loc.z.toFloat())
        }
    }

    // implement array index: vec3[index]
    operator fun get(i: Int): Float {
        when ( i ) {
            0 -> return this.x
            1 -> return this.y
            2 -> return this.z
            else -> throw ArrayIndexOutOfBoundsException(i)
        }
    }

    operator fun set(i: Int, v: Float) {
        when ( i ) {
            0 -> this.x = v
            1 -> this.y = v
            2 -> this.z = v
            else -> throw ArrayIndexOutOfBoundsException(i)
        }
    }

    public fun clone(): Vector3f {
        return Vector3f(this.x, this.y, this.z)
    }

    public fun copy(other: Vector3f): Vector3f {
        this.x = other.x
        this.y = other.y
        this.z = other.z
        return this
    }

    public fun set(x: Double, y: Double, z: Double): Vector3f {
        this.x = x.toFloat()
        this.y = y.toFloat()
        this.z = z.toFloat()
        return this
    }

    public fun set(x: Float, y: Float, z: Float): Vector3f {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    public fun setScalar(scalar: Double): Vector3f {
        this.x = scalar.toFloat()
        this.y = scalar.toFloat()
        this.z = scalar.toFloat()
        return this
    }
    
    public fun setX(x: Double): Vector3f {
        this.x = x.toFloat()
        return this
    }

    public fun setY(y: Double): Vector3f {
        this.y = y.toFloat()
        return this
    }

    public fun setZ(z: Double): Vector3f {
        this.z = z.toFloat()
        return this
    }

    public fun add(v: Vector3f): Vector3f {
        this.x += v.x
        this.y += v.y
        this.z += v.z
        return this
    }

    public fun addScalar(d: Double): Vector3f {
        val f = d.toFloat()
        this.x += f
        this.y += f
        this.z += f
        return this
    }

    public fun addVectors(a: Vector3f, b: Vector3f): Vector3f {
        this.x = a.x + b.x
        this.y = a.y + b.y
        this.z = a.z + b.z
        return this
    }

    public fun addScaledVector(v: Vector3f, d: Double): Vector3f {
        val f = d.toFloat()
        this.x += (v.x * f)
        this.y += (v.y * f)
        this.z += (v.z * f)
        return this
    }

    public fun sub(v: Vector3f): Vector3f {
        this.x -= v.x
        this.y -= v.y
        this.z -= v.z
        return this
    }

    public fun subScalar(d: Double): Vector3f {
        val f = d.toFloat()
        this.x -= f
        this.y -= f
        this.z -= f
        return this
    }

    public fun subVectors(a: Vector3f, b: Vector3f): Vector3f {
        this.x = a.x - b.x
        this.y = a.y - b.y
        this.z = a.z - b.z
        return this
    }

    public fun multiply(v: Vector3f): Vector3f {
        this.x *= v.x
        this.y *= v.y
        this.z *= v.z
        return this
    }

    public fun multiplyScalar(d: Double): Vector3f {
        val f = d.toFloat()
        this.x *= f
        this.y *= f
        this.z *= f
        return this
    }

    public fun multiplyVectors(a: Vector3f, b: Vector3f): Vector3f {
        this.x = a.x * b.x
        this.y = a.y * b.y
        this.z = a.z * b.z
        return this
    }
    
    public fun divide(v: Vector3f): Vector3f {
        this.x /= v.x
        this.y /= v.y
        this.z /= v.z
        return this
    }

    public fun divideScalar(d: Double): Vector3f {
        val f = 1f / d.toFloat()
        this.x *= f
        this.y *= f
        this.z *= f
        return this
    }

    public fun divideVectors(a: Vector3f, b: Vector3f): Vector3f {
        this.x = a.x / b.x
        this.y = a.y / b.y
        this.z = a.z / b.z
        return this
    }

    public fun dot(v: Vector3f): Double {
        return (this.x * v.x + this.y * v.y + this.z * v.z).toDouble()
    }

    public fun cross(v: Vector3f): Vector3f {
        return this.crossVectors(this, v)
    }

    public fun crossVectors(a: Vector3f, b: Vector3f): Vector3f {
        this.x = a.y * b.z - a.z * b.y
        this.y = a.z * b.x - a.x * b.z
        this.z = a.x * b.y - a.y * b.x
        return this
    }

    public fun lengthSquared(): Double {
        return (this.x * this.x + this.y * this.y + this.z * this.z).toDouble()
    }

    public fun length(): Double {
        return Math.sqrt( (this.x * this.x + this.y * this.y + this.z * this.z).toDouble() )
    }

    public fun manhattanLength(): Double {
        return Math.abs(this.x.toDouble()) + Math.abs(this.y.toDouble()) + Math.abs(this.z.toDouble())
    }

    public fun distanceToSquared(v: Vector3f ): Double {
        val dx = this.x - v.x
        val dy = this.y - v.y
        val dz = this.z - v.z
        return (dx * dx + dy * dy + dz * dz).toDouble()
    }

    public fun distanceTo(v: Vector3f): Double {
        return Math.sqrt( this.distanceToSquared(v) )
    }

    public fun manhattanDistanceTo(v: Vector3f): Double {
        return Math.abs( (this.x - v.x).toDouble() ) + Math.abs( (this.y - v.y).toDouble() ) + Math.abs( (this.z - v.z).toDouble() )
    }

    public fun normalize(): Vector3f {
        return this.divideScalar(this.length())
    }

    public fun setLength(length: Double): Vector3f {
        return this.normalize().multiplyScalar(length)
    }

    public fun min(v: Vector3f): Vector3f {
        this.x = Math.min( this.x, v.x )
        this.y = Math.min( this.y, v.y )
        this.z = Math.min( this.z, v.z )
        return this
    }

    public fun max(v: Vector3f): Vector3f {
        this.x = Math.max( this.x, v.x )
        this.y = Math.max( this.y, v.y )
        this.z = Math.max( this.z, v.z )
        return this
    }
    
    // component-wise clamp
    // assumes min < max
    public fun clamp(min: Vector3f, max: Vector3f): Vector3f {
        this.x = Math.max( min.x.toDouble(), Math.min( max.x.toDouble(), this.x.toDouble() ) ).toFloat()
        this.y = Math.max( min.y.toDouble(), Math.min( max.y.toDouble(), this.y.toDouble() ) ).toFloat()
        this.z = Math.max( min.z.toDouble(), Math.min( max.z.toDouble(), this.z.toDouble() ) ).toFloat()
        return this
    }

    public fun clampScalar(min: Double, max: Double): Vector3f {
        this.x = Math.max( min, Math.min( max, this.x.toDouble() ) ).toFloat()
        this.y = Math.max( min, Math.min( max, this.y.toDouble() ) ).toFloat()
        this.z = Math.max( min, Math.min( max, this.z.toDouble() ) ).toFloat()
        return this
    }

    public fun clampLength(min: Double, max: Double): Vector3f {
        val length = this.length()
        return this.divideScalar(length).multiplyScalar( Math.max( min, Math.min( max, length ) ) )
    }

    public fun floor(): Vector3f {
        this.x = Math.floor( this.x.toDouble() ).toFloat()
        this.y = Math.floor( this.y.toDouble() ).toFloat()
        this.z = Math.floor( this.z.toDouble() ).toFloat()
        return this
    }

    public fun ceil(): Vector3f {
        this.x = Math.ceil( this.x.toDouble() ).toFloat()
        this.y = Math.ceil( this.y.toDouble() ).toFloat()
        this.z = Math.ceil( this.z.toDouble() ).toFloat()
        return this
    }

    public fun round(): Vector3f {
        this.x = Math.round( this.x.toDouble() ).toFloat()
        this.y = Math.round( this.y.toDouble() ).toFloat()
        this.z = Math.round( this.z.toDouble() ).toFloat()
        return this
    }

    public fun roundToZero(): Vector3f {
        this.x = if ( this.x < 0f ) Math.ceil( this.x.toDouble() ).toFloat() else Math.floor( this.x.toDouble() ).toFloat()
        this.y = if ( this.y < 0f ) Math.ceil( this.y.toDouble() ).toFloat() else Math.floor( this.y.toDouble() ).toFloat()
        this.z = if ( this.z < 0f ) Math.ceil( this.z.toDouble() ).toFloat() else Math.floor( this.z.toDouble() ).toFloat()
        return this
    }

    public fun negate(): Vector3f {
        this.x = -this.x
        this.y = -this.y
        this.z = -this.z
        return this
    }

    public fun applyEuler(euler: Euler): Vector3f {
        return this.applyQuaternion( Quaternion.fromEuler(euler) )
    }

    public fun applyAxisAngle(axis: Vector3f, angle: Double): Vector3f {
        return this.applyQuaternion( Quaternion.fromAxisAngle(axis, angle) )
    }

    // multiply by matrix4: matrix x vector = [4x4] x [4x1]
    // vector3 -> implicit [x y z 1]
    public fun applyMatrix4(mat4: Matrix4f): Vector3f {
        val x = this.x
        val y = this.y
        val z = this.z
        val e = mat4.elements

        val w = 1f / ( e[m30] * x + e[m31] * y + e[m32] * z + e[m33] )

        this.x = ( e[m00] * x + e[m01] * y + e[m02] * z + e[m03] ) * w
        this.y = ( e[m10] * x + e[m11] * y + e[m12] * z + e[m13] ) * w
        this.z = ( e[m20] * x + e[m21] * y + e[m22] * z + e[m23] ) * w

        return this
    }

    // multiply by rotation component contained within a 4x4 matrix
    // assumes the upper-left 3x3 portion of the matrix is a pure rotation
    // (i.e. no scale)
    public fun applyRotationMatrix4(mat4: Matrix4f): Vector3f {
        val x = this.x
        val y = this.y
        val z = this.z
        val e = mat4.elements

        this.x = e[m00] * x + e[m01] * y + e[m02] * z
        this.y = e[m10] * x + e[m11] * y + e[m12] * z
        this.z = e[m20] * x + e[m21] * y + e[m22] * z

        return this
    }

    public fun applyQuaternion(q: Quaternion): Vector3f {
        val x = this.x
        val y = this.y
        val z = this.z
        val qx = q.x
        val qy = q.y
        val qz = q.z
        val qw = q.w

        // calculate quat * vector
        val ix = qw * x + qy * z - qz * y
        val iy = qw * y + qz * x - qx * z
        val iz = qw * z + qx * y - qy * x
        val iw = -qx * x - qy * y - qz * z

        // calculate result * inverse quat
        this.x = ix * qw + iw * -qx + iy * -qz - iz * -qy
        this.y = iy * qw + iw * -qy + iz * -qx - ix * -qz
        this.z = iz * qw + iw * -qz + ix * -qy - iy * -qx

        return this
    }

    // input: affine matrix
    // vector interpreted as a direction
    public fun transformDirection(mat4: Matrix4f): Vector3f {
        val x = this.x
        val y = this.y
        val z = this.z
        val e = mat4.elements

        this.x = e[m00] * x + e[m01] * y + e[m02] * z
        this.y = e[m10] * x + e[m11] * y + e[m12] * z
        this.z = e[m20] * x + e[m21] * y + e[m22] * z

        return this.normalize()
    }

    // linear interpolation with another vector
    public fun lerp(v: Vector3f, alpha: Double): Vector3f {
        val af = alpha.toFloat()
        this.x += ( v.x - this.x ) * af
        this.y += ( v.y - this.y ) * af
        this.z += ( v.z - this.z ) * af
        return this
    }

    // linear interpolation between two vectors
    public fun lerpVectors(v1: Vector3f, v2: Vector3f, alpha: Double): Vector3f {
        val af = alpha.toFloat()
        this.x = v1.x * (1f - af) + v2.x * af
        this.y = v1.y * (1f - af) + v2.y * af
        this.z = v1.z * (1f - af) + v2.z * af
        return this
    }

    public fun projectOnVector(v: Vector3f): Vector3f {

        val mag = v.lengthSquared()

        if ( mag == 0.0 ) {
            return this.set(0.0, 0.0, 0.0)
        }

        val scalar = (v.dot(this) / mag).toFloat()
        
        this.x = v.x * scalar
        this.y = v.y * scalar
        this.z = v.z * scalar

        return this
    }

    public fun projectOnPlane(planeNormal: Vector3f): Vector3f {
        val v = this.clone().projectOnVector(planeNormal)
        return this.sub(v)
    }

    // reflect returns incident vector off plane orthogonal to normal
    // normal assumed to have unit length
    public fun reflect(normal: Vector3f): Vector3f {
        return this.sub(normal).multiplyScalar(2.0 * this.dot(normal))
    }

    public fun angleTo(v: Vector3f): Double {

        val denominator = Math.sqrt( this.lengthSquared() * v.lengthSquared() );

        if ( denominator == 0.0 ) {
            return Math.PI / 2.0
        }

        var theta = this.dot(v) / denominator

        // clamp, to handle numerical problems
        theta = Math.max(-1.0, Math.min(1.0, theta))

        return Math.acos(theta)
    }

    public fun setFromMatrixPosition(m: Matrix4f): Vector3f {
        val e = m.elements
        this.x = e[m03]
        this.y = e[m13]
        this.z = e[m23]
        return this
    }

    public fun setFromMatrixScale(m: Matrix4f): Vector3f {
        val sx = this.setFromMatrixColumn(m, 0).length()
        val sy = this.setFromMatrixColumn(m, 1).length()
        val sz = this.setFromMatrixColumn(m, 2).length()

        this.x = sx.toFloat()
        this.y = sy.toFloat()
        this.z = sz.toFloat()

        return this
    }

    public fun fromFloatArray(array: FloatArray, offset: Int): Vector3f {
        this.x = array[offset]
        this.y = array[offset + 1]
        this.z = array[offset + 2]
        return this
    }

    public fun toFloatArray(): FloatArray {
        val array: FloatArray = FloatArray(3)
        array[0] = this.x
        array[1] = this.y
        array[2] = this.z
        return array
    }

    public fun toFloatArray(array: FloatArray, offset: Int): FloatArray {
        array[offset] = this.x
        array[offset + 1] = this.y
        array[offset + 2] = this.z
        return array
    }

    // get matrix column, matrices in column-major format
    // index column is [0, 1, 2, 3]
    public fun setFromMatrixColumn(m: Matrix4f, index: Int): Vector3f {
        when ( index ) {
            0 -> {
                this.x = m.elements[m00]
                this.y = m.elements[m10]
                this.z = m.elements[m20]
            }
            1 -> {
                this.x = m.elements[m01]
                this.y = m.elements[m11]
                this.z = m.elements[m21]
            }
            2 -> {
                this.x = m.elements[m02]
                this.y = m.elements[m12]
                this.z = m.elements[m22]
            }
            3 -> {
                this.x = m.elements[m03]
                this.y = m.elements[m13]
                this.z = m.elements[m23]
            }
            else -> throw ArrayIndexOutOfBoundsException(index)
        }

        return this
    }
}