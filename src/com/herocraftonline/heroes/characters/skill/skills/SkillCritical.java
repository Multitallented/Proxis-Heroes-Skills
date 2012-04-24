package com.herocraftonline.heroes.characters.skill.skills;


import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.events.WeaponDamageEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Setting;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

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
        double chance = (SkillConfigManager.getUseSetting(hero, this, Setting.CHANCE.node(), 0.2, false) +
                (SkillConfigManager.getUseSetting(hero, this, Setting.CHANCE_LEVEL.node(), 0.0, false) * hero.getSkillLevel(this))) * 100;
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
        node.set(Setting.CHANCE.node(), 0.2);
        node.set(Setting.CHANCE_LEVEL.node(), 0);
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
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled() ||event.getCause() != DamageCause.ENTITY_ATTACK || !(event.getEntity() instanceof Player) ||
                    event.getDamage() == 0 || !(event instanceof EntityDamageByEntityEvent))
                return;
            EntityDamageByEntityEvent edby = (EntityDamageByEntityEvent) event;
            if (edby.getDamager() instanceof Player) {
                Player player = (Player) edby.getDamager();
                Hero hero = plugin.getCharacterManager().getHero(player);

                if (hero.hasEffect("Critical")) {
                    double chance = (SkillConfigManager.getUseSetting(hero, skill, Setting.CHANCE.node(), 0.2, false) +
                            (SkillConfigManager.getUseSetting(hero, skill, Setting.CHANCE_LEVEL.node(), 0.0, false) * hero.getSkillLevel(skill))) * 100;
                    chance = chance > 0 ? chance : 0;
                    if (Math.random() <= chance) {
                        double damageMod = (SkillConfigManager.getUseSetting(hero, skill, "damage-multiplier", 0.2, false) +
                                (SkillConfigManager.getUseSetting(hero, skill, "damage-multiplier-increase", 0.0, false) * hero.getSkillLevel(skill)));
                        damageMod = damageMod > 0 ? damageMod : 0;
                        event.setDamage((int) (event.getDamage() * damageMod));
                    }
                }
            } else if (edby.getDamager() instanceof Projectile) {
                if (((Projectile) edby.getDamager()).getShooter() instanceof Player) {
                    Player player = (Player) ((Projectile) edby.getDamager()).getShooter();
                    Hero hero = plugin.getCharacterManager().getHero(player);

                    if (hero.hasEffect("Critical")) {
                        double chance = (SkillConfigManager.getUseSetting(hero, skill, Setting.CHANCE.node(), 0.2, false) +
                                (SkillConfigManager.getUseSetting(hero, skill, Setting.CHANCE_LEVEL.node(), 0.0, false) * hero.getSkillLevel(skill))) * 100;
                        chance = chance > 0 ? chance : 0;
                        if (Math.random() <= chance) {
                            double damageMod = (SkillConfigManager.getUseSetting(hero, skill, "damage-multiplier", 0.2, false) +
                                    (SkillConfigManager.getUseSetting(hero, skill, "damage-multiplier-increase", 0.0, false) * hero.getSkillLevel(skill)));
                            damageMod = damageMod > 0 ? damageMod : 0;
                            event.setDamage((int) (event.getDamage() * damageMod));
                            
                        }
                    }
                }
            }
        }
    }
}