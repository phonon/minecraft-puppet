/**
 * Handles all events for creating/deleting
 * player town name tags
 */

package phonon.puppet.listeners

import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.entity.EntityType

public class EntityDamageListener: Listener {

    @EventHandler
    public fun onEntityDamage(event: EntityDamageEvent) {
        if ( event.entity.type == EntityType.ARMOR_STAND ) {
            event.setCancelled(true)
        }
    }

}