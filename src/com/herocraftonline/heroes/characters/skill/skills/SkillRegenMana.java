package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.events.HeroRegainManaEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SkillRegenMana extends PassiveSkill {

    public SkillRegenMana(Heroes plugin) {
        super(plugin, "RegenMana");
        setDescription("Adds $1 mana to your regeneration");
        setTypes(SkillType.COUNTER, SkillType.MANA);
        Bukkit.getServer().getPluginManager().registerEvents(new SkillHeroListener(this), plugin);
        //registerEvent(Type.CUSTOM_EVENT, new SkillHeroListener(this), Priority.Normal);
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set("regen-amount", 1.0);
        node.set("regen-increase", 0.0);
        return node;
    }

    @Override
    public String getDescription(Hero hero) {
        int regen = (int) (SkillConfigManager.getUseSetting(hero, this, "regen-amount", 1.0, false)
                + (SkillConfigManager.getUseSetting(hero, this, "regen-increase", 0.0, false) * hero.getSkillLevel(this)));
        String description = getDescription().replace("$1", regen + "");
        return description;
    }
    
    public class SkillHeroListener implements Listener {
        private Skill skill;
        public SkillHeroListener(Skill skill) {
            this.skill = skill;
        }
        @EventHandler
        public void onHeroRegainMana(HeroRegainManaEvent event) {
            if (event.isCancelled())
                return;
            Hero hero = event.getHero();
            if (hero.hasEffect("RegenMana")) {
                event.setAmount((int) (event.getAmount() + (int) (SkillConfigManager.getUseSetting(hero, skill, "regen-amount", 1.0, false)
                + (SkillConfigManager.getUseSetting(hero, skill, "regen-increase", 0.0, false) * hero.getSkillLevel(skill)))));
            }
        }
    }

}