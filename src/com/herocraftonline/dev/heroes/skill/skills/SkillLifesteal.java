package com.herocraftonline.dev.heroes.skill.skills;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.HeroesEventListener;
import com.herocraftonline.dev.heroes.api.WeaponDamageEvent;
import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.PassiveSkill;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.configuration.ConfigurationSection;
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
        
        registerEvent(Type.CUSTOM_EVENT, new SkillHeroListener(this), Priority.Normal);
    }

    @Override
    public String getDescription(Hero hero) {
        long cooldown = (long) (SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN.node(), 500, false) -
                (SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN_REDUCE.node(), 0.0, false) * hero.getLevel())) / 1000;
        cooldown = cooldown > 0 ? cooldown : 0;
        int health = (int) (SkillConfigManager.getUseSetting(hero, this, "health-per-attack", 1, false) +
                (SkillConfigManager.getUseSetting(hero, this, "health-increase", 0.0, false) * hero.getLevel()));
        health = health > 0 ? health : 0;
        String description = getDescription().replace("$1", cooldown + "").replace("$2", health + "");
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set("health-per-attack", 1);
        node.set("health-increase", 0);
        node.set(Setting.COOLDOWN.node(), 500);
        node.set(Setting.COOLDOWN_REDUCE.node(), 0);
        node.set("exp-per-heal", 0);
        return node;
    }
    
    public class SkillHeroListener extends HeroesEventListener {
        private Skill skill;
        public SkillHeroListener(Skill skill) {
            this.skill = skill;
        }
        @Override
        public void onWeaponDamage(WeaponDamageEvent event) {
            if (event.getCause() != DamageCause.ENTITY_ATTACK || event.isCancelled() || event.getDamage() == 0 || !(event.getEntity() instanceof Player))
                return;
            if (event.getDamager() instanceof Player) {
                Player player = (Player) event.getDamager();
                Hero hero = plugin.getHeroManager().getHero(player);

                if (hero.hasEffect("Lifesteal")) {
                    if (hero.getCooldown("Lifesteal") == null || hero.getCooldown("Lifesteal") <= System.currentTimeMillis()) {
                        int health = (int) (SkillConfigManager.getUseSetting(hero, skill, "health-per-attack", 1, false) +
                                (SkillConfigManager.getUseSetting(hero, skill, "health-increase", 0.0, false) * hero.getLevel()));
                        health = health > 0 ? health : 0;
                        long cooldown = (long) (SkillConfigManager.getUseSetting(hero, skill, Setting.COOLDOWN.node(), 500, false) -
                                (SkillConfigManager.getUseSetting(hero, skill, Setting.COOLDOWN_REDUCE.node(), 0.0, false) * hero.getLevel()));
                        cooldown = cooldown > 0 ? cooldown : 0;
                        hero.setCooldown("Lifesteal", cooldown + System.currentTimeMillis());
                        if (hero.getHealth() + health >= hero.getMaxHealth()) {
                            hero.setHealth(100.0);
                        } else {
                            hero.setHealth(health + hero.getHealth());
                        }
                        hero.syncHealth();
                        double exp = SkillConfigManager.getUseSetting(hero, skill, "exp-per-heal", 0, false);
                        if (exp > 0) {
                            if (hero.hasParty()) {
                                hero.getParty().gainExp(exp, ExperienceType.SKILL, player.getLocation());
                            } else {
                                hero.gainExp(exp, ExperienceType.SKILL);
                            }
                        }
                        return;
                    }
                }
            } else if (event.getDamager() instanceof Projectile) {
                if (((Projectile) event.getDamager()).getShooter() instanceof Player) {
                    Player player = (Player) ((Projectile) event.getDamager()).getShooter();
                    Hero hero = plugin.getHeroManager().getHero(player);

                    if (hero.hasEffect("Lifesteal")) {
                        if (hero.getCooldown("Lifesteal") == null || hero.getCooldown("Lifesteal") <= System.currentTimeMillis()) {
                            int health = (int) (SkillConfigManager.getUseSetting(hero, skill, "health-per-attack", 1, false) +
                                    (SkillConfigManager.getUseSetting(hero, skill, "health-increase", 0.0, false) * hero.getLevel()));
                            health = health > 0 ? health : 0;
                            long cooldown = (long) (SkillConfigManager.getUseSetting(hero, skill, Setting.COOLDOWN.node(), 500, false) -
                                    (SkillConfigManager.getUseSetting(hero, skill, Setting.COOLDOWN_REDUCE.node(), 0.0, false) * hero.getLevel()));
                            cooldown = cooldown > 0 ? cooldown : 0;
                            hero.setCooldown("Lifesteal", cooldown + System.currentTimeMillis());
                            if (hero.getHealth() + health >= hero.getMaxHealth()) {
                                hero.setHealth(100.0);
                            } else {
                                hero.setHealth(health + hero.getHealth());
                            }
                            hero.syncHealth();
                            
                            double exp = SkillConfigManager.getUseSetting(hero, skill, "exp-per-heal", 0, false);
                            if (exp > 0) {
                                if (hero.hasParty()) {
                                    hero.getParty().gainExp(exp, ExperienceType.SKILL, player.getLocation());
                                } else {
                                    hero.gainExp(exp, ExperienceType.SKILL);
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