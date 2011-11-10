package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.effects.EffectType;
import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Messaging;
import java.util.List;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityListener;

public class SkillHunt extends ActiveSkill {

    private String huntText;

    public SkillHunt(Heroes plugin) {
        super(plugin, "Hunt");
        setDescription("Locates a player");
        setUsage("/skill hunt <player>");
        setArgumentRange(1, 1);
        setIdentifiers(new String[] { "skill hunt" });
        
        setTypes(SkillType.TELEPORT, SkillType.KNOWLEDGE, SkillType.SILENCABLE);
        registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("radius", 200);
        node.setProperty("hunter-nearby-text", "Someone is hunting players in your area");
        return node;
    }

    @Override
    public void init() {
        super.init();
        huntText = getSetting(null, "hunter-nearby-text", "Someone is hunting players in your area");
    }
    
    @Override
    public boolean use(Hero hero, String[] args) {
        Player player = hero.getPlayer();

        Player target = getPlugin().getServer().getPlayer(args[0]);
        if (target == null) {
            Messaging.send(player, "Target not found.");
            return false;
        }

        Location location = target.getLocation();
        int radius = (int) getSetting(hero, "radius", 200, false);
        int xRadius = (int) (Math.random()*radius);
        if (Math.random() > .5) {
            xRadius = xRadius *-1;
        }
        int x = location.getBlockX() + xRadius;
        //y = 122
        int zRadius = (int) ((Math.sqrt(radius*radius - xRadius*xRadius)));
        if (Math.random() > .5) {
            zRadius = zRadius *-1;
        }
        int z = location.getBlockZ() + zRadius;
        hero.getPlayer().teleport(getPlugin().getServer().getWorld(location.getWorld().getName()).getBlockAt(x, 122, z).getLocation());
        SafefallEffect fEffect = new SafefallEffect(this, 10000);
        hero.addEffect(fEffect);
        List<Entity> nearbyEntities = player.getNearbyEntities(200, 128, 200);
        for (Entity e : nearbyEntities) {
            if (e instanceof Player) {
                Messaging.send((Player) e, huntText);
            }
        }
        return true;
    }
    public class SafefallEffect extends ExpirableEffect {

        public SafefallEffect(Skill skill, long duration) {
            super(skill, "Safefall", duration);
            this.types.add(EffectType.DISPELLABLE);
            this.types.add(EffectType.BENEFICIAL);
        }

        @Override
        public void apply(Hero hero) {
            super.apply(hero);
        }

        @Override
        public void remove(Hero hero) {
            super.remove(hero);
        }

    }

    public class SkillEntityListener extends EntityListener {

        @Override
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled() || event.getCause() != DamageCause.FALL) {
                return;
            }

            Entity defender = event.getEntity();
            if (defender instanceof Player) {
                Player player = (Player) defender;
                Hero hero = plugin.getHeroManager().getHero(player);
                if (hero.hasEffect("Safefall")) {
                    event.setCancelled(true);
                }
            }
        }
    }

}