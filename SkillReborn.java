package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.PassiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

public class SkillReborn extends PassiveSkill {
    private String rebornText;

    public SkillReborn(Heroes plugin) {
        super(plugin, "Reborn");
        setDescription("Passive saves you from death if not on cooldown");
        setTypes(SkillType.COUNTER, SkillType.DARK);
        
        registerEvent(Type.ENTITY_DAMAGE, new RebornListener(), Priority.Normal);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("health-percent-on-rebirth", .5);
        node.setProperty("on-reborn-text", "%hero% is saved from death, but weakened!");
        node.setProperty(Setting.COOLDOWN.node(), 600000);
        return node;
    }
    
    @Override
    public void init() {
        super.init();
        rebornText = getSetting(null, "on-reborn-text", "%hero% is saved from death, but weakened!").replace("%hero%", "$1");
    }
    
    public class RebornListener extends EntityListener {
        @Override
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled() || !(event.getEntity() instanceof Player) || event.getDamage() == 0 ||
                    event.getDamage() < getPlugin().getHeroManager().getHero((Player) event.getEntity()).getHealth()) {
                return;
            }
            Player player = (Player) event.getEntity();
            Hero hero = getPlugin().getHeroManager().getHero(player);
            if (hero.hasEffect("Reborn")) {
                if (hero.getCooldown("Reborn") == null || hero.getCooldown("Reborn") <= System.currentTimeMillis()) {
                    event.setDamage(0);
                    event.setCancelled(true);
                    hero.setHealth((double) Math.round(hero.getMaxHealth() / 2));
                    hero.syncHealth();
                    long cooldown = (long) getSetting(hero, Setting.COOLDOWN.node(), 600000, false);
                    hero.setCooldown("Reborn", cooldown + System.currentTimeMillis());
                    broadcast(player.getLocation(),rebornText,player.getDisplayName());
                }
            }
        }
    }
}