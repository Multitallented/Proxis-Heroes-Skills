package com.herocraftonline.dev.heroes.skill.skills;

import com.herocraftonline.dev.heroes.skill.SkillType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.effects.EffectType;
import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.TargettedSkill;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;

public class SkillSlow extends TargettedSkill {

    private String applyText;
    private String expireText;

    public SkillSlow(Heroes plugin) {
        super(plugin, "Slow");
        setDescription("Slows the target's movement speed & attack speed");
        setUsage("/skill slow");
        setArgumentRange(0, 1);
        setIdentifiers("skill slow");
        setTypes(SkillType.DEBUFF, SkillType.MOVEMENT, SkillType.SILENCABLE, SkillType.HARMFUL);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("speed-multiplier", 2);
        node.setProperty(Setting.DURATION.node(), 15000);
        node.setProperty("apply-text", "%hero% has been slowed!");
        node.setProperty("expire-text", "%hero% returned to normal speed!");
        node.setProperty(Setting.DAMAGE.node(), 0);
        return node;
    }

    @Override
    public void init() {
        super.init();
        applyText = getSetting(null, "apply-text", "%hero% has been slowed!").replace("%hero%", "$1");
        expireText = getSetting(null, "expire-text", "%hero% returned to normal speed!").replace("%hero%", "$1");
    }

    @Override
    public boolean use(Hero hero, LivingEntity target, String[] args) {
        if (!(target instanceof Player)) {
            Messaging.send(hero.getPlayer(), "Invalid Target!");
            return false;
        }
        Player player = hero.getPlayer();
        Player tPlayer = (Player) target;
        if (!damageCheck(player, tPlayer)) {
            Messaging.send(player, "You can't harm that target");
            return false;
        }
        tPlayer.damage((int) getSetting(hero, Setting.DAMAGE.node(), 0, false), player);
        int duration = getSetting(hero, Setting.DURATION.node(), 15000, false);
        int multiplier = getSetting(hero, "speed-multiplier", 2, false);
        if (multiplier > 20) {
            multiplier = 20;
        }
        SlowEffect effect = new SlowEffect(this, duration, multiplier);
        plugin.getHeroManager().getHero((Player) target).addEffect(effect);
        broadcastExecuteText(hero, target);
        return true;
    }

    public class SlowEffect extends ExpirableEffect {

        public SlowEffect(Skill skill, long duration, int amplifier) {
            super(skill, "Slow", duration);
            this.types.add(EffectType.DISPELLABLE);
            this.types.add(EffectType.HARMFUL);
            this.types.add(EffectType.SLOW);
            addMobEffect(2, (int) (duration / 1000) * 20, amplifier, false);
            addMobEffect(4, (int) (duration / 1000) * 20, amplifier, false);
        }

        @Override
        public void apply(Hero hero) {
            super.apply(hero);
            Player player = hero.getPlayer();
            broadcast(player.getLocation(), applyText, player.getDisplayName());
        }

        @Override
        public void remove(Hero hero) {
            super.remove(hero);
            Player player = hero.getPlayer();
            broadcast(player.getLocation(), expireText, player.getDisplayName());
        }
    }
}