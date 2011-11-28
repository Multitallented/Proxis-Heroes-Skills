package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.LivingEntity;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.skill.TargettedSkill;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class SkillHealBomb extends TargettedSkill {

    public SkillHealBomb(Heroes plugin) {
        super(plugin, "HealBomb");
        setDescription("Heal the target and deal damage in an area around the target");
        setUsage("/skill healbomb <target>");
        setArgumentRange(0, 1);
        setIdentifiers("skill healbomb");
        setTypes(SkillType.HEAL, SkillType.DAMAGING, SkillType.SILENCABLE);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty(Setting.HEALTH.node(), 10);
        node.setProperty(Setting.DAMAGE.node(), 5);
        node.setProperty(Setting.RADIUS.node(), 6);
        node.setProperty(Setting.MAX_DISTANCE.node(), 15);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, LivingEntity le, String[] strings) {
        Player player = hero.getPlayer();
        if (le.equals(player)) {
            broadcastExecuteText(hero, player);
            healBomb(hero, hero);
        } else if (le instanceof Player) {
            Hero tHero = getPlugin().getHeroManager().getHero((Player) le);
            if (hero.getParty() != null && hero.getParty().getMembers().contains(tHero)) {
                broadcastExecuteText(hero, le);
                healBomb(hero, tHero);
            }
        } else {
            return SkillResult.INVALID_TARGET;
        }
        return SkillResult.NORMAL;
    }
    
    private void healBomb(Hero hero, Hero target) {
        Player player = hero.getPlayer();
        Player tPlayer = target.getPlayer();
        double radius = getSetting(hero, Setting.RADIUS.node(), 6, false);
        int damage = getSetting(hero, Setting.DAMAGE.node(), 5, false);
        double health = getSetting(hero, Setting.HEALTH.node(), 10, false);
        health = health + target.getHealth() > target.getMaxHealth() ? target.getMaxHealth() : health + target.getHealth();
        target.setHealth(health);
        target.syncHealth();
        for (Entity e : tPlayer.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof LivingEntity) {
                if (e instanceof Creature) {
                    Creature c = (Creature) e;
                    c.damage(damage, player);
                } else if (e instanceof Player) {
                    Player p = (Player) e;
                    if (hero.getParty() == null || !hero.getParty().getMembers().contains(getPlugin().getHeroManager().getHero(p))) {
                        p.damage(damage, player);
                    }
                }
            }
        }
    }

}