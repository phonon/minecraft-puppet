/**
 * Euler angles Float32
 * 
 * NOTE: Minecraft Euler order is ZYX
 * so that is used as default euler order
 */

package phonon.puppet.math

// utility function to clamp v in [min, max]
private fun clamp(v: Double, min: Double, max: Double): Double {
    return Math.max(min, Math.min(max, v))
}
public enum class EulerOrder {
    XYZ,
    YZX,
    ZXY,
    XZY,
    YXZ,
    ZYX
}

public data class Euler(
    var x: Float,
    var y: Float,
    var z: Float,
    var order: EulerOrder
) {

    companion object {
        val XYZ = EulerOrder.XYZ
        val YZX = EulerOrder.YZX
        val ZXY = EulerOrder.ZXY
        val XZY = EulerOrder.XZY
        val YXZ = EulerOrder.YXZ
        val ZYX = EulerOrder.ZYX

        // return quaternion of all zeros
        public fun zero(): Euler {
            return Euler(0f, 0f, 0f, EulerOrder.ZYX)
        }

        // return new euler from quaternion
        public fun fromQuaternion(q: Quaternion, order: EulerOrder): Euler {
            val euler = Euler(0f, 0f, 0f, order)
            return euler.setFromQuaternion(q, order)
        }

        // return new euler from rotation matrix
        public fun fromRotationMatrix(mat4: Matrix4f, order: EulerOrder): Euler {
            val euler = Euler(0f, 0f, 0f, order)
            return euler.setFromRotationMatrix(mat4, order)
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

    public fun clone(): Euler {
        return Euler(this.x, this.y, this.z, this.order)
    }

    public fun copy(other: Euler): Euler {
        this.x = other.x
        this.y = other.y
        this.z = other.z
        return this
    }

    public fun set(x: Float, y: Float, z: Float, order: EulerOrder): Euler {
        this.x = x
        this.y = y
        this.z = z
        this.order = order
        return this
    }

    public fun set(x: Double, y: Double, z: Double, order: EulerOrder): Euler {
        this.x = x.toFloat()
        this.y = y.toFloat()
        this.z = z.toFloat()
        this.order = order
        return this
    }
    
    // assumes the upper 3x3 of m is a pure rotation matrix (i.e, unscaled)
    public fun setFromRotationMatrix(mat4: Matrix4f, order: EulerOrder): Euler {

        val e = mat4.elements
        val e00 = e[m00].toDouble(); val e01 = e[m01].toDouble(); val e02 = e[m02].toDouble();
        val e10 = e[m10].toDouble(); val e11 = e[m11].toDouble(); val e12 = e[m12].toDouble();
        val e20 = e[m20].toDouble(); val e21 = e[m21].toDouble(); val e22 = e[m22].toDouble();

        when ( order ) {

            EulerOrder.XYZ -> {
                this.y = Math.asin( clamp( e02, -1.0, 1.0 ) ).toFloat()

                if ( Math.abs( e02 ) < 0.9999999 ) {
                    this.x = Math.atan2( -e12, e22 ).toFloat()
                    this.z = Math.atan2( -e01, e00 ).toFloat()
                }
                else {
                    this.x = Math.atan2( e21, e11 ).toFloat()
                    this.z = 0f
                }
            }

            EulerOrder.YXZ -> {
                this.x = Math.asin( - clamp( e12, -1.0, 1.0 ) ).toFloat()

                if ( Math.abs( e12 ) < 0.9999999 ) {
                    this.y = Math.atan2( e02, e22 ).toFloat()
                    this.z = Math.atan2( e10, e11 ).toFloat()
                }
                else {
                    this.y = Math.atan2( -e20, e00 ).toFloat()
                    this.z = 0f
                }
            }
            
            EulerOrder.ZXY -> {
                this.x = Math.asin( clamp( e21, -1.0, 1.0 ) ).toFloat()

                if ( Math.abs( e21 ) < 0.9999999 ) {
                    this.y = Math.atan2( -e20, e22 ).toFloat()
                    this.z = Math.atan2( -e01, e11 ).toFloat()
                }
                else {
                    this.y = 0f
                    this.z = Math.atan2( e10, e00 ).toFloat()
                }
            }

            EulerOrder.ZYX -> {
                this.y = Math.asin( - clamp( e20, -1.0, 1.0 ) ).toFloat()

                if ( Math.abs( e20 ) < 0.9999999 ) {
                    this.x = Math.atan2( e21, e22 ).toFloat()
                    this.z = Math.atan2( e10, e00 ).toFloat()
                }
                else {
                    this.x = 0f
                    this.z = Math.atan2( -e01, e11 ).toFloat()
                }
            }

            EulerOrder.YZX -> {
                this.z = Math.asin( clamp( e10, -1.0, 1.0 ) ).toFloat()

                if ( Math.abs( e10 ) < 0.9999999 ) {
                    this.x = Math.atan2( -e12, e11 ).toFloat()
                    this.y = Math.atan2( -e20, e00 ).toFloat()
                }
                else {
                    this.x = 0f
                    this.y = Math.atan2( e02, e22 ).toFloat()
                }
            }

            EulerOrder.XZY -> {
                this.z = Math.asin( - clamp( e01, -1.0, 1.0 ) ).toFloat()

                if ( Math.abs( e01 ) < 0.9999999 ) {
                    this.x = Math.atan2( e21, e11 ).toFloat()
                    this.y = Math.atan2( e02, e00 ).toFloat()
                }
                else {
                    this.x = Math.atan2( -e12, e22 ).toFloat()
                    this.y = 0f
                }
            }
        }

        this.order = order
        
        return this
    }

    // 1. derives 3x3 rotation matrix parameters
    // 2. converts to euler based on order
    // re-uses code above for calculating from a rotation matrix 
    public fun setFromQuaternion(q: Quaternion, order: EulerOrder): Euler {
        val x = q.x
        val y = q.y
        val z = q.z
        val w = q.w
        
        val x2 = x + x
        val y2 = y + y
        val z2 = z + z

        val xx = x * x2; val xy = x * y2; val xz = x * z2;
        val yy = y * y2; val yz = y * z2; val zz = z * z2;
        val wx = w * x2; val wy = w * y2; val wz = w * z2;

        // rotation matrix column parameters
        val e00 = ( 1f - ( yy + zz ) ).toDouble()
        val e10 = ( xy + wz ).toDouble()
        val e20 = ( xz - wy ).toDouble()

        val e01 = ( xy - wz ).toDouble()
        val e11 = ( 1f - ( xx + zz ) ).toDouble()
        val e21 = ( yz + wx ).toDouble()

        val e02 = ( xz + wy ).toDouble()
        val e12 = ( yz - wx ).toDouble()
        val e22 = ( 1f - ( xx + yy ) ).toDouble()

        when ( order ) {

            EulerOrder.XYZ -> {
                this.y = Math.asin( clamp( e02, -1.0, 1.0 ) ).toFloat()

                if ( Math.abs( e02 ) < 0.9999999 ) {
                    this.x = Math.atan2( -e12, e22 ).toFloat()
                    this.z = Math.atan2( -e01, e00 ).toFloat()
                }
                else {
                    this.x = Math.atan2( e21, e11 ).toFloat()
                    this.z = 0f
                }
            }

            EulerOrder.YXZ -> {
                this.x = Math.asin( - clamp( e12, -1.0, 1.0 ) ).toFloat()

                if ( Math.abs( e12 ) < 0.9999999 ) {
                    this.y = Math.atan2( e02, e22 ).toFloat()
                    this.z = Math.atan2( e10, e11 ).toFloat()
                }
                else {
                    this.y = Math.atan2( -e20, e00 ).toFloat()
                    this.z = 0f
                }
            }
            
            EulerOrder.ZXY -> {
                this.x = Math.asin( clamp( e21, -1.0, 1.0 ) ).toFloat()

                if ( Math.abs( e21 ) < 0.9999999 ) {
                    this.y = Math.atan2( -e20, e22 ).toFloat()
                    this.z = Math.atan2( -e01, e11 ).toFloat()
                }
                else {
                    this.y = 0f
                    this.z = Math.atan2( e10, e00 ).toFloat()
                }
            }

            EulerOrder.ZYX -> {
                this.y = Math.asin( - clamp( e20, -1.0, 1.0 ) ).toFloat()

                if ( Math.abs( e20 ) < 0.9999999 ) {
                    this.x = Math.atan2( e21, e22 ).toFloat()
                    this.z = Math.atan2( e10, e00 ).toFloat()
                }
                else {
                    this.x = 0f
                    this.z = Math.atan2( -e01, e11 ).toFloat()
                }
            }

            EulerOrder.YZX -> {
                this.z = Math.asin( clamp( e10, -1.0, 1.0 ) ).toFloat()

                if ( Math.abs( e10 ) < 0.9999999 ) {
                    this.x = Math.atan2( -e12, e11 ).toFloat()
                    this.y = Math.atan2( -e20, e00 ).toFloat()
                }
                else {
                    this.x = 0f
                    this.y = Math.atan2( e02, e22 ).toFloat()
                }
            }

            EulerOrder.XZY -> {
                this.z = Math.asin( - clamp( e01, -1.0, 1.0 ) ).toFloat()

                if ( Math.abs( e01 ) < 0.9999999 ) {
                    this.x = Math.atan2( e21, e11 ).toFloat()
                    this.y = Math.atan2( e02, e00 ).toFloat()
                }
                else {
                    this.x = Math.atan2( -e12, e22 ).toFloat()
                    this.y = 0f
                }
            }
        }

        this.order = order
        
        return this
    }
    
    public fun setFromVector3(v: Vector3f, order: EulerOrder): Euler {
        this.x = v.x
        this.y = v.y
        this.z = v.z
        this.order = order
        return this
    }

    // warning: this discards revolution information
    public fun reorder(newOrder: EulerOrder): Euler {
        val q = Quaternion.fromEuler(this)
        return this.setFromQuaternion(q, newOrder)
    }

    public fun fromFloatArray(array: FloatArray, offset: Int): Euler {
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

    public fun toVector3(): Vector3f {
        return Vector3f(this.x, this.y, this.z)
    }
    
    public fun toVector3(target: Vector3f): Vector3f {
        return target.set(this.x, this.y, this.z)
    }

}
