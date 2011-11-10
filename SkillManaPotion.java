package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;

public class SkillManaPotion extends ActiveSkill {

    public SkillManaPotion(Heroes plugin) {
        super(plugin, "ManaPotion");
        setDescription("Gives you mana");
        setUsage("/skill manapotion");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill manapotion"});
        
        setTypes(SkillType.MANA);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("mana-given", 10);
        node.setProperty(Setting.REAGENT.node(), "REDSTONE");
        node.setProperty(Setting.REAGENT_COST.node(), 16);
        return node;
    }

    @Override
    public boolean use(Hero hero, String[] args) {
        
        broadcastExecuteText(hero);
        int currentMana = hero.getMana();
        int addMana = getSetting(hero, "mana-given", 10, false);
        if (addMana < 0 || addMana > 100) {
            addMana = 10;
        }
        if (currentMana + addMana > 100) {
            addMana = 100 - currentMana;
        }
        hero.setMana(currentMana + addMana);
        return true;
    }
    

}