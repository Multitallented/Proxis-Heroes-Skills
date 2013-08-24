package com.herocraftonline.heroes.characters.skill.skills;

import org.bukkit.entity.Player;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillCreepingPain extends TargettedSkill {
    private String applyText;
    private String expireText;
    private String missText;
    public HashMap<Player, Double> affectedPlayers = new HashMap<Player, Double>();

    public SkillCreepingPain(Heroes plugin) {
        super(plugin, "CreepingPain");
        setDescription("Multiplies the damage done to the target over $1s");
        setUsage("/skill creepingpain");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill creepingpain"});
        Bukkit.getServer().getPluginManager().registerEvents(new SkillEntityListener(), plugin);
        //registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);
        
        setTypes(SkillType.DEBUFF, SkillType.DAMAGING, SkillType.DARK, SkillType.SILENCABLE);
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
        node.set("no_effect_expire_text", "%target%s urge to throw up passes!");
        node.set(SkillSetting.APPLY_TEXT.node(), "%target% feels a sudden urge to throw up!");
        node.set(SkillSetting.EXPIRE_TEXT.node(), "%target% falls to his knees and pukes blood!");
        return node;
    }

    @Override
    public void init() {
        super.init();
        missText = SkillConfigManager.getUseSetting(null, this, "no_effect_expire_text", "%target%s urge to throw up passes!").replace("%target%", "$1");
        applyText = SkillConfigManager.getUseSetting(null, this, SkillSetting.APPLY_TEXT.node(), "%target% feels a sudden urge to throw up!").replace("%target%", "$1");
        expireText = SkillConfigManager.getUseSetting(null, this, SkillSetting.EXPIRE_TEXT.node(), "%target% falls to his knees and pukes blood!").replace("%target%", "$1");
    }

    @Override
    public SkillResult use(Hero hero, LivingEntity target, String[] args) {
        Player player = hero.getPlayer();
        if (target.equals(player) || target instanceof Creature) {
            return SkillResult.INVALID_TARGET;
        }

        if (target instanceof Player && hero.getParty() != null) {
            for (Hero h : hero.getParty().getMembers()) {
                if (target.equals(h.getPlayer())) {
                    return SkillResult.INVALID_TARGET;
                }
            }
        }
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this)));
        duration = duration > 0 ? duration : 0;
        CurseEffect cEffect = new CurseEffect(this, duration, player);
        if (target instanceof Player) {
            Hero tHero = plugin.getCharacterManager().getHero((Player) target);
            tHero.addEffect(cEffect);
            return SkillResult.NORMAL;
        }
        return SkillResult.INVALID_TARGET;
    }

    public class CurseEffect extends ExpirableEffect {
        
        private Player caster;
        public CurseEffect(Skill skill, long duration, Player caster) {
            super(skill, "CreepingPain", duration);
            this.types.add(EffectType.DISEASE);
            this.types.add(EffectType.POISON);
            this.types.add(EffectType.HARMFUL);
            this.types.add(EffectType.BLEED);
            this.caster = caster;
        }

        @Override
        public void applyToHero(Hero hero) {
            super.applyToHero(hero);
            Player player = hero.getPlayer();
            affectedPlayers.put(player, 0D);
            broadcast(player.getLocation(), applyText, player.getDisplayName());
        }

        @Override
        public void removeFromHero(Hero hero) {
            super.removeFromHero(hero);

            Player player = hero.getPlayer();
            if (affectedPlayers.containsKey(player)&& affectedPlayers.get(player) > 0) {
                addSpellTarget(player,plugin.getCharacterManager().getHero(caster));
                damageEntity(player, caster, affectedPlayers.get(player), DamageCause.MAGIC);
                //player.damage(affectedPlayers.get(player), caster);
                affectedPlayers.remove(player);
                broadcast(player.getLocation(), expireText, player.getDisplayName());
            } else {
                if (affectedPlayers.containsKey(player)) {
                    affectedPlayers.remove(player);
                }
                broadcast(player.getLocation(), missText, player.getDisplayName());
            }
        }
    }

    public class SkillEntityListener implements Listener {

        @EventHandler
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.getEntity() instanceof Player) {
                Hero hero = plugin.getCharacterManager().getHero((Player) event.getEntity());
                Player player = hero.getPlayer();
                if (hero.hasEffect("CreepingPain")) {
                    double damage = event.getDamage();
                    if (affectedPlayers.containsKey(player)) {
                        affectedPlayers.put(player, damage + affectedPlayers.get(player));
                    } else {
                        affectedPlayers.put(player, damage);
                    }
                }
            }
        }
    }
    

}