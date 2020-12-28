package net.mcxk.minehunt.listener;

import net.mcxk.minehunt.MineHunt;
import net.mcxk.minehunt.game.GameStatus;
import net.mcxk.minehunt.game.PlayerRole;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;

public class GameWinnerListener implements Listener {
    private final MineHunt plugin = MineHunt.getInstance();
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void playerDeath(PlayerDeathEvent event){
        if(plugin.getGame().getStatus() != GameStatus.GAME_STARTED){
            return;
        }
        Optional<PlayerRole> role = plugin.getGame().getPlayerRole(event.getEntity());
        if(role.isPresent()){
            if(role.get() == PlayerRole.RUNNER){
                String killer = event.getDeathMessage();
                if(event.getEntity().getLastDamageCause() != null){
                   killer = event.getEntity().getLastDamageCause().getEntity().getName();
                   if(killer.equals(event.getEntity().getName())){
                       killer = event.getDeathMessage();
                   }
                }
                event.getEntity().spigot().respawn();
                String finalKiller = killer;
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        plugin.getGame().getGameEndingData().setRunnerKiller(finalKiller);
                        plugin.getGame().stop(PlayerRole.HUNTER, event.getEntity().getLocation().add(0,3,0));
                    }
                }.runTaskLater(plugin,1);
            }
        }
    }

    private String dragonKiller = "Magic";

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void entityDeath(EntityDamageByEntityEvent event){
        if(plugin.getGame().getStatus() != GameStatus.GAME_STARTED){
            return;
        }
        if(event.getEntityType() != EntityType.ENDER_DRAGON){
            return;
        }
        if(event.getDamager() instanceof Player){
            Optional<PlayerRole> role = MineHunt.getInstance().getGame().getPlayerRole(((Player) event.getDamager()));
            if(role.isPresent()){
                if(role.get() == PlayerRole.HUNTER){
                    event.setCancelled(true);
                    event.getEntity().sendMessage(ChatColor.RED+"猎人是末影龙的好伙伴，你不可以对龙造成伤害！");
                    return;
                }
            }
        }
        dragonKiller = event.getDamager().getName();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void entityDeath(EntityDamageByBlockEvent event){
        if(plugin.getGame().getStatus() != GameStatus.GAME_STARTED){
            return;
        }
        if(event.getEntityType() != EntityType.ENDER_DRAGON){
            return;
        }
        if(event.getDamager() == null){
            return;
        }
        dragonKiller = event.getDamager().getType().name();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void entityDeath(EntityDeathEvent event){
        if(plugin.getGame().getStatus() != GameStatus.GAME_STARTED){
            return;
        }
        if(event.getEntityType() != EntityType.ENDER_DRAGON){
            return;
        }
        plugin.getGame().getGameEndingData().setDragonKiller(dragonKiller);
        plugin.getGame().stop(PlayerRole.RUNNER, new Location(event.getEntity().getLocation().getWorld(),0,85,0));
    }
}
