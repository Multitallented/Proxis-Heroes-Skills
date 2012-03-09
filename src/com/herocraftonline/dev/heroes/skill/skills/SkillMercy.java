package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.Effect;
import com.herocraftonline.heroes.characters.effects.EffectType;
import org.bukkit.entity.Player;

import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillMercy extends ActiveSkill {
    

    public SkillMercy(Heroes plugin) {
        super(plugin, "Mercy");
        setDescription("Toggle-able passive that stops pvp with you and anyone not level $1-$2");
        setUsage("/skill mercy");
        setArgumentRange(0, 0);
        setIdentifiers("skill mercy");

        setTypes(SkillType.COUNTER);
        Bukkit.getServer().getPluginManager().registerEvents(new EntityDamageListener(this), plugin);
        //registerEvent(Type.ENTITY_DAMAGE, new EntityDamageListener(this), Priority.Normal);
    }

    @Override
    public String getDescription(Hero hero) {
        int levelRange = SkillConfigManager.getUseSetting(hero, this, "lvl-multiplier-range", 2, false);
        int lowerLimit = hero.getTieredLevel(hero.getHeroClass()) - levelRange;
        lowerLimit = lowerLimit > 0 ? lowerLimit : 0;
        int upperLimit = hero.getTieredLevel(hero.getHeroClass()) + levelRange;
        String description = getDescription().replace("$1", lowerLimit + "").replace("$2", upperLimit + "");
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set("lvl-multiplier-range", 2);
        return node;
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        if (hero.hasEffect("Mercy")) {
            hero.removeEffect(hero.getEffect("Mercy"));
        } else {
            hero.addEffect(new MercyEffect(this));
        }
        return SkillResult.NORMAL;
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
    
    public class EntityDamageListener implements Listener {
        private Skill skill;
        public EntityDamageListener(Skill skill) {
            this.skill = skill;
        }

        @EventHandler
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled())
                return;
            if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent edBy = (EntityDamageByEntityEvent) event;
                if (event.getEntity() instanceof Player) {
                    Hero tHero = plugin.getHeroManager().getHero((Player) event.getEntity());
                    Entity damager = edBy.getDamager(); 
                    if (event.getCause() == DamageCause.PROJECTILE) {
                        damager = ((Projectile)damager).getShooter();
                    }
                    if (!(damager instanceof Player)) {
                        return;
                    }
                    Hero hero = plugin.getHeroManager().getHero((Player) damager);
                    //debug message
                    //Messaging.send(hero.getPlayer(), "$1, $2, $3, $4", !hero.hasEffect("Mercy"), hero.hasSkill("Mercy"), !tHero.hasEffect("Mercy"), tHero.hasSkill("Mercy"));
                    if ((!hero.hasEffect("Mercy") && hero.canUseSkill("Mercy")) || (!tHero.hasEffect("Mercy") && tHero.canUseSkill("Mercy"))) {
                        int heroLVL = hero.getTieredLevel(hero.getHeroClass());
                        int tHeroLVL = tHero.getTieredLevel(tHero.getHeroClass());
                        int expMultiplier = (int) SkillConfigManager.getUseSetting(tHero, skill, "lvl-multiplier-range", 2, false);
                        //debug message
                        //Messaging.send(hero.getPlayer(), "$1, $2, $3, $4, $5", heroLVL, tHeroLVL, expMultiplier, heroLVL / expMultiplier >= tHeroLVL, heroLVL * expMultiplier <= tHeroLVL);
                        if (heroLVL / expMultiplier >= tHeroLVL) {
                            event.setCancelled(true);
                            Messaging.send(hero.getPlayer(), "You ($2) are too strong to fight $1 ($3)", tHero.getPlayer().getDisplayName(), heroLVL, tHeroLVL);
                        } else if (heroLVL * expMultiplier <= tHeroLVL) {
                            event.setCancelled(true);
                            Messaging.send(hero.getPlayer(), "You ($2) are too weak to fight $1, ($3)", tHero.getPlayer().getDisplayName(), heroLVL, tHeroLVL);
                        }
                    }
                }
            }

        }
    }
}