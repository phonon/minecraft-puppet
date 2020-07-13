/**
 * Object that has a world transform:
 * 
 * matrix - local transform relative to a parent
 * matrixWorld - world transform (relative to absolute space)
 * 
 */

package phonon.puppet.objects

import phonon.puppet.math.*

interface Transform {
    
    // local transform
    val matrix: Matrix4f
    
    // world transform (absolute coordinates)
    val worldMatrix: Matrix4f

    // dirty flag
    val needsUpdate: Boolean

    // position vector, must keep in sync with matrix
    val position: Vector3f

    // rotation euler angles, must keep in sync with matrix
    val rotation: Euler

    // rotation quaternion, must keep in sync with matrix
    val quaternion: Quaternion

    // update local and world transform
    public fun updateTransform()

    // set local position, update matrices
    public fun setPosition(x: Double, y: Double, z: Double, update: Boolean = true) {
        this.position.set(x, y, z)

        if ( update ) {
            this.updateTransform()
        }
    }

    // set local rotation, update matrices
    public fun setRotation(x: Double, y: Double, z: Double, update: Boolean = true) {
        this.rotation.set(x, y, z, this.rotation.order)
        this.quaternion.setFromEuler(this.rotation)

        if ( update ) {
            this.updateTransform()
        }
    }

    // set local rotation, update matrices
    public fun setRotation(x: Float, y: Float, z: Float, update: Boolean = true) {
        this.rotation.set(x, y, z, this.rotation.order)
        this.quaternion.setFromEuler(this.rotation)

        if ( update ) {
            this.updateTransform()
        }
    }

    // set local quaternion, update matrices
    public fun setQuaternion(x: Double, y: Double, z: Double, w: Double, update: Boolean = true) {
        this.quaternion.set(x, y, z, w)
        this.rotation.setFromQuaternion(this.quaternion, this.rotation.order)

        if ( update ) {
            this.updateTransform()
        }
    }

}