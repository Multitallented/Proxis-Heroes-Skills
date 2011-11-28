package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.effects.Effect;
import com.herocraftonline.dev.heroes.effects.EffectType;
import com.herocraftonline.dev.heroes.effects.common.InvulnerabilityEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;
import com.herocraftonline.dev.heroes.util.Util;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

public class SkillEscape extends ActiveSkill {


    public SkillEscape(Heroes plugin) {
        super(plugin, "Escape");
        setDescription("Grants immunity, dispel, and short range teleport");
        setUsage("/skill escape");
        setArgumentRange(0, 0);
        setIdentifiers(new String[] { "skill escape" });

        setTypes(SkillType.TELEPORT, SkillType.COUNTER);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty(Setting.DURATION.node(), 1000);
        node.setProperty(Setting.MAX_DISTANCE.node(), 6);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        int distance = getSetting(hero, Setting.MAX_DISTANCE.node(), 6, false);
        Block prev = null;
        Block b;
        BlockIterator iter = null;
        try {
            iter = new BlockIterator(player, distance);
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
            int duration = getSetting(hero, Setting.DURATION.node(), 1000, false);
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