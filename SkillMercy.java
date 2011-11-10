package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.HeroesEventListener;
import com.herocraftonline.dev.heroes.api.SkillDamageEvent;
import com.herocraftonline.dev.heroes.classes.HeroClass;
import com.herocraftonline.dev.heroes.effects.Effect;
import com.herocraftonline.dev.heroes.effects.EffectType;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Messaging;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityListener;

public class SkillMercy extends ActiveSkill {
    

    public SkillMercy(Heroes plugin) {
        super(plugin, "Mercy");
        setDescription("Disables PvP against weak and superior foes");
        setUsage("/skill mercy");
        setArgumentRange(0, 0);
        setIdentifiers("skill mercy");

        setTypes(SkillType.COUNTER);
        registerEvent(Type.ENTITY_DAMAGE, new EntityDamageListener(), Priority.Normal);
        registerEvent(Type.CUSTOM_EVENT, new HeroesSkillListener(), Priority.Normal);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("max-lvl", 20);
        node.setProperty("lvl-multiplier-range", 2);
        return node;
    }
    
    @Override
    public boolean use(Hero hero, String[] args) {
        if (hero.hasEffect("Mercy")) {
            hero.removeEffect(hero.getEffect("Mercy"));
        } else {
            hero.addEffect(new MercyEffect(this));
        }
        return true;
    }
    
    public class MercyEffect extends Effect {
        public MercyEffect(Skill skill) {
            super(skill, "Mercy");
            this.types.add(EffectType.BENEFICIAL);
        }
        
        @Override
        public void apply(Hero hero) {
            super.apply(hero);
            Player player = hero.getPlayer();
            broadcast(player.getLocation(), "$1 has disabled Mercy", player.getDisplayName());
        }

        @Override
        public void remove(Hero hero) {
            super.remove(hero);
            Player player = hero.getPlayer();
            broadcast(player.getLocation(), "$1 has enabled Mercy", player.getDisplayName());
        }
    }
    
    public class EntityDamageListener extends EntityListener {

        @Override
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled())
                return;
            if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent edBy = (EntityDamageByEntityEvent) event;
                if (event.getEntity() instanceof Player) {
                    Hero tHero = getPlugin().getHeroManager().getHero((Player) event.getEntity());
                    Entity damager = edBy.getDamager(); 
                    if (event.getCause() == DamageCause.PROJECTILE) {
                        damager = ((Projectile)damager).getShooter();
                    }
                    if (!(damager instanceof Player)) {
                        return;
                    }
                    Hero hero = getPlugin().getHeroManager().getHero((Player) damager);
                    if ((!hero.hasEffect("Mercy") && hero.hasSkill("Mercy")) || (!tHero.hasEffect("Mercy") && tHero.hasSkill("Mercy"))) {
                        HeroClass heroClass = hero.getHeroClass();
                        HeroClass tHeroClass = tHero.getHeroClass();
                        int heroLVL = hero.getLevel();
                        int tHeroLVL = tHero.getLevel();
                        int expMultiplier = (int) getSetting(tHero, "lvl-multiplier-range", 2, false);
                        while (heroClass.getParents().size() >= 1) {
                            heroClass = heroClass.getParents().get(0);
                            heroLVL += getSetting(hero, "max-lvl", 20, false);
                        }
                        while (tHeroClass.getParents().size() >= 1) {
                            tHeroClass = tHeroClass.getParents().get(0);
                            tHeroLVL += getSetting(tHero, "max-lvl", 20, false);
                        }
                        if (heroLVL / expMultiplier > tHeroLVL) {
                            event.setCancelled(true);
                            Messaging.send(hero.getPlayer(), "You ($2) are too strong to fight $1 ($3)", tHero.getPlayer().getDisplayName(), heroLVL, tHeroLVL);
                        } else if (heroLVL * expMultiplier < tHeroLVL) {
                            event.setCancelled(true);
                            Messaging.send(hero.getPlayer(), "You ($2) are too weak to fight $1, ($3)", tHero.getPlayer().getDisplayName(), heroLVL, tHeroLVL);
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
            if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
                Hero tHero = getPlugin().getHeroManager().getHero((Player) event.getEntity());
                Hero hero = getPlugin().getHeroManager().getHero((Player) event.getDamager());
                if ((hero.hasSkill("Mercy") && !hero.hasEffect("Mercy")) || (!tHero.hasEffect("Mercy") && tHero.hasSkill("Mercy"))) {
                    HeroClass heroClass = hero.getHeroClass();
                    HeroClass tHeroClass = tHero.getHeroClass();
                    int heroLVL = hero.getLevel();
                    int tHeroLVL = tHero.getLevel();
                    int expMultiplier = (int) getSetting(tHero, "lvl-multiplier-range", 2, false);
                    while (heroClass.getParents().size() >= 1) {
                        heroClass = heroClass.getParents().get(0);
                        heroLVL += getSetting(hero, "max-lvl", 20, false);
                    }
                    while (tHeroClass.getParents().size() >= 1) {
                        tHeroClass = tHeroClass.getParents().get(0);
                        tHeroLVL += getSetting(tHero, "max-lvl", 20, false);
                    }
                    if (heroLVL / expMultiplier > tHeroLVL) {
                        event.setCancelled(true);
                        Messaging.send(hero.getPlayer(), "You are too strong to fight $1, $2 : $3", tHero.getPlayer().getDisplayName(), heroLVL, tHeroLVL);
                    } else if (heroLVL * expMultiplier < tHeroLVL) {
                        event.setCancelled(true);
                        Messaging.send(hero.getPlayer(), "You are too weak to fight $1, $2 : $3", tHero.getPlayer().getDisplayName(), heroLVL, tHeroLVL);
                    }
                }
            }
        }
    }
}