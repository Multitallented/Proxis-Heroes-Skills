package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.common.StunEffect;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.util.Messaging;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillStun extends TargettedSkill {

    public SkillStun(Heroes plugin) {
        super(plugin, "Stun");
        setDescription("$1s Stun + $2 damage.");
        setUsage("/skill stun");
        setArgumentRange(0, 0);
        setIdentifiers("skill stun");
        setTypes(SkillType.SILENCABLE, SkillType.HARMFUL, SkillType.DAMAGING, SkillType.DEBUFF);
    }

    @Override
    public String getDescription(Hero hero) {
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this))) / 1000;
        duration = duration > 0 ? duration : 0;
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getSkillLevel(this)));
        damage = damage > 0 ? damage : 0;
        String description = getDescription().replace("$1", duration + "").replace("$2", damage + "");
        
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
        node.set(SkillSetting.DURATION.node(), 5000);
        node.set("duration-increase", 0);
        node.set(SkillSetting.DAMAGE.node(), 6);
        node.set("damage-increase", 0);
        return node;
    }
    
    @Override
    public SkillResult use(Hero hero, LivingEntity target, String args[]) {
        Player player = hero.getPlayer();
        if (!(target instanceof Player)) {
            return SkillResult.INVALID_TARGET;
        }
        if (((Player) target).equals(player)) {
            return SkillResult.INVALID_TARGET;
        }
        Player tPlayer = (Player) target;
        if (!damageCheck(player, tPlayer)) {
            Messaging.send(player, "You can't harm that target");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        Hero tHero = plugin.getCharacterManager().getHero(tPlayer);
        broadcastExecuteText(hero, target);
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this)));
        duration = duration > 0 ? duration : 0;
        double damage = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getSkillLevel(this)));
        damage = damage > 0 ? damage : 0;
        if (duration > 0) {
            tHero.addEffect(new StunEffect(this, duration));
        }
        if (damage > 0) {
            addSpellTarget(tPlayer,hero);
            damageEntity(tPlayer, player, damage, DamageCause.MAGIC);
            //tPlayer.damage(damage, player);
        }
        return SkillResult.NORMAL;
    }
}