package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass.ExperienceType;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillLifesteal extends PassiveSkill {

    public SkillLifesteal(Heroes plugin) {
        super(plugin, "Lifesteal");
        setDescription("Passive health gain on non-skill damage");
        setTypes(SkillType.COUNTER, SkillType.BUFF);
        Bukkit.getServer().getPluginManager().registerEvents(new SkillHeroListener(this), plugin);
        //registerEvent(Type.CUSTOM_EVENT, new SkillHeroListener(this), Priority.Normal);
    }

    @Override
    public String getDescription(Hero hero) {
        long cooldown = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN.node(), 500, false) -
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN_REDUCE.node(), 0.0, false) * hero.getSkillLevel(this))) / 1000;
        cooldown = cooldown > 0 ? cooldown : 0;
        int health = (int) (SkillConfigManager.getUseSetting(hero, this, "health-per-attack", 1, false) +
                (SkillConfigManager.getUseSetting(hero, this, "health-increase", 0.0, false) * hero.getSkillLevel(this)));
        health = health > 0 ? health : 0;
        String description = getDescription().replace("$1", cooldown + "").replace("$2", health + "");
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set("health-per-attack", 1);
        node.set("health-increase", 0);
        node.set(SkillSetting.COOLDOWN.node(), 500);
        node.set(SkillSetting.COOLDOWN_REDUCE.node(), 0);
        node.set("exp-per-heal", 0);
        return node;
    }
    
    public class SkillHeroListener implements Listener {
        private Skill skill;
        public SkillHeroListener(Skill skill) {
            this.skill = skill;
        }
        @EventHandler
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.getCause() != DamageCause.ENTITY_ATTACK || event.isCancelled() || event.getDamage() == 0 || !(event.getEntity() instanceof Player) || !(event instanceof EntityDamageEvent))
                return;
            EntityDamageByEntityEvent edby = (EntityDamageByEntityEvent) event;
            if (edby.getDamager() instanceof Player) {
                Player player = (Player) edby.getDamager();
                Hero hero = plugin.getCharacterManager().getHero(player);

                if (hero.hasEffect("Lifesteal")) {
                    if (hero.getCooldown("Lifesteal") == null || hero.getCooldown("Lifesteal") <= System.currentTimeMillis()) {
                        int health = (int) (SkillConfigManager.getUseSetting(hero, skill, "health-per-attack", 1, false) +
                                (SkillConfigManager.getUseSetting(hero, skill, "health-increase", 0.0, false) * hero.getSkillLevel(skill)));
                        health = health > 0 ? health : 0;
                        long cooldown = (long) (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN.node(), 500, false) -
                                (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN_REDUCE.node(), 0.0, false) * hero.getSkillLevel(skill)));
                        cooldown = cooldown > 0 ? cooldown : 0;
                        hero.setCooldown("Lifesteal", cooldown + System.currentTimeMillis());
                        if (player.getHealth() + health >= player.getMaxHealth()) {
                            player.setHealth(player.getMaxHealth());
                        } else {
                            player.setHealth(health + player.getHealth());
                        }
                        double exp = SkillConfigManager.getUseSetting(hero, skill, "exp-per-heal", 0, false);
                        if (exp > 0) {
                            if (hero.hasParty()) {
                                hero.getParty().gainExp(exp, ExperienceType.SKILL, player.getLocation());
                            } else {
                                hero.gainExp(exp, ExperienceType.SKILL, player.getLocation());
                            }
                        }
                        return;
                    }
                }
            } else if (edby.getDamager() instanceof Projectile) {
                if (((Projectile) edby.getDamager()).getShooter() instanceof Player) {
                    Player player = (Player) ((Projectile) edby.getDamager()).getShooter();
                    Hero hero = plugin.getCharacterManager().getHero(player);

                    if (hero.hasEffect("Lifesteal")) {
                        if (hero.getCooldown("Lifesteal") == null || hero.getCooldown("Lifesteal") <= System.currentTimeMillis()) {
                            int health = (int) (SkillConfigManager.getUseSetting(hero, skill, "health-per-attack", 1, false) +
                                    (SkillConfigManager.getUseSetting(hero, skill, "health-increase", 0.0, false) * hero.getSkillLevel(skill)));
                            health = health > 0 ? health : 0;
                            long cooldown = (long) (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN.node(), 500, false) -
                                    (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN_REDUCE.node(), 0.0, false) * hero.getSkillLevel(skill)));
                            cooldown = cooldown > 0 ? cooldown : 0;
                            hero.setCooldown("Lifesteal", cooldown + System.currentTimeMillis());
                            if (player.getHealth() + health >= player.getMaxHealth()) {
                                player.setHealth(player.getMaxHealth());
                            } else {
                                player.setHealth(health + player.getHealth());
                            }
                            
                            double exp = SkillConfigManager.getUseSetting(hero, skill, "exp-per-heal", 0, false);
                            if (exp > 0) {
                                if (hero.hasParty()) {
                                    hero.getParty().gainExp(exp, ExperienceType.SKILL, player.getLocation());
                                } else {
                                    hero.gainExp(exp, ExperienceType.SKILL, player.getLocation());
                                }
                            }
                            return;
                        }
                    }
                }
            }
        }
    }
}