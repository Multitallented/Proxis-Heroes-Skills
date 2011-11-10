package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.PassiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.util.Vector;

public class SkillSpines extends PassiveSkill {

    public SkillSpines(Heroes plugin) {
        super(plugin, "Spines");
        setDescription("Passive chance to shoot arrows when damaged");
        setTypes(SkillType.COUNTER, SkillType.HARMFUL);
        
        registerEvent(Type.ENTITY_DAMAGE, new SkillHeroListener(), Priority.Normal);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("chance-to-shoot-arrows", .1);
        node.setProperty(Setting.COOLDOWN.node(), 500);
        return node;
    }
    
    public class SkillHeroListener extends EntityListener {
        @Override
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled() || event.getDamage() == 0 || event.getCause() != DamageCause.ENTITY_ATTACK || !(event.getEntity() instanceof Player))
                return;
            Player player = (Player) event.getEntity();
            Hero hero = getPlugin().getHeroManager().getHero(player);
            if (hero.hasEffect("Spines")) {
                if (hero.getCooldown("Spines") == null || hero.getCooldown("Spines") <= System.currentTimeMillis()) {
                    double chance = (double) getSetting(hero, "chance-to-shoot-arrows", .1, false);
                    long cooldown = getSetting(hero, Setting.COOLDOWN.node(), 500, false);
                    hero.setCooldown("Spines", cooldown + System.currentTimeMillis());
                    if (Math.random() <= chance) {
                        double diff = 2 * Math.PI / 12;
                        for (double a = 0; a < 2 * Math.PI; a += diff) {
                            Vector vel = new Vector(Math.cos(a), 0, Math.sin(a));
                            player.shootArrow().setVelocity(vel);
                        }
                    }
                }
            }
        }
    }
}