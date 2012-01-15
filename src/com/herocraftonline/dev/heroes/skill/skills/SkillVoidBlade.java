package com.herocraftonline.dev.heroes.skill.skills;


import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.HeroesEventListener;
import com.herocraftonline.dev.heroes.api.WeaponDamageEvent;
import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
import com.herocraftonline.dev.heroes.effects.common.SilenceEffect;
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

public class SkillVoidBlade extends PassiveSkill {
        private Skill voidblade;
    
    public SkillVoidBlade(Heroes plugin) {
        super(plugin, "VoidBlade");
        setDescription("Passive $1% chance to silence for $2s on attack.");
        setTypes(SkillType.COUNTER, SkillType.BUFF);
        
        registerEvent(Type.CUSTOM_EVENT, new SkillHeroListener(this), Priority.Normal);
    }
    
    @Override
    public String getDescription(Hero hero) {
        int level = hero.getLevel();
        double chance = (SkillConfigManager.getUseSetting(hero, this, Setting.CHANCE.node(), 0.2, false) + 
                (SkillConfigManager.getUseSetting(hero, this, Setting.CHANCE_LEVEL.node(), 0.2, false) * level)) * 100;
        chance = chance > 0 ? chance : 0;
        double duration = (SkillConfigManager.getUseSetting(hero, this, Setting.DURATION.node(), 2000, false) + 
                (level * SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0, false))) / 1000;
        duration = duration > 0 ? duration : 0;
        String description = getDescription().replace("$1", chance + "").replace("$2", duration + "");
        
        //COOLDOWN
        int cooldown = (SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN_REDUCE.node(), 0, false) * hero.getLevel()) / 1000;
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
        node.set("exp-per-silence", 0);
        return node;
    }
    
    @Override
    public void init() {
        super.init();        
        voidblade = this;
    }
    
    public class SkillHeroListener extends HeroesEventListener {
        private final Skill skill;
        public SkillHeroListener(Skill skill) {
            this.skill = skill;
        }
        
        @Override
        public void onWeaponDamage(WeaponDamageEvent event) {
            if (event.isCancelled() || event.getDamage() == 0 || event.getCause() != DamageCause.ENTITY_ATTACK || !(event.getEntity() instanceof Player))
                return;
            Player tPlayer = (Player) event.getEntity();
            if (event.getDamager() instanceof Player) {
                Player player = (Player) event.getDamager();
                Hero hero = plugin.getHeroManager().getHero(player);
                
                if (hero.hasEffect("VoidBlade")) {
                    if (hero.getCooldown("VoidBlade") == null || hero.getCooldown("VoidBlade") <= System.currentTimeMillis()) {
                        double chance = (double) SkillConfigManager.getUseSetting(hero, skill, Setting.CHANCE.node(), 0.2, false) + 
                                (hero.getLevel() * SkillConfigManager.getUseSetting(hero, skill, Setting.CHANCE_LEVEL.node(), 0.0, false));
                        chance = chance > 0 ? chance : 0;
                        long cooldown = (long) (SkillConfigManager.getUseSetting(hero, skill, Setting.COOLDOWN.node(), 0, false)
                                - SkillConfigManager.getUseSetting(hero, skill, Setting.COOLDOWN_REDUCE.node(), 0, false) * hero.getLevel());
                        hero.setCooldown("VoidBlade", cooldown + System.currentTimeMillis());
                        if (Math.random() <= chance) {
                            long duration = (long) (SkillConfigManager.getUseSetting(hero, skill, Setting.DURATION.node(), 2000, false) 
                                    + (hero.getLevel() * SkillConfigManager.getUseSetting(hero, skill, "duration-increase", 0, false)));
                            duration = duration > 0 ? duration : 0;
                            plugin.getHeroManager().getHero(tPlayer).addEffect(new SilenceEffect(voidblade, duration));
                            double exp = SkillConfigManager.getUseSetting(hero, skill, "exp-per-silence", 0, false);
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

                    if (hero.hasEffect("VoidBlade")) {
                        if (hero.getCooldown("VoidBlade") == null || hero.getCooldown("VoidBlade") <= System.currentTimeMillis()) {
                            double chance = (double) SkillConfigManager.getUseSetting(hero, skill, Setting.CHANCE.node(), 0.2, false) + 
                                    (hero.getLevel() * SkillConfigManager.getUseSetting(hero, skill, Setting.CHANCE_LEVEL.node(), 0.0, false));
                            chance = chance > 0 ? chance : 0;
                            long cooldown = (long) (SkillConfigManager.getUseSetting(hero, skill, Setting.COOLDOWN.node(), 0, false)
                                    - SkillConfigManager.getUseSetting(hero, skill, Setting.COOLDOWN_REDUCE.node(), 0, false) * hero.getLevel());
                            cooldown = cooldown > 0 ? cooldown : 0;
                            hero.setCooldown("VoidBlade", cooldown + System.currentTimeMillis());
                            if (Math.random() <= chance) {
                                long duration = (long) (SkillConfigManager.getUseSetting(hero, skill, Setting.DURATION.node(), 2000, false) 
                                        + (hero.getLevel() * SkillConfigManager.getUseSetting(hero, skill, "duration-increase", 0, false)));
                                duration = duration > 0 ? duration : 0;
                                plugin.getHeroManager().getHero(tPlayer).addEffect(new SilenceEffect(voidblade, duration));
                                double exp = SkillConfigManager.getUseSetting(hero, skill, "exp-per-silence", 0, false);
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