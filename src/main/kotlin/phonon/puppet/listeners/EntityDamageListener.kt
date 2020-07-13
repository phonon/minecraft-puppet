/**
 * Handle cancelling damage on actor ArmorStand entities.
 */

package phonon.puppet.listeners

import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.entity.EntityType
import phonon.puppet.Puppet

public class EntityDamageListener: Listener {

    @EventHandler
    public fun onEntityDamage(event: EntityDamageEvent) {
        val entity = event.entity
        if ( entity.type == EntityType.ARMOR_STAND ) {
            if ( Puppet.getActorFromEntity(entity) !== null ) {
                event.setCancelled(true)
            }
        }
    }

}