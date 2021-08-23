package net.mcxk.deathswap.listener;

import net.mcxk.deathswap.DeathSwap;
import net.mcxk.deathswap.game.GameStatus;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void chat(AsyncPlayerChatEvent event) {
        if (DeathSwap.getInstance().getGame().getStatus() != GameStatus.GAME_STARTED) {
            return;
        }
        if (!DeathSwap.getInstance().getGame().getInGamePlayers().contains(event.getPlayer())) {
            event.setFormat(ChatColor.GRAY + "[OBSERVER] " + event.getPlayer().getDisplayName() + " " + event.getMessage());
            return;
        }
        event.setFormat(ChatColor.GREEN + "[PLAYER] " + event.getPlayer().getDisplayName() + " " + ChatColor.RESET + event.getMessage());
    }
}
