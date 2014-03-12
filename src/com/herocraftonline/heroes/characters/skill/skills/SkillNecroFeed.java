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
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class SkillNecroFeed extends PassiveSkill {

    public SkillNecroFeed(Heroes plugin) {
        super(plugin, "NecroFeed");
        setDescription("Passive $1 health gain on enemy killed");
        setTypes(SkillType.COUNTER, SkillType.BUFF);
        Bukkit.getServer().getPluginManager().registerEvents(new SkillHeroListener(this), plugin);
    }

    @Override
    public String getDescription(Hero hero) {
        double health = (double) (SkillConfigManager.getUseSetting(hero, this, "health-percent", 0.5, false) +
                (SkillConfigManager.getUseSetting(hero, this, "health-percent-increase", 0.0, false) * hero.getSkillLevel(this)));
        health = health > 0 ? health : 0;
        String description = getDescription().replace("$1", health + "");
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set("health-percent", 1);
        node.set("health-percent-increase", 0);
        node.set(SkillSetting.USE_TEXT.node(), "%hero% feeds on an enemies corpse");
        return node;
    }
    
    public class SkillHeroListener implements Listener {
        private Skill skill;
        public SkillHeroListener(Skill skill) {
            this.skill = skill;
        }
        @EventHandler(ignoreCancelled = true)
        public void onEntityDeath(EntityDeathEvent event) {
            if (!(event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent)) {
                return;
            }
            EntityDamageByEntityEvent edby = (EntityDamageByEntityEvent) event.getEntity().getLastDamageCause();
            Player player = null;
            if (edby.getDamager().getClass().equals(Player.class)) {
                player = (Player) edby.getDamager();
            } else if (edby.getDamager() instanceof Projectile) {
                Entity e = (Entity) ((Projectile) edby.getDamager()).getShooter();
                if (!(e instanceof Player)) {
                    return;
                }
                player = (Player) ((Projectile) edby.getDamager()).getShooter();
            } else {
                return;
            }
            if (player == null) {
                return;
            }
            Hero hero = plugin.getCharacterManager().getHero(player);
            if (!hero.hasEffect("NecroFeed")) {
                return;
            }
            LivingEntity e;
            try {
                e = (LivingEntity) event.getEntity();
            } catch (Exception ex) {
                return;
            }
            double health = (double) (SkillConfigManager.getUseSetting(hero, skill, "health-percent", 0.5, false) +
                (SkillConfigManager.getUseSetting(hero, skill, "health-percent-increase", 0.0, false) * hero.getSkillLevel(skill)));
            health = health > 0 ? health : 0;
            double amount = health * e.getMaxHealth();
            if (player.getHealth() + amount > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            } else {
                player.setHealth(player.getHealth() + amount);
            }
            broadcast(player.getLocation(),
                    SkillConfigManager.getUseSetting(hero, skill, SkillSetting.USE_TEXT, "%hero% feeds on an enemies corpse").replace("%hero%", player.getDisplayName()));
        }
    }
}