/**
 * Animation time series track for updating position, quaternion
 * properties for multiple bones.
 * 
 * Cache animation p (position) and q (quaternion) states per tick into
 * pre-sampled FloatArray tracks on run in format:
 *     position:   [p0.x, p0.y, p0.z,       p1.x, p1.y, p1.z,       ...]
 *     quaternion: [q0.x, q0.y, q0.z, q0.w, q1.x, q1.y, q1.z, q1.w, ... ]
 * 
 * Playing animations can iterate the track and consume
 * in chunks to gather position, quaternion data.
 * 
 * Keyframe name format for bone properties: "[name].[property].[component]"
 * e.g. "head.position.0", "head.quaternion.0", ...
 */

package phonon.puppet.animation

import phonon.puppet.math.*


// wrapper arround individual sampled property tracks
public class TransformTrack(
    val position: FloatArray?,
    val quaternion: FloatArray?
)

// keyframes map bone name -> keyframe list
public class AnimationTrack(
    val name: String,
    val keyframesPosition: HashMap<String, List<Keyframe<Vector3f>>>,
    val keyframesQuaternion: HashMap<String, List<Keyframe<Quaternion>>>
) {

    // map bone name -> sampled transform data in FloatArray format
    // containing pre-sampled position, quaternion data tracks
    val transformTracks: HashMap<String, TransformTrack> = hashMapOf()

    // animation length in ticks (calculated from input keyframes)
    val length: Int

    /**
     * Initialization:
     * - get length of animation (last keyframe point in time)
     * - presample keyframes to generate transform data tracks.
     */
    init {
        
        // get all bones involved in this animation
        val bonesToTransform: Set<String> = this.keyframesPosition.keys + this.keyframesQuaternion.keys
        var firstKeyframeTick: Int = 0
        var lastKeyframeTick: Int = 0

        // sort all keyframes (just in case)
        // and get animation length (last keyframe tick)
        for ( boneName in bonesToTransform ) {
            val keyframePositionList = this.keyframesPosition.get(boneName)
            if ( keyframePositionList !== null ) {
                val sortedKeyframeList = keyframePositionList.sortedBy { it.tick }

                this.keyframesPosition.put(boneName, sortedKeyframeList)

                val firstKeyframe = sortedKeyframeList.firstOrNull()
                if ( firstKeyframe !== null ) {
                    firstKeyframeTick = if ( firstKeyframe.tick < firstKeyframeTick ) {
                        firstKeyframe.tick
                    } else {
                        firstKeyframeTick
                    }
                }

                val lastKeyframe = sortedKeyframeList.lastOrNull()
                if ( lastKeyframe !== null ) {
                    lastKeyframeTick = if ( lastKeyframe.tick > lastKeyframeTick ) {
                        lastKeyframe.tick
                    } else {
                        lastKeyframeTick
                    }
                }
            }

            val keyframeQuaternionList = this.keyframesQuaternion.get(boneName)
            if ( keyframeQuaternionList !== null ) {
                val sortedKeyframeList = keyframeQuaternionList.sortedBy { it.tick }

                this.keyframesQuaternion.put(boneName, sortedKeyframeList)

                val firstKeyframe = sortedKeyframeList.firstOrNull()
                if ( firstKeyframe !== null ) {
                    firstKeyframeTick = if ( firstKeyframe.tick < firstKeyframeTick ) {
                        firstKeyframe.tick
                    } else {
                        firstKeyframeTick
                    }
                }

                val lastKeyframe = sortedKeyframeList.lastOrNull()
                if ( lastKeyframe !== null ) {
                    lastKeyframeTick = if ( lastKeyframe.tick > lastKeyframeTick ) {
                        lastKeyframe.tick
                    } else {
                        lastKeyframeTick
                    }
                }
            }
        }

        // add 1 to length because keyframe convention starts at 0
        this.length = 1 + lastKeyframeTick - firstKeyframeTick

        // form sampled transform tracks for each bone
        for ( boneName in bonesToTransform ) {
            val positionTrack = this.keyframesPosition.get(boneName)?.let { keyframeList ->
                val buffer = FloatArray(this.length * 3, { 0f })
                sampleVectorKeyframesIntoBuffer(keyframeList, buffer, this.length)
                buffer
            }

            val quaternionTrack = this.keyframesQuaternion.get(boneName)?.let { keyframeList ->
                val buffer = FloatArray(this.length * 4, { i -> if ( (i+1) % 4 == 0 ) 1f else 0f })
                sampleQuaternionKeyframesIntoBuffer(keyframeList, buffer, this.length)
                buffer
            }
            
            this.transformTracks.put(boneName, TransformTrack(positionTrack, quaternionTrack))
        }

        // for ( (trackName, keyframeList) in keyframesInput.entries ) {
        //     val targetPropertyParts: List<String> = trackName.split(".")
        //     if ( targetPropertyParts.size < 3 ) {
        //         continue
        //     }

        //     val boneName: String = targetPropertyParts[0]

        //     // mark bone to be transformed
        //     bonesToTransform.add(boneName)

        //     // sort keyframe list by time (just in case)
        //     val sortedKeyframeList = keyframeList.sortedBy { it.tick }
        //     this.keyframes.put(trackName, sortedKeyframeList)

        //     // get last keyframe and update tick
        //     val lastKeyframe = sortedKeyframeList.lastOrNull()
        //     if ( lastKeyframe !== null ) {
        //         lastKeyframeTick = if ( lastKeyframe.tick > lastKeyframeTick ) {
        //             lastKeyframe.tick
        //         } else {
        //             lastKeyframeTick
        //         }
        //     }
        // }

        // // set animation length
        // this.length = lastKeyframeTick

        // // for each bone, add transform tracks
        // for ( boneName in bonesToTransform ) {

        //     // get all bone transform properties
        //     val bonePosX = "${boneName}.position.x"
        //     val bonePosY = "${boneName}.position.y"
        //     val bonePosZ = "${boneName}.position.z"
        //     val boneQuatX = "${boneName}.quaternion.x"
        //     val boneQuatY = "${boneName}.quaternion.y"
        //     val boneQuatZ = "${boneName}.quaternion.z"
        //     val boneQuatW = "${boneName}.quaternion.w"
            
        //     // position keyframes: can sample components separately
        //     val keyframesPosX = this.keyframes.get(bonePosX)
        //     val keyframesPosY = this.keyframes.get(bonePosY)
        //     val keyframesPosZ = this.keyframes.get(bonePosZ)

        //     val positionTrack: FloatArray? = if (
        //         keyframesPosX !== null ||
        //         keyframesPosY !== null ||
        //         keyframesPosZ !== null
        //     ) {
        //         val buffer = FloatArray(this.length * 3, { 0f })
        //         keyframesPosX?.let { keys -> sampleKeyframesIntoBuffer(keys, buffer, this.length, 3, 0) }
        //         keyframesPosY?.let { keys -> sampleKeyframesIntoBuffer(keys, buffer, this.length, 3, 1) }
        //         keyframesPosZ?.let { keys -> sampleKeyframesIntoBuffer(keys, buffer, this.length, 3, 2) }
        //         buffer
        //     } else {
        //         null
        //     }

        //     // quaternion keyframes: merge components into Quaternion keyframes
        //     // in form (tick, Quaternion, INTERPOLATION) so we can slerp
        //     var keyframesQuatX = this.keyframes.get(boneQuatX)
        //     var keyframesQuatY = this.keyframes.get(boneQuatY)
        //     var keyframesQuatZ = this.keyframes.get(boneQuatZ)
        //     var keyframesQuatW = this.keyframes.get(boneQuatW)

        //     val quaternionTrack: FloatArray? = if (
        //         keyframesQuatX !== null ||
        //         keyframesQuatY !== null ||
        //         keyframesQuatZ !== null ||
        //         keyframesQuatW !== null
        //     ) {
        //         val buffer = FloatArray(this.length * 4, { i -> if ( (i+1) % 4 == 0 ) 1f else 0f })
                
        //         keyframesQuatX = keyframesQuatX ?: listOf(Keyframe<Double>(0, 0.0, Interpolation.LINEAR))
        //         keyframesQuatY = keyframesQuatY ?: listOf(Keyframe<Double>(0, 0.0, Interpolation.LINEAR))
        //         keyframesQuatZ = keyframesQuatZ ?: listOf(Keyframe<Double>(0, 0.0, Interpolation.LINEAR))
        //         keyframesQuatW = keyframesQuatW ?: listOf(Keyframe<Double>(0, 1.0, Interpolation.LINEAR))
                
        //         val quaternionKeyframes = formQuaternionKeyframes(keyframesQuatX, keyframesQuatY, keyframesQuatZ, keyframesQuatW)
                
        //         sampleQuaternionKeyframesIntoBuffer(quaternionKeyframes, buffer, this.length)

        //         buffer
        //     } else {
        //         null
        //     }

        //     this.transformTracks.put(boneName, TransformTrack(positionTrack, quaternionTrack))
        // }
    }
    
    /**
     * Write track data for bone into position, quaternion structures.
     * 
     * @param name name of data track (should correspond to model bone name)
     * @param tick frame tick in animation
     * @param position position vector output to write data
     * @param quaternion quaternion output to write data
     * @return boolean status if bone track found and written successfully
     */
    public fun writeIntoPositionQuaternion(name: String, tick: Int, position: Vector3f, quaternion: Quaternion): Boolean {
        val dataTrack = this.transformTracks.get(name)
        if ( dataTrack === null ) {
            return false
        }

        val positionTrack = dataTrack.position
        val quaternionTrack = dataTrack.quaternion

        if ( positionTrack !== null ) {
            val index = tick * 3
            position.x = positionTrack[index]
            position.y = positionTrack[index+1]
            position.z = positionTrack[index+2]
        }
        
        if ( quaternionTrack !== null ) {
            val index = tick * 4
            quaternion.x = quaternionTrack[index]
            quaternion.y = quaternionTrack[index+1]
            quaternion.z = quaternionTrack[index+2]
            quaternion.w = quaternionTrack[index+3]
        }

        return true
    }

    
    // static manager methods
    companion object {
        // track library
        public val library: HashMap<String, AnimationTrack> = hashMapOf()

        /**
         * Add AnimationTrack prototype into library.
         * Will overwrite existing keys in the library.
         */
        public fun save(animTrack: AnimationTrack) {
            AnimationTrack.library.put(animTrack.name, animTrack)
        }

        /**
         * Delete data in library
         */
        public fun clear() {
            AnimationTrack.library.clear()
        }
        
        /**
         * Return AnimationTrack stored in library
         * 
         * @param name name in library
         */
        public fun get(name: String): AnimationTrack? {
            return AnimationTrack.library.get(name)
        }

        /**
         * Return list of track names
         */
        public fun list(): List<String> {
            return AnimationTrack.library.keys.toList()
        }
    }
}



// merge component keyframes into Quaternion object keyframes
// ASSUME all components have keyframes at same points in time
// so, use keyframesX as reference for iterating
private fun formQuaternionKeyframes(
    keyframesX: List<Keyframe<Double>>,
    keyframesY: List<Keyframe<Double>>,
    keyframesZ: List<Keyframe<Double>>,
    keyframesW: List<Keyframe<Double>>
): List<Keyframe<Quaternion>>  {

    // other components
    val iterKeysY = keyframesY.iterator()
    val iterKeysZ = keyframesZ.iterator()
    val iterKeysW = keyframesW.iterator()

    var currKeyY = iterKeysY.next()
    var currKeyZ = iterKeysZ.next()
    var currKeyW = iterKeysW.next()

    val keyframesQuat: MutableList<Keyframe<Quaternion>> = mutableListOf()
    
    for ( currKeyX in keyframesX ) {
        var frame: Int = currKeyX.tick

        currKeyY = if ( iterKeysY.hasNext() && currKeyY.tick < frame ) iterKeysY.next() else currKeyY
        currKeyZ = if ( iterKeysZ.hasNext() && currKeyZ.tick < frame ) iterKeysZ.next() else currKeyZ
        currKeyW = if ( iterKeysW.hasNext() && currKeyW.tick < frame ) iterKeysW.next() else currKeyW

        val x = currKeyX.value.toFloat()
        val y = currKeyY.value.toFloat()
        val z = currKeyZ.value.toFloat()
        val w = currKeyW.value.toFloat()

        keyframesQuat.add(Keyframe(
            frame,
            Quaternion(x, y, z, w).normalize(),
            currKeyX.interpolation
        ))
    }

    return keyframesQuat.toList()
}


// sample double valued keyframe list into blocked array
private fun sampleDoubleKeyframesIntoBuffer(keyframes: List<Keyframe<Double>>, buffer: FloatArray, numBlocks: Int, block: Int, offset: Int) {
    if ( keyframes.size == 0 ) {
        return
    }

    val keyIter = keyframes.iterator()
    var currKey = keyIter.next()
    var nextKey = if ( keyIter.hasNext() ) keyIter.next() else currKey

    // interpolate and write samples
    for ( i in 0 until numBlocks ) {
        // update current, next keyframes
        if ( i >= nextKey.tick && keyIter.hasNext() ) {
            currKey = nextKey
            nextKey = keyIter.next()
        }

        // interpolate value
        val value = if ( nextKey.tick > currKey.tick ) {
            nextKey.interpolation.interpolate(currKey.value, nextKey.value, (i - currKey.tick).toDouble() / (nextKey.tick - currKey.tick).toDouble() )
        }
        else {
            currKey.value
        }

        buffer[i*block + offset] = value.toFloat()
    }
}

// sample Vector3f keyframes list into blocked array
private fun sampleVectorKeyframesIntoBuffer(keyframes: List<Keyframe<Vector3f>>, buffer: FloatArray, numBlocks: Int) {
    if ( keyframes.size == 0 ) {
        return
    }

    val keyIter = keyframes.iterator()
    var currKey = keyIter.next()
    var nextKey = if ( keyIter.hasNext() ) keyIter.next() else currKey

    val v = Vector3f.zero()

    // interpolate and write samples
    for ( i in 0 until numBlocks ) {
        // update current, next keyframes
        if ( i >= nextKey.tick && keyIter.hasNext() ) {
            currKey = nextKey
            nextKey = keyIter.next()
        }

        // slerp interpolation parameter
        val a = if ( nextKey.tick > currKey.tick ) {
            nextKey.interpolation.interpolate(0.0, 1.0, (i - currKey.tick).toDouble() / (nextKey.tick - currKey.tick).toDouble() )
        }
        else {
            0.0
        }

        v.lerpVectors(currKey.value, nextKey.value, a)

        val index = i * 3
        buffer[index] = v.x
        buffer[index+1] = v.y
        buffer[index+2] = v.z
    }
}

// sample quaternion keyframes list into blocked array
private fun sampleQuaternionKeyframesIntoBuffer(keyframes: List<Keyframe<Quaternion>>, buffer: FloatArray, numBlocks: Int) {
    if ( keyframes.size == 0 ) {
        return
    }

    val keyIter = keyframes.iterator()
    var currKey = keyIter.next()
    var nextKey = if ( keyIter.hasNext() ) keyIter.next() else currKey

    val q = Quaternion.zero()

    // interpolate and write samples
    for ( i in 0 until numBlocks ) {
        // update current, next keyframes
        if ( i >= nextKey.tick && keyIter.hasNext() ) {
            currKey = nextKey
            nextKey = keyIter.next()
        }

        // slerp interpolation parameter
        val a = if ( nextKey.tick > currKey.tick ) {
            nextKey.interpolation.interpolate(0.0, 1.0, (i - currKey.tick).toDouble() / (nextKey.tick - currKey.tick).toDouble() )
        }
        else {
            0.0
        }

        q.slerp(currKey.value, nextKey.value, a)

        val index = i * 4
        buffer[index] = q.x
        buffer[index+1] = q.y
        buffer[index+2] = q.z
        buffer[index+3] = q.w
    }
}
