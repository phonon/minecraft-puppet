/**
 * Actor manipulation commands, for users
 */

package phonon.puppet.commands

import org.bukkit.Bukkit
import org.bukkit.Location
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
    "info",
    "create",
    "kill",
    "killall",
    "list",
    "models",
    "mesh",
    "reset",
    "bone",
    "move",
    "teleport",
    "rotate",
    "pose",
    "armorstands",
    "animinfo",
    "animlist",
    "play",
    "pause",
    "stop",
    "stopall",
    "start",
    "step",
    "restart"
)

public class ActorCommand : CommandExecutor, TabCompleter {

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
            "info" -> actorInfo(sender, args)
            "create" -> createActor(sender, args)
            "mesh" -> createMesh(sender, args)
            "kill" -> killActor(sender, args)
            "killall" -> killAllActor(sender, args)
            "list" -> listActors(sender, args)
            "models" -> printModels(sender)
            "reset" -> resetActorPose(sender, args)
            "move" -> moveActor(sender, args)
            "teleport" -> teleportActor(sender, args)
            "rotate" -> rotateActor(sender, args)
            "pose" -> poseActor(sender, args)
            "armorstands" -> toggleArmorStands(sender, args)
            "animinfo" -> animationInfo(sender, args)
            "animlist" -> listAnimations(sender, args)
            "play" -> playAnimation(sender, args)
            "pause" -> pauseAnimation(sender, args)
            "stop" -> stopAnimation(sender, args)
            "stopall" -> stopAllAnimation(sender, args)
            "start" -> startAnimation(sender, args)
            "step" -> stepAnimation(sender, args)
            "restart" -> restartAnimation(sender, args)
            "bone" -> boneCommands(sender, args)
            else -> { Message.error(sender, "Invalid command, use \"/actor help\"") }
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
                "create" -> {
                    if ( args.size == 2 ) {
                        return filterByStart(Actor.types(), args[1])
                    }
                }

                "animinfo" -> {
                    if ( args.size == 2 ) {
                        return filterByStart(AnimationTrack.list(), args[1])
                    }
                }

                // /actor [subcommand] [x] [y] [z] [actor0] [actor1] ...
                "move",
                "teleport",
                "rotate" -> {
                    if ( args.size >= 5 ) {
                        return filterByStart(Puppet.getActorNames(), args[args.size-1])
                    }
                }

                // /actor [subcommand] [actor0] [actor1] ...
                "info",
                "kill",
                "reset",
                "pose",
                "pause",
                "start",
                "stopall",
                "step",
                "restart" -> {
                    if ( args.size >= 2 ) {
                        return filterByStart(Puppet.getActorNames(), args[args.size-1])
                    }
                }

                // /actor play [animation] [actor0] [actor1] ...
                "play",
                "stop" -> {
                    if ( args.size == 2 ) {
                        return filterByStart(AnimationTrack.list(), args[1])
                    }
                    else if ( args.size >= 3 ) {
                        return filterByStart(Puppet.getActorNames(), args[args.size-1])
                    }
                }

                // /actor armorstands [show/hide] [actor0] [actor1] ...
                "armorstands" -> {
                    if ( args.size == 2 ) {
                        return filterByStart(listOf("show", "hide"), args[1])
                    }
                    else if ( args.size >= 3 ) {
                        return filterByStart(Puppet.getActorNames(), args[args.size-1])
                    }
                }

                // /actor bone [subcommand] [actor] [bone] ...
                "bone" -> {
                    if ( args.size == 2 ) {
                        return filterByStart(listOf("info", "rotate", "position"), args[1])
                    }
                    else if ( args.size == 3 ) {
                        return filterByStart(Puppet.getActorNames(), args[2])
                    }
                    else if ( args.size == 4 ) {
                        val actorName = args[2]
                        val skeleton = Puppet.getActor(actorName)?.skeleton
                        if ( skeleton === null ) {
                            return listOf()
                        }

                        return filterByStart(skeleton.bones.keys.toList(), args[3])
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
        Message.print(sender, "Type \"/actor help\" to see list of actor commands.")
    }

    /**
     * @command /actor help
     * Print command list and descriptions
     */
    private fun printHelp(sender: CommandSender) {
        Message.print(sender, "[Puppet] Actor Commands:")
        Message.print(sender, "/actor info${ChatColor.WHITE}: Print info about actor")
        Message.print(sender, "/actor create${ChatColor.WHITE}: Create an actor")
        Message.print(sender, "/actor mesh${ChatColor.WHITE}: Create mesh type actor")
        Message.print(sender, "/actor kill${ChatColor.WHITE}: Kill an actor")
        Message.print(sender, "/actor killall${ChatColor.WHITE}: Kill all actors")
        Message.print(sender, "/actor list${ChatColor.WHITE}: List actors created")
        Message.print(sender, "/actor models${ChatColor.WHITE}: List all available models")
        Message.print(sender, "/actor reset${ChatColor.WHITE}: Reset actor pose to default")
        Message.print(sender, "/actor move${ChatColor.WHITE}: Adjust actor position")
        Message.print(sender, "/actor teleport${ChatColor.WHITE}: Teleport actor to location")
        Message.print(sender, "/actor rotate${ChatColor.WHITE}: Adjust actor rotation")
        Message.print(sender, "/actor pose${ChatColor.WHITE}: Adjust actor position/rotation using player movement")
        Message.print(sender, "/actor armorstands${ChatColor.WHITE}: Show actor Armor Stands")
        Message.print(sender, "/actor animinfo${ChatColor.WHITE}: Print info about an animation")
        Message.print(sender, "/actor animlist${ChatColor.WHITE}: List all animations")
        Message.print(sender, "/actor play${ChatColor.WHITE}: Make actor play animation")
        Message.print(sender, "/actor pause${ChatColor.WHITE}: Stop actor animation (can resume)")
        Message.print(sender, "/actor start${ChatColor.WHITE}: Start playing actor animations")
        Message.print(sender, "/actor stop${ChatColor.WHITE}: Stop and remove actor animations")
        Message.print(sender, "/actor stopall${ChatColor.WHITE}: Stop and remove all actor animations")
        Message.print(sender, "/actor step${ChatColor.WHITE}: Run 1 animation frame for actor")
        Message.print(sender, "/actor restart${ChatColor.WHITE}: Restart animation")
        Message.print(sender, "/actor bone info${ChatColor.WHITE}: Print actor bone info")
        Message.print(sender, "/actor bone rotate${ChatColor.WHITE}: Set actor bone rotation")
        Message.print(sender, "/actor bone position${ChatColor.WHITE}: Set actor bone position")
    }
    
    /**
     * @command /actor info [actor0] [actor1] ...
     * Print info about actors in input list or from what
     * player is looking at.
     */
    private fun actorInfo(sender: CommandSender, args: Array<String>) {
        // get actor targets
        val targets = getActorTargets(sender, args, 1)

        if ( targets.size == 0 ) {
            Message.error(sender, "Usage: /actor info: info for actor player is looking at")
            Message.error(sender, "Usage: /actor info [actor0] [actor1] ...: info for actor id")
            return
        }

        for ( actor in targets ) {
            Message.print(sender, "Actor: ${actor.name}")
            Message.print(sender, "- skeleton: ${actor.skeleton?.name ?: "none"}")
            Message.print(sender, "- animations:")
            for ( anim in actor.animation.playing.keys ) {
                Message.print(sender, "   - ${anim}")
            }
        }
    }

    /**
     * @command /actor create [type] [x] [y] [z]
     * Create an actor with given type at player location,
     * or (x, y, z) if those are entered.
     */
    private fun createActor(sender: CommandSender, args: Array<String>) {
        if ( args.size < 2 ) {
            Message.error(sender, "Usage: /actor create [type] [x] [y] [z]")
            Message.error(sender, "Will create at player location if (x, y, z) not specified.")
            return
        }

        val player = if ( sender is Player ) sender else null

        val spawnLocation = if ( player === null ) {
            if ( args.size < 5 ) {
                Message.error(sender, "Usage: /actor create [type] [x] [y] [z]")
                Message.error(sender, "Must enter all (x, y, z) values.")
                return
            }
            else {
                val x = args[2].toDouble()
                val y = args[3].toDouble()
                val z = args[4].toDouble()
                Location(Bukkit.getWorlds()[0], x, y, z)
            }
        } else {
            player.location
        }

        val type = args[1]
        val result = Puppet.createActorAtLocation(type, spawnLocation)

        if ( result.isSuccess ) {
            val actor = result.getOrNull()!!
            Message.print(sender, "Created actor ${type} named \"${actor.name}\" at (${spawnLocation.x}, ${spawnLocation.y}, ${spawnLocation.z})")
        }
        else {
            Message.error(sender, "Invalid mesh type: ${type}")
        }
    }

    /**
     * @command /actor mesh [type] [x] [y] [z]
     * Create a single model "mesh" actor with given model
     * type at player location or (x, y, z) if those are entered.
     */
    private fun createMesh(sender: CommandSender, args: Array<String>) {
        if ( args.size < 2 ) {
            Message.error(sender, "Usage: /actor mesh [type] [x] [y] [z]")
            Message.error(sender, "Will create at player location if (x, y, z) not specified.")
            return
        }

        val player = if ( sender is Player ) sender else null

        val spawnLocation = if ( player === null ) {
            if ( args.size < 5 ) {
                Message.error(sender, "Usage: /actor mesh [type] [x] [y] [z]")
                Message.error(sender, "Must enter all (x, y, z) values.")
                return
            }
            else {
                val x = args[2].toDouble()
                val y = args[3].toDouble()
                val z = args[4].toDouble()
                Location(Bukkit.getWorlds()[0], x, y, z)
            }
        } else {
            player.location
        }

        val type = args[1]
        val result = Puppet.createMeshAtLocation(type, spawnLocation)

        if ( result.isSuccess ) {
            val actor = result.getOrNull()!!
            Message.print(sender, "Created mesh ${type} named \"${actor.name}\" at (${spawnLocation.x}, ${spawnLocation.y}, ${spawnLocation.z})")
        }
        else {
            Message.error(sender, "Invalid mesh type: ${type}")
        }
    }

    /**
     * @command /actor kill [actor0] [actor1] ...
     * Remove actor. Will remove actors from list of names,
     * or the actor the player is looking at.
     */
    private fun killActor(sender: CommandSender, args: Array<String>) {

        // get actor targets
        val targets = getActorTargets(sender, args, 1)

        if ( targets.size < 1 ) {
            Message.error(sender, "Usage: /actor kill [actor0] [actor1] ...")
            Message.error(sender, "If no actor ids are specified, it will run on actor player is looking at")
            return
        }

        for ( actor in targets ) {
            val result = Puppet.destroyActor(actor)
            Message.print(sender, "Destroyed actor \"${actor.name}\"")
        }
    }

    /**
     * @command /actor killall
     * Remove all actors currently in game.
     */
    private fun killAllActor(sender: CommandSender, args: Array<String>) {
        if ( sender is Player ) {
            val player: Player = sender
            if ( !player.isOp() ) {
                Message.error(sender, "Only op players can use /actor killall")
                return
            }
        }

        Puppet.destroyAllActors()

        Message.print(sender, "Destroyed all actors")
    }
    
    /**
     * @command /actor list
     * Print list of actors currently in game and their location
     */
    private fun listActors(sender: CommandSender, args: Array<String>) {
        Message.print(sender, "Actors currently in game:")
        for ( (name, actor) in Puppet.actors ) {
            val p = actor.position
            Message.print(sender, "- ${name}: (${p.x}, ${p.y}, ${p.z})")
        }
    }
    
    /**
     * @command /actor models
     * print list of custom model data
     */
    private fun printModels(sender: CommandSender) {
        if ( sender !== null ) {
            Mesh.printInfo(sender)
        }
    }

    /**
     * @command /actor reset [actor0] [actor1] ...
     * Reset actor rotation and all bone poses.
     */
    private fun resetActorPose(sender: CommandSender, args: Array<String>) {
        // get actor targets
        val targets = getActorTargets(sender, args, 1)

        if ( targets.size < 1 ) {
            Message.error(sender, "Usage: /actor reset: run on actor you are looking at")
            Message.error(sender, "Usage: /actor reset [actor0] [actor1] ...: run on actor list")
            return
        }

        // run on targets
        for ( actor in targets ) {
            // reset rotation
            actor.setRotation(0.0, 0.0, 0.0)

            val skeleton = actor.skeleton
            if ( skeleton !== null ) {
                skeleton.reset()
                Message.print(sender, "Resetting actor \"${actor.name}\" pose.")
            }
            else {
                Message.error(sender, "Actor \"${actor.name}\" has no bones.")
            }
        }
    }

    /**
     * @command /actor move [x] [y] [z] [actor0] [actor1] ...
     * Move actors by (x, y, z) relative to its current location.
     */
    private fun moveActor(sender: CommandSender, args: Array<String>) {
        if ( args.size < 4 ) {
            Message.error(sender, "Usage: /actor move [x] [y] [z] [actor0] [actor1] ...")
            Message.error(sender, "If no actor ids are specified, it will run on actor player is looking at")
            return
        }
        else if ( args.size < 5 && !(sender is Player) ) {
            Message.error(sender, "Usage: /actor move [x] [y] [z] [actor0] [actor1] ...")
            Message.error(sender, "Actor id required if you are not a player in game")
            return
        }

        // get x, y, z
        val dx = args[1].toDouble()
        val dy = args[2].toDouble()
        val dz = args[3].toDouble()

        // get actor targets
        val targets = getActorTargets(sender, args, 4)

        // run on targets
        for ( actor in targets ) {
            val p = actor.position
            actor.setPosition(p.x + dx, p.y + dy, p.z + dz)
            Message.print(sender, "Moving actor \"${actor.name}\" by (${dx}, ${dy}, ${dz})")
        }
    }

    /**
     * @command /actor teleport [x] [y] [z] [actor0] [actor1] ...
     * Move actor to (x, y, z) location in its world.
     */
    private fun teleportActor(sender: CommandSender, args: Array<String>) {
        if ( args.size < 4 ) {
            Message.error(sender, "Usage: /actor teleport [x] [y] [z] [actor0] [actor1] ...")
            Message.error(sender, "If no actor ids are specified, it will run on actor player is looking at")
            return
        }
        else if ( args.size < 5 && !(sender is Player) ) {
            Message.error(sender, "Usage: /actor teleport [x] [y] [z] [actor0] [actor1] ...")
            Message.error(sender, "Actor id required if you are not a player in game")
            return
        }

        // get x, y, z
        val x = args[1].toDouble()
        val y = args[2].toDouble()
        val z = args[3].toDouble()

        // get actor targets
        val targets = getActorTargets(sender, args, 4)

        // run on targets
        for ( actor in targets ) {
            actor.setPosition(x, y, z)
            Message.print(sender, "Teleporting actor \"${actor.name}\" to (${x}, ${y}, ${z})")
        }
    }

    /**
     * @command /actor rotate [x] [y] [z] [actor0] [actor1] ...
     * Rotate actor by (x, y, z) relative to its current rotation.
     * Note: Minecraft rotation order is ZYX.
     */
    private fun rotateActor(sender: CommandSender, args: Array<String>) {
        if ( args.size < 4 ) {
            Message.error(sender, "Usage: /actor rotate [x] [y] [z] [actor0] [actor1] ...")
            Message.error(sender, "If no actor ids are specified, it will run on actor player is looking at")
            return
        }
        else if ( args.size < 5 && !(sender is Player) ) {
            Message.error(sender, "Usage: /actor rotate [x] [y] [z] [actor0] [actor1] ...")
            Message.error(sender, "Actor id required if you are not a player in game")
            return
        }

        // get x, y, z
        val dxDeg = args[1].toDouble()
        val dyDeg = args[2].toDouble()
        val dzDeg = args[3].toDouble()

        val dx = Math.toRadians(dxDeg)
        val dy = Math.toRadians(dyDeg)
        val dz = Math.toRadians(dzDeg)

        // get actor targets
        val targets = getActorTargets(sender, args, 4)

        // run on targets
        for ( actor in targets ) {
            val r = actor.rotation
            actor.setRotation(r.x + dx, r.y + dy, r.z + dz)
            Message.print(sender, "Rotating actor \"${actor.name}\" by (${dxDeg}, ${dyDeg}, ${dzDeg})")
        }
    }

    /**
     * @command /actor pose [actor0] [actor1] ...
     * Move and rotate actor using player movement. The actor
     * will move with player and face same direction as player.
     */
    private fun poseActor(sender: CommandSender, args: Array<String>) {
        val player = if ( sender is Player ) sender else null
        if ( player === null ) {
            return
        }

        if ( args.size < 2 ) {
            val posingActor = Puppet.playerPosingActor.get(player)
            if ( posingActor !== null ) {
                Puppet.playerPosingActor.remove(player)
                Message.print(player, "Stopped posing actor: ${posingActor.name}")
                return
            }
        }

        // get actor targets
        val targets = getActorTargets(sender, args, 1)

        // run on targets
        // TODO: allow this to work on multiple actors
        if ( targets.size >= 1 ) {
            val actor = targets[0]
            Puppet.playerPosingActor.put(player, actor)
            Message.print(sender, "Posing actor \"${actor.name}\" by player movement")
            Message.print(sender, "(Type \"/actor pose\" again to stop posing)")
        }
        else {
            Message.error(sender, "Usage: /actor pose [actor0] [actor1] ...")
        }
    }

    /**
     * @command /actor armorstands [show/hide] [actor0] [actor1] ...
     * Show or hide ArmorStand entities used for animating
     * actor models. Used for debug.
     */
    private fun toggleArmorStands(sender: CommandSender, args: Array<String>) {
        if ( args.size < 2 ) {
            Message.print(sender, "Usage: /actor armorstands [show/hide] [actor0] [actor1] ...")
            return
        }

        val visible: Boolean = when ( args[1].toLowerCase() ) {
            "show" -> true
            "hide" -> false
            else -> false
        }

        // get actor targets
        val targets = getActorTargets(sender, args, 2)

        // run on targets
        for ( actor in targets ) {
            Puppet.toggleArmorStands(actor, visible)
            Message.print(sender, "Set actor \"${actor.name}\" armor stands visiblity: ${visible}")
        }
    }

    /**
     * @command /actor animlist
     * Print list of all available animations
     */
    private fun listAnimations(sender: CommandSender, args: Array<String>) {
        Message.print(sender, "Animations:")
        
        val animations = AnimationTrack.list()
        for ( name in animations ) {
            Message.print(sender, "- ${name}")
        }
    }

    /**
     * @command /actor animinfo [animation]
     * Print information about an animation
     */
    private fun animationInfo(sender: CommandSender, args: Array<String>) {
        if ( args.size < 2 ) {
            Message.print(sender, "Usage: /actor animinfo [animation]")
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

    /**
     * @command /actor play [animation] [actor0] [actor1] ...
     * Make actors play animation from name `[animation]`.
     */
    private fun playAnimation(sender: CommandSender, args: Array<String>) {
        if ( args.size < 2 ) {
            Message.error(sender, "Usage: /actor play [animation]: run on actor you are looking at")
            Message.error(sender, "Usage: /actor play [animation] [actor0] [actor1] ...: run on actor list")
            return
        }

        // animation name
        val animName = args[1]

        // get actor targets
        val targets = getActorTargets(sender, args, 2)

        // run on targets
        for ( actor in targets ) {
            actor.animation.play(animName, 1.0)
            Message.print(sender, "Actor \"${actor.name}\" playing: \"${animName}\"")
        }
    }

    /**
     * @command /actor stop [animation] [actor0] [actor1] ...
     * Stop actor from playing animation from name `[animation]`.
     * Unlike pause, this removes a specific animation from the actor.
     */
    private fun stopAnimation(sender: CommandSender, args: Array<String>) {
        if ( args.size < 2 ) {
            Message.error(sender, "Usage: /actor stop [animation]: run on actor you are looking at")
            Message.error(sender, "Usage: /actor stop [animation] [actor0] [actor1] ...: run on actor list")
            return
        }
        
        // animation name
        val animName = args[1]

        // get actor targets
        val targets = getActorTargets(sender, args, 2)

        // run on targets
        for ( actor in targets ) {
            actor.animation.stop(animName)
            Message.print(sender, "Actor \"${actor.name}\" stopped playing: \"${animName}\".")
        }
    }

    /**
     * @command /actor stopall [actor0] [actor1] ...
     * Stop all actor animations. Unlike pause, this removes
     * all animations from the actor.
     */
    private fun stopAllAnimation(sender: CommandSender, args: Array<String>) {
        // get actor targets
        val targets = getActorTargets(sender, args, 1)

        if ( targets.size < 1 ) {
            Message.error(sender, "Usage: /actor stopall: run on actor you are looking at")
            Message.error(sender, "Usage: /actor stopall [actor0] [actor1] ...: run on actor list")
            return
        }
        
        // run on targets
        for ( actor in targets ) {
            actor.animation.stopAll()
            Message.print(sender, "Actor \"${actor.name}\" stopped all animations.")
        }
    }

    /**
     * @command /actor pause [actor0] [actor1] ...
     * Pause actor from playing all animations. This will not
     * remove any animations, it will only stop animations
     * from updating.
     */
    private fun pauseAnimation(sender: CommandSender, args: Array<String>) {
        // get actor targets
        val targets = getActorTargets(sender, args, 1)

        if ( targets.size < 1 ) {
            Message.error(sender, "Usage: /actor pause: run on actor you are looking at")
            Message.error(sender, "Usage: /actor pause [actor0] [actor1] ...: run on actor list")
            return
        }
        
        // run on targets
        for ( actor in targets ) {
            actor.animation.enable(false)
            Message.print(sender, "Actor \"${actor.name}\" paused animations.")
        }
    }

    /**
     * @command /actor start [actor0] [actor1] ...
     * Start (unpause) actor animations. Use this after `/actor pause`
     * to start animations again. 
     */
    private fun startAnimation(sender: CommandSender, args: Array<String>) {
        // get actor targets
        val targets = getActorTargets(sender, args, 1)

        if ( targets.size < 1 ) {
            Message.error(sender, "Usage: /actor start: run on actor you are looking at")
            Message.error(sender, "Usage: /actor start [actor0] [actor1] ...: run on actor list")
            return
        }

        // run on targets
        for ( actor in targets ) {
            actor.animation.enable(true)
            Message.print(sender, "Actor \"${actor.name}\" unpaused animations.")
        }
    }

    /**
     * @command /actor step [actor0] [actor1] ...
     * Run single animation update step for actors.
     * This works on paused actors, so you can view
     * animations frame-by-frame.
     */
    private fun stepAnimation(sender: CommandSender, args: Array<String>) {
        // get actor targets
        val targets = getActorTargets(sender, args, 1)

        if ( targets.size < 1 ) {
            Message.error(sender, "Usage: /actor step: run on actor you are looking at")
            Message.error(sender, "Usage: /actor step [actor0] [actor1] ...: run on actor list")
            return
        }

        // run on targets
        for ( actor in targets ) {
            // if engine running, just update animation
            if ( Puppet.isRunning ) {
                actor.animation.update()
            }
            else { // update full actor transform and manually render
                actor.update()
                actor.render()
            }

            Message.print(sender, "Actor \"${actor.name}\" stepped animation by 1 tick.")
        }
    }

    /**
     * @command /actor restart [actor0] [actor1] ...
     * Restart all animations from their initial frame.
     * Use this to sync animations across multiple actors.
     */
    private fun restartAnimation(sender: CommandSender, args: Array<String>) {
        // get actor targets
        val targets = getActorTargets(sender, args, 1)
        
        if ( targets.size < 1 ) {
            Message.error(sender, "Usage: /actor restart: run on actor you are looking at")
            Message.error(sender, "Usage: /actor restart [actor0] [actor1] ...: run on actor list")
            return
        }
        
        // run on targets
        for ( actor in targets ) {
            actor.animation.restart()
            Message.print(sender, "Actor \"${actor.name}\" restarted all animations.")
        }
    }
    
    //=======================================
    // Bone pose commands
    // ======================================
    private fun boneCommands(sender: CommandSender, args: Array<String>) {

        fun printBoneCommandInfo(sender: CommandSender) {
            Message.print(sender, "Actor bone commands:")
            Message.print(sender, "/actor bone info${ChatColor.WHITE}: Print actor bone info")
            Message.print(sender, "/actor bone rotate${ChatColor.WHITE}: Set actor bone rotation")
            Message.print(sender, "/actor bone position${ChatColor.WHITE}: Set actor bone position")
            Message.print(sender, "Use command with no arguments to see usage.")
        }

        // parse subcommand: /actor bone [subcommand]
        if ( args.size < 2 ) {
            printBoneCommandInfo(sender)
            return
        }

        when ( args[1].toLowerCase() ) {
            "info" -> printBoneInfo(sender, args)
            "rotate" -> setBoneRotation(sender, args)
            "position" -> setBonePosition(sender, args)
            else -> {
                Message.error(sender, "Invalid /actor bone subcommand")
                printBoneCommandInfo(sender)
            }
        }
    }

    /**
     * @command /actor bone info [actor] [bone]
     * Prints info (position, rotation, ...) about a bone in an
     * actor skeleton.
     */
    private fun printBoneInfo(sender: CommandSender, args: Array<String>) {
        if ( args.size < 4 ) {
            Message.error(sender, "Usage: /actor bone info [actor] [bone]")
            return
        }

        val actorName = args[2]
        val actor = Puppet.getActor(actorName)
        if ( actor === null ) {
            Message.error(sender, "Invalid actor: ${actorName}")
            return
        }

        val boneName = args[3]
        val bone = actor.skeleton?.bones?.get(boneName)
        if ( bone === null ) {
            Message.error(sender, "Invalid bone: ${boneName}")
            return
        }

        Message.print(sender, "Actor \"${actor.name}\":")
        bone.printInfo(sender)
    }

    /**
     * @command /actor bone rotate [actor] [bone] [x] [y] [z]
     * Set rotation for actor named `[actor]` bone named `[bone]`
     * to euler angle input (x, y, z) in degrees. Euler rotation
     * order is ZYX.
     */
    private fun setBoneRotation(sender: CommandSender, args: Array<String>) {
        if ( args.size < 4 ) {
            Message.error(sender, "Usage: /actor bone rotate [actor] [bone] [x] [y] [z]")
            return
        }

        val actorName = args[2]
        val actor = Puppet.getActor(actorName)
        if ( actor === null ) {
            Message.error(sender, "Invalid actor: ${actorName}")
            return
        }

        val boneName = args[3]
        val bone = actor.skeleton?.bones?.get(boneName)
        if ( bone === null ) {
            Message.error(sender, "Invalid bone: ${boneName}")
            return
        }

        // not enough inputs args to change bone
        if ( args.size < 7 ) {
            Message.error(sender, "Usage: /actor bone rotate [actor] [bone] [x] [y] [z]")
            Message.error(sender, "Need to input all (x, y, z) values...")
            return
        }
        
        val xDeg = args[4].toDouble()
        val yDeg = args[5].toDouble()
        val zDeg = args[6].toDouble()

        val x = Math.toRadians(xDeg)
        val y = Math.toRadians(yDeg)
        val z = Math.toRadians(zDeg)

        bone.setRotation(x, y, z)
        actor.update()

        Message.print(sender, "Set actor \"${actor.name}\" bone \"${bone.name}\" rotation to (${xDeg}, ${yDeg}, ${zDeg})")

        // need manual render if engine is not running
        if ( !Puppet.isRunning ) {
            actor.render()
        }
    }

    /**
     * @command /actor bone position [actor] [bone] [x] [y] [z]
     * Set position for actor named `[actor]` bone named `[bone]`
     * to input (x, y, z).
     */
    public fun setBonePosition(sender: CommandSender, args: Array<String>) {
        if ( args.size < 4 ) {
            Message.error(sender, "Usage: /actor bone position [actor] [bone] [x] [y] [z]")
            return
        }

        val actorName = args[2]
        val actor = Puppet.getActor(actorName)
        if ( actor === null ) {
            Message.error(sender, "Invalid actor: ${actorName}")
            return
        }

        val boneName = args[3]
        val bone = actor.skeleton?.bones?.get(boneName)
        if ( bone === null ) {
            Message.error(sender, "Invalid bone: ${boneName}")
            return
        }

        // not enough inputs args to change bone
        if ( args.size < 7 ) {
            Message.error(sender, "Usage: /actor bone position [actor] [bone] [x] [y] [z]")
            Message.error(sender, "Need to input all (x, y, z) values...")
            return
        }
        
        val x = args[4].toDouble()
        val y = args[5].toDouble()
        val z = args[6].toDouble()

        bone.setPosition(x, y, z)
        actor.update()

        Message.print(sender, "Set actor \"${actor.name}\" bone \"${bone.name}\" position to (${x}, ${y}, ${z})")

        // need manual render if engine is not running
        if ( !Puppet.isRunning ) {
            actor.render()
        }
    }
}

/**
 * Typical actor command format is:
 * 
 *     /actor subcommand [inputs] [actor0] [actor1] [actor2] ...
 * 
 * Where [actor0, actor1, actor2, ...] are actor names for target of command.
 * If command user is ingame and no names specified, then the actor player
 * is looking at becomes the target. Otherwise, parse this list of names
 * and return actors.
 * 
 * "targetStartIndex" is the position of [actor0] is the command args array.
 */
private fun getActorTargets(sender: CommandSender, args: Array<String>, targetStartIndex: Int): List<Actor> {
    // use set to avoid duplicate actor targets
    val targets: MutableSet<Actor> = mutableSetOf()
    
    if ( args.size <= targetStartIndex && (sender is Player) ) {
        // no ids listed, get actor player is looking at
        val actor = Puppet.getActorPlayerIsLookingAt(sender)
        if ( actor !== null ) {
            targets.add(actor)
        }
        else {
            Message.error(sender, "Invalid command target: not looking at an actor...")
        }
    }
    else {
        // parse list of actor id targets
        for ( i in targetStartIndex until args.size ) {
            val actorName = args[i]
            val actor = Puppet.getActor(actorName)
            if ( actor === null ) {
                Message.error(sender, "Invalid actor: ${actorName}")
                continue
            }
            targets.add(actor)
        }
    }
    
    return targets.toList()
}