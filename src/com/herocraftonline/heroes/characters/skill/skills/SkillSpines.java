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
import org.bukkit.util.Vector;

public class SkillSpines extends PassiveSkill {

    public SkillSpines(Heroes plugin) {
        super(plugin, "Spines");
        setDescription("Passive $1% chance to shoot arrows when hit. CD:$2");
        setTypes(SkillType.COUNTER, SkillType.HARMFUL);
        Bukkit.getServer().getPluginManager().registerEvents(new SkillHeroListener(this), plugin);
        //registerEvent(Type.ENTITY_DAMAGE, new SkillHeroListener(this), Priority.Normal);
    }

    @Override
    public String getDescription(Hero hero) {
        double chance = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE.node(), 0.1, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.CHANCE_LEVEL.node(), 0, false) * hero.getSkillLevel(this))) * 100;
        chance = chance > 0 ? chance : 0;
        long cooldown = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN.node(), 500, false) -
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN_REDUCE.node(), 0, false) * hero.getSkillLevel(this))) / 1000;
        cooldown = cooldown > 0 ? cooldown : 0;
        String description = getDescription().replace("$1", chance + "").replace("$2", cooldown + "");
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(SkillSetting.CHANCE.node(), .1);
        node.set(SkillSetting.CHANCE_LEVEL.node(), 0);
        node.set(SkillSetting.COOLDOWN.node(), 500);
        node.set(SkillSetting.COOLDOWN_REDUCE.node(), 0);
        return node;
    }
    
    public class SkillHeroListener implements Listener {
        private Skill skill;
        public SkillHeroListener(Skill skill) {
            this.skill = skill;
        }
        @SuppressWarnings("deprecation")
        @EventHandler
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled() || event.getDamage() == 0 || event.getCause() != DamageCause.ENTITY_ATTACK || !(event.getEntity() instanceof Player))
                return;
            Player player = (Player) event.getEntity();
            Hero hero = plugin.getCharacterManager().getHero(player);
            if (hero.hasEffect("Spines")) {
                if (hero.getCooldown("Spines") == null || hero.getCooldown("Spines") <= System.currentTimeMillis()) {
                    double chance = (SkillConfigManager.getUseSetting(hero, this.skill, SkillSetting.CHANCE.node(), 0.1, false) +
                            (SkillConfigManager.getUseSetting(hero, this.skill, SkillSetting.CHANCE_LEVEL.node(), 0, false) * hero.getSkillLevel(skill)));
                    chance = chance > 0 ? chance : 0;
                    long cooldown = (long) (SkillConfigManager.getUseSetting(hero, this.skill, SkillSetting.COOLDOWN.node(), 500, false) -
                            (SkillConfigManager.getUseSetting(hero, this.skill, SkillSetting.COOLDOWN_REDUCE.node(), 0, false) * hero.getSkillLevel(skill)));
                    cooldown = cooldown > 0 ? cooldown : 0;
                    hero.setCooldown("Spines", cooldown + System.currentTimeMillis());
                    if (Math.random() <= chance) {
                        double diff = 2 * Math.PI / 12;
                        for (double a = 0; a < 2 * Math.PI; a += diff) {
                            Vector vel = new Vector(Math.cos(a), 0, Math.sin(a));
                            player.shootArrow().setVelocity(vel);
                        }
                    }
                }
            }
        }
    }
}