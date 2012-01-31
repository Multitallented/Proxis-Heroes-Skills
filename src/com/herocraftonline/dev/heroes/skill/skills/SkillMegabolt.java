package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Player;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillMegabolt extends ActiveSkill {
    public SkillMegabolt(Heroes plugin) {
        super(plugin, "Megabolt");
        setDescription("Strikes lightning on every mob/player around you");
        setUsage("/skill megabolt");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill megabolt"});

        setTypes(SkillType.LIGHTNING, SkillType.DAMAGING, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 30, false) +
                (SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS_INCREASE.node(), 0.0, false) * hero.getLevel()));
        radius = radius > 0 ? radius : 0;
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE.node(), 14, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getLevel()));
        damage = damage > 0 ? damage : 0;
        String description = getDescription().replace("$1", radius + "");
        
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
        node.set(Setting.RADIUS.node(), 30);
        node.set(Setting.RADIUS_INCREASE.node(), 0);
        node.set(Setting.DAMAGE.node(), 14);
        node.set("damage-increase", 0);
        node.set("exp-per-target-struck", 0);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 30, false) +
                (SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS_INCREASE.node(), 0.0, false) * hero.getLevel()));
        radius = radius > 0 ? radius : 0;
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE.node(), 14, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getLevel()));
        damage = damage > 0 ? damage : 0;
        double expTarget = SkillConfigManager.getUseSetting(hero, this, "exp-per-target-struck", 0, false);
        double exp = 0;
        for ( Entity wMonster : player.getNearbyEntities(radius, radius, radius) ) {
            if ((wMonster instanceof LivingEntity) && !(wMonster instanceof Enderman)) {
                LivingEntity target = (LivingEntity) wMonster;
                if (damageCheck(player, target)) {
                    target.getWorld().strikeLightningEffect(target.getLocation());
                    damageEntity(target, player, damage, DamageCause.MAGIC); 
                    //target.damage(damage, player);
                    exp += expTarget;
                }
            }
        }
        if (exp > 0) {
            if (hero.hasParty()) {
                hero.getParty().gainExp(exp, ExperienceType.SKILL, player.getLocation());
            } else {
                hero.gainExp(exp, ExperienceType.SKILL);
            }
        }
        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }

}