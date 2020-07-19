/**
 * Container for a Bone object graph
 */

package phonon.puppet.objects

import phonon.puppet.animation.AnimationMixer

public class Skeleton(
    val name: String,
    val root: Bone
) {
    
    // map name -> bone for easy access
    val bones: LinkedHashMap<String, Bone> = LinkedHashMap()

    init {
        // go through bone tree and create links
        fun saveBone(boneMap: MutableMap<String, Bone>, bone: Bone) {
            boneMap.put(bone.name, bone)
            for ( child in bone.children ) {
                if ( child is Bone ) {
                    saveBone(boneMap, child)
                }
            }
        }

        saveBone(this.bones, this.root)
    }

    // create deep clone of this skeleton
    public fun clone(): Skeleton {
        // recursive deep clone
        fun cloneBone(parent: TransformGraphNode?, bone: Bone): Bone {
            val boneCopy = Bone(
                bone.name,
                bone.boneMatrix,
                bone.boneMatrixWorld,
                parent
            )

            for ( child in bone.children ) {
                val childCopy = cloneBone(boneCopy, child as Bone)
                boneCopy.children.add(childCopy)
            }

            return boneCopy
        }
        
        val copy = Skeleton(
            this.name,
            cloneBone(null, this.root)
        )

        return copy
    }

    /**
     * Update skeleton bone transforms. 
     */
    public fun update() {
        this.root.updateTransform()
    }

    /**
     * Reset all bones in skeleton to bind pose
     */
    public fun reset() {
        this.root.traverse({ bone ->
            if ( bone is Bone ) {
                bone.reset()
            }
        })
        
        this.root.updateTransform()
    }

    /**
     * Static class manager methods
     */
    companion object {
        // prototype library, clone one of these to get a skeleton
        val library: HashMap<String, Skeleton> = hashMapOf()

        /**
         * Add skeleton prototype to library.
         * Will overwrite existing keys in the library.
         */
        public fun save(skeleton: Skeleton) {
            Skeleton.library.put(skeleton.name, skeleton)
        }

        /**
         * Delete data in library
         */
        public fun clear() {
            Skeleton.library.clear()
        }

        /**
         * Create skeleton by cloning existing skeleton in library
         */
        public fun create(name: String): Skeleton? {
            val skeleton = Skeleton.library.get(name)
            
            if ( skeleton === null ) {
                return null
            }

            // return deep clone
            return skeleton.clone()
        }
    }
}