package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Messaging;

public class SkillUnmark extends ActiveSkill {

    public SkillUnmark(Heroes plugin) {
        super(plugin, "Unmark");
        setDescription("Eradicates all marks in your area");
        setUsage("/skill unmark");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill unmark"});
        
        setTypes(SkillType.SILENCABLE, SkillType.LIGHT);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("radius", 30);
        return node;
    }
    
    @Override
    public boolean use(Hero hero, String[] args) {
        int x = (int) hero.getPlayer().getLocation().getX();
        int z = (int) hero.getPlayer().getLocation().getZ();
        for (Player p : getPlugin().getServer().getOnlinePlayers()) {

            try {
                Hero currentHero = getPlugin().getHeroManager().getHero(p);
                if (currentHero.getSkillSettings("Recall") != null) {
                    int radius = (int) getSetting(hero, "radius", 30, false);
                    double currentX = Double.parseDouble(currentHero.getSkillSettings("Recall").get("x"));
                    double currentZ = Double.parseDouble(currentHero.getSkillSettings("Recall").get("z"));
                    if (Math.abs(currentX - x) <= radius && Math.abs(currentZ - z) <= radius) {
                        Player currentPlayer = currentHero.getPlayer();
                        currentHero.setSkillSetting("Recall", "world", "");
                        Messaging.send(currentPlayer, "Your Mark was eradicated by $1", hero.getPlayer().getDisplayName());
                        getPlugin().getHeroManager().saveHero(currentPlayer);
                    }
                }
            } catch (Exception e) {
            }
        }
        return true;
    }
}