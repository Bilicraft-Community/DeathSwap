package net.mcxk.deathswap.game;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.mcxk.deathswap.DeathSwap;
import net.mcxk.deathswap.util.StatisticsBaker;
import net.mcxk.deathswap.util.Util;
import net.mcxk.deathswap.watcher.ReconnectWatcher;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class Game {
    @Getter
    final Map<Player, Double> teamDamageData = new HashMap<>();
    private final DeathSwap plugin = DeathSwap.getInstance();
    @Getter
    private final List<Player> inGamePlayers = Lists.newCopyOnWriteArrayList(); //线程安全
    @Getter
    private final int countdown = 30;
    @Getter
    private final Map<Player, Long> reconnectTimer = new HashMap<>();
    private final Map<World, Difficulty> difficultyMap = new HashMap<>();
    @Getter
    @Setter
    private GameStatus status = GameStatus.WAITING_PLAYERS;
    @Getter
    private final int maxPlayers = plugin.getConfig().getInt("max-players");
    @Getter
    private final int minPlayers = plugin.getConfig().getInt("min-players");

    @Getter
    private BukkitTask task;

    public Game() {
        fixConfig();
    }

    public boolean playerJoining(Player player) {
        reconnectTimer.remove(player);
        if (inGamePlayers.size() < maxPlayers) {
            inGamePlayers.add(player);
            return true;
        }
        return false;
    }

    public void fixConfig() {
    }

    public void playerLeaving(Player player) {
        if (status == GameStatus.WAITING_PLAYERS) {
            this.inGamePlayers.remove(player);
        } else {
            this.reconnectTimer.put(player, System.currentTimeMillis());
        }
    }

    public void playerLeft(Player player) {
        this.inGamePlayers.remove(player);

        if (inGamePlayers.isEmpty()) {
            Bukkit.broadcastMessage("由于游戏人数不足，游戏被迫终止。");
            Bukkit.broadcastMessage("服务器将会在 10 秒钟后重新启动。");
            new BukkitRunnable() {
                @Override
                public void run() {
                    Bukkit.shutdown();
                }
            }.runTaskLater(plugin, 200);
            return;
        }
        Bukkit.broadcastMessage("玩家：" + player.getName() + " 因长时间未能重新连接回对战而被从列表中剔除");
    }

    public void start() {
        if (status != GameStatus.WAITING_PLAYERS) {
            return;
        }
        Bukkit.broadcastMessage("正在随机传送位置...");
        inGamePlayers.forEach(player -> player.teleport(airDrop(player.getWorld().getSpawnLocation())));
        Bukkit.broadcastMessage("设置游戏规则...");
        inGamePlayers.forEach(p -> {
            p.setGameMode(GameMode.SURVIVAL);
            p.setFoodLevel(40);
            p.setHealth(p.getMaxHealth());
            p.setExp(0.0f);
            p.setCompassTarget(p.getWorld().getSpawnLocation());
            p.getInventory().clear();
        });
        Bukkit.getScoreboardManager().getMainScoreboard().registerNewObjective("deaths", "deathCount", "死亡计分板");
        switchWorldRuleForReady(true);
        Bukkit.broadcastMessage("游戏开始！");
        Bukkit.broadcastMessage(ChatColor.AQUA + "欢迎来到 " + ChatColor.GREEN + "死亡交换 " + ChatColor.AQUA + "!");
        Bukkit.broadcastMessage(ChatColor.AQUA + "在本游戏中，将会有 " + ChatColor.YELLOW + inGamePlayers.size() + ChatColor.AQUA + " 名玩家共同游戏");
        Bukkit.broadcastMessage(ChatColor.AQUA + "你需要在有限的时间内收集资源并设置致命陷阱。");
        Bukkit.broadcastMessage(ChatColor.GREEN + "游戏每隔 5 分钟将会交换所有玩家的位置。");
        Bukkit.broadcastMessage(ChatColor.AQUA + "你需要想方设法利用位置交换击杀你的对手。");
        Bukkit.broadcastMessage(ChatColor.AQUA + "死亡数最少的玩家将会获得最后的胜利。");
        Bukkit.broadcastMessage(ChatColor.AQUA + "当任意玩家死亡数达到 10 次时，游戏将会自动结束。");
        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "祝君好运！");
        Bukkit.broadcastMessage(ChatColor.GREEN + "本场游戏参与的玩家: " + Util.list2String(inGamePlayers.stream().map(Player::getName).collect(Collectors.toList())));
        status = GameStatus.GAME_STARTED;
        this.registerWatchers();
        inGamePlayers.forEach(player -> player.sendTitle(ChatColor.GREEN + "游戏开始", "DeathSwap - 死亡交换", 0, 80, 0));
//        new BukkitRunnable(){
//            @Override
//            public void run() {
//                stop();
//            }
//        }.runTaskLater(plugin,20*60*30);
        task = new BukkitRunnable(){
            @Override
            public void run() {
                swapLoc();
            }
        }.runTaskTimerAsynchronously(plugin,20*60*5,20*60*5);
    }

    @SneakyThrows
    public void swapLoc(){
        int sec = 10;
        for (int i = 0; i < sec; i++) {
            for (Player p : inGamePlayers) {
                p.sendMessage(ChatColor.RED+""+ChatColor.BOLD+"位置将会在 "+(sec-i)+" 秒种后交换");
                p.playSound(p.getLocation(),Sound.BLOCK_DISPENSER_LAUNCH,0.3f,1.0f);
            }
            Thread.sleep(1000);
        }
        Bukkit.getScheduler().runTask(plugin,()->{
            Location oldLoc = null;
            for (Player inGamePlayer : inGamePlayers) {
                if (oldLoc == null) {
                    oldLoc = inGamePlayer.getLocation();
                    continue;
                }
                Location loc = inGamePlayer.getLocation();
                inGamePlayer.teleport(oldLoc);
                oldLoc = loc;
            }
            inGamePlayers.get(0).teleport(oldLoc);
        });
    }

    public void switchWorldRuleForReady(boolean ready) {
        if (!ready) {
            Bukkit.getWorlds().forEach(world -> {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                world.setGameRule(GameRule.DO_FIRE_TICK, false);
                world.setGameRule(GameRule.MOB_GRIEFING, false);
                difficultyMap.put(world, world.getDifficulty());
                world.setDifficulty(Difficulty.PEACEFUL);
            });
        } else {
            Bukkit.getWorlds().forEach(world -> {
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
                world.setGameRule(GameRule.DO_MOB_SPAWNING, true);
                world.setGameRule(GameRule.DO_FIRE_TICK, true);
                world.setGameRule(GameRule.MOB_GRIEFING, true);
                world.setDifficulty(difficultyMap.getOrDefault(world, Difficulty.NORMAL));
            });
        }
    }

    public void stop(@Nullable Player endingPlayer) {
        this.status = GameStatus.ENDED;
        Bukkit.broadcastMessage(ChatColor.YELLOW + "游戏结束! 服务器将在30秒后重新启动！");

        Player minDeath = null;
        int deaths = 0;
        for (Player inGamePlayer : inGamePlayers) {
            int death = inGamePlayer.getStatistic(Statistic.DEATHS);
            if(minDeath == null){
                minDeath = inGamePlayer;
                deaths = death;
                continue;
            }
            if(death < deaths){
                minDeath = inGamePlayer;
                deaths = death;
            }
            if(endingPlayer != null)
                inGamePlayer.teleport(endingPlayer);
            inGamePlayer.setGameMode(GameMode.SPECTATOR);
        }

        Player finalMinDeath = minDeath;
        int finalDeaths = deaths;
        new BukkitRunnable() {
            @Override
            public void run() {
                //开始结算阶段
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.MUSIC_DISC_WAIT, 1.0f, 1.0f));
                        sendEndingAnimation(finalMinDeath, finalDeaths);
                    }
                }.runTaskLaterAsynchronously(plugin, 20 * 10);
            }
        }.runTaskLater(DeathSwap.getInstance(), 20 * 10);
    }

    @SneakyThrows
    private void sendEndingAnimation(Player mineDeath, int deaths){
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if(onlinePlayer.equals(mineDeath)){
                onlinePlayer.sendTitle(ChatColor.GOLD+"胜利", ChatColor.AQUA+"你在本局死亡交换中死亡次数最少！", 0, 200, 0);
            }else{
                onlinePlayer.sendTitle(ChatColor.GREEN+"获胜者", mineDeath.getName() +ChatColor.BLUE+"死亡"+ChatColor.AQUA+deaths+ChatColor.BLUE+"次", 0,200,0);
            }
        }
        Thread.sleep(5*1000);
        StatisticsBaker baker = new StatisticsBaker();
        if(!StringUtils.isEmpty(baker.getCraftingMaster())){
            Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(ChatColor.RED + "世界创造者", baker.getCraftingMaster(), 0, 20000, 0));
        }
        Thread.sleep(3*1000);
        if(!StringUtils.isEmpty(baker.getDamageTakenMaster())){
            Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(ChatColor.RED + "一定是在针对我吧!", baker.getCraftingMaster(), 0, 20000, 0));
        }
        Thread.sleep(3*1000);
        if(!StringUtils.isEmpty(baker.getTrapMaster())){
            Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(ChatColor.RED + "为什么要自己来?", baker.getCraftingMaster(), 0, 20000, 0));
        }
        Thread.sleep(3*1000);
        if(!StringUtils.isEmpty(baker.getBadGuy())){
            Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(ChatColor.RED + "一肚子坏水!", baker.getBadGuy(), 0, 20000, 0));
        }
        Thread.sleep(3*1000);
        Bukkit.getOnlinePlayers().forEach(p -> p.sendTitle(ChatColor.GREEN + "死亡交换-Bilicraft", "Thanks for playing!", 0, 20000, 0));
        Thread.sleep(10*1000);
        Bukkit.shutdown();
    }
    private void registerWatchers() {
        new ReconnectWatcher();
    }



    //Code from ManHunt

    private Location airDrop(Location spawnpoint) {
        Location loc = spawnpoint.clone();
        loc = new Location(loc.getWorld(), loc.getBlockX(), 0, loc.getBlockZ());
        Random random = new Random();
        loc.add(random.nextInt(200) + 100, 0, random.nextInt(200) + 100);
        loc = loc.getWorld().getHighestBlockAt(loc).getLocation();
        loc.getBlock().setType(Material.GLASS);
        loc.setY(loc.getY() + 1);
        return loc;
    }
}
