package com.herocraftonline.dev.heroes.skill.skills;

import java.util.List;

import org.bukkit.entity.Entity;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.effects.common.StunEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

public class SkillWrestle extends ActiveSkill {
    
    public SkillWrestle(Heroes plugin) {
        super(plugin, "Wrestle");
        setDescription("Roots and silences you and all players near you");
        setUsage("/skill wrestle");
        setArgumentRange(0, 0);
        setIdentifiers(new String[] { "skill wrestle" });
        
        setTypes(SkillType.PHYSICAL, SkillType.COUNTER, SkillType.DEBUFF, SkillType.MOVEMENT);
    }
    
    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty(Setting.DURATION.node(), 10000);
        node.setProperty(Setting.RADIUS.node(), 3);
        return node;
    }

    @Override
    public boolean use(Hero hero, String[] args) {
        int radius = (int) getSetting(hero, Setting.RADIUS.node(), 3, false);
        radius = radius < 2 ? 3 : radius;
        List<Entity> entities = hero.getPlayer().getNearbyEntities(radius, radius, radius);
        long duration = 10000;
        duration = getSetting(hero, Setting.DURATION.node(), 10000, false);
        StunEffect cEffect = new StunEffect(this, duration);
        for (Entity n : entities) {
            if (n instanceof Player) {
                Hero tHero = getPlugin().getHeroManager().getHero((Player) n);
                tHero.addEffect(cEffect);
            }
        }
        
        
        broadcastExecuteText(hero);
        return true;
    }

}

