/**
 * Top level commands
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
import phonon.puppet.math.*
import phonon.puppet.objects.*
import phonon.puppet.animation.*
import phonon.puppet.utils.filterByStart

// list of all subcommands, used for onTabComplete
private val SUBCOMMANDS: List<String> = listOf(
    "help",
    "reload",
    "models",
    "mesh",
    "actor",
    "list",
    "bone",
    "pose",
    "stands",
    "animinfo",
    "animlist",
    "playanim",
    "stopanim",
    "engine"
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
            "resourcepack" -> reloadResources(sender)
            "models" -> printModels(sender)
            "mesh" -> createMesh(player, args)
            "actor" -> createActor(sender, args)
            "list" -> listActors(sender, args)
            "bone" -> setBone(sender, args)
            "pose" -> poseActor(sender, args)
            "stands" -> toggleArmorStands(sender, args)
            "animinfo" -> animationInfo(sender, args)
            "animlist" -> listAnimations(sender, args)
            "playanim" -> playAnimation(sender, args)
            "stopanim" -> stopAnimation(sender, args)
            "start" -> startEngine()
            "stop" -> stopEngine()
            "step" -> stepEngine()
            else -> { println("Invalid command, use /puppet help") }
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
                "actor" -> {
                    
                }

                "animinfo" -> {
                    if ( args.size == 2 ) {
                        return filterByStart(AnimationTrack.list(), args[1])
                    }
                }

                // /puppet playanim [actorId] [animation]
                "playanim",
                "stopanim" -> {
                    if ( args.size == 3 ) {
                        return filterByStart(AnimationTrack.list(), args[2])
                    }
                }
            }
        }

        return listOf()
    }

    private fun printInfo(sender: CommandSender?) {
        Message.print(sender, "${ChatColor.BOLD}Puppet Animation Engine v${Puppet.version}")
        Message.print(sender, "Library:")
        Message.print(sender, "- Models: ${Mesh.library.size}")
        Message.print(sender, "- Skeletons: ${Skeleton.library.size}")
        Message.print(sender, "- Animations: ${AnimationTrack.library.size}")
        Message.print(sender, "Type \"/puppet help\" to see list of commands.")
        return
    }

    private fun printHelp(sender: CommandSender?) {
        Message.print(sender, "[Puppet] Commands:")
        Message.print(sender, "/puppet reload${ChatColor.WHITE}: Reload libraries and re-create resourcepack")
        return
    }

    /**
    * @command /puppet reload
    * Reloads plugin.
    */
    private fun reload(sender: CommandSender?) {
        Message.print(sender, "[Puppet] Reloading resources...")
        
        Puppet.loadResources()

        Message.print(sender, "- Models: ${Mesh.library.size}")
        Message.print(sender, "- Skeletons: ${Skeleton.library.size}")
        Message.print(sender, "- Animations: ${AnimationTrack.library.size}")
    }

    /**
     * @command /puppet resourcepack
     * Reloads resources and re-generates resource pack.
     * Will add any new resources to engine.
     */
    private fun reloadResources(sender: CommandSender) {
        Message.print(sender, "[Puppet] Generating Resource Pack")
    }

    // print list of custom model data
    private fun printModels(sender: CommandSender?) {
        if ( sender !== null ) {
            Mesh.print(sender)
        }
    }

    private fun createMesh(player: Player?, args: Array<String>) {
        if ( player === null ) return

        if ( args.size < 2 ) {
            Message.print(player, "Usage: /puppet mesh [name/id]")

            for ( (name, customModelData) in Mesh.library ) {
                Message.print(player, "- ${name}: ${customModelData}")
            }

            return
        }

        val meshName = args[1]
        val spawnLocation = Vector3f.fromLocation(player.location)
        Puppet.createMesh(meshName, spawnLocation)
    }
    
    private fun createActor(sender: CommandSender, args: Array<String>) {
        if ( args.size < 2 ) {
            Message.print(sender, "Usage: /puppet actor [name]")
            return
        }

        val player = if ( sender is Player ) sender else null

        val spawnLocation = if ( player === null ) {
            Vector3f.zero()
        } else {
            Vector3f.fromLocation(player.location)
        }

        val type = args[1]
        Puppet.createActor(type, spawnLocation)

        Message.print(sender, "CREATING ACTOR ${type} at ${spawnLocation}")
    }

    private fun listActors(sender: CommandSender, args: Array<String>) {
        for ( (id, actor) in Actor.actors ) {
            Message.print(sender, "- ${id}: ${actor.name}")
        }
    }

    private fun setBone(sender: CommandSender, args: Array<String>) {
        if ( args.size < 3 ) {
            Message.print(sender, "Usage: /puppet bone [actorID] [boneName] [rot.x] [rot.y] [rot.z]")
            return
        }

        val actorId = args[1].toInt()
        val actor = Actor.get(actorId)
        if ( actor === null ) {
            Message.error(sender, "Invalid actor id: ${actorId}")
            return
        }

        val boneName = args[2]
        val bone = actor.skeleton?.bones?.get(boneName)
        if ( bone === null ) {
            Message.error(sender, "Invalid bone: ${boneName}")
            return
        }

        // no input args to change bone: print current bone info
        if ( args.size < 6 ) {
            Message.print(sender, "Actor \"${actor.name}\" bone \"${boneName}\":")
            Message.print(sender, "- position = [ ${bone.position.x}, ${bone.position.y}, ${bone.position.z} ]")
            Message.print(sender, "- rotation = [ ${bone.rotation.x}, ${bone.rotation.y}, ${bone.rotation.z} ]")
            Message.print(sender, "- quaternion = [ ${bone.quaternion.x}, ${bone.quaternion.y}, ${bone.quaternion.z} ${bone.quaternion.w} ]")
            return
        }
        
        // 
        val rotX = args[3].toDouble()
        val rotY = args[4].toDouble()
        val rotZ = args[5].toDouble()

        bone.setRotation(rotX, rotY, rotZ)
        actor.updateTransform()
    }

    private fun poseActor(sender: CommandSender, args: Array<String>) {
        val player = if ( sender is Player ) sender else null
        if ( player === null ) {
            return
        }

        if ( args.size < 2 ) {
            val posingActor = Puppet.playerPosingActor.get(player)
            if ( posingActor !== null ) {
                Puppet.playerPosingActor.remove(player)
                Message.print(player, "Stopped posing actor: ${posingActor.id}")
                return
            }

            Message.print(player, "Usage: /puppet pose [actorID]")
            return
        }

        val actorId = args[1].toInt()
        val actor = Actor.get(actorId)
        if ( actor === null ) {
            Message.error(sender, "Invalid actor id: ${actorId}")
            return
        }

        Puppet.playerPosingActor.put(player, actor)
        
        Message.print(sender, "Posing actor ${actorId} by player movement")
    }

    private fun toggleArmorStands(sender: CommandSender, args: Array<String>) {
        if ( args.size < 2 ) {
            Message.print(sender, "Usage: /puppet pose [actorID]")
            return
        }

        val actorId = args[1].toInt()
        val actor = Actor.get(actorId)
        if ( actor === null ) {
            Message.error(sender, "Invalid actor id: ${actorId}")
            return
        }
        
        val visible: Boolean = if ( args.size < 3 ) {
            false
        }
        else {
            when ( args[2].toLowerCase() ) {
                "show" -> true
                "hide" -> false
                else -> false
            }
        }

        Puppet.toggleArmorStands(actor, visible)
        
        Message.print(sender, "Setting actor ${actorId} armor stands: ${visible}")
    }

    private fun listAnimations(sender: CommandSender, args: Array<String>) {
        Message.print(sender, "Animations:")
        
        val animations = AnimationTrack.list()
        for ( name in animations ) {
            Message.print(sender, "- ${name}")
        }
    }

    private fun animationInfo(sender: CommandSender, args: Array<String>) {
        if ( args.size < 2 ) {
            Message.print(sender, "Usage: /puppet animinfo [animation]")
            return
        }
        
        val animTrack = AnimationTrack.get(args[1])

        if ( animTrack === null ) {
            Message.error(sender, "Animation \"${args[1]}\" does not exist")
            return
        }
        
        Message.print(sender, "Animation: ${animTrack.name}")
        Message.print(sender, "- length: ${animTrack.length}")

        // print track values at time
        if ( args.size > 2 ) {
            val boneName = args[2]
            val transformTracks = animTrack.transformTracks.get(boneName)
            if ( transformTracks !== null ) {
                Message.print(sender, "- position: ${transformTracks.position?.contentToString()}")
                Message.print(sender, "- quaternion: ${transformTracks.quaternion?.contentToString()}")
            }
        }
    }

    private fun playAnimation(sender: CommandSender, args: Array<String>) {
        if ( args.size < 3 ) {
            Message.print(sender, "Usage: /puppet playanim [actorID] [animation]")
            return
        }

        val actorId = args[1].toInt()
        val actor = Actor.get(actorId)
        if ( actor === null ) {
            Message.error(sender, "Invalid actor id: ${actorId}")
            return
        }

        val animName = args[2]

        actor.playAnimation(animName, 1.0)

        Message.print(sender, "Actor ${actor.name} (id=${actorId}): playing ${animName}")
    }

    private fun stopAnimation(sender: CommandSender, args: Array<String>) {
        if ( args.size < 2 ) {
            Message.print(sender, "Usage: /puppet stopanim [actorID]")
            Message.print(sender, "Usage: /puppet stopanim [actorID] [animation]")
            return
        }

        val actorId = args[1].toInt()
        val actor = Actor.get(actorId)
        if ( actor === null ) {
            Message.error(sender, "Invalid actor id: ${actorId}")
            return
        }

    }

    private fun startEngine() {
        Puppet.startEngine()
    }

    private fun stopEngine() {
        Puppet.stopEngine()
    }

    private fun stepEngine() {
        Puppet.stepEngine()
    }
    
}