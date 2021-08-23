package net.mcxk.deathswap.util;

import net.mcxk.deathswap.DeathSwap;
import net.mcxk.deathswap.game.Game;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class StatisticsBaker {
    private final Game game = DeathSwap.getInstance().getGame();

    public String getDamageTakenMaster() {
        Map.Entry<String, Double> result = getHighest(Statistic.DAMAGE_TAKEN);
        if (result.getValue() == 0.0d) {
            return "";
        }
        return "全场最惨 "+result.getKey() + " 共受到了 " + result.getValue().intValue() + " 点伤害";
    }

    public String getTrapMaster() {
        Map.Entry<String, AtomicInteger> result = getHighest(DeathSwap.getInstance().getPlayerServerListener().getBlockPlaceCountingMap());
        if (result.getValue().get() == 0) {
            return "";
        }
        return result.getKey() + " 为了陷阱精心放置了 " + result.getValue().intValue() + " 方块";
    }

    public String getBadGuy() {
        Map.Entry<String, AtomicInteger> result = getHighest(DeathSwap.getInstance().getPlayerServerListener().getSwapRunningCountingMap());
        if (result.getValue().get() == 0) {
            return "";
        }
        return result.getKey() + " 在交换前尝试坑害其他玩家 " + result.getValue().intValue() + " 次";
    }




    public String getJumpMaster() {
        Map.Entry<String, Double> result = getHighest(Statistic.JUMP);
        if (result.getValue() == 0.0d) {
            return "";
        }
        return "生命不息，空格不停 " + result.getKey() + " 共跳跃了 " + result.getValue().intValue() + " 次";
    }

    public String getCraftingMaster(){
        Map.Entry<String, Double> result = getHighest(Statistic.CRAFTING_TABLE_INTERACTION);
        if (result.getValue() == 0.0d) {
            return "";
        }
        return "万物手中来 "+result.getKey() + " 共合成了 " + result.getValue().intValue() + " 次";
    }

    public Map.Entry<String, Double> getHighest(Statistic statistic) {
        Player playerMax = null;
        double dataMax = 0.0d;
        for (Player filtering : game.getInGamePlayers()) {
            if (playerMax == null) {
                playerMax = filtering;
                dataMax = filtering.getStatistic(statistic);
                continue;
            }
            double data = filtering.getStatistic(statistic);
            if (dataMax < data) {
                playerMax = filtering;
                dataMax = data;
            }
        }
        if (playerMax == null) {
            return new AbstractMap.SimpleEntry<>("Null", 0.0d);
        }
        return new AbstractMap.SimpleEntry<>(playerMax.getName(), dataMax);
    }

    public Map.Entry<String, AtomicInteger> getHighest(Map<Player, AtomicInteger> record) {
        Player playerMax = null;
        AtomicInteger dataMax = new AtomicInteger(0);
        for (Player filtering : game.getInGamePlayers()) {
            if (playerMax == null) {
                playerMax = filtering;
                dataMax = record.get(filtering);
                continue;
            }
            AtomicInteger data =  record.get(filtering);
            if(data == null)
                continue;
            if (dataMax.get() < data.get()) {
                playerMax = filtering;
                dataMax = data;
            }
        }
        if (playerMax == null || dataMax == null || dataMax.get() == 0) {
            return new AbstractMap.SimpleEntry<>("Null", new AtomicInteger(0));
        }
        return new AbstractMap.SimpleEntry<>(playerMax.getName(), dataMax);
    }
}
