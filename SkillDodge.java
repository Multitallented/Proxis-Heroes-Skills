package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.PassiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

public class SkillDodge extends PassiveSkill {

    public SkillDodge(Heroes plugin) {
        super(plugin, "Dodge");
        setDescription("Passive chance to dodge an enemy attack");
        setTypes(SkillType.COUNTER, SkillType.BUFF);
        
        registerEvent(Type.ENTITY_DAMAGE, new SkillHeroListener(), Priority.Normal);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("chance-to-dodge", .1);
        return node;
    }
    
    public class SkillHeroListener extends EntityListener {
        @Override
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled() || !(event.getEntity() instanceof Player))
                return;
            Player player = (Player) event.getEntity();
            Hero hero = getPlugin().getHeroManager().getHero(player);
            if (hero.hasEffect("Dodge")) {
                double chance = (double) getSetting(hero, "chance-to-dodge", .1, false);
                if (Math.random() <= chance) {
                    event.setDamage(0);
                    event.setCancelled(true);
                    broadcast(player.getLocation(), "$1 dodged an attack!", player.getDisplayName());
                }
            }
        }
    }
}