package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class SkillDodge extends PassiveSkill {

    public SkillDodge(Heroes plugin) {
        super(plugin, "Dodge");
        setDescription("Passive $1% chance to dodge enemy attacks.");
        setTypes(SkillType.COUNTER, SkillType.BUFF);
        Bukkit.getServer().getPluginManager().registerEvents(new SkillHeroListener(this), plugin);
        //registerEvent(Type.ENTITY_DAMAGE, new SkillHeroListener(this), Priority.Normal);
    }

    @Override
    public String getDescription(Hero hero) {
        double chance = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE.node(), 0.2, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE_LEVEL.node(), 0.0, false) * hero.getSkillLevel(this))) * 100;
        chance = chance > 0 ? chance : 0;
        String description = getDescription().replace("$1", chance + "");
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(SkillSetting.CHANCE.node(), 0.1);
        node.set(SkillSetting.CHANCE_LEVEL.node(), 0);
        return node;
    }
    
    public class SkillHeroListener implements Listener {
        private Skill skill;
        public SkillHeroListener(Skill skill) {
            this.skill=skill;
        }
        @EventHandler
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled() || !(event.getEntity() instanceof Player))
                return;
            Player player = (Player) event.getEntity();
            Hero hero = plugin.getCharacterManager().getHero(player);
            if (hero.hasEffect("Dodge")) {
                double chance = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE.node(), 0.2, false) +
                        (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE_LEVEL.node(), 0.0, false) * hero.getSkillLevel(skill)));
                chance = chance > 0 ? chance : 0;
                if (Math.random() <= chance) {
                    event.setDamage(0);
                    event.setCancelled(true);
                    broadcast(player.getLocation(), "$1 dodged an attack!", player.getDisplayName());
                }
            }
        }
    }
}