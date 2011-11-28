package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.HeroesEventListener;
import com.herocraftonline.dev.heroes.api.WeaponDamageEvent;
import com.herocraftonline.dev.heroes.effects.common.StunEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.PassiveSkill;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillBasher extends PassiveSkill {
        private Skill basher;
    
    public SkillBasher(Heroes plugin) {
        super(plugin, "Basher");
        setDescription("Passive chance to stun on non-skill damage");
        setTypes(SkillType.COUNTER, SkillType.BUFF);
        
        registerEvent(Type.CUSTOM_EVENT, new SkillHeroListener(), Priority.Normal);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("chance-to-stun", 0.2);
        node.setProperty(Setting.COOLDOWN.node(), 500);
        node.setProperty(Setting.DURATION.node(), 2000);
        return node;
    }
    
    @Override
    public void init() {
        super.init();        
        basher = this;
    }
    
    public class SkillHeroListener extends HeroesEventListener {
        @Override
        public void onWeaponDamage(WeaponDamageEvent event) {
            if (event.isCancelled() || event.getDamage() == 0 || event.getCause() != DamageCause.ENTITY_ATTACK || !(event.getEntity() instanceof Player))
                return;
            Player tPlayer = (Player) event.getEntity();
            if (event.getDamager() instanceof Player) {
                Player player = (Player) event.getDamager();
                Hero hero = plugin.getHeroManager().getHero(player);
                
                if (hero.hasEffect("Basher")) {
                    if (hero.getCooldown("Basher") == null || hero.getCooldown("Basher") <= System.currentTimeMillis()) {
                        double chance = (double) getSetting(hero, "chance-to-stun", 0.2, false);
                        long cooldown = (long) getSetting(hero, Setting.COOLDOWN.node(), 500, false);
                        hero.setCooldown("Basher", cooldown + System.currentTimeMillis());
                        if (Math.random() <= chance) {
                            long duration = (long) getSetting(hero, Setting.DURATION.node(), 2000, false);
                            getPlugin().getHeroManager().getHero(tPlayer).addEffect(new StunEffect(basher, duration));
                        }
                    }
                }
            } else if (event.getDamager() instanceof Projectile) {
                if (((Projectile) event.getDamager()).getShooter() instanceof Player) {
                    Player player = (Player) ((Projectile) event.getDamager()).getShooter();
                    Hero hero = plugin.getHeroManager().getHero(player);

                    if (hero.hasEffect("Basher")) {
                        if (hero.getCooldown("Basher") == null || hero.getCooldown("Basher") <= System.currentTimeMillis()) {
                            double chance = (double) getSetting(hero, "chance-to-stun", 0.2, false);
                            long cooldown = (long) getSetting(hero, Setting.COOLDOWN.node(), 500, false);
                            hero.setCooldown("Basher", cooldown + System.currentTimeMillis());
                            if (Math.random() <= chance) {
                                long duration = (long) getSetting(hero, Setting.DURATION.node(), 2000, false);
                                getPlugin().getHeroManager().getHero(tPlayer).addEffect(new StunEffect(basher, duration));
                            }
                        }
                    }
                }
            }
        }
    }
}