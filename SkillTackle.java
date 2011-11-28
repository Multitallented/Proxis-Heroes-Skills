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
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class SkillTackle extends TargettedSkill {

    public SkillTackle(Heroes plugin) {
        super(plugin, "Tackle");
        setDescription("Charges towards your target");
        setUsage("/skill tackle");
        setArgumentRange(0, 0);
        setIdentifiers("skill tackle");
        setTypes(SkillType.MOVEMENT, SkillType.SILENCABLE, SkillType.HARMFUL, SkillType.PHYSICAL, SkillType.DAMAGING, SkillType.DEBUFF);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty(Setting.RADIUS.node(), 3);
        node.setProperty(Setting.DURATION.node(), 3000);
        node.setProperty(Setting.DAMAGE.node(), 6);
        return node;
    }
    
    @Override
    public SkillResult use(Hero hero, LivingEntity target, String args[]) {
        Player player = hero.getPlayer();
        if (target instanceof Player && ((Player) target).equals(player)) {
            return SkillResult.INVALID_TARGET;
        }
        player.teleport(target.getLocation());
        broadcastExecuteText(hero, target);
        int radius = (int) getSetting(hero, Setting.RADIUS.node(), 3, false);
        long duration = (long) getSetting(hero, Setting.DURATION.node(), 3000, false);
        int damage = (int) getSetting(hero, Setting.DAMAGE.node(), 6, false);
        for (Entity e : ((Entity) hero.getPlayer()).getNearbyEntities(radius, radius, radius)) {
            if (e instanceof Player) {
                Player tPlayer = (Player) e;
                Hero tHero = getPlugin().getHeroManager().getHero(tPlayer);
                if (damageCheck(player, tPlayer)) {
                    if (duration > 0) {
                        tHero.addEffect(new StunEffect(this, duration));
                    }
                    if (damage > 0) {
                        tPlayer.damage(damage, player);
                    }
                }
            } else if (e instanceof Creature) {
                LivingEntity le = (LivingEntity) e;
                if (damage > 0) {
                    le.damage(damage, player);
                }
            }
        }
        return SkillResult.NORMAL;
    }
}