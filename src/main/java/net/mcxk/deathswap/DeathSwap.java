package net.mcxk.deathswap;

import lombok.Getter;
import net.mcxk.deathswap.game.Game;
import net.mcxk.deathswap.listener.ChatListener;
import net.mcxk.deathswap.listener.PlayerInteractListener;
import net.mcxk.deathswap.listener.PlayerServerListener;
import net.mcxk.deathswap.watcher.CountDownWatcher;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class DeathSwap extends JavaPlugin {
    @Getter
    private static DeathSwap instance;
    @Getter
    private Game game;

    @Getter
    private CountDownWatcher countDownWatcher;

    @Getter
    private PlayerServerListener playerServerListener;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        instance = this;
        game = new Game();
        countDownWatcher = new CountDownWatcher();
        playerServerListener = new PlayerServerListener();
        game.switchWorldRuleForReady(false);
        Bukkit.getPluginManager().registerEvents(playerServerListener, this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(), this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return false;
    }

}
