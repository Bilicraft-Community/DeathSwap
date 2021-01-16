package net.mcxk.minehunt;

import lombok.Getter;
import net.mcxk.minehunt.game.Game;
import net.mcxk.minehunt.game.GameStatus;
import net.mcxk.minehunt.game.PlayerRole;
import net.mcxk.minehunt.listener.*;
import net.mcxk.minehunt.watcher.CountDownWatcher;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class MineHunt extends JavaPlugin {
    @Getter
    private static MineHunt instance;
    @Getter
    private Game game;

    @Getter
    private CountDownWatcher countDownWatcher;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        instance = this;
        game = new Game();
        countDownWatcher = new CountDownWatcher();
        game.switchWorldRuleForReady(false);
        Bukkit.getPluginManager().registerEvents(new PlayerServerListener(),this);
        Bukkit.getPluginManager().registerEvents(new PlayerInteractListener(),this);
        Bukkit.getPluginManager().registerEvents(new PlayerCompassListener(),this);
        Bukkit.getPluginManager().registerEvents(new ProgressDetectingListener(),this);
        Bukkit.getPluginManager().registerEvents(new GameWinnerListener(),this);
        Bukkit.getPluginManager().registerEvents(new ChatListener(),this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!sender.hasPermission("minehunt.admin")){
            return false;
        }
        if(args.length < 1){
            return false;
        }
        //不安全命令 完全没做检查，确认你会用再执行
        if(args[0].equalsIgnoreCase("hunter") || args[0].equalsIgnoreCase("runner")) {
            Player player = (Player) sender;
            this.getGame().getInGamePlayers().add(player);
            if (args[0].equalsIgnoreCase("hunter")) {
                this.getGame().getRoleMapping().put(player, PlayerRole.HUNTER);
            } else {
                this.getGame().getRoleMapping().put(player, PlayerRole.RUNNER);
            }
            player.setGameMode(GameMode.SURVIVAL);
            Bukkit.broadcastMessage("玩家 "+sender.getName()+" 强制加入了游戏！ 身份："+args[0]);
            return true;
        }
        if(args[0].equalsIgnoreCase("resetcountdown") && this.getGame().getStatus() == GameStatus.WAITING_PLAYERS){
            this.getCountDownWatcher().resetCountdown();
            return true;
        }
        if(args[0].equalsIgnoreCase("forcestart") && this.getGame().getStatus() == GameStatus.WAITING_PLAYERS){
            this.getCountDownWatcher().resetCountdown();
            return true;
        }

        return false;
    }

}
