package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.HeroesEventListener;
import com.herocraftonline.dev.heroes.api.WeaponDamageEvent;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.PassiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillCritical extends PassiveSkill {

    public SkillCritical(Heroes plugin) {
        super(plugin, "Critical");
        setDescription("Passive chance to do a critical hit");
        setTypes(SkillType.COUNTER, SkillType.BUFF);
        
        registerEvent(Type.CUSTOM_EVENT, new SkillHeroListener(), Priority.Normal);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("chance", 0.2);
        node.setProperty("damage-multiplier", 2.0);
        return node;
    }
    
    public class SkillHeroListener extends HeroesEventListener {
        @Override
        public void onWeaponDamage(WeaponDamageEvent event) {
            if (event.isCancelled() ||event.getCause() != DamageCause.ENTITY_ATTACK || !(event.getEntity() instanceof Player) ||
                    event.getDamage() == 0)
                return;
            if (event.getDamager() instanceof Player) {
                Player player = (Player) event.getDamager();
                Hero hero = plugin.getHeroManager().getHero(player);

                if (hero.hasEffect("Critical")) {
                    double chance = (double) getSetting(hero, "chance", 0.2, false);
                    if (Math.random() <= chance) {
                        double damageMult = getSetting(hero, "damage-multiplier", 2.0, false);
                        event.setDamage((int) (event.getDamage() * damageMult));
                    }
                }
            } else if (event.getDamager() instanceof Projectile) {
                if (((Projectile) event.getDamager()).getShooter() instanceof Player) {
                    Player player = (Player) ((Projectile) event.getDamager()).getShooter();
                    Hero hero = plugin.getHeroManager().getHero(player);

                    if (hero.hasEffect("Critical")) {
                        double chance = (double) getSetting(hero, "chance", 0.2, false);
                        if (Math.random() <= chance) {
                            double damageMult = getSetting(hero, "damage-multiplier", 2.0, false);
                            event.setDamage((int) (event.getDamage() * damageMult));
                            
                        }
                    }
                }
            }
        }
    }
}