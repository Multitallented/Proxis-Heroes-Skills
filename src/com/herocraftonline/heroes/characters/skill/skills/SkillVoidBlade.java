package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass.ExperienceType;
import com.herocraftonline.heroes.characters.effects.common.SilenceEffect;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillVoidBlade extends PassiveSkill {
        private Skill voidblade;
    
    public SkillVoidBlade(Heroes plugin) {
        super(plugin, "VoidBlade");
        setDescription("Passive $1% chance to silence for $2s on attack.");
        setTypes(SkillType.COUNTER, SkillType.BUFF);
        Bukkit.getServer().getPluginManager().registerEvents(new SkillHeroListener(this), plugin);
        //registerEvent(Type.CUSTOM_EVENT, new SkillHeroListener(this), Priority.Normal);
    }
    
    @Override
    public String getDescription(Hero hero) {
        int level = hero.getSkillLevel(this);
        double chance = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE.node(), 0.2, false) + 
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE_LEVEL.node(), 0.2, false) * level)) * 100;
        chance = chance > 0 ? chance : 0;
        double duration = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 2000, false) + 
                (level * SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0, false))) / 1000;
        duration = duration > 0 ? duration : 0;
        String description = getDescription().replace("$1", chance + "").replace("$2", duration + "");
        
        //COOLDOWN
        int cooldown = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN_REDUCE.node(), 0, false) * hero.getSkillLevel(this)) / 1000;
        if (cooldown > 0) {
            description += " CD:" + cooldown + "s";
        }
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(SkillSetting.CHANCE.node(), 0.2);
        node.set(SkillSetting.CHANCE_LEVEL.node(), 0.0);
        node.set(SkillSetting.COOLDOWN.node(), 500);
        node.set(SkillSetting.DURATION.node(), 2000);
        node.set("duration-increase", 0);
        node.set("exp-per-silence", 0);
        return node;
    }
    
    @Override
    public void init() {
        super.init();        
        voidblade = this;
    }
    
    public class SkillHeroListener implements Listener {
        private final Skill skill;
        public SkillHeroListener(Skill skill) {
            this.skill = skill;
        }
        
        @EventHandler
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled() || event.getDamage() == 0 || event.getCause() != DamageCause.ENTITY_ATTACK || !(event.getEntity() instanceof Player) || !(event instanceof EntityDamageByEntityEvent))
                return;
            EntityDamageByEntityEvent edby = (EntityDamageByEntityEvent) event;
            Player tPlayer = (Player) event.getEntity();
            if (edby.getDamager() instanceof Player) {
                Player player = (Player) edby.getDamager();
                Hero hero = plugin.getCharacterManager().getHero(player);
                
                if (hero.hasEffect("VoidBlade")) {
                    if (hero.getCooldown("VoidBlade") == null || hero.getCooldown("VoidBlade") <= System.currentTimeMillis()) {
                        double chance = (double) SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE.node(), 0.2, false) + 
                                (hero.getSkillLevel(skill) * SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE_LEVEL.node(), 0.0, false));
                        chance = chance > 0 ? chance : 0;
                        long cooldown = (long) (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN.node(), 0, false)
                                - SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN_REDUCE.node(), 0, false) * hero.getSkillLevel(skill));
                        hero.setCooldown("VoidBlade", cooldown + System.currentTimeMillis());
                        if (Math.random() <= chance) {
                            long duration = (long) (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION.node(), 2000, false) 
                                    + (hero.getSkillLevel(skill) * SkillConfigManager.getUseSetting(hero, skill, "duration-increase", 0, false)));
                            duration = duration > 0 ? duration : 0;
                            plugin.getCharacterManager().getHero(tPlayer).addEffect(new SilenceEffect(voidblade, duration));
                            double exp = SkillConfigManager.getUseSetting(hero, skill, "exp-per-silence", 0, false);
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
            } else if (edby.getDamager() instanceof Projectile) {
                if (((Projectile) edby.getDamager()).getShooter() instanceof Player) {
                    Player player = (Player) ((Projectile) edby.getDamager()).getShooter();
                    Hero hero = plugin.getCharacterManager().getHero(player);

                    if (hero.hasEffect("VoidBlade")) {
                        if (hero.getCooldown("VoidBlade") == null || hero.getCooldown("VoidBlade") <= System.currentTimeMillis()) {
                            double chance = (double) SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE.node(), 0.2, false) + 
                                    (hero.getSkillLevel(skill) * SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE_LEVEL.node(), 0.0, false));
                            chance = chance > 0 ? chance : 0;
                            long cooldown = (long) (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN.node(), 0, false)
                                    - SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN_REDUCE.node(), 0, false) * hero.getSkillLevel(skill));
                            cooldown = cooldown > 0 ? cooldown : 0;
                            hero.setCooldown("VoidBlade", cooldown + System.currentTimeMillis());
                            if (Math.random() <= chance) {
                                long duration = (long) (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.DURATION.node(), 2000, false) 
                                        + (hero.getSkillLevel(skill) * SkillConfigManager.getUseSetting(hero, skill, "duration-increase", 0, false)));
                                duration = duration > 0 ? duration : 0;
                                plugin.getCharacterManager().getHero(tPlayer).addEffect(new SilenceEffect(voidblade, duration));
                                double exp = SkillConfigManager.getUseSetting(hero, skill, "exp-per-silence", 0, false);
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
}