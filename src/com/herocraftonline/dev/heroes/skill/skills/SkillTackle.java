package com.herocraftonline.dev.heroes.skill.skills;


import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.effects.common.StunEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.skill.TargettedSkill;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillTackle extends TargettedSkill {

    public SkillTackle(Heroes plugin) {
        super(plugin, "Tackle");
        setDescription("Teleport towards your target. Damage:$1, Radius:$2, Stun:$3s");
        setUsage("/skill tackle");
        setArgumentRange(0, 0);
        setIdentifiers("skill tackle");
        setTypes(SkillType.MOVEMENT, SkillType.SILENCABLE, SkillType.HARMFUL, SkillType.PHYSICAL, SkillType.DAMAGING, SkillType.DEBUFF);
    }

    @Override
    public String getDescription(Hero hero) {
        double damage = (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE.node(), 6, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getLevel()));
        damage = damage > 0 ? damage : 0;
        int radius = (int) ((SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 3, false) +
                (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0.0, false) * hero.getLevel())));
        radius = radius > 0 ? radius : 0;
        int duration = (int) ((SkillConfigManager.getUseSetting(hero, this, Setting.DURATION.node(), 3000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getLevel()))) / 1000;
        duration = duration > 0 ? duration : 0;
        String description = getDescription().replace("$1", damage + "").replace("$2", radius + "").replace("$3", duration + "");
        
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
        node.set(Setting.RADIUS.node(), 3);
        node.set("radius-increase", 0.0);
        node.set(Setting.DURATION.node(), 3000);
        node.set("duration-increase", 0);
        node.set(Setting.DAMAGE.node(), 6);
        node.set("damage-increase", 0.0);
        return node;
    }
    
    @Override
    public SkillResult use(Hero hero, LivingEntity target, String args[]) {
        Player player = hero.getPlayer();
        if (target instanceof Player && ((Player) target).equals(player)) {
            return SkillResult.INVALID_TARGET;
        }
        player.teleport(target.getLocation());
        broadcastExecuteText(hero, target);
        int radius = (int) ((SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 3, false) +
                (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0.0, false) * hero.getLevel())));
        radius = radius > 0 ? radius : 0;
        long duration = (long) ((SkillConfigManager.getUseSetting(hero, this, Setting.DURATION.node(), 3000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getLevel())));
        duration = duration > 0 ? duration : 0;
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE.node(), 6, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getLevel()));
        damage = damage > 0 ? damage : 0;
        for (Entity e : ((Entity) hero.getPlayer()).getNearbyEntities(radius, radius, radius)) {
            if (e instanceof Player) {
                Player tPlayer = (Player) e;
                Hero tHero = plugin.getHeroManager().getHero(tPlayer);
                if (damageCheck(player, tPlayer)) {
                    if (duration > 0) {
                        tHero.addEffect(new StunEffect(this, duration));
                    }
                    if (damage > 0) {
                        damageEntity(tPlayer, player, damage, DamageCause.MAGIC);
                        //tPlayer.damage(damage, player);
                    }
                }
            } else if (e instanceof Creature) {
                LivingEntity le = (LivingEntity) e;
                if (damage > 0) {
                    damageEntity(le, player, damage, DamageCause.MAGIC);
                    //le.damage(damage, player);
                }
            }
        }
        return SkillResult.NORMAL;
    }
}