package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.api.events.HeroRegainHealthEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.PeriodicHealEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;
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
                (SkillConfigManager.getUseSetting(hero, this, "mana-tick-increase", 0.0, false) * hero.getSkillLevel(this)));
        manaTick = manaTick > 0 ? manaTick : 0;
        int healthTick = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_TICK.node(), 2, false) +
                (SkillConfigManager.getUseSetting(hero, this, "health-tick-increase", 0.0, false) * hero.getSkillLevel(this)));
        healthTick = healthTick > 0 ? healthTick : 0;
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 10, false) +
                (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0.0, false) * hero.getSkillLevel(this)));
        radius = radius > 0 ? radius : 0;
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 12000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this)));
        duration = duration > 0 ? duration : 0;
        long period = (long) SkillConfigManager.getUseSetting(hero, this, SkillSetting.PERIOD.node(), 3000, false);
        int ticks = (int) (duration / period);
        String description = getDescription().replace("$1", manaTick + "").replace("$2", healthTick + "").replace("$3", radius + "").replace("$4", ticks + "");
        
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
        node.set("mana-tick", 4);
        node.set("mana-tick-increase", 0);
        node.set(SkillSetting.RADIUS.node(), 10);
        node.set("radius-increase",0);
        node.set(SkillSetting.DURATION.node(), 12000);
        node.set("duration-increase", 0);
        node.set(SkillSetting.HEALTH_TICK.node(), 2);
        node.set("health-tick-increase", 0);
        node.set(SkillSetting.PERIOD.node(), 3000);
        return node;
    }

    @Override
    public void init() {
        super.init();
        applyText = SkillConfigManager.getUseSetting(null, this, SkillSetting.APPLY_TEXT.node(), "Your feel a bit wiser!");
        expireText = SkillConfigManager.getUseSetting(null, this, SkillSetting.EXPIRE_TEXT.node(), "You no longer feel as wise!");
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 12000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this)));
        duration = duration > 0 ? duration : 0;
        int manaTick = (int) (SkillConfigManager.getUseSetting(hero, this, "mana-tick", 4, false) +
                (SkillConfigManager.getUseSetting(hero, this, "mana-tick-increase", 0.0, false) * hero.getSkillLevel(this)));
        manaTick = manaTick > 0 ? manaTick : 0;
        double healthTick = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_TICK.node(), 2, false) +
                (SkillConfigManager.getUseSetting(hero, this, "health-tick-increase", 0.0, false) * hero.getSkillLevel(this)));
        healthTick = healthTick > 0 ? healthTick : 0;
        long period = SkillConfigManager.getUseSetting(hero, this, SkillSetting.PERIOD.node(), 3000, false);

        WisdomEffect mEffect = new WisdomEffect(this, period, duration, healthTick, player, manaTick);
        if (!hero.hasParty()) {
            hero.addEffect(mEffect);
        } else {
            int radius = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 10, false) +
                    (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0.0, false) * hero.getSkillLevel(this)));
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
                pHero.addEffect(mEffect);
            }
        }

        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }

    public class WisdomEffect extends PeriodicHealEffect {
        private final double amount;
        private final int manaMultiplier;

        public WisdomEffect(Skill skill, long period, long duration, double amount, Player applier, int manaMultiplier) {
            super(skill, "Vitalize", period, duration, amount, applier);
            this.manaMultiplier = manaMultiplier;
            this.amount=amount;
            this.types.add(EffectType.DISPELLABLE);
            this.types.add(EffectType.BENEFICIAL);
            this.types.add(EffectType.HEAL);
        }

        @Override
        public void applyToHero(Hero hero) {
            super.applyToHero(hero);
            Player player = hero.getPlayer();
            Messaging.send(player, applyText);
        }
        
        @Override
        public void tickHero(Hero hero) {
            super.tickHero(hero);
            HeroRegainHealthEvent hrhEvent = new HeroRegainHealthEvent(hero, amount, skill);
            plugin.getServer().getPluginManager().callEvent(hrhEvent);
            int addMana = hero.getMana() + manaMultiplier > hero.getMaxMana() ? hero.getMaxMana() - hero.getMana() : manaMultiplier;
            hero.setMana(addMana + hero.getMana());
        }

        @Override
        public void removeFromHero(Hero hero) {
            super.removeFromHero(hero);
            Player player = hero.getPlayer();
            Messaging.send(player, expireText);
        }
    }
    

}