package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Setting;
import java.util.HashSet;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;

public class SkillSpreadingSkele extends ActiveSkill {
    public SkillSpreadingSkele(Heroes plugin) {
        super(plugin, "SpreadingSkele");
        setDescription("Spawn 1 skeleton that will multiply $1 times if not killed.");
        setUsage("/skill spreadingskele");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill spreadingskele"});
        
        setTypes(SkillType.DARK, SkillType.SUMMON, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        int multiplyTimes = (int) SkillConfigManager.getUseSetting(hero, this, "multiplier", 2, false);
        multiplyTimes = multiplyTimes < 0 ? 0 : multiplyTimes;
        String description = getDescription().replace("$1", multiplyTimes + "");
        
        //COOLDOWN
        int cooldown = (SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN_REDUCE.node(), 0, false) * hero.getSkillLevel(this)) / 1000;
        if (cooldown > 0) {
            description += " CD:" + cooldown + "s";
        }
        
        //MANA
        int mana = SkillConfigManager.getUseSetting(hero, this, Setting.MANA.node(), 10, false)
                - (SkillConfigManager.getUseSetting(hero, this, Setting.MANA_REDUCE.node(), 0, false) * hero.getSkillLevel(this));
        if (mana > 0) {
            description += " M:" + mana;
        }
        
        //HEALTH_COST
        int healthCost = SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST, 0, false) - 
                (SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST_REDUCE, mana, true) * hero.getSkillLevel(this));
        if (healthCost > 0) {
            description += " HP:" + healthCost;
        }
        
        //STAMINA
        int staminaCost = SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA.node(), 0, false)
                - (SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA_REDUCE.node(), 0, false) * hero.getSkillLevel(this));
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
        node.set("multiplier", 2);
        node.set("multiply-delay", 5000);
        return node;
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        broadcastExecuteText(hero);
        Block wTargetBlock = player.getTargetBlock(null, 20).getRelative(
                        BlockFace.UP);
        final Skeleton le = player.getWorld().spawn(wTargetBlock.getLocation(), Skeleton.class);
        if (le == null || le.isDead()) {
            return SkillResult.INVALID_TARGET;
        }
        le.getWorld().playEffect(le.getLocation(), Effect.SMOKE, 3);
        int multiplyTimes = (int) SkillConfigManager.getUseSetting(hero, this, "multiplier", 2, false);
        multiplyTimes = multiplyTimes < 0 ? 0 : multiplyTimes;
        long multiplyDelay = (long) SkillConfigManager.getUseSetting(hero, this, "multiply-delay", 5000, false);
        multiplyDelay = multiplyDelay < 0 ? 0 : multiplyDelay;
        multiplyDelay = multiplyDelay / 50;
        final HashSet<Skeleton> tempSet = new HashSet<Skeleton>();
        tempSet.add(le);
        for (int i=0; i<multiplyTimes; i++) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    HashSet<Skeleton> addLater = new HashSet<Skeleton>();
                    for (Skeleton sk : tempSet) {
                        Skeleton le = spawnSkele(sk);
                        if (le != null) {
                            addLater.add(le);
                        }
                    }
                    for (Skeleton sk : addLater) {
                        tempSet.add(sk);
                    }
                }
            }, multiplyDelay * (i+1));
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                for (Skeleton sk : tempSet) {
                    if (sk != null && !sk.isDead()) {
                        sk.damage(sk.getMaxHealth());
                    }
                }
            }
        }, multiplyDelay * (multiplyTimes + 2));
        return SkillResult.NORMAL;
    }
    
    private Skeleton spawnSkele(Skeleton le) {
        if (le == null || le.isDead()) {
            return null;
        }
        return le.getWorld().spawn(le.getLocation(), Skeleton.class);
    }

}