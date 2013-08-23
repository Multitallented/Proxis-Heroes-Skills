package com.herocraftonline.heroes.characters.skill.skills;

/**
 *
 * @author Multitallented
 */

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class SkillMight extends ActiveSkill {
    
    public SkillMight(Heroes plugin) {
        super(plugin, "Might");
        setDescription("Grants bonus damage to you an allies nearby");
        setUsage("/skill might");
        setArgumentRange(0, 0);
        setIdentifiers(new String[] { "skill might" });
        setTypes(SkillType.BUFF);
    }

    @Override
    public String getDescription(Hero hero) {
        String description = getDescription();
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
        node.set(SkillSetting.RADIUS.node(), 7);
        node.set("give-damage", 10);
        return node;
    }
    
    @Override
    public void init() {
        super.init();
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS, 10, false);
        long duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 10000, false);
        hero.addEffect(new MightEffect(this, duration, hero));
        if (hero.hasParty()) {
            for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
                if (e instanceof Player) {
                    LivingEntity le = (LivingEntity) e;
                    Hero chero = plugin.getCharacterManager().getHero((Player) le);
                    if (hero.getParty().isPartyMember(chero) && !chero.equals(hero)) {
                        chero.addEffect(new MightEffect(this, duration, hero));
                    }
                }
            }
        }
        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }
    
    public class MightEffect extends ExpirableEffect {
        private Hero caster;
        public MightEffect(Skill skill, long duration, Hero caster) {
            super(skill, "Might", duration);
            this.caster = caster;
        }
        public Hero getCaster() {
            return caster;
        }
    }
    
    public class SkillHeroListener implements Listener {
        private Skill skill;
        public SkillHeroListener(Skill skill) {
            this.skill = skill;
        }
        @EventHandler
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK || event.isCancelled() || event.getDamage() == 0 || !(event.getEntity() instanceof Player) || !(event instanceof EntityDamageEvent)) {
                return;
            }
            EntityDamageByEntityEvent edby = (EntityDamageByEntityEvent) event;
            if (edby.getDamager() instanceof Player) {
                Player player = (Player) edby.getDamager();
                Hero hero = plugin.getCharacterManager().getHero(player);

                if (hero.hasEffect("Might")) {
                    double damage = SkillConfigManager.getUseSetting(((MightEffect) hero.getEffect("Might")).getCaster(), skill, "give-damage", 10, false);
                    event.setDamage(event.getDamage() + damage);
                }
            } else if (edby.getDamager() instanceof Projectile) {
                if (((Projectile) edby.getDamager()).getShooter() instanceof Player) {
                    Player player = (Player) ((Projectile) edby.getDamager()).getShooter();
                    Hero hero = plugin.getCharacterManager().getHero(player);

                    if (hero.hasEffect("Might")) {
                        double damage = SkillConfigManager.getUseSetting(((MightEffect) hero.getEffect("Might")).getCaster(), skill, "give-damage", 10, false);
                        event.setDamage(event.getDamage() + damage);
                    }
                }
            }
        }
    }
}