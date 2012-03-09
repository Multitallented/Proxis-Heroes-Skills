package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass.ExperienceType;
import com.herocraftonline.heroes.characters.effects.common.SlowEffect;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Setting;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillIcebrand extends PassiveSkill {
        private Skill icebrand;
    
    public SkillIcebrand(Heroes plugin) {
        super(plugin, "Icebrand");
        setDescription("Passive $1% chance to slow for $2s on attack.");
        setTypes(SkillType.COUNTER, SkillType.BUFF);
        Bukkit.getServer().getPluginManager().registerEvents(new SkillHeroListener(this), plugin);
        //registerEvent(Type.CUSTOM_EVENT, new SkillHeroListener(this), Priority.Normal);
    }
    
    @Override
    public String getDescription(Hero hero) {
        int level = hero.getSkillLevel(this);
        double chance = (SkillConfigManager.getUseSetting(hero, this, Setting.CHANCE.node(), 0.2, false) + 
                (SkillConfigManager.getUseSetting(hero, this, Setting.CHANCE_LEVEL.node(), 0.2, false) * level)) * 100;
        chance = chance > 0 ? chance : 0;
        double duration = (SkillConfigManager.getUseSetting(hero, this, Setting.DURATION.node(), 2000, false) + 
                (level * SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0, false))) / 1000;
        duration = duration > 0 ? duration : 0;
        String description = getDescription().replace("$1", chance + "").replace("$2", duration + "");
        
        //COOLDOWN
        int cooldown = (SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN_REDUCE.node(), 0, false) * hero.getSkillLevel(this)) / 1000;
        if (cooldown > 0) {
            description += " CD:" + cooldown + "s";
        }
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(Setting.CHANCE.node(), 0.2);
        node.set(Setting.CHANCE_LEVEL.node(), 0.0);
        node.set(Setting.COOLDOWN.node(), 500);
        node.set(Setting.DURATION.node(), 2000);
        node.set("duration-increase", 0);
        node.set("exp-per-slow", 0);
        return node;
    }
    
    @Override
    public void init() {
        super.init();        
        icebrand = this;
    }
    
    public class SkillHeroListener implements Listener {
        private final Skill skill;
        public SkillHeroListener(Skill skill) {
            this.skill = skill;
        }
        
        @EventHandler
        public void onWeaponDamage(WeaponDamageEvent event) {
            if (event.isCancelled() || event.getDamage() == 0 || event.getCause() != DamageCause.ENTITY_ATTACK || !(event.getEntity() instanceof Player))
                return;
            Player tPlayer = (Player) event.getEntity();
            if (event.getDamager() instanceof Player) {
                Player player = (Player) event.getDamager();
                Hero hero = plugin.getHeroManager().getHero(player);
                
                if (hero.hasEffect("Icebrand")) {
                    if (hero.getCooldown("Icebrand") == null || hero.getCooldown("Icebrand") <= System.currentTimeMillis()) {
                        double chance = (double) SkillConfigManager.getUseSetting(hero, skill, Setting.CHANCE.node(), 0.2, false) + 
                                (hero.getSkillLevel(skill) * SkillConfigManager.getUseSetting(hero, skill, Setting.CHANCE_LEVEL.node(), 0.0, false));
                        chance = chance > 0 ? chance : 0;
                        long cooldown = (long) (SkillConfigManager.getUseSetting(hero, skill, Setting.COOLDOWN.node(), 0, false)
                                - SkillConfigManager.getUseSetting(hero, skill, Setting.COOLDOWN_REDUCE.node(), 0, false) * hero.getSkillLevel(skill));
                        hero.setCooldown("Icebrand", cooldown + System.currentTimeMillis());
                        if (Math.random() <= chance) {
                            long duration = (long) (SkillConfigManager.getUseSetting(hero, skill, Setting.DURATION.node(), 2000, false) 
                                    + (hero.getSkillLevel(skill) * SkillConfigManager.getUseSetting(hero, skill, "duration-increase", 0, false)));
                            duration = duration > 0 ? duration : 0;
                            plugin.getHeroManager().getHero(tPlayer).addEffect(new SlowEffect(icebrand, duration, 2, false,"","",hero));
                            double exp = SkillConfigManager.getUseSetting(hero, skill, "exp-per-slow", 0, false);
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
            } else if (event.getDamager() instanceof Projectile) {
                if (((Projectile) event.getDamager()).getShooter() instanceof Player) {
                    Player player = (Player) ((Projectile) event.getDamager()).getShooter();
                    Hero hero = plugin.getHeroManager().getHero(player);

                    if (hero.hasEffect("Icebrand")) {
                        if (hero.getCooldown("Icebrand") == null || hero.getCooldown("Icebrand") <= System.currentTimeMillis()) {
                            double chance = (double) SkillConfigManager.getUseSetting(hero, skill, Setting.CHANCE.node(), 0.2, false) + 
                                    (hero.getSkillLevel(skill) * SkillConfigManager.getUseSetting(hero, skill, Setting.CHANCE_LEVEL.node(), 0.0, false));
                            chance = chance > 0 ? chance : 0;
                            long cooldown = (long) (SkillConfigManager.getUseSetting(hero, skill, Setting.COOLDOWN.node(), 0, false)
                                    - SkillConfigManager.getUseSetting(hero, skill, Setting.COOLDOWN_REDUCE.node(), 0, false) * hero.getSkillLevel(skill));
                            cooldown = cooldown > 0 ? cooldown : 0;
                            hero.setCooldown("Icebrand", cooldown + System.currentTimeMillis());
                            if (Math.random() <= chance) {
                                long duration = (long) (SkillConfigManager.getUseSetting(hero, skill, Setting.DURATION.node(), 2000, false) 
                                        + (hero.getSkillLevel(skill) * SkillConfigManager.getUseSetting(hero, skill, "duration-increase", 0, false)));
                                duration = duration > 0 ? duration : 0;
                                plugin.getHeroManager().getHero(tPlayer).addEffect(new SlowEffect(icebrand, duration, 2, false,"","",hero));
                                double exp = SkillConfigManager.getUseSetting(hero, skill, "exp-per-slow", 0, false);
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
}