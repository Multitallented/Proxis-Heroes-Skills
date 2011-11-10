package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.HeroesEventListener;
import com.herocraftonline.dev.heroes.api.SkillDamageEvent;
import com.herocraftonline.dev.heroes.effects.EffectType;
import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillVoid extends ActiveSkill {

    private String applyText;
    private String expireText;
    private String skillBlockText;

    public SkillVoid(Heroes plugin) {
        super(plugin, "Void");
        setDescription("Pvp disabled for duration!");
        setUsage("/skill void");
        setArgumentRange(0, 0);
        setIdentifiers(new String[] { "skill void" });

        registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);
        registerEvent(Type.CUSTOM_EVENT, new HeroesSkillListener(), Priority.Highest);
        
        setTypes(SkillType.COUNTER, SkillType.BUFF, SkillType.DARK);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty(Setting.DURATION.node(), 5000);
        node.setProperty(Setting.APPLY_TEXT.node(), "%hero% became intangible!");
        node.setProperty(Setting.EXPIRE_TEXT.node(), "%hero% became tangible again!");
        node.setProperty("skill-block-text", "%name% can't be hurt by %hero%'s %skill%.");
        return node;
    }

    @Override
    public void init() {
        super.init();
        applyText = getSetting(null, Setting.APPLY_TEXT.node(), "%hero% became intangible!").replace("%hero%", "$1");
        expireText = getSetting(null, Setting.EXPIRE_TEXT.node(), "%hero% became tangible again!").replace("%hero%", "$1");
        skillBlockText = getSetting(null, "skill-block-text", "%name% can't be hurt by %hero%'s %skill%.").replace("%name%", "$1").replace("%hero%", "$2").replace("%skill%", "$3");
    }

    @Override
    public boolean use(Hero hero, String[] args) {
        broadcastExecuteText(hero);

        int duration = getSetting(hero, Setting.DURATION.node(), 5000, false);
        hero.addEffect(new FlameshieldEffect(this, duration));

        return true;
    }

    public class FlameshieldEffect extends ExpirableEffect {

        public FlameshieldEffect(Skill skill, long duration) {
            super(skill, "Void", duration);
            this.types.add(EffectType.INVULNERABILITY);
        }

        @Override
        public void apply(Hero hero) {
            super.apply(hero);
            Player player = hero.getPlayer();
            broadcast(player.getLocation(), applyText, player.getDisplayName());
        }

        @Override
        public void remove(Hero hero) {
            Player player = hero.getPlayer();
            broadcast(player.getLocation(), expireText, player.getDisplayName());
        }

    }

    public class SkillEntityListener extends EntityListener {

        @Override
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled())
                return;
            Entity defender = event.getEntity();
            if (defender instanceof Player) {
                if (event instanceof EntityDamageByEntityEvent) {
                    EntityDamageByEntityEvent edby = (EntityDamageByEntityEvent) event;
                    Entity damager = edby.getDamager(); 
                    if (event.getCause() == DamageCause.PROJECTILE) {
                        damager = ((Projectile)damager).getShooter();
                    }
                    if (!(damager instanceof Player)) {
                        return;
                    }
                    Hero tHero = getPlugin().getHeroManager().getHero((Player) damager);
                    if (tHero.hasEffect("Void")) {
                        event.setCancelled(true);
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
                if (event.getDamager().hasEffect("Void")) {
                    String name = event.getDamager().getPlayer().getName();
                    String skillName = event.getSkill().getName().toLowerCase();
                    broadcast(event.getEntity().getLocation(), skillBlockText, new Object[] { player.getName(), name, skillName });
                    event.setCancelled(true);
                }
            }
        }
    }
}