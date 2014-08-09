package com.herocraftonline.heroes.characters.skill.skills;


import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SkillCritical extends PassiveSkill {

    public SkillCritical(Heroes plugin) {
        super(plugin, "Critical");
        setDescription("Passive $1% chance to do $2 times damage.");
        setTypes(SkillType.COUNTER, SkillType.BUFF);
        Bukkit.getServer().getPluginManager().registerEvents(new SkillHeroListener(this), plugin);
        //registerEvent(Type.CUSTOM_EVENT, new SkillHeroListener(this), Priority.Normal);
    }

    @Override
    public String getDescription(Hero hero) {
        double chance = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE.node(), 0.2, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE_LEVEL.node(), 0.0, false) * hero.getSkillLevel(this))) * 100;
        chance = chance > 0 ? chance : 0;
        double damageMod = (SkillConfigManager.getUseSetting(hero, this, "damage-multiplier", 0.2, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-multiplier-increase", 0.0, false) * hero.getSkillLevel(this)));
        damageMod = damageMod > 0 ? damageMod : 0;
        String description = getDescription().replace("$1", chance + "").replace("$2", damageMod + "");
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(SkillSetting.CHANCE.node(), 0.2);
        node.set(SkillSetting.CHANCE_LEVEL.node(), 0);
        node.set("damage-multiplier", 2.0);
        node.set("damage-multiplier-increase", 0);
        return node;
    }
    
    public class SkillHeroListener implements Listener {
        private Skill skill;
        public SkillHeroListener(Skill skill) {
            this.skill = skill;
        }
        
        @EventHandler
        public void onEntityDamage(WeaponDamageEvent event) {
        	if (!(event.isCancelled())&&(event.getDamager() instanceof Hero)){
        		Hero hero = (Hero) event.getDamager();
           	  	if (hero.hasEffect("Critical")) {
                    double chance = (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE.node(), 0.2, false) +
                            (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.CHANCE_LEVEL.node(), 0.0, false) * hero.getSkillLevel(skill)));
                    chance = chance > 0 ? chance : 0;
                    if (Math.random() <= chance) {
                        double damageMod = (SkillConfigManager.getUseSetting(hero, skill, "damage-multiplier", 0.2, false) +
                                (SkillConfigManager.getUseSetting(hero, skill, "damage-multiplier-increase", 0.0, false) * hero.getSkillLevel(skill)));
                        damageMod = damageMod > 0 ? damageMod : 0;
                        event.setDamage((event.getDamage() * damageMod));
                    }
           	  	}
        	}
        }
    }
}