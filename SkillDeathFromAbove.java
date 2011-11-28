package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.effects.EffectType;
import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityListener;

public class SkillDeathFromAbove extends ActiveSkill {
    private String applyText;
    private String removeText;

    public SkillDeathFromAbove(Heroes plugin) {
        super(plugin, "DeathFromAbove");
        setDescription("Deals all of your fall damage to players around you");
        setUsage("/skill deathfromabove");
        setArgumentRange(0, 1);
        setIdentifiers("skill deathfromabove", "skill dfa");
        setTypes(SkillType.DAMAGING, SkillType.PHYSICAL, SkillType.MOVEMENT);
        
        registerEvent(Type.ENTITY_DAMAGE, new DeathFromAboveListener(), Priority.Normal);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty(Setting.DURATION.node(), 10000);
        node.setProperty(Setting.RADIUS.node(), 5);
        node.setProperty("damage-multiplier", 1.0);
        node.setProperty("safefall", "true");
        node.setProperty(Setting.APPLY_TEXT.node(), "%hero% is ready to pounce!");
        node.setProperty("remove-text", "%hero% is not ready to pounce anymore!");
        return node;
    }
    
    @Override
    public void init() {
        super.init();
        applyText = getSetting(null, Setting.APPLY_TEXT.node(), "%hero% is ready to pounce!").replace("%hero%", "$1");
        removeText = getSetting(null, "remove-text", "%hero% is not ready to pounce anymore!").replace("%hero%", "$1");
    }

    @Override
    public SkillResult use(Hero hero, String[] strings) {
        broadcastExecuteText(hero);
        long duration = getSetting(hero, Setting.DURATION.node(), 10000, false);
        hero.addEffect(new DeathFromAboveEffect(this, duration));
        return SkillResult.NORMAL;
    }

    public class DeathFromAboveEffect extends ExpirableEffect {
        public DeathFromAboveEffect(Skill skill, long duration) {
            super(skill, "DeathFromAbove", duration);
            this.types.add(EffectType.BENEFICIAL);
            this.types.add(EffectType.DISPELLABLE);
            this.types.add(EffectType.PHYSICAL);
        }
        @Override
        public void apply(Hero hero) {
            super.apply(hero);
            broadcast(hero.getPlayer().getLocation(), applyText, hero.getPlayer().getDisplayName());
        }
        
        @Override
        public void remove(Hero hero) {
            super.remove(hero);
            broadcast(hero.getPlayer().getLocation(), removeText, hero.getPlayer().getDisplayName());
        }
    }
    
    public class DeathFromAboveListener extends EntityListener {
        @Override
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled() || event.getDamage() == 0 || !(event.getEntity() instanceof Player)
                    || event.getCause() != DamageCause.FALL)
                return;
            Player player = (Player) event.getEntity();
            Hero hero = getPlugin().getHeroManager().getHero(player);
            if (!hero.hasEffect("DeathFromAbove")) {
                return;
            }
            double radius = getSetting(hero, Setting.RADIUS.node(), 5, false);
            int damage = getSetting(hero, Setting.DAMAGE.node(), 5, false);
            damage = (int) (damage * getSetting(hero, "damage-multiplier", 1.0, false));
            for (Entity e : player.getNearbyEntities(radius,radius,radius)) {
                if (e instanceof Player && !(e.equals(player))) {
                    Player p = (Player) e;
                    p.damage(damage, player);
                } else if (e instanceof Creature) {
                    Creature c = (Creature) e;
                    c.damage(damage, player);
                }
            }
            if (getSetting(hero, "safefall", "true").equals("true")) {
                event.setDamage(0);
                event.setCancelled(true);
            }
        }
    }
}