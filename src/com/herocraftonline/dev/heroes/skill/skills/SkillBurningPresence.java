package com.herocraftonline.dev.heroes.skill.skills;


import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.effects.EffectType;
import com.herocraftonline.dev.heroes.effects.PeriodicEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;
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
                (SkillConfigManager.getUseSetting(hero, this, "tick-damage-increase", 0.0, false) * hero.getLevel()));
        damage = damage > 0 ? damage : 0;
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 10, false) +
                (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0.0, false) * hero.getLevel()));
        radius = radius > 1 ? radius : 1;
        int mana = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.MANA.node(), 1, false) -
                (SkillConfigManager.getUseSetting(hero, this, Setting.MANA_REDUCE.node(), 0.0, false) * hero.getLevel()));
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
        node.set(Setting.PERIOD.node(), 5000);
        node.set(Setting.RADIUS.node(), 10);
        node.set("raidus-increase", 0);
        node.set(Setting.MANA.node(), 1);
        node.set(Setting.MANA_REDUCE.node(), 0.0);
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
            long period = SkillConfigManager.getUseSetting(hero, this, Setting.PERIOD.node(), 5000, false);
            int tickDamage = (int) (SkillConfigManager.getUseSetting(hero, this, "tick-damage", 1.0, false) +
                (SkillConfigManager.getUseSetting(hero, this, "tick-damage-increase", 0.0, false) * hero.getLevel()));
            tickDamage = tickDamage > 0 ? tickDamage : 0;
            int range = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 1.0, false) +
                (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0.0, false) * hero.getLevel()));
            range = range > 1 ? range : 1;
            int mana = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.MANA.node(), 1, false) -
                (SkillConfigManager.getUseSetting(hero, this, Setting.MANA_REDUCE.node(), 0.0, false) * hero.getLevel()));
            mana = mana > 0 ? mana : 0;
            hero.addEffect(new IcyAuraEffect(this, period, tickDamage, range, mana));
        }
        return SkillResult.NORMAL;
    }
    
    public class IcyAuraEffect extends PeriodicEffect {

        private int tickDamage;
        private int range;
        private int mana;

        public IcyAuraEffect(SkillBurningPresence skill, long period, int tickDamage, int range, int manaLoss) {
            super(skill, "BurningPresence", period);
            this.tickDamage = tickDamage;
            this.range = range;
            this.mana = manaLoss;
            this.types.add(EffectType.DISPELLABLE);
            this.types.add(EffectType.BENEFICIAL);
            this.types.add(EffectType.FIRE);
        }

        @Override
        public void apply(Hero hero) {
            super.apply(hero);
            Player player = hero.getPlayer();
            broadcast(player.getLocation(), applyText, player.getDisplayName(), "BurningPresence");
        }

        @Override
        public void remove(Hero hero) {
            super.remove(hero);
            Player player = hero.getPlayer();
            broadcast(player.getLocation(), expireText, player.getDisplayName(), "BurningPresence");
        }

        @Override
        public void tick(Hero hero) {
            super.tick(hero);

            Player player = hero.getPlayer();

            for (Entity entity : player.getNearbyEntities(range, range, range)) {
                if (entity instanceof LivingEntity) {
                    LivingEntity lEntity = (LivingEntity) entity;

                    //lEntity.damage(tickDamage, player);
                    damageEntity(lEntity, player, tickDamage, DamageCause.MAGIC);
                }
            }
            if (mana > 0) {
                if (hero.getMana() - mana < 0) {
                    hero.setMana(0);
                } else {
                    hero.setMana(hero.getMana() - mana);
                }
                if (hero.isVerbose()) {
                    Messaging.send(hero.getPlayer(), Messaging.createManaBar(100));
                }
            }
        }
    }
}