package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.common.SlowEffect;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.util.Setting;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillEmpathy extends TargettedSkill {

    public SkillEmpathy(Heroes plugin) {
        super(plugin, "Empathy");
        setDescription("Deals $1% of the your missing health in damage. $2s slow.");
        setUsage("/skill empathy [target]");
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill empathy" });
        
        setTypes(SkillType.DARK, SkillType.DAMAGING, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        double damageMod = (SkillConfigManager.getUseSetting(hero, this, "damage-modifier", 1.0, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-modifier-increase", 0.0, false) * hero.getSkillLevel(this))) * 100;
        damageMod = damageMod > 0 ? damageMod : 0;
        int slowDuration = (int) (SkillConfigManager.getUseSetting(hero, this, "slow-duration", 0, false) +
                (SkillConfigManager.getUseSetting(hero, this, "slow-duration-increase", 0.0, false) * hero.getSkillLevel(this))) / 1000;
        slowDuration = slowDuration > 0 ? slowDuration : 0;
        String description = getDescription().replace("$1", damageMod + "").replace("$2", slowDuration + "");
        
        //COOLDOWN
        int cooldown = (SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN_REDUCE.node(), 0, false) * hero.getSkillLevel(this)) / 1000;
        if (cooldown > 0) {
            description += " CD:" + cooldown + "s";
        }
        
        //MANA
        int mana = SkillConfigManager.getUseSetting(hero, this, Setting.MANA.node(), 10, false)
                - (SkillConfigManager.getUseSetting(hero, this, Setting.MANA_REDUCE.node(), 0, false) * hero.getSkillLevel(this));
        if (mana > 0) {
            description += " M:" + mana;
        }
        
        //HEALTH_COST
        int healthCost = SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST, 0, false) - 
                (SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST_REDUCE, mana, true) * hero.getSkillLevel(this));
        if (healthCost > 0) {
            description += " HP:" + healthCost;
        }
        
        //STAMINA
        int staminaCost = SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA.node(), 0, false)
                - (SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA_REDUCE.node(), 0, false) * hero.getSkillLevel(this));
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
        node.set("max-damage", 0);
        node.set("max-damage-increase", 0.0);
        node.set("damage-modifier", 1);
        node.set("damage-modifier-increase", 0);
        node.set("slow-duration", 0);
        node.set("slow-duration-increase", 0);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, LivingEntity target, String[] args) {
        Player player = hero.getPlayer();
        if (target.equals(player)) {
            return SkillResult.INVALID_TARGET;
        }
        int maxDamage = (int) (SkillConfigManager.getUseSetting(hero, this, "max-damage", 0, false) +
                SkillConfigManager.getUseSetting(hero, this, "max-damage-increase", 0.0, false) * hero.getSkillLevel(this));
        double damageMod = (SkillConfigManager.getUseSetting(hero, this, "damage-modifier", 1.0, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-modifier-increase", 0.0, false) * hero.getSkillLevel(this)));
        damageMod = damageMod > 0 ? damageMod : 0;
        int damage = (int) (hero.getMaxHealth() - hero.getHealth());
        if (maxDamage != 0 && damage > maxDamage) {
            damage = maxDamage;
        }
        if (target instanceof Player && !damageCheck(player, target)) {
            return SkillResult.CANCELLED;
        }
        long slowDuration = (long) (SkillConfigManager.getUseSetting(hero, this, "slow-duration", 0, false) +
                (SkillConfigManager.getUseSetting(hero, this, "slow-duration-increase", 0.0, false) * hero.getSkillLevel(this)));
        slowDuration = slowDuration > 0 ? slowDuration : 0;
        if (slowDuration > 0) {
            if (target instanceof Player) {
                plugin.getCharacterManager().getHero((Player) target).addEffect(new SlowEffect(this, slowDuration, 2, false, "", "", hero));
            }
        }
        damage = (int) Math.round(damage * damageMod);
        damageEntity(target, player, damage, DamageCause.MAGIC);
        //target.damage(damage, player);
        broadcastExecuteText(hero, target);
        return SkillResult.NORMAL;
    }

}