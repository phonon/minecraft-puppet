/**
 * Engine/admin commands
 */

package phonon.puppet.commands

import org.bukkit.Material
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import phonon.puppet.Puppet
import phonon.puppet.Config
import phonon.puppet.Message
import phonon.puppet.objects.*
import phonon.puppet.animation.*
import phonon.puppet.utils.filterByStart

// list of all subcommands, used for onTabComplete
private val SUBCOMMANDS: List<String> = listOf(
    "help",
    "reload",
    "engine",
    "killall"
)

// /puppet engine [subcommand]
private val ENGINE_SUBCOMMANDS: List<String> = listOf(
    "start",
    "stop",
    "step"
)

public class PuppetCommand : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, cmd: Command, commandLabel: String, args: Array<String>): Boolean {
        
        val player = if ( sender is Player ) sender else null
    
        // no args, print plugin info
        if ( args.size == 0 ) {
            printInfo(sender)
            return true
        }

        // parse subcommand
        when ( args[0].toLowerCase() ) {
            "help" -> printHelp(sender)
            "reload" -> reload(sender)
            "engine" -> manageEngine(sender, args)
            "killall" -> killAllActors(sender, args)
            else -> { Message.error(sender, "Invalid command, use \"/puppet help\"") }
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> {
        // match subcommand
        if ( args.size == 1 ) {
            return filterByStart(SUBCOMMANDS, args[0])
        }
        // match each subcommand format
        else if ( args.size > 1 ) {
            // handle specific subcommands
            when ( args[0].toLowerCase() ) {
                // /puppet engine [subcommand]
                "engine" -> {
                    if ( args.size == 2 ) {
                        return filterByStart(ENGINE_SUBCOMMANDS, args[1])
                    }
                }
            }
        }

        return listOf()
    }

    /**
     * Print engine info
     */
    private fun printInfo(sender: CommandSender) {
        Puppet.printInfo(sender)
        Message.print(sender, "Type \"/puppet help\" to see list of engine commands.")
        return
    }

    /**
     * Print commands help
     */
    private fun printHelp(sender: CommandSender) {
        Message.print(sender, "[Puppet] Engine Commands:")
        Message.print(sender, "/puppet reload${ChatColor.WHITE}: Reload libraries and re-create resourcepack")
        Message.print(sender, "/puppet engine${ChatColor.WHITE}: Start/stop/step rendering engine")
        Message.print(sender, "/puppet killall${ChatColor.WHITE}: Kill all actors")
        return
    }

    /**
     * @command /puppet reload
     * Reloads plugin and re-creates resource pack.
     * Will add any new resources to engine.
     */
    private fun reload(sender: CommandSender) {
        Message.print(sender, "[Puppet] Reloading resources and creating resource pack...")
        
        // reload config
        Puppet.plugin?.loadConfig()

        // reload resources + regenerate resource pack
        Puppet.loadResources()

        Message.print(sender, "- Models: ${Mesh.library.size}")
        Message.print(sender, "- Skeletons: ${Skeleton.library.size}")
        Message.print(sender, "- Animations: ${AnimationTrack.library.size}")
    }

    /**
     * @command /puppet engine
     * Start/stop/step animation render loop engine.
     */
    private fun manageEngine(sender: CommandSender, args: Array<String>) {
        if ( args.size < 2 ) {
            val engineStatus = if ( Puppet.isRunning ) {
                "${ChatColor.GREEN}running"
            } else {
                "${ChatColor.GRAY}stopped"
            }
            Message.print(sender, "Engine status: ${engineStatus}")
            Message.print(sender, "Usage: /puppet engine [start|stop|step]")
            return
        }

        when ( args[1].toLowerCase() ) {
            "start" -> {
                Puppet.startEngine()
                Message.print(sender, "${ChatColor.BOLD}Starting Puppet render engine")
            }
            "stop" -> {
                Puppet.stopEngine()
                Message.print(sender, "${ChatColor.BOLD}Stopping Puppet render engine")
            }
            "step" -> {
                Puppet.stepEngine()
                Message.print(sender, "${ChatColor.BOLD}Step Puppet render engine")
            }
        }
    }
    
    /**
     * @command /puppet killall
     * Kills all actors, same as `/actor killall`, but only requires
     * `/puppet` command permission node instead of operator.
     */
    private fun killAllActors(sender: CommandSender, args: Array<String>) {
        Puppet.destroyAllActors()
        Message.print(sender, "Destroyed all actors")
    }

}