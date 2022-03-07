/**
 * Manager for creating resource packs and loading
 * server side model resource data
 */

package phonon.puppet.resourcepack

import java.io.FileReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.bukkit.Material
import phonon.puppet.Config
import phonon.puppet.objects.Skeleton
import phonon.puppet.animation.AnimationTrack
import phonon.puppet.utils.file.*


// model bone/animation data file extension ending
private val MODEL_DATA_FILE_EXT = ".data.json"

// default resourcepack metadata: pack.mcmeta
private val PACK_FORMAT = 5
private val PACK_METADATA = """{
  "pack": {
    "pack_format": ${PACK_FORMAT},
    "description": "puppet"
  }
}"""

// item.json override file to add CustomModelData entries
// this only returns header part, generateResourcePack()
// completes this with overrides and closes the .json properly
private fun generateItemOverrideJsonHeader(mat: Material): String {
    return """{
    "parent": "item/handheld",
    "textures": {
        "layer0": "item/${mat.toString().toLowerCase()}"
    },
    "display": {},
    "overrides": [
"""
}

/**
 * Wrapper for data returned by Resource.load() 
 * customModelData: list of .json model files without .json extension
 *                  (e.g. "model_name.part.json" -> "model_name.part")
 * skeletons: list of Skeleton objects loaded
 * animations: list of AnimationTrack objects loaded
 */
data class ModelData(
    val customModelData: List<String>,
    val skeletons: List<Skeleton>,
    val animations: List<AnimationTrack>
)

/**
 * Data from model .json files in directories,
 * just needed as intermediate return type
 */
data class ModelFileData(
    val customModelData: List<String>,
    val animationFiles: List<Path>
)

/**
 * Bones + animations of a model
 */
data class BoneAnimationData(
    val skeleton: List<Skeleton>,
    val animations: List<AnimationTrack>
)

/**
 * Manager for data and resourcepack
 */
public object Resource {

    public fun initialize() {
        // generate directories
        val pathModels = Paths.get(Config.pathPlugin, Config.pathModels)
        val pathResourcePack = Paths.get(Config.pathPlugin, Config.pathResourcePack)

        Files.createDirectories(pathModels)
        Files.createDirectories(pathResourcePack)
    }
    
    /**
     * Loads resource model data (models, bones, animations, ...)
     * from .json files in puppet plugin data folders.
     */
    public fun load(): ModelData {
        // get custom model data load existing model list
        val pathModels = Paths.get(Config.pathPlugin, Config.pathModels)
        val pathCustomModelData = pathModels.resolve(Config.pathCustomModelData)
        val existingCustomModelData = loadCustomModelDataCache(pathCustomModelData)
        val (customModelData, animationFiles) = loadCustomModelData(existingCustomModelData)

        // generate resource pack
        Resource.generateResourcePack(customModelData)

        // update write model list cache
        Resource.saveCustomModelDataCache(customModelData, pathCustomModelData)
        
        // load animations
        val skeletons: MutableList<Skeleton> = mutableListOf()
        val animations: MutableList<AnimationTrack> = mutableListOf()

        for ( animData in animationFiles ) {
            try {
                val (modelSkeleton, modelAnimations) = Resource.loadSkeletonAnimation(animData)
                skeletons.addAll(modelSkeleton)
                animations.addAll(modelAnimations)
            }
            catch ( err: Exception ) {
                System.err.println("[Puppet] Failed to parse ${animData}")
                err.printStackTrace()
            }
        }

        return ModelData(
            customModelData,
            skeletons.toList(),
            animations.toList()
        )
    }

    /**
     * Read cached custom model list in "models.json" file,
     * default config in "puppet/data/models.json"
     */
    public fun loadCustomModelDataCache(path: Path): List<String> {
        if ( Files.exists(path) ) {
            return loadCustomModelDataFromJson(path)
        }

        return listOf()
    }

    /**
     * Write custom model list into .json file,
     * default config in "puppet/data/models.json"
     */
    public fun saveCustomModelDataCache(customModelData: List<String>, outputPath: Path) {
        try {
            val json = StringBuilder()

            json.append("{\n  \"models\": [\n")
            for ( (i, s) in customModelData.withIndex() ) {
                json.append("    \"${s}\"")
                if ( i < customModelData.size - 1 ) {
                    json.append(",\n")
                }
            }
            json.append("\n  ]\n}")

            writeStringToFile(json.toString(), outputPath)
        }
        catch ( err: Exception ) {
            err.printStackTrace()
        }
    }

    /**
     * Load skeleton bone + animation data from .json path.
     * Internally, this will call Skeleton.save and AnimationTrack.save
     * which will update those object's data libraries.
     */
    public fun loadSkeletonAnimation(path: Path): BoneAnimationData {
        return loadSkeletonAnimationFromJson(path)
    }

    /**
     * Returns list of .json model strings, where each index in
     * the list corresponds to its CustomModelData integer item
     * metadata. The input `existingData` allows appending to an
     * existing CustomModelData list, so that existing models with
     * metadata are not adjusted.
     * 
     * @param existingData list of .json models to append
     */
    public fun loadCustomModelData(existingData: List<String> = listOf()): ModelFileData {

        val existingDataSet: Set<String> = existingData.toSet()
        val modelsToAdd: MutableList<String> = mutableListOf()

        val pathPlugin = Paths.get(Config.pathPlugin)
        val pathModels = pathPlugin.resolve(Config.pathModels)
        val modelDirectories = Files.walk(pathModels, 1)
            .filter { item -> Files.isDirectory(item) && !item.equals(pathModels) }

        val animationFiles: MutableList<Path> = mutableListOf()

        // go through each model components in separate directory
        for ( dir in modelDirectories ) {
            Files.walk(dir, 1)
                .filter { item -> Files.isRegularFile(item) }
                .map { item -> item.getName(item.getNameCount()-1).toString() } // file name chunk at end
                .filter { s -> 
                    if ( s.endsWith(".json") ) {
                        if ( s.endsWith(MODEL_DATA_FILE_EXT) ) {
                            animationFiles.add(dir.resolve(s))
                            false
                        }
                        else {
                            true
                        }
                    }
                    else {
                        false
                    }
                }
                .map { s -> removeFileExtension(s) }
                .filter { s -> !existingDataSet.contains(s) }
                .forEach { s -> modelsToAdd.add(s) }
        }

        val customModelData: List<String> = existingData + modelsToAdd

        return ModelFileData(customModelData, animationFiles)
    }

    /**
     * Create resourcepack from models inside data directory.
     */
    public fun generateResourcePack(customModelData: List<String>) {
        // ===============================
        // clean/create temp build output directory
        // ===============================
        val pathPlugin = Paths.get(Config.pathPlugin)
        val pathResourcePack = pathPlugin.resolve(Config.pathResourcePack)
        val pathBuild = pathResourcePack.resolve("build")
        deleteDirectory(pathBuild)
        Files.createDirectories(pathBuild)

        // ===============================
        // write resourcepack metadata
        // ===============================
        val pathMetadataInput = pathResourcePack.resolve(Config.pathResourcePackMeta)
        val pathIconInput = pathResourcePack.resolve(Config.pathResourcePackIcon)
        val pathMetadataOutput = pathBuild.resolve("pack.mcmeta")
        val pathIconOutput = pathBuild.resolve("pack.png")

        // pack metadata
        if ( Files.exists(pathMetadataInput) && Files.isRegularFile(pathMetadataInput) ) {
            Files.copy(pathMetadataInput, pathMetadataOutput, StandardCopyOption.REPLACE_EXISTING)
        }
        else { // create default
            writeStringToFile(PACK_METADATA, pathMetadataOutput)
        }

        // pack icon
        if ( Files.exists(pathIconInput) && Files.isRegularFile(pathIconInput) ) {
            Files.copy(pathIconInput, pathIconOutput, StandardCopyOption.REPLACE_EXISTING)
        }

        // ===============================
        // copy custom model parts into folders
        // ===============================
        
        // output base path for models, textures
        val pathBuildModels = pathBuild.resolve(Paths.get("assets", "minecraft", "models", "item"))
        val pathBuildTextures = pathBuild.resolve(Paths.get("assets", "minecraft", "textures"))
        Files.createDirectories(pathBuildModels)
        Files.createDirectories(pathBuildTextures)

        val pathModels = pathPlugin.resolve(Config.pathModels)
        val modelDirectories = Files.walk(pathModels, 1)
            .filter { item -> Files.isDirectory(item) && !item.equals(pathModels) }

        // map "model.chunk" -> "model"
        // in case chunks named differently than expected format "model.chunk"
        val modelPartToName: HashMap<String, String> = hashMapOf()

        for ( dir in modelDirectories ) {
            // list of model .json files
            val jsonFiles: MutableList<Path> = mutableListOf()

            // map model texture name -> texture image file path in directory
            // e.g. if a model has texture "item/a/b", the texture name is "b"
            val textures: HashMap<String, Path> = hashMapOf()

            // sort into json and texture files
            Files.walk(dir, 1)
                .filter { item -> Files.isRegularFile(item) }
                .forEach { item ->
                    val s = item.toString()

                    if ( s.endsWith(".json") && !s.endsWith(MODEL_DATA_FILE_EXT) ) {
                        jsonFiles.add(item)
                    }
                    else if ( s.endsWith(".png") || s.endsWith(".jpg") || s.endsWith(".jpeg") ) {
                        val textureName = removeFileExtension(item.getName(item.getNameCount()-1).toString())
                        textures.put(textureName, item)
                    }
                }
            
            // skip empty directory
            if ( jsonFiles.size == 0 ) {
                continue
            }

            // create model directory in output
            val modelName = dir.getName(dir.getNameCount()-1).toString()
            Files.createDirectories(pathBuildModels.resolve(modelName))

            // map texture output path -> input path file
            // order is out -> in if for strange reasons someone writes the
            // same input texture into multiple output directories
            val texturesToCopy: HashMap<Path, Path> = hashMapOf()

            // copy into build directory and gather texture path outputs
            for ( modelPart in jsonFiles ) {
                val pathModelOutput = pathBuildModels.resolve(pathModels.relativize(modelPart))
                Files.copy(modelPart, pathModelOutput, StandardCopyOption.REPLACE_EXISTING)

                // map "model.chunk" -> "model"
                val chunkName = removeFileExtension(modelPart.getName(modelPart.getNameCount()-1).toString())
                modelPartToName.put(chunkName, modelName)

                // get texture file path
                try {
                    val texturePaths: List<Path> = readModelTextures(modelPart)
                    
                    // match with texture files in directory
                    for ( texture in texturePaths ) {
                        val textureName = texture.getName(texture.getNameCount()-1).toString()
                        val pathTextureIn = textures.get(textureName)
                        if ( pathTextureIn !== null ) {
                            // output path components:
                            // "build/assets/minecraft/textures" + "item/dir/.../" + "texture_file_name.png"
                            val textureFileName = pathTextureIn.getName(pathTextureIn.getNameCount()-1)
                            val pathTextureOut = pathBuildTextures.resolve(texture.getParent()).resolve(textureFileName)
                            texturesToCopy.put(pathTextureOut, pathTextureIn)
                        }
                    }
                }
                catch ( err: Exception ) {
                    err.printStackTrace()
                }
            }

            for ( (pathTextureOut, pathTextureIn) in texturesToCopy ) {
                val textureOutDir = pathTextureOut.getParent()
                Files.createDirectories(textureOutDir)
                Files.copy(pathTextureIn, pathTextureOut, StandardCopyOption.REPLACE_EXISTING)
            }
        }

        // ===============================
        // write models.json custom model index into item used for custom models
        // ===============================
        val itemName = Config.modelItem.toString().toLowerCase()
        val pathCustomModelItem = pathBuildModels.resolve("${itemName}.json")
        
        val itemJsonOverride = StringBuilder(generateItemOverrideJsonHeader(Config.modelItem))
        for ( (i, modelPart) in customModelData.withIndex() ) {
            val modelName = modelPartToName.get(modelPart)
            itemJsonOverride.append("        {\"predicate\": { \"custom_model_data\": ${i+1} }, \"model\": \"item/${modelName}/${modelPart}\"}")
            
            if ( i < customModelData.size - 1 ) {
                itemJsonOverride.append(",\n")
            }
        }
        itemJsonOverride.append("\n]}")

        writeStringToFile(itemJsonOverride.toString(), pathCustomModelItem)

        // ===============================
        // zip temp directory into resourcepack.zip
        // ===============================
        val pathZip = pathResourcePack.resolve(Config.resourcePackName)
        zipDirectory(pathBuild, pathZip)
    }
}

/**
 * Read model texture block from .json
 */
private fun readModelTextures(pathModel: Path): List<Path> {
    val json = JsonParser().parse(FileReader(pathModel.toFile()))
    val jsonObj = json.getAsJsonObject()
    
    val jsonTextures = jsonObj.get("textures")?.getAsJsonObject()
    if ( jsonTextures === null ) {
        return listOf()
    }

    val texturePaths: MutableSet<String> = mutableSetOf()

    for ( (_, texturePath) in jsonTextures.entrySet() ) {
        texturePaths.add(texturePath.getAsString())
    }

    return texturePaths.map { item -> Paths.get(item) }
}