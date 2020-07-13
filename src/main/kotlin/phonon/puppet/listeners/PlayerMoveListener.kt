package phonon.puppet.listeners

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerTeleportEvent
import phonon.puppet.Puppet
import phonon.puppet.objects.Actor

public class PlayerMoveListener: Listener {
    
    // TODO: create event listener on demand when a player is posing
    @EventHandler
    public fun onPlayerMove(event: PlayerMoveEvent) {

        val player = event.player
        val actorPosing = Puppet.playerPosingActor.get(player)
        if ( actorPosing === null ) {
            return
        }

        val from = event.getFrom()
        val to = event.getTo()

        if ( from === null || to === null ) {
            return
        }

        val dx = to.x - from.x
        val dy = to.y - from.y
        val dz = to.z - from.z

        val p = actorPosing.position
        actorPosing.setPosition(p.x + dx, p.y + dy, p.z + dz)

        val rotation = -player.location.yaw
        val r = actorPosing.rotation
        actorPosing.setRotation(r.x, Math.toRadians(rotation.toDouble()).toFloat(), r.z)
    }

    // handle player teleport -> cancel posing
    @EventHandler
    public fun onPlayerTeleport(event: PlayerTeleportEvent) {

    }
}
