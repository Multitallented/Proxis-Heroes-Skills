package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.classes.HeroClass.ExperienceType;
import com.herocraftonline.heroes.characters.effects.Effect;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.PeriodicExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import java.util.HashMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class SkillTether extends ActiveSkill {

    private String applyText;
    private String expireText;
    private HashMap<Player, Player> affectedPlayers = new HashMap<Player, Player>();
    
    public SkillTether(Heroes plugin) {
        super(plugin, "Tether");
        setDescription("Tethers enemies around you for $1s.");
        setUsage("/skill tether");
        setArgumentRange(0, 0);
        setIdentifiers(new String[] { "skill tether" });
        
        setTypes(SkillType.DEBUFF, SkillType.COUNTER, SkillType.MOVEMENT, SkillType.PHYSICAL);
    }

    @Override
    public String getDescription(Hero hero) {
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this))) / 1000;
        duration = duration > 0 ? duration : 0;
        String description = getDescription().replace("$1", duration + "");
        
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
        node.set("exp-per-creature-tethered", 0);
        node.set("exp-per-player-tethered", 0);
        node.set(SkillSetting.APPLY_TEXT.node(), "%target% was tethered!");
        node.set(SkillSetting.EXPIRE_TEXT.node(), "%target% got away!");
        return node;
    }
    
    @Override
    public void init() {
        super.init();
        applyText = SkillConfigManager.getUseSetting(null, this, SkillSetting.APPLY_TEXT.node(), "%target% was tethered!").replace("%target%", "$1");
        expireText = SkillConfigManager.getUseSetting(null, this, SkillSetting.EXPIRE_TEXT.node(), "%target% got away!").replace("%target%", "$1");
        
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        List<Entity> entities = hero.getPlayer().getNearbyEntities(3, 3, 3);
        Player player = hero.getPlayer();
        double expCreature = SkillConfigManager.getUseSetting(hero, this, "exp-per-creature-tethered", 0, false);
        double expPlayer = SkillConfigManager.getUseSetting(hero, this, "exp-per-player-tethered", 0, false);
        double exp = 0;
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this)));
        duration = duration > 0 ? duration : 0;
        for (Entity n : entities) {
            if (n instanceof Monster) {
                ((Monster) n).setTarget(hero.getPlayer());
                exp += expCreature;
            } else if (n instanceof Player && n != player && Skill.damageCheck(player, (LivingEntity) n)) {
                CurseEffect cEffect = new CurseEffect(this, duration, hero.getPlayer());
                Hero tHero = plugin.getCharacterManager().getHero((Player) n);
                tHero.addEffect(cEffect);
                exp += expPlayer;
            }
        }
        if (exp > 0) {
            if (hero.hasParty()) {
                hero.getParty().gainExp(exp, ExperienceType.SKILL, player.getLocation());
            } else {
                hero.gainExp(exp, ExperienceType.SKILL, player.getLocation());
            }
        }
        for (Effect e : hero.getEffects()) {
            if (e.isType(EffectType.DISABLE) || e.isType(EffectType.ROOT) || e.isType(EffectType.STUN)) {
                hero.removeEffect(e);
            }
            
        }
        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }
    
    public class CurseEffect extends PeriodicExpirableEffect {
        
        private Player caster;
        public CurseEffect(Skill skill, long duration, Player caster) {
            super(skill, "Tether", 20L, duration);
            this.types.add(EffectType.HARMFUL);
            this.types.add(EffectType.PHYSICAL);
            this.caster = caster;
        }

        @Override
        public void applyToHero(Hero hero) {
            super.applyToHero(hero);
            Player player = hero.getPlayer();
            affectedPlayers.put(player, caster);
            broadcast(player.getLocation(), applyText, player.getDisplayName());
        }

        @Override
        public void removeFromHero(Hero hero) {
            super.removeFromHero(hero);

            Player player = hero.getPlayer();
            if (affectedPlayers.containsKey(player)) {
                affectedPlayers.remove(player);
                broadcast(player.getLocation(), expireText, player.getDisplayName());
            }
        }
        
        @Override
        public void tickHero(Hero hero) {
            Player player = hero.getPlayer();
            try {
                if (player.getLocation().distance(caster.getLocation()) > 5) {
                    player.teleport(caster);
                }
            } catch (IllegalArgumentException iae) {
                
            }
        }

        @Override
        public void tickMonster(com.herocraftonline.heroes.characters.Monster mnstr) {
            //throw new UnsupportedOperationException("Not supported yet.");
        }
    }

}

