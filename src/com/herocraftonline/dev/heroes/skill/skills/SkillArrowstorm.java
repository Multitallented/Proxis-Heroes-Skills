package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Player;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.api.SkillResult.ResultType;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SkillArrowstorm extends ActiveSkill {
    private Map<Hero, Integer> shootingPlayers = new HashMap<Hero, Integer>();
    
    public SkillArrowstorm(Heroes plugin) {
        super(plugin, "Arrowstorm");
        setDescription("Shoots between $1-$2 arrows at a rate of $3-$4 per second. CD: $5s M: $6");
        setUsage("/skill arrowstorm");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill arrowstorm"});
        
        setTypes(SkillType.PHYSICAL, SkillType.DAMAGING);
    }

    @Override
    public String getDescription(Hero hero) {
        int maxArrows = SkillConfigManager.getUseSetting(hero, this, "max_arrows", 30, false)
                + (SkillConfigManager.getUseSetting(hero, this, "arrows-per-level", 0, false) * hero.getLevel());
        int minArrows = SkillConfigManager.getUseSetting(hero, this, "min_arrows", 15, false)
                + (SkillConfigManager.getUseSetting(hero, this, "arrows-per-level", 0, false) * hero.getLevel());
        int maxRate = SkillConfigManager.getUseSetting(hero, this, "max_arrows", 20, false);
        int minRate = SkillConfigManager.getUseSetting(hero, this, "max_arrows", 2, false);
        String description = getDescription().replace("$1", maxArrows + "").replace("$2", minArrows + "").replace("$3", maxRate + "").replace("$4", minRate + "");
        
        //COOLDOWN
        int cooldown = (SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN_REDUCE.node(), 0, false) * hero.getLevel()) / 1000;
        if (cooldown > 0) {
            description += " CD:" + cooldown + "s";
        }
        
        //MANA
        int mana = SkillConfigManager.getUseSetting(hero, this, Setting.MANA.node(), 10, false)
                - (SkillConfigManager.getUseSetting(hero, this, Setting.MANA_REDUCE.node(), 0, false) * hero.getLevel());
        if (mana > 0) {
            description += " M:" + mana;
        }
        
        //HEALTH_COST
        int healthCost = SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST, 0, false) - 
                (SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST_REDUCE, mana, true) * hero.getLevel());
        if (healthCost > 0) {
            description += " HP:" + healthCost;
        }
        
        //STAMINA
        int staminaCost = SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA.node(), 0, false)
                - (SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA_REDUCE.node(), 0, false) * hero.getLevel());
        if (staminaCost > 0) {
            description += " FP:" + staminaCost;
        }
        
        //DELAY
        int delay = SkillConfigManager.getUseSetting(hero, this, Setting.DELAY.node(), 0, false) / 1000;
        if (delay > 0) {
            description += " W:" + delay + "s";
        }
        
        //EXP
        int exp = SkillConfigManager.getUseSetting(hero, this, Setting.EXP.node(), 0, false);
        if (exp > 0) {
            description += " XP:" + exp;
        }
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set("max_arrows", 30);
        node.set("min_arrows",15);
        node.set("min_rate", 2);
        node.set("max_rate", 20);
        node.set("arrows-per-level", 0.0);
        node.set(Setting.COOLDOWN.node(), 1000);
        node.set(Setting.MANA.node(), 10);
        return node;
    }

    @Override
    public SkillResult use(final Hero hero, String[] args) {
        if (shootingPlayers.containsKey(hero)) {
            plugin.getServer().getScheduler().cancelTask(shootingPlayers.get(hero));
            shootingPlayers.remove(hero);
            long cooldown = SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN.node(), 1000, false);
            hero.setCooldown("Arrowstorm", System.currentTimeMillis() + cooldown);
            Messaging.send(hero.getPlayer(), "$1 stopped shooting arrows prematurely.", hero.getPlayer().getDisplayName());
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        final Player player = hero.getPlayer();
        PlayerInventory inv = player.getInventory();
        int minArrows = (int) SkillConfigManager.getUseSetting(hero, this, "min_arrows", 15, false)
                + (SkillConfigManager.getUseSetting(hero, this, "arrows-per-level", 0, false)* hero.getLevel());
        if (minArrows <= 0) {
            minArrows = 1;
        } else if (minArrows > 64) {
            minArrows = 64;
        }
        int maxArrows = (int) SkillConfigManager.getUseSetting(hero, this, "max_arrows", 30, false)
                + (SkillConfigManager.getUseSetting(hero, this, "arrows-per-level", 0, false)* hero.getLevel());
        if (maxArrows < minArrows) {
            maxArrows = minArrows;
        } else if (maxArrows > 64) {
            maxArrows = 64;
        }
        int minRate = (int) SkillConfigManager.getUseSetting(hero, this, "min_rate", 2, false);
        if (minRate != 1 | minRate != 2 | minRate != 4 | minRate != 5 |
                minRate != 10 | minRate != 20) {
            minRate = 2;
        }
        int maxRate = (int) SkillConfigManager.getUseSetting(hero, this, "max_rate", 10, false);
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
        final Skill skill = this;
        float rate = 20/sleepTime;
        Messaging.send(player, "Casting $1 at a rate of $2 per second", preTotalArrows, rate);
        shootingPlayers.put(hero, plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                player.shootArrow();
            }
        }, 0L, sleepTime));
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                try {
                    plugin.getServer().getScheduler().cancelTask(shootingPlayers.get(hero));
                    shootingPlayers.remove(hero);
                    hero.setCooldown("Arrowstorm", SkillConfigManager.getUseSetting(hero, skill, Setting.COOLDOWN.node(), 1000, false)
                            + System.currentTimeMillis());
                } catch (Exception e) {
                    
                }
            }
        }, preTotalArrows * sleepTime);
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                hero.setCooldown("Arrowstorm", System.currentTimeMillis());
            }
        }, 1L);
        return SkillResult.NORMAL;
    }
    
}