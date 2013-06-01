package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.Effect;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.common.InvulnerabilityEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.util.Util;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

public class SkillEscape extends ActiveSkill {


    public SkillEscape(Heroes plugin) {
        super(plugin, "Escape");
        setDescription("$1s Invuln + $2 block teleport.");
        setUsage("/skill escape");
        setArgumentRange(0, 0);
        setIdentifiers(new String[] { "skill escape" });

        setTypes(SkillType.TELEPORT, SkillType.COUNTER);
    }

    @Override
    public String getDescription(Hero hero) {
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 1000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this))) / 1000;
        duration = duration > 0 ? duration : 0;
        
        int maxDistance = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE.node(), 6, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        maxDistance = maxDistance > 0 ? maxDistance : 0;
        String description = getDescription().replace("$1", duration + "").replace("$1", maxDistance + "");
        
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
        node.set(SkillSetting.DURATION.node(), 1000);
        node.set("duration-increase", 0);
        node.set(SkillSetting.MAX_DISTANCE.node(), 6);
        node.set(SkillSetting.MAX_DISTANCE_INCREASE.node(), 0);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        int maxDistance = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE.node(), 6, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        maxDistance = maxDistance > 0 ? maxDistance : 0;
        Block prev = null;
        Block b;
        BlockIterator iter = null;
        try {
            iter = new BlockIterator(player, maxDistance);
        } catch (IllegalStateException e) {
            Messaging.send(player, "There was an error getting your blink location!");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        while (iter.hasNext()) {
            b = iter.next();
            if (Util.transparentBlocks.contains(b.getType()) && ( Util.transparentBlocks.contains(b.getRelative(BlockFace.UP).getType()) || Util.transparentBlocks.contains(b.getRelative(BlockFace.DOWN).getType()))) {
                prev = b;
            } else {
                break;
            }
        }
        if (prev != null) {
            broadcastExecuteText(hero);
            long duration = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 1000, false) +
                    (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this)));
            duration = duration > 0 ? duration : 0;
            // Remove any harmful effects on the caster
            for (Effect effect : hero.getEffects()) {
                if (effect.isType(EffectType.HARMFUL)) {
                    hero.removeEffect(effect);
                }
            }
            hero.addEffect(new InvulnerabilityEffect(this, duration));
            Location teleport = prev.getLocation().clone();
            //Set the blink location yaw/pitch to that of the player
            teleport.setPitch(player.getLocation().getPitch());
            teleport.setYaw(player.getLocation().getYaw());
            player.teleport(teleport);
            return SkillResult.NORMAL;
        } else {
            Messaging.send(player, "No location to blink to.");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
    }
}