package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Player;
import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.configuration.ConfigurationSection;

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
                    int radius = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 30, false)
                            + (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0, false) * hero.getLevel()));
        String description = getDescription().replace("$1", radius + "");
        
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
        node.set(Setting.RADIUS.node(), 30);
        node.set("radius-increase", 0);
        return node;
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        int x = (int) hero.getPlayer().getLocation().getX();
        int z = (int) hero.getPlayer().getLocation().getZ();
        for (Player p : plugin.getServer().getOnlinePlayers()) {

            try {
                Hero currentHero = plugin.getHeroManager().getHero(p);
                if (currentHero.getSkillSettings("Recall") != null) {
                    int radius = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 30, false)
                            + (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0, false) * hero.getLevel()));
                    double currentX = (Double) currentHero.getSkillSettings("Recall").get("x");
                    double currentZ = (Double) currentHero.getSkillSettings("Recall").get("z");
                    if (Math.abs(currentX - x) <= radius && Math.abs(currentZ - z) <= radius) {
                        Player currentPlayer = currentHero.getPlayer();
                        currentHero.setSkillSetting("Recall", "world", "");
                        Messaging.send(currentPlayer, "Your Mark was eradicated by $1", hero.getPlayer().getDisplayName());
                        plugin.getHeroManager().saveHero(currentPlayer, false);
                    }
                }
            } catch (Exception e) {
            }
        }
        return SkillResult.NORMAL;
    }
}