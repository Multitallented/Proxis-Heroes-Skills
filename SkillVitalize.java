package com.herocraftonline.dev.heroes.skill.skills;


import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.HeroRegainHealthEvent;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.effects.EffectType;
import com.herocraftonline.dev.heroes.effects.PeriodicHealEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

public class SkillVitalize extends ActiveSkill {
    private String applyText;
    private String expireText;

    public SkillVitalize(Heroes plugin) {
        super(plugin, "Vitalize");
        setDescription("Gives you and your party mana and health");
        setUsage("/skill vitalize");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill vitalize"});

        setTypes(SkillType.BUFF, SkillType.HEAL, SkillType.MANA);
    }
    
    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("tick-mana", 4);
        node.setProperty(Setting.RADIUS.node(), 10);
        node.setProperty(Setting.DURATION.node(), 12000); // in Milliseconds - 10 minutes
        node.setProperty("tick-heal", 2);
        node.setProperty(Setting.PERIOD.node(), 3000);
        return node;
    }

    @Override
    public void init() {
        super.init();
        applyText = getSetting(null, Setting.APPLY_TEXT.node(), "Your feel a bit wiser!");
        expireText = getSetting(null, Setting.EXPIRE_TEXT.node(), "You no longer feel as wise!");
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        int duration = getSetting(hero, Setting.DURATION.node(), 600000, false);
        int manaMultiplier = getSetting(hero, "tick-mana", 4, false);
        int amount = getSetting(hero, "tick-heal", 2, false);
        long period = getSetting(hero, Setting.PERIOD.node(), 3000, false);

        WisdomEffect mEffect = new WisdomEffect(this, period, duration, amount, player, manaMultiplier);
        if (!hero.hasParty()) {
            if (hero.hasEffect("Vitalize")) {
                if (((WisdomEffect) hero.getEffect("Wisdom")).getManaMultiplier() > mEffect.getManaMultiplier()) {
                    Messaging.send(player, "You have a more powerful effect already!");
                }
            }
            hero.addEffect(mEffect);
        } else {
            int rangeSquared = (int) Math.pow(getSetting(hero, Setting.RADIUS.node(), 10, false), 2);
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