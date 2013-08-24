package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass.ExperienceType;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillDeathFromAbove extends ActiveSkill {
    private String applyText;
    private String removeText;

    public SkillDeathFromAbove(Heroes plugin) {
        super(plugin, "DeathFromAbove");
        setDescription("For $1s, deals your fall damage times $2 to players within $3 blocks of you.");
        setUsage("/skill deathfromabove");
        setArgumentRange(0, 1);
        setIdentifiers("skill deathfromabove", "skill dfa");
        setTypes(SkillType.DAMAGING, SkillType.PHYSICAL, SkillType.MOVEMENT);
        Bukkit.getServer().getPluginManager().registerEvents(new DeathFromAboveListener(this), plugin);
        //registerEvent(Type.ENTITY_DAMAGE, new DeathFromAboveListener(this), Priority.Normal);
    }

    @Override
    public String getDescription(Hero hero) {
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this))) / 1000;
        duration = duration > 0 ? duration : 0;
        double damageMulti = (SkillConfigManager.getUseSetting(hero, this, "damage-multiplier", 1.0, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-multi-increase", 0.0, false) * hero.getSkillLevel(this)));
        damageMulti = damageMulti > 0 ? damageMulti : 0;
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 5, false) +
                (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0.0, false) * hero.getSkillLevel(this)));
        radius = radius > 0 ? radius : 0;
        String description = getDescription().replace("$1", duration + "").replace("$2", damageMulti + "").replace("$3", radius + "");
        
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
        node.set(SkillSetting.RADIUS.node(), 5);
        node.set("radius-increase", 0);
        node.set("damage-multiplier", 1.0);
        node.set("damage-multi-increase", 0);
        node.set("safefall", "true");
        node.set("exp-per-player-hit", 0);
        node.set("exp-per-creature-hit", 0);
        node.set(SkillSetting.APPLY_TEXT.node(), "%hero% is ready to pounce!");
        node.set("remove-text", "%hero% is not ready to pounce anymore!");
        return node;
    }
    
    @Override
    public void init() {
        super.init();
        applyText = SkillConfigManager.getUseSetting(null, this, SkillSetting.APPLY_TEXT.node(), "%hero% is ready to pounce!").replace("%hero%", "$1");
        removeText = SkillConfigManager.getUseSetting(null, this, "remove-text", "%hero% is not ready to pounce anymore!").replace("%hero%", "$1");
    }

    @Override
    public SkillResult use(Hero hero, String[] strings) {
        broadcastExecuteText(hero);
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this)));
        duration = duration > 0 ? duration : 0;
        hero.addEffect(new DeathFromAboveEffect(this, duration));
        return SkillResult.NORMAL;
    }

    public class DeathFromAboveEffect extends ExpirableEffect {
        public DeathFromAboveEffect(Skill skill, long duration) {
            super(skill, "DeathFromAbove", duration);
            this.types.add(EffectType.BENEFICIAL);
            this.types.add(EffectType.DISPELLABLE);
            this.types.add(EffectType.PHYSICAL);
        }
        @Override
        public void applyToHero(Hero hero) {
            super.applyToHero(hero);
            broadcast(hero.getPlayer().getLocation(), applyText, hero.getPlayer().getDisplayName());
        }
        
        @Override
        public void removeFromHero(Hero hero) {
            super.removeFromHero(hero);
            broadcast(hero.getPlayer().getLocation(), removeText, hero.getPlayer().getDisplayName());
        }
    }
    
    public class DeathFromAboveListener implements Listener {
        private Skill skill;
        public DeathFromAboveListener(Skill skill) {
            this.skill = skill;
        }
        @EventHandler
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled() || event.getDamage() == 0 || !(event.getEntity() instanceof Player)
                    || event.getCause() != DamageCause.FALL)
                return;
            Player player = (Player) event.getEntity();
            Hero hero = plugin.getCharacterManager().getHero(player);
            if (!hero.hasEffect("DeathFromAbove")) {
                return;
            }
            int radius = (int) (SkillConfigManager.getUseSetting(hero, skill, SkillSetting.RADIUS.node(), 5, false) +
                    (SkillConfigManager.getUseSetting(hero, skill, "radius-increase", 0.0, false) * hero.getSkillLevel(skill)));
            radius = radius > 0 ? radius : 0;
            double damage = event.getDamage();
            double damageMulti = (SkillConfigManager.getUseSetting(hero, skill, "damage-multiplier", 1.0, false) +
                    (SkillConfigManager.getUseSetting(hero, skill, "damage-multi-increase", 0.0, false) * hero.getSkillLevel(skill)));
            damageMulti = damageMulti > 0 ? damageMulti : 0;
            damage = (int) (damage * damageMulti);
            double expPlayer = SkillConfigManager.getUseSetting(hero, skill, "exp-per-player-hit", 0, false);
            double expCreature = SkillConfigManager.getUseSetting(hero, skill, "exp-per-creature-hit", 0, false);
            double exp = 0;
            for (Entity e : player.getNearbyEntities(radius,radius,radius)) {
                if (e instanceof Player && !(e.equals(player))) {
                    Player p = (Player) e;
                    addSpellTarget(p,plugin.getCharacterManager().getHero(player));
                    damageEntity(p, player, damage, DamageCause.MAGIC);
                    //p.damage(damage, player);
                    if (expPlayer > 0) {
                        exp += expPlayer;
                    }
                } else if (e instanceof Creature) {
                    Creature c = (Creature) e;
                    addSpellTarget(c,plugin.getCharacterManager().getHero(player));
                    damageEntity(c, player, damage, DamageCause.MAGIC);
                    //c.damage(damage, player);
                    if (expCreature > 0) {
                        exp += expCreature;
                    }
                }
            }
            if (exp > 0) {
                if (hero.hasParty()) {
                    hero.getParty().gainExp(exp, ExperienceType.SKILL, player.getLocation());
                } else {
                    hero.gainExp(exp, ExperienceType.SKILL, player.getLocation());
                }
            }
            if (SkillConfigManager.getUseSetting(hero, skill, "safefall", "true").equals("true")) {
                event.setDamage(0);
                event.setCancelled(true);
            }
        }
    }
}