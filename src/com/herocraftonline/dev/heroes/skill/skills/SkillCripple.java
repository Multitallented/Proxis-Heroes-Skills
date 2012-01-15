package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.LivingEntity;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.effects.EffectType;
import com.herocraftonline.dev.heroes.effects.PeriodicExpirableEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.skill.TargettedSkill;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class SkillCripple extends TargettedSkill {
    private String applyText;
    private String removeText;

    public SkillCripple(Heroes plugin) {
        super(plugin, "Cripple");
        setDescription("Deal $2 damage, + $3 if they move for $1. R:$4");
        setUsage("/skill cripple [target]");
        setArgumentRange(0, 1);
        setIdentifiers("skill cripple");
        setTypes(SkillType.DEBUFF, SkillType.DAMAGING, SkillType.PHYSICAL);
    }

    @Override
    public String getDescription(Hero hero) {
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, Setting.DURATION.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getLevel())) / 1000;
        duration = duration > 0 ? duration : 0;
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE.node(), 5, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getLevel()));
        damage = damage > 0 ? damage : 0;
        int tickDamage = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE_TICK.node(), 2, false) +
                (SkillConfigManager.getUseSetting(hero, this, "tick-damage-increase", 0.0, false) * hero.getLevel()));
        tickDamage = tickDamage > 0 ? tickDamage : 0;
        int maxDistance = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.MAX_DISTANCE.node(), 2, false) +
                (SkillConfigManager.getUseSetting(hero, this, Setting.MAX_DISTANCE_INCREASE.node(), 0.0, false) * hero.getLevel()));
        maxDistance = maxDistance > 0 ? maxDistance : 0;
        String description = getDescription().replace("$1", duration + "").replace("$2", damage + "").replace("$3", tickDamage + "");
        
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
        node.set(Setting.DURATION.node(), 10000);
        node.set("duration-increase", 0);
        node.set(Setting.PERIOD.node(), 1000);
        node.set(Setting.DAMAGE_TICK.node(), 2);
        node.set("tick-damage-increase", 0);
        node.set(Setting.DAMAGE.node(), 5);
        node.set("damage-incrase", 0);
        node.set(Setting.MAX_DISTANCE.node(), 15);
        node.set(Setting.MAX_DISTANCE_INCREASE.node(), 0);
        node.set(Setting.APPLY_TEXT.node(), "%target% has been Crippled by %hero%!");
        node.set("remove-text", "%target% has recovered from %hero%s Crippling blow!");
        return node;
    }
    
    @Override
    public void init() {
        super.init();
        applyText = SkillConfigManager.getUseSetting(null, this, Setting.APPLY_TEXT.node(), "%target% has been Crippled by %hero%!").replace("%target%", "$1").replace("%hero%", "$2");
        removeText = SkillConfigManager.getUseSetting(null, this, "remove-text", "%target% has recovered from %hero%s Crippling blow!").replace("%target%", "$1").replace("%hero%", "$2");
    }

    @Override
    public SkillResult use(Hero hero, LivingEntity le, String[] strings) {
        Player player = hero.getPlayer();
        if (!(le.equals(player)) && le instanceof Player) {
            Hero tHero = plugin.getHeroManager().getHero((Player) le);
            if (hero.getParty() == null || !(hero.getParty().getMembers().contains(tHero))) {
                if (damageCheck(player, tHero.getPlayer())) {
                    broadcastExecuteText(hero, le);
                    int damage = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE.node(), 5, false) +
                            (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getLevel()));
                    damage = damage > 0 ? damage : 0;
                    tHero.getPlayer().damage(damage, player);
                    long duration = (long) (SkillConfigManager.getUseSetting(hero, this, Setting.DURATION.node(), 10000, false) +
                            (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getLevel()));
                    duration = duration > 0 ? duration : 0;
                    long period = SkillConfigManager.getUseSetting(hero, this, Setting.PERIOD.node(), 1000, false);
                    int tickDamage = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE_TICK.node(), 2, false) +
                            (SkillConfigManager.getUseSetting(hero, this, "tick-damage-increase", 0.0, false) * hero.getLevel()));
                    tickDamage = tickDamage > 0 ? tickDamage : 0;
                    CrippleEffect cEffect = new CrippleEffect(this, period, duration, tickDamage, player);
                    tHero.addEffect(cEffect);
                    return SkillResult.NORMAL;
                }
            }
        }
        return SkillResult.INVALID_TARGET;
    }

    public class CrippleEffect extends PeriodicExpirableEffect {
        private Player caster;
        private Location prevLocation;
        private final int damageTick;
        public CrippleEffect(Skill skill, long period, long duration, int damageTick, Player caster) {
            super(skill, "Cripple", period, duration);
            this.caster=caster;
            this.damageTick = damageTick;
            this.types.add(EffectType.BLEED);
            this.types.add(EffectType.DISPELLABLE);
            this.types.add(EffectType.PHYSICAL);
        }
        
        @Override
        public void tick(Hero hero) {
            super.tick(hero);
            if (prevLocation != null
                    && Math.abs(hero.getPlayer().getLocation().getX() - prevLocation.getX()) >= 1
                    && Math.abs(hero.getPlayer().getLocation().getZ() - prevLocation.getZ()) >= 1) {
                hero.getPlayer().damage(damageTick, caster);
            }
            prevLocation = hero.getPlayer().getLocation();
        }
        
        @Override
        public void apply(Hero hero) {
            super.apply(hero);
            broadcast(hero.getPlayer().getLocation(), applyText, hero.getPlayer().getDisplayName(), caster.getDisplayName());
            this.prevLocation = hero.getPlayer().getLocation();
        }
        
        @Override
        public void remove(Hero hero) {
            super.remove(hero);
            broadcast(hero.getPlayer().getLocation(), removeText, hero.getPlayer().getDisplayName(), caster.getDisplayName());
        }
    }
}