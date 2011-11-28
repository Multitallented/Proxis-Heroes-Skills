package com.herocraftonline.dev.heroes.skill.skills;


import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;

public class SkillReagent extends ActiveSkill {

    public SkillReagent(Heroes plugin) {
        super(plugin, "Reagent");
        setDescription("consumes an item for exp");
        setUsage("/skill reagent");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill reagent"});

        setTypes(SkillType.ITEM);
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }
    

}