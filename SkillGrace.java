package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.HeroesEventListener;
import com.herocraftonline.dev.heroes.api.SkillDamageEvent;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.PassiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;
import java.util.Date;
import java.util.HashMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

public class SkillGrace extends PassiveSkill {
    
    private HashMap<Player, Date> hasDiedRecently = new HashMap<Player, Date>();
    private String skillBlockText;
    private String youBlockText;
    private String deathText;

    public SkillGrace(Heroes plugin) {
        super(plugin, "Grace");
        setDescription("disables pvp and spells for duration on death");
        setArgumentRange(0, 0);
        
	registerEvent(Type.ENTITY_DEATH, new EntityDeathListener(), Priority.Normal);
        registerEvent(Type.ENTITY_DAMAGE, new EntityDamageListener(), Priority.Normal);
        registerEvent(Type.CUSTOM_EVENT, new HeroesSkillListener(), Priority.Normal);
        
        setTypes(SkillType.COUNTER);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty(Setting.DURATION.node(), 300000);
        node.setProperty("enemy-disabled-damage-text", "%hero% has pvp immunity for %duration%");
        node.setProperty("you-disabled-damage-text", "You have pvp immunity for %duration%");
        node.setProperty("on-death-text", "You have gained pvp immunity for %duration%");
        return node;
    }
    
    @Override
    public void init() {
        super.init();
        skillBlockText = getSetting(null, "enemy-disabled-damage-text", "%hero% has pvp immunity for %duration%").replace("%hero%", "$1").replace("%duration%", "$2");
        youBlockText = getSetting(null, "you-disabled-damage-text", "You have pvp immunity for %duration%").replace("%duration%", "$1");
        deathText = getSetting(null, "on-death-text", "You have gained pvp immunity for %duration%").replace("%duration%", "$1");
    }
    
    public int pvpEnabled(Player player) {
        if (hasDiedRecently.containsKey(player)) {
            return (int) ((new Date().getTime() - hasDiedRecently.get(player).getTime())/1000);
        } else {
            return getSetting(getPlugin().getHeroManager().getHero(player), Setting.DURATION.node(), 300000, false) / 1000;
        }
    }
    public class EntityDeathListener extends EntityListener {

	
        @Override
        public void onEntityDeath(EntityDeathEvent event) {
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                hasDiedRecently.put(player, new Date());
                int duration = (int) getSetting(getPlugin().getHeroManager().getHero(player), Setting.DURATION.node(), 300000, false)/1000;
                Messaging.send(player, deathText, duration + " seconds");
            }
        }
    }
    
    public class EntityDamageListener extends EntityListener {

        @Override
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
                    Hero hero = getPlugin().getHeroManager().getHero(player);
                    Hero tHero = getPlugin().getHeroManager().getHero(tPlayer);
                    if (hero.hasEffect("Grace") || tHero.hasEffect("Grace")) {
                        int targetDuration = pvpEnabled(player);
                        int damagerDuration = pvpEnabled(tPlayer);
                        int duration = (int) getSetting(getPlugin().getHeroManager().getHero(player), Setting.DURATION.node(), 300000, false)/1000;
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
    public class HeroesSkillListener extends HeroesEventListener {

        @Override
        public void onSkillDamage(SkillDamageEvent event) {
            if (event.isCancelled())
                return;
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                Hero hero = getPlugin().getHeroManager().getHero(player);
                if (hero.hasEffect("Grace") || event.getDamager().hasEffect("Grace")) {
                    Player tPlayer = event.getDamager().getPlayer();
                    int targetDuration = pvpEnabled(player);
                    int damagerDuration = pvpEnabled(tPlayer);
                    int duration = (int) getSetting(getPlugin().getHeroManager().getHero(player), Setting.DURATION.node(), 300000, false)/1000;
                    if (targetDuration < duration) {
                        event.setCancelled(true);
                        Messaging.send(tPlayer, skillBlockText, (player).getDisplayName(), targetDuration + " seconds");
                    } else if (damagerDuration < duration) {
                        event.setCancelled(true);
                        Messaging.send(tPlayer, skillBlockText, (tPlayer).getDisplayName(), damagerDuration + " seconds");
                    }
                }
            }
        }
    }
    
}

