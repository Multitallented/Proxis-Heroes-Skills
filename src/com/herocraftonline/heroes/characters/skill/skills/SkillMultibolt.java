package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillMultibolt extends ActiveSkill {

    public SkillMultibolt(Heroes plugin) {
        super(plugin, "Multibolt");
        setDescription("$3% chance to strikes up to $2 times with lightning ($4 damage). R:$1");
        setUsage("/skill multibolt");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill multibolt"});

        setTypes(SkillType.LIGHTNING, SkillType.DAMAGING, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        int distance = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE.node(), 40, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        distance = distance > 0 ? distance : 0;
        int maxStrikes = (int) (SkillConfigManager.getUseSetting(hero, this, "max-strikes", 4, false) +
                (SkillConfigManager.getUseSetting(hero, this, "max-strikes-increase", 0.0, false) * hero.getSkillLevel(this)));
        maxStrikes = maxStrikes > 0 ? maxStrikes : 0;
        double chance = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE.node(), 0.5, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE_LEVEL.node(), 0.0, false) * hero.getSkillLevel(this))) * 100;
        chance = chance > 0 ? chance : 0;
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE.node(), 14, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getSkillLevel(this)));
        damage = damage > 0 ? damage : 0;
        String description = getDescription().replace("$1", distance + "").replace("$2", maxStrikes + "").replace("$3", chance + "").replace("$4", damage + "");
        
        //COOLDOWN
        int cooldown = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN_REDUCE.node(), 0, false) * hero.getSkillLevel(this)) / 1000;
        if (cooldown > 0) {
            description += " CD:" + cooldown + "s";
        }
        
        //MANA
        int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA.node(), 10, false)
                - (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA_REDUCE.node(), 0, false) * hero.getSkillLevel(this));
        if (mana > 0) {
            description += " M:" + mana;
        }
        
        //HEALTH_COST
        int healthCost = SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_COST, 0, false) - 
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_COST_REDUCE, mana, true) * hero.getSkillLevel(this));
        if (healthCost > 0) {
            description += " HP:" + healthCost;
        }
        
        //STAMINA
        int staminaCost = SkillConfigManager.getUseSetting(hero, this, SkillSetting.STAMINA.node(), 0, false)
                - (SkillConfigManager.getUseSetting(hero, this, SkillSetting.STAMINA_REDUCE.node(), 0, false) * hero.getSkillLevel(this));
        if (staminaCost > 0) {
            description += " FP:" + staminaCost;
        }
        
        //DELAY
        int delay = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DELAY.node(), 0, false) / 1000;
        if (delay > 0) {
            description += " W:" + delay + "s";
        }
        
        //EXP
        int exp = SkillConfigManager.getUseSetting(hero, this, SkillSetting.EXP.node(), 0, false);
        if (exp > 0) {
            description += " XP:" + exp;
        }
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(SkillSetting.MAX_DISTANCE.node(), 40);
        node.set(SkillSetting.MAX_DISTANCE_INCREASE.node(), 0);
        node.set("max-strikes", 4);
        node.set("max-strikes-increase", 0);
        node.set(SkillSetting.CHANCE.node(), 0.5);
        node.set(SkillSetting.CHANCE_LEVEL.node(), 0);
        node.set(SkillSetting.DAMAGE.node(), 14);
        node.set("damage-increase", 0);
        return node;
    }

    @Override
    public SkillResult use(final Hero hero, String[] args) {
        final Player player = hero.getPlayer();
        int distance = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE.node(), 40, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        distance = distance > 0 ? distance : 0;
        final Location wLocation = player.getTargetBlock(null, distance).getLocation();
        Player aTargetPlayer = null;
        for (int i=0; i< player.getWorld().getPlayers().size(); i++) {
            if (Math.abs(player.getWorld().getPlayers().get(i).getLocation().getBlockX() - wLocation.getBlockX()) <= 1 &&
                Math.abs(player.getWorld().getPlayers().get(i).getLocation().getBlockZ() - wLocation.getBlockZ()) <= 1) {
                aTargetPlayer = player.getWorld().getPlayers().get(i);
            }
        }
        /*if (aTargetPlayer != null && !damageCheck(player, aTargetPlayer)) {
            Messaging.send(player, "You can't harm that target");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }*/
        final Player targetPlayer = aTargetPlayer;
        broadcastExecuteText(hero);
        int preDamage = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE.node(), 14, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getSkillLevel(this)));
        preDamage = preDamage > 0 ? preDamage : 0;
        final int damage = preDamage;
        if (aTargetPlayer != null) {
            addSpellTarget(aTargetPlayer, hero);
            aTargetPlayer.getWorld().strikeLightningEffect(aTargetPlayer.getLocation());
            damageEntity(aTargetPlayer, player, damage, DamageCause.MAGIC);
            //aTargetPlayer.damage(damage, player);
        } else {
            player.getWorld().strikeLightning(wLocation);
        }
        double chance = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE.node(), 0.5, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE_LEVEL.node(), 0.0, false) * hero.getSkillLevel(this)));
        chance = chance > 0 ? chance : 0;
        final double finalChance = chance;
        int maxStrikes = (int) (SkillConfigManager.getUseSetting(hero, this, "max-strikes", 4, false) +
                (SkillConfigManager.getUseSetting(hero, this, "max-strikes-increase", 0.0, false) * hero.getSkillLevel(this)));
        maxStrikes = maxStrikes > 0 ? maxStrikes : 0;
        final int strikes = maxStrikes;
        if (targetPlayer != null && Math.random() > finalChance && strikes > 1) {
            
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    addSpellTarget(targetPlayer, hero);
                    targetPlayer.getWorld().strikeLightningEffect(targetPlayer.getLocation());
                    damageEntity(targetPlayer, player, damage, DamageCause.MAGIC);
                    //player.getWorld().strikeLightning(targetPlayer.getLocation());
                    Messaging.send(player, "Twice Struck!");
                    if (Math.random() > finalChance && strikes > 2) {
                        
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                addSpellTarget(targetPlayer, hero);
                                targetPlayer.getWorld().strikeLightningEffect(targetPlayer.getLocation());
                                damageEntity(targetPlayer, player, damage, DamageCause.MAGIC);
                                Messaging.send(player, "Hat Trick!");
                                if (Math.random() > finalChance && strikes > 3) {
                                    
                                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                        @Override
                                        public void run() {
                                            addSpellTarget(targetPlayer, hero);
                                            targetPlayer.getWorld().strikeLightningEffect(targetPlayer.getLocation());
                                            damageEntity(targetPlayer, player, damage, DamageCause.MAGIC);
                                            Messaging.send(player, "Overkill!");
                                        }
                                    }, 20L);
                                }
                            }
                        }, 20L);
                    }
                }
            }, 20L);
        }
        return SkillResult.NORMAL;
    }

    
    

}