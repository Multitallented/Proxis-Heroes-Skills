package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.skill.TargettedSkill;
import com.herocraftonline.dev.heroes.util.Messaging;

public class SkillInversion extends TargettedSkill {

    public SkillInversion(Heroes plugin) {
        super(plugin, "Inversion");
        setDescription("Deals direct damage to the target equal to the mana they are missing");
        setUsage("/skill inversion [target]");
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill inversion" });
        
        setTypes(SkillType.MANA, SkillType.DAMAGING, SkillType.SILENCABLE);
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
        if (!(target instanceof Player)) {
            return false;
        }
        Player player = hero.getPlayer();
        Hero enemy = getPlugin().getHeroManager().getHero((Player) target);
        if (target == player) {
            Messaging.send(player, "You need a target!");
            return false;
        }
        int maxDamage = (int) getSetting(hero, "max-damage", 10, false);
        double damageModifier = (double) getSetting(hero, "damage-modifier", 1, false);
        int damage = (int) Math.round((100-hero.getMana())*enemy.getMaxHealth()*damageModifier);
        if (maxDamage != 0 && damage > maxDamage) {
            damage = maxDamage;
        }
        if (damageCheck(player, target))
            return false;
        target.damage(damage, player);
        broadcastExecuteText(hero, target);
        return true;
    }

}