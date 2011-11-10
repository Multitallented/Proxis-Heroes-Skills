package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.HeroRegainManaEvent;
import com.herocraftonline.dev.heroes.api.HeroesEventListener;
import com.herocraftonline.dev.heroes.skill.PassiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;

public class SkillRegenMana extends PassiveSkill {

    public SkillRegenMana(Heroes plugin) {
        super(plugin, "RegenMana");
        setDescription("Adds to your mana regeneration");
        setTypes(SkillType.COUNTER, SkillType.MANA);
        
        registerEvent(Type.CUSTOM_EVENT, new SkillHeroListener(), Priority.Normal);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("regen-amount", 1.0);
        return node;
    }
    
    public class SkillHeroListener extends HeroesEventListener {

        @Override
        public void onHeroRegainMana(HeroRegainManaEvent event) {
            if (event.isCancelled())
                return;

            if (event.getHero().hasEffect("RegenMana")) {
                event.setAmount((int) (event.getAmount() + getSetting(event.getHero(), "regen-amount", 1.0, false)));
            }
        }
    }

}