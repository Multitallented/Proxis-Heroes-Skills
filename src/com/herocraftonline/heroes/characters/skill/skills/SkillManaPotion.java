package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import org.bukkit.configuration.ConfigurationSection;

public class SkillManaPotion extends ActiveSkill {

    public SkillManaPotion(Heroes plugin) {
        super(plugin, "ManaPotion");
        setDescription("Gives you $1 mana.");
        setUsage("/skill manapotion");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill manapotion"});
        
        setTypes(SkillType.MANA);
    }

    @Override
    public String getDescription(Hero hero) {
        int manaGiven = (int) (SkillConfigManager.getUseSetting(hero, this, "mana-given", 1.0, false) +
                (SkillConfigManager.getUseSetting(hero, this, "mana-given-increase", 0.0, false) * hero.getSkillLevel(this)));
        manaGiven = manaGiven > 0 ? manaGiven : 0;
        String description = getDescription().replace("$1", manaGiven + "");
        
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
        node.set("mana-given", 10);
        node.set("mana-given-increase", 0);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        
        broadcastExecuteText(hero);
        int currentMana = hero.getMana();
        int manaGiven = (int) (SkillConfigManager.getUseSetting(hero, this, "mana-given", 1.0, false) +
                (SkillConfigManager.getUseSetting(hero, this, "mana-given-increase", 0.0, false) * hero.getSkillLevel(this)));
        manaGiven = manaGiven > 0 ? manaGiven : 0;
        if (manaGiven < 0 || manaGiven > 100) {
            manaGiven = 10;
        }
        if (currentMana + manaGiven > 100) {
            manaGiven = 100 - currentMana;
        }
        hero.setMana(currentMana + manaGiven);
        return SkillResult.NORMAL;
    }
    

}