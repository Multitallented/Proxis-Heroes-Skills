package com.herocraftonline.dev.heroes.skill.skills;


import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.HeroRegainHealthEvent;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.effects.EffectType;
import com.herocraftonline.dev.heroes.effects.PeriodicHealEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class SkillVitalize extends ActiveSkill {
    private String applyText;
    private String expireText;

    public SkillVitalize(Heroes plugin) {
        super(plugin, "Vitalize");
        setDescription("You and party members within $3 gain $1 mana and $2 health $4 times.");
        setUsage("/skill vitalize");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill vitalize"});

        setTypes(SkillType.BUFF, SkillType.HEAL, SkillType.MANA);
    }

    @Override
    public String getDescription(Hero hero) {
        int manaTick = (int) (SkillConfigManager.getUseSetting(hero, this, "mana-tick", 4, false) +
                (SkillConfigManager.getUseSetting(hero, this, "mana-tick-increase", 0.0, false) * hero.getLevel()));
        manaTick = manaTick > 0 ? manaTick : 0;
        int healthTick = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_TICK.node(), 2, false) +
                (SkillConfigManager.getUseSetting(hero, this, "health-tick-increase", 0.0, false) * hero.getLevel()));
        healthTick = healthTick > 0 ? healthTick : 0;
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 10, false) +
                (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0.0, false) * hero.getLevel()));
        radius = radius > 0 ? radius : 0;
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, Setting.DURATION.node(), 12000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getLevel()));
        duration = duration > 0 ? duration : 0;
        long period = (long) SkillConfigManager.getUseSetting(hero, this, Setting.PERIOD.node(), 3000, false);
        int ticks = (int) (duration / period);
        String description = getDescription().replace("$1", manaTick + "").replace("$2", healthTick + "").replace("$3", radius + "").replace("$4", ticks + "");
        
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
        node.set("mana-tick", 4);
        node.set("mana-tick-increase", 0);
        node.set(Setting.RADIUS.node(), 10);
        node.set("radius-increase",0);
        node.set(Setting.DURATION.node(), 12000);
        node.set("duration-increase", 0);
        node.set(Setting.HEALTH_TICK.node(), 2);
        node.set("health-tick-increase", 0);
        node.set(Setting.PERIOD.node(), 3000);
        return node;
    }

    @Override
    public void init() {
        super.init();
        applyText = SkillConfigManager.getUseSetting(null, this, Setting.APPLY_TEXT.node(), "Your feel a bit wiser!");
        expireText = SkillConfigManager.getUseSetting(null, this, Setting.EXPIRE_TEXT.node(), "You no longer feel as wise!");
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, Setting.DURATION.node(), 12000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getLevel()));
        duration = duration > 0 ? duration : 0;
        int manaTick = (int) (SkillConfigManager.getUseSetting(hero, this, "mana-tick", 4, false) +
                (SkillConfigManager.getUseSetting(hero, this, "mana-tick-increase", 0.0, false) * hero.getLevel()));
        manaTick = manaTick > 0 ? manaTick : 0;
        int healthTick = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_TICK.node(), 2, false) +
                (SkillConfigManager.getUseSetting(hero, this, "health-tick-increase", 0.0, false) * hero.getLevel()));
        healthTick = healthTick > 0 ? healthTick : 0;
        long period = SkillConfigManager.getUseSetting(hero, this, Setting.PERIOD.node(), 3000, false);

        WisdomEffect mEffect = new WisdomEffect(this, period, duration, healthTick, player, manaTick);
        if (!hero.hasParty()) {
            if (hero.hasEffect("Vitalize")) {
                if (((WisdomEffect) hero.getEffect("Wisdom")).getManaMultiplier() > mEffect.getManaMultiplier()) {
                    Messaging.send(player, "You have a more powerful effect already!");
                }
            }
            hero.addEffect(mEffect);
        } else {
            int radius = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 10, false) +
                    (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0.0, false) * hero.getLevel()));
            radius = radius > 0 ? radius : 0;
            int rangeSquared = (int) Math.pow(radius, 2);
            for (Hero pHero : hero.getParty().getMembers()) {
                Player pPlayer = pHero.getPlayer();
                if (!pPlayer.getWorld().equals(player.getWorld())) {
                    continue;
                }
                if (pPlayer.getLocation().distanceSquared(player.getLocation()) > rangeSquared) {
                    continue;
                }
                if (pHero.hasEffect("Vitalize")) {
                    if (((WisdomEffect) pHero.getEffect("Vitalize")).getManaMultiplier() > mEffect.getManaMultiplier()) {
                        continue;
                    }
                }
                pHero.addEffect(mEffect);
            }
        }

        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }

    public class WisdomEffect extends PeriodicHealEffect {

        private final int manaMultiplier;

        public WisdomEffect(Skill skill, long period, long duration, int amount, Player applier, int manaMultiplier) {
            super(skill, "Vitalize", period, duration, amount, applier);
            this.manaMultiplier = manaMultiplier;
            this.types.add(EffectType.DISPELLABLE);
            this.types.add(EffectType.BENEFICIAL);
            this.types.add(EffectType.HEAL);
        }

        @Override
        public void apply(Hero hero) {
            super.apply(hero);
            Player player = hero.getPlayer();
            Messaging.send(player, applyText);
        }
        
        @Override
        public void tick(Hero hero) {
            super.tick(hero);
            HeroRegainHealthEvent hrhEvent = new HeroRegainHealthEvent(hero, super.getTickDamage(), skill);
            plugin.getServer().getPluginManager().callEvent(hrhEvent);
            if (hrhEvent.isCancelled())
                return;
            
            int addMana = hero.getMana() + manaMultiplier > 100 ? 100 - hero.getMana() : manaMultiplier;
            hero.setMana(addMana + hero.getMana());
            hero.setHealth(hero.getHealth() + hrhEvent.getAmount());
            hero.syncHealth();
        }
        
        public double getManaMultiplier() {
            return manaMultiplier;
        }

        @Override
        public void remove(Hero hero) {
            super.remove(hero);
            Player player = hero.getPlayer();
            Messaging.send(player, expireText);
        }
    }
    

}