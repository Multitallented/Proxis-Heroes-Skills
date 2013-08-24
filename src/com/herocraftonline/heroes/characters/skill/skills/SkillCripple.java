package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.characters.Monster;
import org.bukkit.entity.LivingEntity;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.PeriodicExpirableEffect;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

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
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this))) / 1000;
        duration = duration > 0 ? duration : 0;
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE.node(), 5, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getSkillLevel(this)));
        damage = damage > 0 ? damage : 0;
        int tickDamage = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_TICK.node(), 2, false) +
                (SkillConfigManager.getUseSetting(hero, this, "tick-damage-increase", 0.0, false) * hero.getSkillLevel(this)));
        tickDamage = tickDamage > 0 ? tickDamage : 0;
        int maxDistance = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE.node(), 2, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        maxDistance = maxDistance > 0 ? maxDistance : 0;
        String description = getDescription().replace("$1", duration + "").replace("$2", damage + "").replace("$3", tickDamage + "");
        
        //COOLDOWN
        int cooldown = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN_REDUCE.node(), 0, false) * hero.getSkillLevel(this)) / 1000;
        if (cooldown > 0) {
            description += " CD:" + cooldown + "s";
        }
        
        //MANA
        int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA.node(), 10, false)
                - (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA_REDUCE.node(), 0, false) * hero.getSkillLevel(this));
        if (mana > 0) {
            description += " M:" + mana;
        }
        
        //HEALTH_COST
        int healthCost = SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_COST, 0, false) - 
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_COST_REDUCE, mana, true) * hero.getSkillLevel(this));
        if (healthCost > 0) {
            description += " HP:" + healthCost;
        }
        
        //STAMINA
        int staminaCost = SkillConfigManager.getUseSetting(hero, this, SkillSetting.STAMINA.node(), 0, false)
                - (SkillConfigManager.getUseSetting(hero, this, SkillSetting.STAMINA_REDUCE.node(), 0, false) * hero.getSkillLevel(this));
        if (staminaCost > 0) {
            description += " FP:" + staminaCost;
        }
        
        //DELAY
        int delay = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DELAY.node(), 0, false) / 1000;
        if (delay > 0) {
            description += " W:" + delay + "s";
        }
        
        //EXP
        int exp = SkillConfigManager.getUseSetting(hero, this, SkillSetting.EXP.node(), 0, false);
        if (exp > 0) {
            description += " XP:" + exp;
        }
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(SkillSetting.DURATION.node(), 10000);
        node.set("duration-increase", 0);
        node.set(SkillSetting.PERIOD.node(), 1000);
        node.set(SkillSetting.DAMAGE_TICK.node(), 2);
        node.set("tick-damage-increase", 0);
        node.set(SkillSetting.DAMAGE.node(), 5);
        node.set("damage-incrase", 0);
        node.set(SkillSetting.MAX_DISTANCE.node(), 15);
        node.set(SkillSetting.MAX_DISTANCE_INCREASE.node(), 0);
        node.set(SkillSetting.APPLY_TEXT.node(), "%target% has been Crippled by %hero%!");
        node.set("remove-text", "%target% has recovered from %hero%s Crippling blow!");
        return node;
    }
    
    @Override
    public void init() {
        super.init();
        applyText = SkillConfigManager.getUseSetting(null, this, SkillSetting.APPLY_TEXT.node(), "%target% has been Crippled by %hero%!").replace("%target%", "$1").replace("%hero%", "$2");
        removeText = SkillConfigManager.getUseSetting(null, this, "remove-text", "%target% has recovered from %hero%s Crippling blow!").replace("%target%", "$1").replace("%hero%", "$2");
    }

    @Override
    public SkillResult use(Hero hero, LivingEntity le, String[] strings) {
        Player player = hero.getPlayer();
        if (!(le.equals(player)) && le instanceof Player) {
            Hero tHero = plugin.getCharacterManager().getHero((Player) le);
            if (hero.getParty() == null || !(hero.getParty().getMembers().contains(tHero))) {
                if (damageCheck(player, tHero.getPlayer())) {
                    broadcastExecuteText(hero, le);
                    double damage = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE.node(), 5, false) +
                            (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getSkillLevel(this)));
                    damage = damage > 0 ? damage : 0;
                    addSpellTarget(tHero.getPlayer(),plugin.getCharacterManager().getHero(player));
                    damageEntity(tHero.getPlayer(), player, damage, DamageCause.ENTITY_ATTACK);
                    //tHero.getPlayer().damage(damage, player);
                    long duration = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 10000, false) +
                            (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this)));
                    duration = duration > 0 ? duration : 0;
                    long period = SkillConfigManager.getUseSetting(hero, this, SkillSetting.PERIOD.node(), 1000, false);
                    double tickDamage = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_TICK.node(), 2, false) +
                            (SkillConfigManager.getUseSetting(hero, this, "tick-damage-increase", 0.0, false) * hero.getSkillLevel(this)));
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
        private final double damageTick;
        public CrippleEffect(Skill skill, long period, long duration, double damageTick, Player caster) {
            super(skill, "Cripple", period, duration);
            this.caster=caster;
            this.damageTick = damageTick;
            this.types.add(EffectType.BLEED);
            this.types.add(EffectType.DISPELLABLE);
            this.types.add(EffectType.PHYSICAL);
        }
        
        @Override
        public void tickHero(Hero hero) {
            if (prevLocation != null
                    && Math.abs(hero.getPlayer().getLocation().getX() - prevLocation.getX()) >= 1
                    && Math.abs(hero.getPlayer().getLocation().getZ() - prevLocation.getZ()) >= 1) {
                addSpellTarget(hero.getPlayer(),plugin.getCharacterManager().getHero(caster));
                damageEntity(hero.getPlayer(), caster, damageTick, DamageCause.ENTITY_ATTACK);
                //hero.getPlayer().damage(damageTick, caster);
            }
            prevLocation = hero.getPlayer().getLocation();
        }
        
        @Override
        public void applyToHero(Hero hero) {
            super.applyToHero(hero);
            broadcast(hero.getPlayer().getLocation(), applyText, hero.getPlayer().getDisplayName(), caster.getDisplayName());
            this.prevLocation = hero.getPlayer().getLocation();
        }
        
        @Override
        public void removeFromHero(Hero hero) {
            super.removeFromHero(hero);
            broadcast(hero.getPlayer().getLocation(), removeText, hero.getPlayer().getDisplayName(), caster.getDisplayName());
        }

        @Override
        public void tickMonster(Monster mnstr) {
            super.tick(mnstr);
            addSpellTarget(mnstr.getEntity(),plugin.getCharacterManager().getHero(caster));
            damageEntity(mnstr.getEntity(), caster, damageTick, DamageCause.ENTITY_ATTACK);
        }
    }
}