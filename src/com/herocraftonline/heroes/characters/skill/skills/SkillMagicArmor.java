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
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillMagicArmor extends PassiveSkill {
        private Skill magicArmor;
    
    public SkillMagicArmor(Heroes plugin) {
        super(plugin, "MagicArmor");
        setDescription("Passive $1% reduction of all magic damage.");
        setTypes(SkillType.COUNTER, SkillType.BUFF);
        
        Bukkit.getServer().getPluginManager().registerEvents(new SkillHeroListener(), plugin);
        //registerEvent(Type.CUSTOM_EVENT, new SkillHeroListener(this), Priority.Normal);
    }
    
    @Override
    public String getDescription(Hero hero) {
        int level = hero.getSkillLevel(this);
        double amount = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.AMOUNT.node(), 0.25, false) + 
                (SkillConfigManager.getUseSetting(hero, this, "amount-increase", 0.0, false) * level)) * 100;
        amount = amount > 0 ? amount : 0;
        String description = getDescription().replace("$1", amount + "");
        
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(SkillSetting.AMOUNT.node(), 0.25);
        node.set("amount-increase", 0.0);
        return node;
    }
    
    @Override
    public void init() {
        super.init();        
        magicArmor = this;
    }
    
    public class SkillHeroListener implements Listener {
        @EventHandler()
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled() || event.getCause() != DamageCause.MAGIC || !(event.getEntity() instanceof Player) || event.getDamage() < 1) {
                return;
            }
            Player player = (Player) event.getEntity();
            Hero hero = plugin.getCharacterManager().getHero(player);
            if (!hero.hasEffect("MagicArmor")) {
                return;
            }
            double amount = (SkillConfigManager.getUseSetting(hero, magicArmor, SkillSetting.AMOUNT.node(), 0.25, false) + 
                    (SkillConfigManager.getUseSetting(hero, magicArmor, "amount-increase", 0.0, false) * hero.getSkillLevel(magicArmor)));
            amount = amount > 0 ? amount : 0;
            event.setDamage((int) (event.getDamage() * (1 - amount)));
        }
    }
}