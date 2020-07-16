/**
 * Interface for a node in a 3D object graph
 */

package phonon.puppet.objects

interface TransformGraphNode: Transform {
    var parent: TransformGraphNode?
    val children: ArrayList<TransformGraphNode>

    // cleanup self
    fun destroy()

    // add an graph node as a child
    // This will overwrite the existing parent of the obj
    fun add(obj: TransformGraphNode) {
        // make sure not already contained
        for ( child in this.children ) {
            if ( obj === child ) {
                return
            }
        }
        
        // create link
        obj.parent = this
        this.children.add(obj)
    }

    // remove a child
    fun remove(obj: TransformGraphNode) {
        for ( (i, child) in this.children.withIndex() ) {
            if ( obj === child ) {
                // remove link
                this.children.removeAt(i)
                obj.parent = null
                return
            }
        }
    }

    /**
     * Run function on every child recursively.
     * Function takes this object as input.
     */
    fun traverse(fn: (TransformGraphNode) -> Unit) {
        fn(this)
        
        for ( child in this.children ) {
            child.traverse(fn)
        }
    }
}