package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.effects.common.StunEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.skill.TargettedSkill;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class SkillStun extends TargettedSkill {

    public SkillStun(Heroes plugin) {
        super(plugin, "Stun");
        setDescription("Stuns and damages your opponent");
        setUsage("/skill stun");
        setArgumentRange(0, 0);
        setIdentifiers("skill stun");
        setTypes(SkillType.SILENCABLE, SkillType.HARMFUL, SkillType.DAMAGING, SkillType.DEBUFF);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty(Setting.DURATION.node(), 5000);
        node.setProperty(Setting.DAMAGE.node(), 6);
        return node;
    }
    
    @Override
    public boolean use(Hero hero, LivingEntity target, String args[]) {
        Player player = hero.getPlayer();
        if (!(target instanceof Player)) {
            Messaging.send(player, "Invalid target");
            return false;
        }
        if (((Player) target).equals(player)) {
            Messaging.send(player, "Invalid target");
            return false;
        }
        Player tPlayer = (Player) target;
        if (!damageCheck(player, tPlayer)) {
            Messaging.send(player, "You can't harm that target");
            return false;
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
        return true;
    }
}