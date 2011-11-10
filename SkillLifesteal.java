package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.HeroesEventListener;
import com.herocraftonline.dev.heroes.api.WeaponDamageEvent;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.PassiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillLifesteal extends PassiveSkill {

    public SkillLifesteal(Heroes plugin) {
        super(plugin, "Lifesteal");
        setDescription("Passive health gain on non-skill damage");
        setTypes(SkillType.COUNTER, SkillType.BUFF);
        
        registerEvent(Type.CUSTOM_EVENT, new SkillHeroListener(), Priority.Normal);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("health-per-attack", 1.0);
        node.setProperty(Setting.COOLDOWN.node(), 500);
        return node;
    }
    
    public class SkillHeroListener extends HeroesEventListener {
        @Override
        public void onWeaponDamage(WeaponDamageEvent event) {
            if (event.getCause() != DamageCause.ENTITY_ATTACK || event.isCancelled() || event.getDamage() == 0 || !(event.getEntity() instanceof Player))
                return;
            if (event.getDamager() instanceof Player) {
                Player player = (Player) event.getDamager();
                Hero hero = plugin.getHeroManager().getHero(player);

                if (hero.hasEffect("Lifesteal")) {
                    if (hero.getCooldown("Lifesteal") == null || hero.getCooldown("Lifesteal") <= System.currentTimeMillis()) {
                        double stealAmount = (double) getSetting(hero, "health-per-attack", 1, false);
                        long cooldown = getSetting(hero, Setting.COOLDOWN.node(), 500, false);
                        hero.setCooldown("Lifesteal", cooldown + System.currentTimeMillis());
                        if (hero.getHealth() + stealAmount >= hero.getMaxHealth()) {
                            hero.setHealth(100.0);
                        } else {
                            hero.setHealth(stealAmount + hero.getHealth());
                        }
                        hero.syncHealth();
                    }
                }
            } else if (event.getDamager() instanceof Projectile) {
                if (((Projectile) event.getDamager()).getShooter() instanceof Player) {
                    Player player = (Player) ((Projectile) event.getDamager()).getShooter();
                    Hero hero = plugin.getHeroManager().getHero(player);

                    if (hero.hasEffect("Lifesteal")) {
                        if (hero.getCooldown("Lifesteal") == null || hero.getCooldown("Lifesteal") <= System.currentTimeMillis()) {
                            double stealAmount = (double) getSetting(hero, "health-per-attack", 1, false);
                            long cooldown = getSetting(hero, Setting.COOLDOWN.node(), 500, false);
                            hero.setCooldown("Lifesteal", cooldown + System.currentTimeMillis());
                            if (hero.getHealth() + stealAmount >= hero.getMaxHealth()) {
                                hero.setHealth(100.0);
                            } else {
                                hero.setHealth(stealAmount + hero.getHealth());
                            }
                            hero.syncHealth();
                        }
                    }
                }
            }
        }
    }
}