/**
 * Config
 * 
 * Global config state variables, read in from 
 * plugin's config.yml file
 */

package phonon.puppet

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.Material

public object Config {

    // ========================================
    // Ingame settings
    // ========================================
    
    // automatically start rendering engine when plugin loads
    public var autoStartEngine: Boolean = true

    // item type to use for custom models metadata
    public var modelItem: Material = Material.BONE

    // make model armorstands invincible (with event listener)
    public var makeModelsInvulnerable: Boolean = true

    // ========================================
    // Path settings
    // ========================================

    // main plugin directory for config, saves, data, etc.. 
    public var pathPlugin = "plugins/puppet"
    
    // directory for individual model .json data = pathPlugin + pathModels
    public var pathModels = "data"
    
    // file path for custom model data list = pathModels + pathCustomModelData
    public var pathCustomModelData = "models.json"

    // directory for generating resourcepack = pathPlugin + pathResourcePack
    public var pathResourcePack = "resourcepack"
    
    // path for custom resource pack icon = pathResourcePack  + pathResourcePackIcon
    // if not provided, not pack item will appear
    public var pathResourcePackIcon = "pack.png"

    // path for custom resource pack pack.mcmeta = pathResourcePack  + pathResourcePackMeta
    // if not provided, default pack.mcmeta will be written
    public var pathResourcePackMeta = "pack.mcmeta"

    // Path for custom resource pack item override = pathResourcePack  + pathResourcePackOverrideItem
    // This is the item that will be used to display custom models (same as modelItem)
    // 
    // If not provided, default .json text will be written:
    //     "parent": "item/handheld",
    //     "textures": {
    //         "layer0": "item/MATERIAL_NAME"
    //     },
    //     "display": {},
    public var pathResourcePackOverrideItem = "bone.json"

    // name for server generated resourcepack
    public var resourcePackName = "puppet_resourcepack.zip"
    

    /**
     * Load config from config.yml file
     */
    public fun load(config: FileConfiguration) {
        // ingame settings
        Config.autoStartEngine = config.getBoolean("autoStartEngine", Config.autoStartEngine)
        Config.modelItem = Material.matchMaterial(config.getString("modelItem", "")!!) ?: Config.modelItem
        Config.makeModelsInvulnerable = config.getBoolean("makeModelsInvulnerable", Config.makeModelsInvulnerable)
        
        // path settings
        Config.pathPlugin = config.getString("pathPlugin", Config.pathPlugin)!!
        Config.pathModels = config.getString("pathModels", Config.pathModels)!!
        Config.pathCustomModelData = config.getString("pathCustomModelData", Config.pathCustomModelData)!!
        Config.pathResourcePack = config.getString("pathResourcePack", Config.pathResourcePack)!!
        Config.pathResourcePackIcon = config.getString("pathResourcePackIcon", Config.pathResourcePackIcon)!!
        Config.pathResourcePackMeta = config.getString("pathResourcePackMeta", Config.pathResourcePackMeta)!!
        Config.pathResourcePackOverrideItem = config.getString("pathResourcePackOverrideItem", Config.pathResourcePackOverrideItem)!!
        Config.resourcePackName = config.getString("resourcePackName", Config.resourcePackName)!!
    }
    
}