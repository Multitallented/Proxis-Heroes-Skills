package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class SkillUnmark extends ActiveSkill {

    public SkillUnmark(Heroes plugin) {
        super(plugin, "Unmark");
        setDescription("Eradicates all marks within $1 blocks.");
        setUsage("/skill unmark");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill unmark"});
        
        setTypes(SkillType.SILENCABLE, SkillType.LIGHT);
    }

    @Override
    public String getDescription(Hero hero) {
                    int radius = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 30, false)
                            + (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0, false) * hero.getSkillLevel(this)));
        String description = getDescription().replace("$1", radius + "");
        
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
        node.set(SkillSetting.RADIUS.node(), 30);
        node.set("radius-increase", 0);
        return node;
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        int x = (int) hero.getPlayer().getLocation().getX();
        int z = (int) hero.getPlayer().getLocation().getZ();
        for (Player p : plugin.getServer().getOnlinePlayers()) {

            try {
                Hero currentHero = plugin.getCharacterManager().getHero(p);
                if (currentHero.getSkillSettings("Recall") != null) {
                    int radius = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 30, false)
                            + (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0, false) * hero.getSkillLevel(this)));
                    double currentX = currentHero.getSkillSettings("Recall").getDouble("x");
                    double currentZ = currentHero.getSkillSettings("Recall").getDouble("z");
                    if (Math.abs(currentX - x) <= radius && Math.abs(currentZ - z) <= radius) {
                        Player currentPlayer = currentHero.getPlayer();
                        currentHero.setSkillSetting("Recall", "world", "");
                        Messaging.send(currentPlayer, "Your Mark was eradicated by $1", hero.getPlayer().getDisplayName());
                        plugin.getCharacterManager().saveHero(currentPlayer, false);
                    }
                }
            } catch (Exception e) {
            }
        }
        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }
}