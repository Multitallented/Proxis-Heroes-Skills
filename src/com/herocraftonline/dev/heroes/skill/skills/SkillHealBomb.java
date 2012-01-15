package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.LivingEntity;
import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.skill.TargettedSkill;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class SkillHealBomb extends TargettedSkill {

    public SkillHealBomb(Heroes plugin) {
        super(plugin, "HealBomb");
        setDescription("Heal the target for $1 and deal $2 damage players $3 blocks around them. R:$4");
        setUsage("/skill healbomb [target]");
        setArgumentRange(0, 1);
        setIdentifiers("skill healbomb");
        setTypes(SkillType.HEAL, SkillType.DAMAGING, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        int health = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH.node(), 10, false) +
                (SkillConfigManager.getUseSetting(hero, this, "health-increase", 0.0, false) * hero.getLevel()));
        health = health > 0 ? health : 0;
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE.node(), 5, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getLevel()));
        damage = damage > 0 ? damage : 0;
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 6, false) +
                (SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS_INCREASE.node(), 0.0, false) * hero.getLevel()));
        radius = radius > 0 ? radius : 0;
        int distance = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.MAX_DISTANCE.node(), 15, false) +
                (SkillConfigManager.getUseSetting(hero, this, Setting.MAX_DISTANCE_INCREASE.node(), 0.0, false) * hero.getLevel()));
        distance = distance > 0 ? distance : 0;
        String description = getDescription().replace("$1", health + "").replace("$2", damage + "").replace("$3", radius + "").replace("$4", distance + "");
        
        //COOLDOWN
        int cooldown = (SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN_REDUCE.node(), 0, false) * hero.getLevel()) / 1000;
        if (cooldown > 0) {
            description += " CD:" + cooldown + "s";
        }
        
        //MANA
        int mana = SkillConfigManager.getUseSetting(hero, this, Setting.MANA.node(), 10, false)
                - (SkillConfigManager.getUseSetting(hero, this, Setting.MANA_REDUCE.node(), 0, false) * hero.getLevel());
        if (mana > 0) {
            description += " M:" + mana;
        }
        
        //HEALTH_COST
        int healthCost = SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST, 0, false) - 
                (SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST_REDUCE, mana, true) * hero.getLevel());
        if (healthCost > 0) {
            description += " HP:" + healthCost;
        }
        
        //STAMINA
        int staminaCost = SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA.node(), 0, false)
                - (SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA_REDUCE.node(), 0, false) * hero.getLevel());
        if (staminaCost > 0) {
            description += " FP:" + staminaCost;
        }
        
        //DELAY
        int delay = SkillConfigManager.getUseSetting(hero, this, Setting.DELAY.node(), 0, false) / 1000;
        if (delay > 0) {
            description += " W:" + delay + "s";
        }
        
        //EXP
        int exp = SkillConfigManager.getUseSetting(hero, this, Setting.EXP.node(), 0, false);
        if (exp > 0) {
            description += " XP:" + exp;
        }
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(Setting.HEALTH.node(), 10);
        node.set("health-increase", 0);
        node.set(Setting.DAMAGE.node(), 5);
        node.set("damage-increase", 0);
        node.set(Setting.RADIUS.node(), 6);
        node.set(Setting.RADIUS_INCREASE.node(), 0);
        node.set(Setting.MAX_DISTANCE.node(), 15);
        node.set(Setting.MAX_DISTANCE_INCREASE.node(), 0);
        node.set("exp-per-damaged-enemy", 0);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, LivingEntity le, String[] strings) {
        Player player = hero.getPlayer();
        if (le.equals(player)) {
            broadcastExecuteText(hero, player);
            healBomb(hero, hero);
        } else if (le instanceof Player) {
            Hero tHero = plugin.getHeroManager().getHero((Player) le);
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
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 6, false) +
                (SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS_INCREASE.node(), 0.0, false) * hero.getLevel()));
        radius = radius > 0 ? radius : 0;
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE.node(), 5, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getLevel()));
        damage = damage > 0 ? damage : 0;
        double health = (SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH.node(), 10, false) +
                (SkillConfigManager.getUseSetting(hero, this, "health-increase", 0.0, false) * hero.getLevel()));
        health = health > 0 ? health : 0;
        health = health + target.getHealth() > target.getMaxHealth() ? target.getMaxHealth() : health + target.getHealth();
        target.setHealth(health);
        target.syncHealth();
        double exp = SkillConfigManager.getUseSetting(hero, this, "exp-per-damaged-enemy", 0, false);
        double totalExp = 0;
        for (Entity e : tPlayer.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof LivingEntity) {
                if (e instanceof Creature) {
                    Creature c = (Creature) e;
                    c.damage(damage, player);
                    totalExp += exp;
                } else if (e instanceof Player) {
                    Player p = (Player) e;
                    if (hero.getParty() == null || !hero.getParty().getMembers().contains(plugin.getHeroManager().getHero(p))) {
                        p.damage(damage, player);
                    }
                    totalExp += exp;
                }
            }
        }
        if (totalExp > 0) {
            if (hero.hasParty()) {
                hero.getParty().gainExp(totalExp, ExperienceType.SKILL, player.getLocation());
            } else {
                hero.gainExp(totalExp, ExperienceType.SKILL);
            }
        }
    }

}