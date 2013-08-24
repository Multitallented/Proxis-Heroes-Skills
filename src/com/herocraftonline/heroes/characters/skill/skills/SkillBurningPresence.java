package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.PeriodicEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillBurningPresence extends ActiveSkill {
    private String applyText;
    private String expireText;

    public SkillBurningPresence(Heroes plugin) {
        super(plugin, "BurningPresence");
        setDescription("Toggle-able passive $1 aoe damage $2 blocks around self.");
        setUsage("/skill burningpresence");
        setArgumentRange(0, 0);
        setIdentifiers("skill burningpresence", "skill burnpres");
        setTypes(SkillType.FIRE, SkillType.SILENCABLE, SkillType.HARMFUL, SkillType.BUFF);
    }

    @Override
    public String getDescription(Hero hero) {
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, "tick-damage", 1.0, false) +
                (SkillConfigManager.getUseSetting(hero, this, "tick-damage-increase", 0.0, false) * hero.getSkillLevel(this)));
        damage = damage > 0 ? damage : 0;
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 10, false) +
                (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0.0, false) * hero.getSkillLevel(this)));
        radius = radius > 1 ? radius : 1;
        int mana = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA.node(), 1, false) -
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA_REDUCE.node(), 0.0, false) * hero.getSkillLevel(this)));
        mana = mana > 0 ? mana : 0;
        String description = getDescription().replace("$1", damage + "").replace("$2", radius + "");
        if (mana > 0) {
            description += " M:" + mana;
        }
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set("on-text", "%hero% heats the air in an %skill%!");
        node.set("off-text", "%hero% stops his %skill%!");
        node.set("tick-damage", 1);
        node.set("tick-damage-increase", 0);
        node.set(SkillSetting.PERIOD.node(), 5000);
        node.set(SkillSetting.RADIUS.node(), 10);
        node.set("raidus-increase", 0);
        node.set(SkillSetting.MANA.node(), 1);
        node.set(SkillSetting.MANA_REDUCE.node(), 0.0);
        return node;
    }
    
    @Override
    public void init() {
        super.init();
        applyText = SkillConfigManager.getRaw(this, "on-text", "%hero% heats the air in an %skill%!").replace("%hero%", "$1").replace("%skill%", "$2");
        expireText = SkillConfigManager.getRaw(this, "off-text", "%hero% stops his %skill%!").replace("%hero%", "$1").replace("%skill%", "$2");
    }
    
    @Override
    public SkillResult use(Hero hero, String args[]) {
        if (hero.hasEffect("BurningPresence")) {
            hero.removeEffect(hero.getEffect("BurningPresence"));
        } else {
            long period = SkillConfigManager.getUseSetting(hero, this, SkillSetting.PERIOD.node(), 5000, false);
            double tickDamage = (SkillConfigManager.getUseSetting(hero, this, "tick-damage", 1.0, false) +
                (SkillConfigManager.getUseSetting(hero, this, "tick-damage-increase", 0.0, false) * hero.getSkillLevel(this)));
            tickDamage = tickDamage > 0 ? tickDamage : 0;
            int range = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 1.0, false) +
                (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0.0, false) * hero.getSkillLevel(this)));
            range = range > 1 ? range : 1;
            int mana = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA.node(), 1, false) -
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA_REDUCE.node(), 0.0, false) * hero.getSkillLevel(this)));
            mana = mana > 0 ? mana : 0;
            hero.addEffect(new IcyAuraEffect(this, period, tickDamage, range, mana));
        }
        return SkillResult.NORMAL;
    }
    
    public class IcyAuraEffect extends PeriodicEffect {

        private double tickDamage;
        private int range;
        private int mana;
        private boolean firstTime = true;

        public IcyAuraEffect(SkillBurningPresence skill, long period, double tickDamage, int range, int manaLoss) {
            super(skill, "BurningPresence", period);
            this.tickDamage = tickDamage;
            this.range = range;
            this.mana = manaLoss;
            this.types.add(EffectType.DISPELLABLE);
            this.types.add(EffectType.BENEFICIAL);
            this.types.add(EffectType.FIRE);
        }

        @Override
        public void applyToHero(Hero hero) {
            firstTime = true;
            super.applyToHero(hero);
            Player player = hero.getPlayer();
            broadcast(player.getLocation(), applyText, player.getDisplayName(), "BurningPresence");
        }

        @Override
        public void removeFromHero(Hero hero) {
            super.removeFromHero(hero);
            Player player = hero.getPlayer();
            broadcast(player.getLocation(), expireText, player.getDisplayName(), "BurningPresence");
        }

        @Override
        public void tickHero(Hero hero) {
            super.tickHero(hero);
            Player player = hero.getPlayer();

            for (Entity entity : player.getNearbyEntities(range, range, range)) {
                if (entity instanceof LivingEntity) {
                    LivingEntity lEntity = (LivingEntity) entity;
                    
                    addSpellTarget(lEntity,hero);
                    damageEntity(lEntity, player, tickDamage, DamageCause.MAGIC);
                }
            }
            Location le = player.getLocation();
            for (int i = 0; i < 9; i++) {
                le.getWorld().playEffect(le, Effect.SMOKE, i);
            }
            for (int i = 0; i < 9; i++) {
                le.getWorld().playEffect(le, Effect.SMOKE, i);
            }
            for (int i = 0; i < 9; i++) {
                le.getWorld().playEffect(le, Effect.SMOKE, i);
            }
            if (mana > 0 && !firstTime) {
                if (hero.getMana() - mana < 0) {
                    hero.setMana(0);
                } else {
                    hero.setMana(hero.getMana() - mana);
                }
            } else if (firstTime) {
                firstTime = false;
            }
            if (hero.getMana() < mana) {
                hero.removeEffect(this);
            }
        }
    }
}