package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.api.SkillResult.ResultType;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Messaging;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SkillArrowstorm extends ActiveSkill {
    
    public SkillArrowstorm(Heroes plugin) {
        super(plugin, "Arrowstorm");
        setDescription("Shoots tons of arrows over time");
        setUsage("/skill arrowstorm");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill arrowstorm"});
        
        setTypes(SkillType.PHYSICAL, SkillType.DAMAGING);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("max_arrows", 30);
        node.setProperty("min_arrows",15);
        node.setProperty("min_rate", 2);
        node.setProperty("max_rate", 20);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        final Player player = hero.getPlayer();
        PlayerInventory inv = player.getInventory();
        int minArrows = (int) getSetting(hero, "min_arrows", 15, false);
        if (minArrows <= 0) {
            minArrows = 1;
        } else if (minArrows > 64) {
            minArrows = 64;
        }
        int maxArrows = (int) getSetting(hero, "max_arrows", 30, false);
        if (maxArrows < minArrows) {
            maxArrows = minArrows;
        } else if (maxArrows > 64) {
            maxArrows = 64;
        }
        int minRate = (int) getSetting(hero, "min_rate", 2, false);
        if (minRate != 1 | minRate != 2 | minRate != 4 | minRate != 5 |
                minRate != 10 | minRate != 20) {
            minRate = 2;
        }
        int maxRate = (int) getSetting(hero, "max_rate", 10, false);
        if (maxRate != 1 | maxRate != 2 | maxRate != 4 | maxRate != 5 |
                maxRate != 10 | maxRate != 20) {
            maxRate = 10;
        }
        if (maxRate < minRate) {
            maxRate = minRate;
        }
        int randRate = maxRate - minRate;
        int randArrows = maxArrows - minArrows;
        
        

        Map<Integer, ? extends ItemStack> arrowSlots = inv.all(Material.ARROW);

        int numArrows = 0;
        for (Map.Entry<Integer, ? extends ItemStack> entry : arrowSlots.entrySet()) {
            numArrows += entry.getValue().getAmount();
        }

        int preTotalArrows = (int) Math.rint(Math.random()*randArrows)+minArrows;
        if (numArrows > preTotalArrows) {
            numArrows = preTotalArrows;
        }
        if (numArrows < minArrows) {
            numArrows = 0;
        }
        if (numArrows == 0) {
            return new SkillResult(ResultType.MISSING_REAGENT, true, minArrows, "Arrows");
        }

        int removedArrows = 0;
        for (Map.Entry<Integer, ? extends ItemStack> entry : arrowSlots.entrySet()) {
            int amount = entry.getValue().getAmount();
            int remove = amount;
            if (removedArrows + remove > numArrows) {
                remove = numArrows - removedArrows;
            }
            removedArrows += remove;
            if (remove == amount) {
                inv.clear(entry.getKey());
            } else {
                inv.getItem(entry.getKey()).setAmount(amount - remove);
            }

            if (removedArrows >= numArrows) {
                break;
            }
        }
        player.updateInventory();
        
        
        broadcastExecuteText(hero);
        final long sleepTime = (long) Math.rint(Math.random()*randRate)+minRate;
        final long totalArrows = (long) numArrows;
        float rate = 20/sleepTime;
        player.sendMessage("Casting "+ totalArrows + " at a rate of " + rate + " per second");
        final int shootArrows = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                player.shootArrow();
            }
        }, 0L, sleepTime);
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getServer().getScheduler().cancelTask(shootArrows);
            }
        }, numArrows * sleepTime);
        return SkillResult.NORMAL;
    }
    

}