/**
 * Bone node in Skeleton for animation
 */

package phonon.puppet.objects

import java.util.UUID
import phonon.puppet.Puppet
import phonon.puppet.math.*

public class Bone(
    val name: String,
    val boneMatrix: Matrix4f,
    val boneMatrixWorld: Matrix4f, // bind pose
    override var parent: TransformGraphNode? = null
): TransformGraphNode {

    // scene graph
    override val children: ArrayList<TransformGraphNode> = arrayListOf()

    // transform
    override val matrix: Matrix4f = Matrix4f.identity()
    override val worldMatrix: Matrix4f = Matrix4f.identity()
    override val position: Vector3f = Vector3f.zero()
    override val rotation: Euler = Euler.zero()
    override val quaternion: Quaternion = Quaternion.new()

    // bind pose transforms
    val bindPosition: Vector3f
    val bindQuaternion: Quaternion

    // bone inverse matrix
    val boneMatrixInverse: Matrix4f = Matrix4f.identity()

    // dirty flag
    override val needsUpdate: Boolean = false

    // identifier
    val uuid: UUID = UUID.randomUUID()
    
    // associated skinned mesh
    var mesh: Mesh? = null

    init {
        // apply bone matrix inputs
        this.worldMatrix.copy(this.boneMatrixWorld)

        // bind time inverse bone matrix
        this.boneMatrixInverse.inv(this.boneMatrixWorld)

        // set local transform from parent
        val parent = this.parent
        val parentWorldMatrixInverse = if ( parent !== null ) {
            Matrix4f.zero().inv(parent.worldMatrix)
        } else {
            Matrix4f.identity()
        }

        this.matrix.multiplyMatrices(parentWorldMatrixInverse, worldMatrix)

        this.matrix.decompose(this.position, this.quaternion)
        this.rotation.setFromQuaternion(this.quaternion, this.rotation.order)

        // save bind position, quaternion
        this.bindPosition = this.position.clone()
        this.bindQuaternion = this.quaternion.clone()
    }

    /**
     * Assume no cleanup needed in bone tree.
     * (Assume no non-bone objects in bone trees)
     */
    override public fun destroy() {}
    
    // update world transform
    override public fun updateTransform() {
        // update local transform
        this.matrix.compose(this.position, this.quaternion)

        // update world transform
        val parent = this.parent
        if ( parent !== null ) {
            this.worldMatrix.multiplyMatrices(parent.worldMatrix, this.matrix)
        }
        else {
            this.worldMatrix.copy(this.matrix)
        }

        // update children
        for ( child in this.children ) {
            child.updateTransform()
        }

        // update linked mesh, overall transform is
        // bone_matrix_world * bind_matrix_inverse * mesh_parent_world * mesh_matrix
        val mesh = this.mesh
        if ( mesh !== null ) {
            val meshLocalInv = Matrix4f.zero().inv(mesh.matrix)

            val meshWorldMatrix = if ( mesh.parent !== null ) {
                mesh.worldMatrix.multiplyMatrices(mesh.parent!!.worldMatrix, mesh.matrix).clone()
            } else {
                mesh.matrix.clone()
            }
            
            // TODO: optimize out these multiplies...
            mesh.worldMatrix.multiplyMatrices(this.boneMatrixInverse, mesh.matrix)
            mesh.worldMatrix.multiplyMatrices(this.worldMatrix, mesh.worldMatrix)
            mesh.worldMatrix.multiplyMatrices(meshLocalInv, mesh.worldMatrix)
            mesh.worldMatrix.multiplyMatrices(meshWorldMatrix, mesh.worldMatrix)
        }
    }

    override public fun toString(): String {
        val children = if ( this.children.size > 0 ) {
            this.children.joinToString(",\n", "[\n", "\n]")
        } else {
            "[]"
        }

        return "Bone { name=${this.name}, children=${children} }"
    }
}