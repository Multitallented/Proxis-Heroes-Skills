package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.skill.TargettedSkill;
import com.herocraftonline.dev.heroes.util.Messaging;
import org.bukkit.util.config.ConfigurationNode;

public class SkillUnholyRitual extends TargettedSkill {

    public SkillUnholyRitual(Heroes plugin) {
        super(plugin, "UnholyRitual");
        setDescription("Target Zombie or Skeleton is sacrificed, necromancer receives mana");
        setUsage("/skill unholyritual");
        setArgumentRange(0, 0);
        setIdentifiers("skill unholyritual", "skill uritual");
        setTypes(SkillType.DARK, SkillType.SILENCABLE, SkillType.DAMAGING);
    }
    
    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("mana-given", 20);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, LivingEntity target, String[] args) {
        Player player = hero.getPlayer();

        if (!(target instanceof Zombie) && !(target instanceof Skeleton)) {
            return SkillResult.INVALID_TARGET;
        }

        addSpellTarget(target, hero);
        target.damage(target.getHealth(), player);
        hero.setMana(hero.getMana() + (int) getSetting(hero, "mana-given", 20, false));
        broadcastExecuteText(hero, target);
        return SkillResult.NORMAL;
    }

}