/**
 * Interface for a node in a 3D object graph
 */

package phonon.puppet.objects

interface GraphNode: Transform {
    var parent: GraphNode?
    val children: ArrayList<GraphNode>

    // add an graph node as a child
    // This will overwrite the existing parent of the obj
    fun add(obj: GraphNode) {
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
    fun remove(obj: GraphNode) {
        for ( (i, child) in this.children.withIndex() ) {
            if ( obj === child ) {
                // remove link
                this.children.removeAt(i)
                obj.parent = null
                return
            }
        }
    }
}