package net.mcxk.deathswap.listener;

import net.mcxk.deathswap.DeathSwap;
import net.mcxk.deathswap.game.GameStatus;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractListener implements Listener {
    private final DeathSwap plugin = DeathSwap.getInstance();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void clickXJB(PlayerInteractEvent event) {
        if (plugin.getGame().getStatus() != GameStatus.GAME_STARTED) {
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void damageXJB(EntityDamageEvent event) {
        if (plugin.getGame().getStatus() != GameStatus.GAME_STARTED) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void runXJB(FoodLevelChangeEvent event) {
        if (plugin.getGame().getStatus() != GameStatus.GAME_STARTED) {
            event.setCancelled(true);
        }
    }
}
