package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.effects.common.StunEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.skill.TargettedSkill;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class SkillFalconPunch extends TargettedSkill {

    public SkillFalconPunch(Heroes plugin) {
        super(plugin, "FalconPunch");
        setDescription("Stuns and does massive damage your opponent");
        setUsage("/skill falconpunch");
        setArgumentRange(0, 0);
        setIdentifiers("skill falconpunch");
        setTypes(SkillType.PHYSICAL, SkillType.HARMFUL, SkillType.DAMAGING, SkillType.DEBUFF);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty(Setting.DURATION.node(), 5000);
        node.setProperty(Setting.DAMAGE.node(), 6);
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
        Hero tHero = getPlugin().getHeroManager().getHero(tPlayer);
        broadcastExecuteText(hero, target);
        long duration = (long) getSetting(hero, Setting.DURATION.node(), 5000, false);
        int damage = (int) getSetting(hero, Setting.DAMAGE.node(), 6, false);
        if (duration > 0) {
            tHero.addEffect(new StunEffect(this, duration));
        }
        if (damage > 0) {
            tPlayer.damage(damage, player);
        }
        return SkillResult.NORMAL;
    }
}