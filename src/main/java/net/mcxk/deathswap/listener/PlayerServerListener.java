package net.mcxk.deathswap.listener;

import net.mcxk.deathswap.DeathSwap;
import net.mcxk.deathswap.event.SwapEvent;
import net.mcxk.deathswap.game.GameStatus;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerServerListener implements Listener {
    private final DeathSwap plugin = DeathSwap.getInstance();
    private final Map<Player, AtomicInteger> blockPlaceCountingMap = new HashMap<>();
    private final Map<Player, AtomicInteger> blockBreakCountingMap = new HashMap<>();
    private final Map<Player, AtomicInteger> swapRunningCountingMap = new HashMap<>();

    public Map<Player, AtomicInteger> getBlockBreakCountingMap() {
        return blockBreakCountingMap;
    }

    public Map<Player, AtomicInteger> getBlockPlaceCountingMap() {
        return blockPlaceCountingMap;
    }

    public Map<Player, AtomicInteger> getSwapRunningCountingMap() {
        return swapRunningCountingMap;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event){
        AtomicInteger count = blockPlaceCountingMap.get(event.getPlayer());
        if(count == null){
            count = new AtomicInteger(0);
            blockPlaceCountingMap.put(event.getPlayer(),count);
        }
        count.incrementAndGet();
    }
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event){
        AtomicInteger count = blockBreakCountingMap.get(event.getPlayer());
        if(count == null){
            count = new AtomicInteger(0);
            blockBreakCountingMap.put(event.getPlayer(),count);
        }
        count.incrementAndGet();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockBreak(SwapEvent event){
        for (Player inGamePlayer : DeathSwap.getInstance().getGame().getInGamePlayers()) {
            if(inGamePlayer.isSprinting() || !inGamePlayer.isOnGround()){
                AtomicInteger count = swapRunningCountingMap.get(inGamePlayer);
                if(count == null){
                    count = new AtomicInteger(0);
                    swapRunningCountingMap.put(inGamePlayer,count);
                }
                count.incrementAndGet();
            }
        }


    }





    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event){
        if(event.getEntity().getStatistic(Statistic.DEATHS) >= 10){
            plugin.getGame().stop(event.getEntity());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void join(PlayerJoinEvent event) {
        if (plugin.getGame().getStatus() == GameStatus.WAITING_PLAYERS) {
            if (plugin.getGame().playerJoining(event.getPlayer())) {
                event.getPlayer().setGameMode(GameMode.ADVENTURE);
            } else {
                event.getPlayer().setGameMode(GameMode.SPECTATOR);
                event.getPlayer().sendMessage("当前游戏已满人，您现在处于观战状态");
            }
        } else {
            //处理玩家重连
            if (plugin.getGame().getInGamePlayers().stream().anyMatch(p -> p.getUniqueId().equals(event.getPlayer().getUniqueId()))) {
                plugin.getGame().getInGamePlayers().removeIf(p -> p.getUniqueId().equals(event.getPlayer().getUniqueId()));
                plugin.getGame().getInGamePlayers().add(event.getPlayer());
                if (plugin.getGame().getInGamePlayers().contains(event.getPlayer())) {
                    Bukkit.broadcastMessage(ChatColor.GREEN + "玩家 " + event.getPlayer().getName() + " 已重新连接");
                    plugin.getGame().getReconnectTimer().entrySet().removeIf(set -> set.getKey().getUniqueId().equals(event.getPlayer().getUniqueId()));
                }

            } else {
                event.getPlayer().setGameMode(GameMode.SPECTATOR);
                event.getPlayer().sendMessage("游戏已经开始，您现在处于观战状态");
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void quit(PlayerQuitEvent event) {
        if (!plugin.getGame().getInGamePlayers().contains(event.getPlayer())) {
            return;
        }
        plugin.getGame().playerLeaving(event.getPlayer());
    }

}
