package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass.ExperienceType;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import org.bukkit.entity.Player;
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
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 30, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        radius = radius > 0 ? radius : 0;
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE.node(), 14, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getSkillLevel(this)));
        damage = damage > 0 ? damage : 0;
        String description = getDescription().replace("$1", radius + "");
        
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
        node.set(SkillSetting.RADIUS.node(), 30);
        node.set(SkillSetting.RADIUS_INCREASE.node(), 0);
        node.set(SkillSetting.DAMAGE.node(), 14);
        node.set("damage-increase", 0);
        node.set("exp-per-target-struck", 0);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 30, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        radius = radius > 0 ? radius : 0;
        double damage = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE.node(), 14, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getSkillLevel(this)));
        damage = damage > 0 ? damage : 0;
        double expTarget = SkillConfigManager.getUseSetting(hero, this, "exp-per-target-struck", 0, false);
        double exp = 0;
        for ( Entity wMonster : player.getNearbyEntities(radius, radius, radius) ) {
            if ((wMonster instanceof LivingEntity) && !(wMonster instanceof Enderman)) {
                LivingEntity target = (LivingEntity) wMonster;
                if (damageCheck(player, target) && target instanceof Player) {
                    target.getWorld().strikeLightningEffect(target.getLocation());
                    addSpellTarget(target,hero);
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
                hero.gainExp(exp, ExperienceType.SKILL, player.getLocation());
            }
        }
        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }

}