/**
 * Handles detecting player movement to pose actors
 */

package phonon.puppet.listeners

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerTeleportEvent
import phonon.puppet.Puppet
import phonon.puppet.objects.Actor

public class PlayerMoveListener: Listener {
    
    // TODO: create event listener on demand when a player is posing?
    @EventHandler
    public fun onPlayerMove(event: PlayerMoveEvent) {

        val player = event.player
        val actorPosing = Puppet.playerPosingActor.get(player)
        if ( actorPosing === null ) {
            return
        }

        val from = event.getFrom()
        val to = event.getTo()

        handleActorPose(player, actorPosing, from, to)
    }

    @EventHandler
    public fun onPlayerTeleport(event: PlayerTeleportEvent) {
        val player = event.player
        val actorPosing = Puppet.playerPosingActor.get(player)
        if ( actorPosing === null ) {
            return
        }

        val from = event.getFrom()
        val to = event.getTo()

        handleActorPose(player, actorPosing, from, to)
    }
}


// handle moving actor
private fun handleActorPose(player: Player, actor: Actor, from: Location, to: Location) {
    val dx = to.x - from.x
    val dy = to.y - from.y
    val dz = to.z - from.z

    val p = actor.position
    actor.setPosition(p.x + dx, p.y + dy, p.z + dz)

    val rotation = -player.location.yaw
    val r = actor.rotation
    actor.setRotation(r.x, Math.toRadians(rotation.toDouble()).toFloat(), r.z)
}