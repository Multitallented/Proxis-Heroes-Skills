package com.herocraftonline.dev.heroes.skill.skills;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.effects.common.StunEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.skill.TargettedSkill;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityListener;

public class SkillFalconPunch extends TargettedSkill {

    private Set<TNTPrimed> explosions = new HashSet<TNTPrimed>();
    public SkillFalconPunch(Heroes plugin) {
        super(plugin, "FalconPunch");
        setDescription("$1s stun $2 damage. R:$3");
        setUsage("/skill falconpunch");
        setArgumentRange(0, 0);
        setIdentifiers("skill falconpunch");
        setTypes(SkillType.PHYSICAL, SkillType.HARMFUL, SkillType.DAMAGING, SkillType.DEBUFF);
        PunchListener pl = new PunchListener(this);
        registerEvent(Type.ENTITY_EXPLODE, pl, Priority.Normal);
        registerEvent(Type.ENTITY_DAMAGE, pl, Priority.Normal);
    }

    @Override
    public String getDescription(Hero hero) {
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, Setting.DURATION.node(), 5000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getLevel())) / 1000;
        duration = duration > 0 ? duration : 0;
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE.node(), 22, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getLevel()));
        damage = damage > 0 ? damage : 0;
        int distance = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.MAX_DISTANCE.node(), 4, false) +
                (SkillConfigManager.getUseSetting(hero, this, Setting.MAX_DISTANCE_INCREASE.node(), 0.0, false) * hero.getLevel()));
        distance = distance > 0 ? distance : 0;
        String description = getDescription().replace("$1", duration + "").replace("$2", damage + "").replace("$3", distance + "");
        
        //COOLDOWN
        int cooldown = (SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN_REDUCE.node(), 0, false) * hero.getLevel()) / 1000;
        if (cooldown > 0) {
            description += " CD:" + cooldown + "s";
        }
        
        //MANA
        int mana = SkillConfigManager.getUseSetting(hero, this, Setting.MANA.node(), 10, false)
                - (SkillConfigManager.getUseSetting(hero, this, Setting.MANA_REDUCE.node(), 0, false) * hero.getLevel());
        if (mana > 0) {
            description += " M:" + mana;
        }
        
        //HEALTH_COST
        int healthCost = SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST, 0, false) - 
                (SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST_REDUCE, mana, true) * hero.getLevel());
        if (healthCost > 0) {
            description += " HP:" + healthCost;
        }
        
        //STAMINA
        int staminaCost = SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA.node(), 0, false)
                - (SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA_REDUCE.node(), 0, false) * hero.getLevel());
        if (staminaCost > 0) {
            description += " FP:" + staminaCost;
        }
        
        //DELAY
        int delay = SkillConfigManager.getUseSetting(hero, this, Setting.DELAY.node(), 0, false) / 1000;
        if (delay > 0) {
            description += " W:" + delay + "s";
        }
        
        //EXP
        int exp = SkillConfigManager.getUseSetting(hero, this, Setting.EXP.node(), 0, false);
        if (exp > 0) {
            description += " XP:" + exp;
        }
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(Setting.DURATION.node(), 5000);
        node.set("duration-increase", 0);
        node.set(Setting.DAMAGE.node(), 22);
        node.set("damage-increase", 0);
        node.set(Setting.MAX_DISTANCE.node(), 4);
        node.set(Setting.MAX_DISTANCE_INCREASE.node(), 0);
        node.set("use-explosions", false);
        node.set("explosion-block-damage", false);
        return node;
    }
    
    @Override
    public SkillResult use(Hero hero, LivingEntity target, String args[]) {
        Player player = hero.getPlayer();
        if (!(target instanceof Player)) {
            return SkillResult.INVALID_TARGET;
        }
        if (((Player) target).equals(player)) {
            return SkillResult.INVALID_TARGET;
        }
        Player tPlayer = (Player) target;
        if (!damageCheck(player, tPlayer)) {
            Messaging.send(player, "You can't harm that target");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        Hero tHero = plugin.getHeroManager().getHero(tPlayer);
        broadcastExecuteText(hero, target);
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, Setting.DURATION.node(), 5000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getLevel()));
        duration = duration > 0 ? duration : 0;
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE.node(), 22, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getLevel()));
        damage = damage > 0 ? damage : 0;
        if (duration > 0) {
            tHero.addEffect(new StunEffect(this, duration));
        }
        if (damage > 0) {
            tPlayer.damage(damage, player);
        }
        if (!SkillConfigManager.getUseSetting(hero, this, "use-explosions", false)) {
            return SkillResult.NORMAL;
        }
        TNTPrimed tnt = tPlayer.getWorld().spawn(tPlayer.getLocation(), TNTPrimed.class);
        tnt.setFuseTicks(1);
        if (explosions.size() > 5) {
            explosions = new HashSet<TNTPrimed>();
        }
        explosions.add(tnt);
        return SkillResult.NORMAL;
    }
    
    
    public class PunchListener extends EntityListener {
        private final SkillFalconPunch skill;

        public PunchListener(SkillFalconPunch aThis) {
            this.skill = aThis;
        }
        
        @Override
        public void onEntityExplode(EntityExplodeEvent event) {
            if (event.isCancelled() || explosions.isEmpty() || !(event.getEntity() instanceof TNTPrimed)) {
                return;
            }
            TNTPrimed tnt = (TNTPrimed) event.getEntity();
            if (!explosions.contains(tnt)) {
                return;
            }
            if (!SkillConfigManager.getRaw(skill, "explosion-block-damage", false)) {
                event.setCancelled(true);
                return;
            }
        }
        
        @Override
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled() || explosions.isEmpty() || !(event.getEntity() instanceof Player) || !(event instanceof EntityDamageByEntityEvent)
                    || event.getCause() != DamageCause.ENTITY_EXPLOSION) {
                return;
            }
            EntityDamageByEntityEvent edby = (EntityDamageByEntityEvent) event;
            if (!(edby.getDamager() instanceof TNTPrimed)) {
                return;
            }
            TNTPrimed tnt = (TNTPrimed) edby.getDamager();
            if (explosions.contains(tnt)) {
                event.setCancelled(true);
                return;
            }
        }
    }
}