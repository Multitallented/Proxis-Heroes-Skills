package com.herocraftonline.dev.heroes.skill.skills;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.PassiveSkill;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;
import java.util.Date;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;

public class SkillGrace extends PassiveSkill {
    
    private HashMap<Player, Date> hasDiedRecently = new HashMap<Player, Date>();
    private String skillBlockText;
    private String youBlockText;
    private String deathText;

    public SkillGrace(Heroes plugin) {
        super(plugin, "Grace");
        setDescription("Disables pvp and spells for $1s after death");
        setArgumentRange(0, 0);
        
        Bukkit.getServer().getPluginManager().registerEvents(new EntityDeathListener(this), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new EntityDamageListener(this), plugin);
	//registerEvent(Type.ENTITY_DEATH, new EntityDeathListener(this), Priority.Normal);
        //registerEvent(Type.ENTITY_DAMAGE, new EntityDamageListener(this), Priority.Normal);
        
        setTypes(SkillType.COUNTER);
    }

    @Override
    public String getDescription(Hero hero) {
        long duration = SkillConfigManager.getUseSetting(hero, this, Setting.DURATION.node(), 300000, false) / 1000;
        String description = getDescription().replace("$1", duration + "");
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(Setting.DURATION.node(), 300000);
        node.set("enemy-disabled-damage-text", "%hero% has pvp immunity for %duration%");
        node.set("you-disabled-damage-text", "You have pvp immunity for %duration%");
        node.set("on-death-text", "You have gained pvp immunity for %duration%");
        return node;
    }
    
    @Override
    public void init() {
        super.init();
        skillBlockText = SkillConfigManager.getUseSetting(null, this, "enemy-disabled-damage-text", "%hero% has pvp immunity for %duration%").replace("%hero%", "$1").replace("%duration%", "$2");
        youBlockText = SkillConfigManager.getUseSetting(null, this, "you-disabled-damage-text", "You have pvp immunity for %duration%").replace("%duration%", "$1");
        deathText = SkillConfigManager.getUseSetting(null, this, "on-death-text", "You have gained pvp immunity for %duration%").replace("%duration%", "$1");
    }
    
    public int pvpEnabled(Player player) {
        if (hasDiedRecently.containsKey(player)) {
            return (int) ((new Date().getTime() - hasDiedRecently.get(player).getTime())/1000);
        } else {
            return SkillConfigManager.getUseSetting(plugin.getHeroManager().getHero(player), this, Setting.DURATION.node(), 300000, false) / 1000;
        }
    }
    public class EntityDeathListener implements Listener {
        private Skill skill;
        public EntityDeathListener(Skill skill) {
            this.skill = skill;
        }
	
        @EventHandler
        public void onEntityDeath(EntityDeathEvent event) {
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                hasDiedRecently.put(player, new Date());
                int duration = (int) SkillConfigManager.getUseSetting(plugin.getHeroManager().getHero(player), skill, Setting.DURATION.node(), 300000, false)/1000;
                Messaging.send(player, deathText, duration + " seconds");
            }
        }
    }
    
    public class EntityDamageListener implements Listener {
        private Skill skill;
        public EntityDamageListener(Skill skill) {
            this.skill = skill;
        }
        @EventHandler
        public void onEntityDamage(EntityDamageEvent event){
            if (event.getEntity() instanceof Player){
                Player player = (Player) event.getEntity();
                if (event instanceof EntityDamageByEntityEvent){
                    EntityDamageByEntityEvent edbye = (EntityDamageByEntityEvent) event;
                    Entity damager = edbye.getDamager();
                    if (edbye.getCause() == DamageCause.PROJECTILE) {
                        damager = ((Projectile)damager).getShooter();
                    }
                    if (!(damager instanceof Player)) {
                        return;
                    }
                    Player tPlayer = (Player) damager;
                    Hero hero = plugin.getHeroManager().getHero(player);
                    Hero tHero = plugin.getHeroManager().getHero(tPlayer);
                    if (hero.hasEffect("Grace") || tHero.hasEffect("Grace")) {
                        int targetDuration = pvpEnabled(player);
                        int damagerDuration = pvpEnabled(tPlayer);
                        int duration = (int) SkillConfigManager.getUseSetting(plugin.getHeroManager().getHero(player), skill, Setting.DURATION.node(), 300000, false)/1000;
                        if (damagerDuration < duration || targetDuration < duration) {
                            if (targetDuration < duration){
                               Messaging.send((Player) damager, skillBlockText, player.getDisplayName(), duration - targetDuration + " seconds");
                            }
                            if (damagerDuration < duration){
                               Messaging.send((Player) damager, youBlockText, duration - damagerDuration + " seconds");
                            }
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }
    
}

