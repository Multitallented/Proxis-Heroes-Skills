package com.herocraftonline.dev.heroes.skill.skills;


import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;
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
                (SkillConfigManager.getUseSetting(hero, this, "mana-given-increase", 0.0, false) * hero.getLevel()));
        manaGiven = manaGiven > 0 ? manaGiven : 0;
        String description = getDescription().replace("$1", manaGiven + "");
        
        //COOLDOWN
        int cooldown = (SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN_REDUCE.node(), 0, false) * hero.getLevel()) / 1000;
        if (cooldown > 0) {
            description += " CD:" + cooldown + "s";
        }
        
        //MANA
        int mana = SkillConfigManager.getUseSetting(hero, this, Setting.MANA.node(), 10, false)
                - (SkillConfigManager.getUseSetting(hero, this, Setting.MANA_REDUCE.node(), 0, false) * hero.getLevel());
        if (mana > 0) {
            description += " M:" + mana;
        }
        
        //HEALTH_COST
        int healthCost = SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST, 0, false) - 
                (SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST_REDUCE, mana, true) * hero.getLevel());
        if (healthCost > 0) {
            description += " HP:" + healthCost;
        }
        
        //STAMINA
        int staminaCost = SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA.node(), 0, false)
                - (SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA_REDUCE.node(), 0, false) * hero.getLevel());
        if (staminaCost > 0) {
            description += " FP:" + staminaCost;
        }
        
        //DELAY
        int delay = SkillConfigManager.getUseSetting(hero, this, Setting.DELAY.node(), 0, false) / 1000;
        if (delay > 0) {
            description += " W:" + delay + "s";
        }
        
        //EXP
        int exp = SkillConfigManager.getUseSetting(hero, this, Setting.EXP.node(), 0, false);
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
                (SkillConfigManager.getUseSetting(hero, this, "mana-given-increase", 0.0, false) * hero.getLevel()));
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