package com.herocraftonline.dev.heroes.skill.skills;


import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.HeroRegainManaEvent;
import com.herocraftonline.dev.heroes.api.HeroesEventListener;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.PassiveSkill;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;

public class SkillRegenMana extends PassiveSkill {

    public SkillRegenMana(Heroes plugin) {
        super(plugin, "RegenMana");
        setDescription("Adds $1 mana to your regeneration");
        setTypes(SkillType.COUNTER, SkillType.MANA);
        
        registerEvent(Type.CUSTOM_EVENT, new SkillHeroListener(this), Priority.Normal);
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
                + (SkillConfigManager.getUseSetting(hero, this, "regen-increase", 0.0, false) * hero.getLevel()));
        String description = getDescription().replace("$1", regen + "");
        return description;
    }
    
    public class SkillHeroListener extends HeroesEventListener {
        private Skill skill;
        public SkillHeroListener(Skill skill) {
            this.skill = skill;
        }
        @Override
        public void onHeroRegainMana(HeroRegainManaEvent event) {
            if (event.isCancelled())
                return;
            Hero hero = event.getHero();
            if (hero.hasEffect("RegenMana")) {
                event.setAmount((int) (event.getAmount() + (int) (SkillConfigManager.getUseSetting(hero, skill, "regen-amount", 1.0, false)
                + (SkillConfigManager.getUseSetting(hero, skill, "regen-increase", 0.0, false) * hero.getLevel()))));
            }
        }
    }

}