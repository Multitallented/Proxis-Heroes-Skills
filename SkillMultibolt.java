package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.Location;

public class SkillMultibolt extends ActiveSkill {

    public SkillMultibolt(Heroes plugin) {
        super(plugin, "Multibolt");
        setDescription("Strikes a person repeatedly with lightning");
        setUsage("/skill multibolt");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill multibolt"});

        setTypes(SkillType.LIGHTNING, SkillType.DAMAGING, SkillType.SILENCABLE);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("range", 40);
        node.setProperty("max_strikes", 4);
        node.setProperty("multiplier_chance", 50);
        node.setProperty(Setting.DAMAGE.node(), 14);
        return node;
    }

    @Override
    public SkillResult use(final Hero hero, String[] args) {
        final Player player = hero.getPlayer();
        final Location wLocation = player.getTargetBlock(null, (int) getSetting(hero, "range", 40, false))
                        .getLocation();
        Player aTargetPlayer = null;
        for (int i=0; i< player.getWorld().getPlayers().size(); i++) {
            if (Math.abs(player.getWorld().getPlayers().get(i).getLocation().getBlockX() - wLocation.getBlockX()) <= 1 &&
                Math.abs(player.getWorld().getPlayers().get(i).getLocation().getBlockZ() - wLocation.getBlockZ()) <= 1) {
                aTargetPlayer = player.getWorld().getPlayers().get(i);
            }
        }
        if (aTargetPlayer != null && !damageCheck(player, aTargetPlayer)) {
            Messaging.send(player, "You can't harm that target");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        final Player targetPlayer = aTargetPlayer;
        broadcastExecuteText(hero);
        final int damage = (int) getSetting(hero, Setting.DAMAGE.node(), 14, false);
        if (aTargetPlayer != null) {
            addSpellTarget(aTargetPlayer, hero);
            aTargetPlayer.getWorld().strikeLightningEffect(aTargetPlayer.getLocation());
            aTargetPlayer.damage(damage, player);
        } else {
            player.getWorld().strikeLightning(wLocation);
        }
        if (targetPlayer != null &&
            Math.random() > ((double) getSetting(hero, "multiplier_chance", 50, false))/100 && 
            (int) getSetting(hero, "max_strikes", 4, false) > 1) {
            
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    addSpellTarget(targetPlayer, hero);
                    targetPlayer.getWorld().strikeLightningEffect(targetPlayer.getLocation());
                    targetPlayer.damage(damage, player);
                    //player.getWorld().strikeLightning(targetPlayer.getLocation());
                    Messaging.send(player, "Twice Struck!");
                    if (Math.random() > ((double) getSetting(hero, "multiplier_chance", 50, false))/100 &&
                        (int) getSetting(hero, "max_strikes", 4, false) > 2) {
                        
                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                addSpellTarget(targetPlayer, hero);
                                targetPlayer.getWorld().strikeLightningEffect(targetPlayer.getLocation());
                                targetPlayer.damage(damage, player);
                                Messaging.send(player, "Hat Trick!");
                                if (Math.random() > ((double) getSetting(hero, "multiplier_chance", 50, false))/100 && 
                                    (int) getSetting(hero, "max_strikes", 4, false) > 3) {
                                    
                                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                        @Override
                                        public void run() {
                                            addSpellTarget(targetPlayer, hero);
                                            targetPlayer.getWorld().strikeLightningEffect(targetPlayer.getLocation());
                                            targetPlayer.damage(damage, player);
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