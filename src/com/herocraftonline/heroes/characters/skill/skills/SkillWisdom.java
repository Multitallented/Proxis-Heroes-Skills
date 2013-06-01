package com.herocraftonline.heroes.characters.skill.skills;


/**
 *
 * @author Multitallented
 */

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class SkillWisdom extends ActiveSkill {
    
    public SkillWisdom(Heroes plugin) {
        super(plugin, "Wisdom");
        setDescription("Grants mana to you an allies nearby");
        setUsage("/skill wisdom");
        setArgumentRange(0, 0);
        setIdentifiers(new String[] { "skill wisdom" });
        setTypes(SkillType.MANA);
    }

    @Override
    public String getDescription(Hero hero) {
        String description = getDescription();
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
        node.set(SkillSetting.RADIUS.node(), 7);
        node.set("give-mana", 10);
        return node;
    }
    
    @Override
    public void init() {
        super.init();
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, 10, false);
        int mana = SkillConfigManager.getUseSetting(hero, this, "give-mana", 10, false);
        giveMana(hero, mana);
        if (hero.hasParty()) {
            for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
                if (e instanceof Player) {
                    LivingEntity le = (LivingEntity) e;
                    Hero chero = plugin.getCharacterManager().getHero((Player) le);
                    if (hero.getParty().isPartyMember(chero) && !chero.equals(hero)) {
                        giveMana(chero, mana);
                    }
                }
            }
        }
        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }
    
    private void giveMana(Hero hero, int mana) {
        if (hero.getMana() + mana > hero.getMaxMana()) {
            hero.setMana(hero.getMaxMana());
        } else {
            hero.setMana(hero.getMana() + mana);
        }
    }
}