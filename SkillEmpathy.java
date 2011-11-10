package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.skill.TargettedSkill;
import com.herocraftonline.dev.heroes.util.Messaging;

public class SkillEmpathy extends TargettedSkill {

    public SkillEmpathy(Heroes plugin) {
        super(plugin, "Empathy");
        setDescription("Deals direct damage to the target equal to the health you are missing");
        setUsage("/skill empathy [target]");
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill empathy" });
        
        setTypes(SkillType.DARK, SkillType.DAMAGING, SkillType.SILENCABLE);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("max-damage", 0);
        node.setProperty("damage-modifier", 1);
        return node;
    }

    @Override
    public boolean use(Hero hero, LivingEntity target, String[] args) {
        Player player = hero.getPlayer();
        if (target == player) {
            Messaging.send(player, "You need a target!");
            return false;
        }
        int maxDamage = (int) getSetting(hero, "max-damage", 10, false);
        double damageModifier = (double) getSetting(hero, "damage-modifier", 1, false);
        if (damageModifier >= 0) {
            damageModifier = .75;
        }
        int damage = (int) (hero.getMaxHealth() - hero.getHealth());
        if (maxDamage != 0 && damage > maxDamage) {
            damage = maxDamage;
        }
        if (target instanceof Player && damageCheck((Player) target, player))
            return false;
        damage = (int) Math.round(damage * damageModifier);
        target.damage(damage, player);
        broadcastExecuteText(hero, target);
        return true;
    }

}