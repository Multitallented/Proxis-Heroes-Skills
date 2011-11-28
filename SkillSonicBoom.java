package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Player;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.effects.common.SilenceEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.util.config.ConfigurationNode;

public class SkillSonicBoom extends ActiveSkill {

    public SkillSonicBoom(Heroes plugin) {
        super(plugin, "SonicBoom");
        setDescription("Damage + Silence everyone around you");
        setUsage("/skill sonicboom");
        setArgumentRange(0, 0);
        setIdentifiers("skill sonicboom");
        setTypes(SkillType.MOVEMENT, SkillType.PHYSICAL);
    }
    
    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty(Setting.DURATION.node(), 10000);
        node.setProperty(Setting.RADIUS.node(), 10);
        node.setProperty(Setting.DAMAGE.node(), 0);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        int radius = getSetting(hero, Setting.RADIUS.node(), 10, false);
        int damage = getSetting(hero, Setting.DAMAGE.node(), 0, false);
        long duration = getSetting(hero, Setting.DURATION.node(), 10000, false);
        Player player = hero.getPlayer();
        for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof Creature) {
                Creature c = (Creature) e;
                c.damage(damage, player);
            } else if (e instanceof Player) {
                Player p = (Player) e;
                if (damageCheck(player, p)) {
                    p.damage(damage, player);
                    Hero tHero = getPlugin().getHeroManager().getHero(p);
                    tHero.addEffect(new SilenceEffect(this, duration));
                }
            }
        }
        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }
}