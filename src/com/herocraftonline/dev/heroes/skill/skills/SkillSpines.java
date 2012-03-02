package com.herocraftonline.dev.heroes.skill.skills;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.PassiveSkill;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;
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
        double chance = (SkillConfigManager.getUseSetting(hero, this, Setting.CHANCE.node(), 0.1, false) +
                (SkillConfigManager.getUseSetting(hero, this, Setting.CHANCE_LEVEL.node(), 0, false) * hero.getLevel())) * 100;
        chance = chance > 0 ? chance : 0;
        long cooldown = (long) (SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN.node(), 500, false) -
                (SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN_REDUCE.node(), 0, false) * hero.getLevel())) / 1000;
        cooldown = cooldown > 0 ? cooldown : 0;
        String description = getDescription().replace("$1", chance + "").replace("$2", cooldown + "");
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(Setting.CHANCE.node(), .1);
        node.set(Setting.CHANCE_LEVEL.node(), 0);
        node.set(Setting.COOLDOWN.node(), 500);
        node.set(Setting.COOLDOWN_REDUCE.node(), 0);
        return node;
    }
    
    public class SkillHeroListener implements Listener {
        private Skill skill;
        public SkillHeroListener(Skill skill) {
            this.skill = skill;
        }
        @EventHandler
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled() || event.getDamage() == 0 || event.getCause() != DamageCause.ENTITY_ATTACK || !(event.getEntity() instanceof Player))
                return;
            Player player = (Player) event.getEntity();
            Hero hero = plugin.getHeroManager().getHero(player);
            if (hero.hasEffect("Spines")) {
                if (hero.getCooldown("Spines") == null || hero.getCooldown("Spines") <= System.currentTimeMillis()) {
                    double chance = (SkillConfigManager.getUseSetting(hero, this.skill, Setting.CHANCE.node(), 0.1, false) +
                            (SkillConfigManager.getUseSetting(hero, this.skill, Setting.CHANCE_LEVEL.node(), 0, false) * hero.getLevel()));
                    chance = chance > 0 ? chance : 0;
                    long cooldown = (long) (SkillConfigManager.getUseSetting(hero, this.skill, Setting.COOLDOWN.node(), 500, false) -
                            (SkillConfigManager.getUseSetting(hero, this.skill, Setting.COOLDOWN_REDUCE.node(), 0, false) * hero.getLevel()));
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