package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass.ExperienceType;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import org.bukkit.entity.LivingEntity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

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
        int health = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH.node(), 10, false) +
                (SkillConfigManager.getUseSetting(hero, this, "health-increase", 0.0, false) * hero.getSkillLevel(this)));
        health = health > 0 ? health : 0;
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE.node(), 5, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getSkillLevel(this)));
        damage = damage > 0 ? damage : 0;
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 6, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        radius = radius > 0 ? radius : 0;
        int distance = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE.node(), 15, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        distance = distance > 0 ? distance : 0;
        String description = getDescription().replace("$1", health + "").replace("$2", damage + "").replace("$3", radius + "").replace("$4", distance + "");
        
        //COOLDOWN
        int cooldown = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN_REDUCE.node(), 0, false) * hero.getSkillLevel(this)) / 1000;
        if (cooldown > 0) {
            description += " CD:" + cooldown + "s";
        }
        
        //MANA
        int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA.node(), 10, false)
                - (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA_REDUCE.node(), 0, false) * hero.getSkillLevel(this));
        if (mana > 0) {
            description += " M:" + mana;
        }
        
        //HEALTH_COST
        int healthCost = SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_COST, 0, false) - 
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_COST_REDUCE, mana, true) * hero.getSkillLevel(this));
        if (healthCost > 0) {
            description += " HP:" + healthCost;
        }
        
        //STAMINA
        int staminaCost = SkillConfigManager.getUseSetting(hero, this, SkillSetting.STAMINA.node(), 0, false)
                - (SkillConfigManager.getUseSetting(hero, this, SkillSetting.STAMINA_REDUCE.node(), 0, false) * hero.getSkillLevel(this));
        if (staminaCost > 0) {
            description += " FP:" + staminaCost;
        }
        
        //DELAY
        int delay = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DELAY.node(), 0, false) / 1000;
        if (delay > 0) {
            description += " W:" + delay + "s";
        }
        
        //EXP
        int exp = SkillConfigManager.getUseSetting(hero, this, SkillSetting.EXP.node(), 0, false);
        if (exp > 0) {
            description += " XP:" + exp;
        }
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(SkillSetting.HEALTH.node(), 10);
        node.set("health-increase", 0);
        node.set(SkillSetting.DAMAGE.node(), 5);
        node.set("damage-increase", 0);
        node.set(SkillSetting.RADIUS.node(), 6);
        node.set(SkillSetting.RADIUS_INCREASE.node(), 0);
        node.set(SkillSetting.MAX_DISTANCE.node(), 15);
        node.set(SkillSetting.MAX_DISTANCE_INCREASE.node(), 0);
        node.set("exp-per-damaged-enemy", 0);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, LivingEntity le, String[] strings) {
        Player player = hero.getPlayer();
        if (le.equals(player)) {
            broadcastExecuteText(hero, player);
            healBomb(hero, hero);
            le.getLocation().getWorld().createExplosion(le.getLocation(), 0.0F, false);
        } else if (le instanceof Player) {
            Hero tHero = plugin.getCharacterManager().getHero((Player) le);
            if (hero.getParty() != null && hero.getParty().getMembers().contains(tHero)) {
                broadcastExecuteText(hero, le);
                healBomb(hero, tHero);
            }
            le.getLocation().getWorld().createExplosion(le.getLocation(), 0.0F, false);
        } else {
            return SkillResult.INVALID_TARGET;
        }
        return SkillResult.NORMAL;
    }
    
    private void healBomb(Hero hero, Hero target) {
        Player player = hero.getPlayer();
        Player tPlayer = target.getPlayer();
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 6, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        radius = radius > 0 ? radius : 0;
        double damage = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE.node(), 5, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getSkillLevel(this)));
        damage = damage > 0 ? damage : 0;
        double health = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH.node(), 10, false) +
                (SkillConfigManager.getUseSetting(hero, this, "health-increase", 0.0, false) * hero.getSkillLevel(this)));
        health = health > 0 ? health : 0;
        health = health + tPlayer.getHealth() > tPlayer.getMaxHealth() ? tPlayer.getMaxHealth() : health + tPlayer.getHealth();
        tPlayer.setHealth((int) health);
        double exp = SkillConfigManager.getUseSetting(hero, this, "exp-per-damaged-enemy", 0, false);
        double totalExp = 0;
        for (Entity e : tPlayer.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof LivingEntity) {
                if (e instanceof Creature) {
                    Creature c = (Creature) e;
                    addSpellTarget(c,hero);
                    damageEntity(c, player, damage, DamageCause.MAGIC);
                    //c.damage(damage, player);
                    totalExp += exp;
                } else if (e instanceof Player) {
                    Player p = (Player) e;
                    if (hero.getParty() == null || !hero.getParty().getMembers().contains(plugin.getCharacterManager().getHero(p))) {
                        addSpellTarget(p,hero);
                        damageEntity(p, player, damage, DamageCause.MAGIC);
                        //p.damage(damage, player);
                    }
                    totalExp += exp;
                }
            }
        }
        if (totalExp > 0) {
            if (hero.hasParty()) {
                hero.getParty().gainExp(totalExp, ExperienceType.SKILL, player.getLocation());
            } else {
                hero.gainExp(totalExp, ExperienceType.SKILL, player.getLocation());
            }
        }
    }

}