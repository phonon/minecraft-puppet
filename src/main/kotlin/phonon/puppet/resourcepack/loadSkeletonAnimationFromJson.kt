/**
 * Load model skeleton and animation data
 */

package phonon.puppet.resourcepack

import java.io.FileReader
import java.nio.file.Path
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser 
import phonon.puppet.math.*
import phonon.puppet.objects.Bone
import phonon.puppet.objects.Skeleton
import phonon.puppet.animation.*
import phonon.puppet.resourcepack.BoneAnimationData

// load json
public fun loadSkeletonAnimationFromJson(path: Path): BoneAnimationData {
    return FileReader(path.toFile()).use { reader ->
        val json = JsonParser().parse(reader)
        val jsonObj = json.getAsJsonObject()

        val modelName = jsonObj.get("name")?.getAsString()
        if ( modelName === null ) {
            System.err.println("Invalid model name while loading ${path}")
            return BoneAnimationData(listOf(), listOf())
        }

        // load skeleton
        val jsonBoneRoot = jsonObj.get("skeleton")?.getAsJsonObject()
        val skeleton: List<Skeleton> = if ( jsonBoneRoot !== null ) {
            // root bone
            val rootName = jsonBoneRoot.get("name")!!.getAsString();
            val rootMatrix = Matrix4f.identity()
            val rootMatrixLocal = Matrix4f.identity()
            val boneRoot = Bone(rootName, rootMatrix, rootMatrixLocal, null)

            // parse hierarchy
            for ( child in jsonBoneRoot.get("children")!!.getAsJsonArray() ) {
                parseBoneNode(boneRoot, child)
            }

            listOf(Skeleton(
                modelName,
                boneRoot
            ))
        }
        else {
            listOf()
        }

        // load animations
        val animations: MutableList<AnimationTrack> = mutableListOf()
        val jsonAnimations = jsonObj.get("animation")?.getAsJsonObject()
        if ( jsonAnimations !== null ) {
            for ( actionName in jsonAnimations.keySet() ) {
                val jsonAnimAction = jsonAnimations.get(actionName)!!.getAsJsonObject()

                // transform keyframes: map bone name -> list of transform keyframes
                val keyframesPosition: HashMap<String, List<Keyframe<Vector3f>>> = hashMapOf()
                val keyframesQuaternion: HashMap<String, List<Keyframe<Quaternion>>> = hashMapOf()

                for ( boneName in jsonAnimAction.keySet() ) {
                    val jsonBoneTrack = jsonAnimAction.get(boneName)!!.getAsJsonObject()
                    
                    // position track
                    val jsonPositionTrack = jsonBoneTrack.get("position").getAsJsonArray()
                    if ( jsonPositionTrack !== null ) {
                        val track: List<Keyframe<Vector3f>> = jsonPositionTrack.asSequence()
                        .map { elem ->
                            val jsonKeyframe = elem.getAsJsonArray()
                            
                            val tick = jsonKeyframe[0].getAsInt()
                            val interpolation = jsonKeyframe[1].getAsString()
                            val x = jsonKeyframe[2].getAsFloat()
                            val y = jsonKeyframe[3].getAsFloat()
                            val z = jsonKeyframe[4].getAsFloat()

                            Keyframe(tick, Vector3f(x, y, z), Interpolation.get(interpolation))
                        }
                        .toList()

                        keyframesPosition.put(boneName, track)
                    }
                    
                    // quaternion track
                    val jsonQuaternionTrack = jsonBoneTrack.get("quaternion").getAsJsonArray()
                    if ( jsonQuaternionTrack !== null ) {
                        val track: List<Keyframe<Quaternion>> = jsonQuaternionTrack.asSequence()
                        .map { elem ->
                            val jsonKeyframe = elem.getAsJsonArray()
                            
                            val tick = jsonKeyframe[0].getAsInt()
                            val interpolation = jsonKeyframe[1].getAsString()
                            val x = jsonKeyframe[2].getAsFloat()
                            val y = jsonKeyframe[3].getAsFloat()
                            val z = jsonKeyframe[4].getAsFloat()
                            val w = jsonKeyframe[5].getAsFloat()

                            Keyframe(tick, Quaternion(x, y, z, w), Interpolation.get(interpolation))
                        }
                        .toList()

                        keyframesQuaternion.put(boneName, track)
                    }
                    
                }

                // save animation
                animations.add(AnimationTrack(
                    actionName,
                    keyframesPosition,
                    keyframesQuaternion
                ))
            }
        }

        BoneAnimationData(
            skeleton,
            animations.toList()
        )
    }
}

// parse individual bone node
internal fun parseBoneNode(parent: Bone, current: JsonElement) {
    val jsonBoneNode = current.getAsJsonObject()

    // get bone matrices
    val boneName = jsonBoneNode.get("name")!!.getAsString()
    val jsonMatrix: List<Float> = jsonBoneNode.get("matrix")!!.getAsJsonArray().map {
        elem -> elem.getAsFloat()
    }
    val jsonMatrixLocal: List<Float> = jsonBoneNode.get("matrix_local")!!.getAsJsonArray().map {
        elem -> elem.getAsFloat()
    }

    // aliases for easier typing
    val m = jsonMatrix
    val ml = jsonMatrixLocal

    // bone matrix is 3x3 matrix on export
    val boneMatrix = Matrix4f.of(
        m[0],  m[1],  m[2],  m[3],
        m[4],  m[5],  m[6],  m[7],
        m[8],  m[9],  m[10], m[11],
        m[12], m[13], m[14], m[15]
    )
    val boneMatrixWorld = Matrix4f.of(
        ml[0],  ml[1],  ml[2],  ml[3],
        ml[4],  ml[5],  ml[6],  ml[7],
        ml[8],  ml[9],  ml[10], ml[11],
        ml[12], ml[13], ml[14], ml[15]
    )

    val boneNode = Bone(boneName, boneMatrix, boneMatrixWorld, parent)

    // add children to this node
    for ( child in jsonBoneNode.get("children")!!.getAsJsonArray() ) {
        parseBoneNode(boneNode, child)
    }

    // attach to parent
    parent.children.add(boneNode)
}
