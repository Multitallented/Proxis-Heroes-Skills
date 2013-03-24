package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class SkillWarp extends ActiveSkill {

    public SkillWarp(Heroes plugin) {
        super(plugin, "Warp");
        setDescription("Teleports you to $1");
        setUsage("/skill warp");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill warp"});
        
        setTypes(SkillType.TELEPORT, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        String description1 = SkillConfigManager.getUseSetting(hero, this, "description", "a set location");
        String description = getDescription().replace("$1", description1 + "");
        
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
        node.set("destination", "world,0,64,0");
        node.set("description", "a set location");
        return node;
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        
        String destinationString = SkillConfigManager.getUseSetting(hero, this, "destination", "world,0,64,0");
        String[] dArgs = destinationString.split(",");
        Location destination = null;
        try {
            destination = new Location(Bukkit.getWorld(dArgs[0]), Double.parseDouble(dArgs[1]), Double.parseDouble(dArgs[2]), Double.parseDouble(dArgs[3]));
            player.teleport(destination);
        } catch (Exception e) {
            player.sendMessage(ChatColor.GRAY + "SkillWarp has an invalid config.");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        broadcastExecuteText(hero);
        
        return SkillResult.NORMAL;
    }

}