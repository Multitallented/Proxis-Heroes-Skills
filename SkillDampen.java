package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.HeroesEventListener;
import com.herocraftonline.dev.heroes.api.SkillDamageEvent;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.PassiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityListener;

public class SkillDampen extends PassiveSkill {
    
    public String skillBlockText;

    public SkillDampen(Heroes plugin) {
        super(plugin, "Dampen");
        setDescription("Makes you invulnerable to opponents with low mana");
        setArgumentRange(0, 0);
        
        setTypes(SkillType.MANA, SkillType.COUNTER);

        registerEvent(Type.ENTITY_DAMAGE, new EntityDamageListener(), Priority.Normal);
        registerEvent(Type.CUSTOM_EVENT, new HeroesSkillListener(), Priority.Normal);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("block-if-mana-below", 15);
        node.setProperty("mana-required", 20);
        node.setProperty("block-text", "%name%s dampening field stopped %target%s attack!");
        return node;
    }
    
    @Override
    public void init() {
        super.init();
        skillBlockText = getSetting(null, "skill-block-text", "%name%s dampening field stopped %target%s attack!").replace("%name%", "$1").replace("%target%", "$2");
    }
    
    public class EntityDamageListener extends EntityListener {

        @Override
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled() || event.getDamage() == 0)
                return;
            if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent edBy = (EntityDamageByEntityEvent) event;
                if (event.getEntity() instanceof Player) {
                    Hero tHero = getPlugin().getHeroManager().getHero((Player) event.getEntity());
                    Entity damager = edBy.getDamager();
                    if (edBy.getCause() == DamageCause.PROJECTILE) {
                        damager = ((Projectile)damager).getShooter();
                    }
                    if (!(damager instanceof Player)) {
                        return;
                    }
                    Hero hero = getPlugin().getHeroManager().getHero((Player) damager);
                    int manaReq = (int) getSetting(hero, "block-if-mana-below", 15, false);
                    int minMana = (int) getSetting(hero, "mana-required", 20, false);
                    if (tHero.hasEffect("Dampen") && tHero.getMana() >= manaReq && hero.getMana() <= minMana) {
                        event.setCancelled(true);
                        broadcast(tHero.getPlayer().getLocation(), skillBlockText, tHero.getPlayer().getDisplayName(), hero.getPlayer().getDisplayName());
                    }
                }
            }

        }
    }

    public class HeroesSkillListener extends HeroesEventListener {

        @Override
        public void onSkillDamage(SkillDamageEvent event) {
            if (event.isCancelled() || event.getDamage() == 0)
                return;
            if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
                Hero tHero = getPlugin().getHeroManager().getHero((Player) event.getEntity());
                Hero hero = getPlugin().getHeroManager().getHero((Player) event.getDamager());
                int manaReq = (int) getSetting(hero, "block-if-mana-below", 15, false);
                int minMana = (int) getSetting(hero, "mana-required", 20, false);
                if (tHero.hasSkill("Dampen") && tHero.getMana() >= manaReq && hero.getMana() <= minMana) {
                    event.setCancelled(true);
                    broadcast(tHero.getPlayer().getLocation(), skillBlockText, tHero.getPlayer().getDisplayName(), hero.getPlayer().getDisplayName());
                }
            }
        }
    }
}