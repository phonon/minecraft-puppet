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
    "actor",
    "create",
    "kill",
    "killall",
    "list",
    "models",
    "mesh",
    "bone",
    "pose",
    "stands",
    "animinfo",
    "animlist",
    "playanim",
    "stopanim",
    "start",
    "stop",
    "step",
    "reset",
    "engine"
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
            "actor" -> actorInfo(sender, args)
            "create" -> createActor(sender, args)
            "kill" -> killActor(sender, args)
            "killall" -> killAllActor(sender, args)
            "list" -> listActors(sender, args)
            "models" -> printModels(sender)
            "mesh" -> createMesh(player, args)
            "bone" -> setBone(sender, args)
            "pose" -> poseActor(sender, args)
            "stands" -> toggleArmorStands(sender, args)
            "animinfo" -> animationInfo(sender, args)
            "animlist" -> listAnimations(sender, args)
            "playanim" -> playAnimation(sender, args)
            "stopanim" -> stopAnimation(sender, args)
            "start" -> startActorAnimation(sender, args)
            "stop" -> stopActorAnimation(sender, args)
            "step" -> stepActorAnimation(sender, args)
            "reset" -> resetActorPose(sender, args)
            "engine" -> manageEngine(sender, args)
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
    private fun printInfo(sender: CommandSender?) {
        Message.print(sender, "${ChatColor.BOLD}Puppet Animation Engine v${Puppet.version}")
        Message.print(sender, "Library:")
        Message.print(sender, "- Models: ${Mesh.library.size}")
        Message.print(sender, "- Skeletons: ${Skeleton.library.size}")
        Message.print(sender, "- Animations: ${AnimationTrack.library.size}")
        Message.print(sender, "Type \"/puppet help\" to see list of commands.")
        return
    }

    /**
     * Print commands help
     */
    private fun printHelp(sender: CommandSender?) {
        Message.print(sender, "[Puppet] Commands:")
        Message.print(sender, "/puppet reload${ChatColor.WHITE}: Reload libraries and re-create resourcepack")
        return
    }

    /**
     * @command /puppet reload
     * Reloads plugin and re-creates resourcepack.
     * Will add any new resources to engine.
     */
    private fun reload(sender: CommandSender?) {
        Message.print(sender, "[Puppet] Reloading resources and creating resource pack...")
        
        Puppet.loadResources()

        Message.print(sender, "- Models: ${Mesh.library.size}")
        Message.print(sender, "- Skeletons: ${Skeleton.library.size}")
        Message.print(sender, "- Animations: ${AnimationTrack.library.size}")
    }
    
    /**
     * 
     */
    private fun actorInfo(sender: CommandSender, args: Array<String>) {
        // no actor id input, see if player is looking at actor
        if ( args.size < 2 ) {
            val player = if ( sender is Player ) sender else null
            if ( player !== null ) {
                val actor = Puppet.getActorPlayerIsLookingAt(player)
                if ( actor !== null ) {
                    Message.print(player, "ACTOR: ${actor}")
                    return
                }
                Message.error(sender, "No actor in sight")
            }

            Message.error(sender, "Usage: /puppet actor: info for actor player is looking at")
            Message.error(sender, "Usage: /puppet actor [id]: info for actor id")
            return
        }
    }

    /**
     * 
     */
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
    
    /**
     * 
     */
    private fun killActor(sender: CommandSender, args: Array<String>) {
        
    }

    /**
     * 
     */
    private fun killAllActor(sender: CommandSender, args: Array<String>) {
        
    }
    
    /**
     * 
     */
    private fun listActors(sender: CommandSender, args: Array<String>) {
        for ( (id, actor) in Actor.actors ) {
            Message.print(sender, "- ${id}: ${actor.name}")
        }
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

    private fun startActorAnimation(sender: CommandSender, args: Array<String>) {
        
    }

    private fun stopActorAnimation(sender: CommandSender, args: Array<String>) {
        
    }

    private fun stepActorAnimation(sender: CommandSender, args: Array<String>) {
        
    }

    private fun resetActorPose(sender: CommandSender, args: Array<String>) {

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
    
}