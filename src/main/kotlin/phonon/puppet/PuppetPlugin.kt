/*
 * PuppetPlugin
 * 
 * Implement bukkit plugin interface
 */

package phonon.puppet

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import kotlin.system.measureTimeMillis
import phonon.puppet.Puppet
import phonon.puppet.objects.Mesh
import phonon.puppet.objects.Skeleton
import phonon.puppet.animation.AnimationTrack
import phonon.puppet.Config
import phonon.puppet.resourcepack.Resource
import phonon.puppet.commands.*
import phonon.puppet.listeners.*

public class PuppetPlugin : JavaPlugin() {
    
    override fun onEnable() {
        
        // measure load time
        val timeStart = System.currentTimeMillis()

        val logger = this.getLogger()

        // get config file
        val configPath = File(getDataFolder().getPath(), "config.yml")
        if ( !configPath.exists() ) {
            logger.info("No config found: generating default config.yml")
            this.saveDefaultConfig()
        }
        val config = this.getConfig()
        if ( config !== null ) {
            Config.load(config)
        }

        // initialize puppet: will load resources + generate resource pack
        Puppet.initialize(this)
        
        // register listeners
        val pm = this.getServer().getPluginManager()
        pm.registerEvents(EntityDamageListener(), this)
        pm.registerEvents(PlayerMoveListener(), this)

        // register commands
        this.getCommand("puppet")?.setExecutor(PuppetCommand())

        // print data loaded
        logger.info("Loaded:")
        logger.info("- Models: ${Mesh.library.size}")
        logger.info("- Skeletons: ${Skeleton.library.size}")
        logger.info("- Animations: ${AnimationTrack.library.size}")

        // print load time
        val timeEnd = System.currentTimeMillis()
        val timeLoad = timeEnd - timeStart
        logger.info("Enabled in ${timeLoad}ms")

        // print success message
        logger.info("now this is epic")
    }

    override fun onDisable() {
        logger.info("wtf i hate puppet now")
    }
}
