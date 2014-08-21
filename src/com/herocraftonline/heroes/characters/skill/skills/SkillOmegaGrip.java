package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.api.events.SkillUseEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.PeriodicExpirableEffect;
import com.herocraftonline.heroes.characters.effects.common.StunEffect;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.util.Messaging;
import com.herocraftonline.heroes.characters.skill.SkillSetting;

import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.inventory.ItemStack;

public class SkillOmegaGrip extends TargettedSkill implements Listener {
    private HashMap<Hero, Hero> grippingPlayers = new HashMap<Hero, Hero>();
    public SkillOmegaGrip(Heroes plugin) {
        super(plugin, "OmegaGrip");
        setDescription("Channeling, Stuns for $3s deals $1 + $2 & drains $4hp, $5mana every $6s.");
        setUsage("/skill omegagrip [target]");
        setArgumentRange(0, 1);
        setIdentifiers("skill omegagrip");
        
        setTypes(SkillType.SILENCABLE, SkillType.ILLUSION);
    }
    
    @Override
    public String getDescription(Hero hero) {
        int damage = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE.node(), 0, false) * hero.getLevel());
        damage = damage < 0 ? 0 : damage;
        
        
        int damageTick = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_TICK.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, "damage-tick-increase", 0, false) * hero.getLevel());
        damageTick = damageTick < 0 ? 0 : damageTick;
        
        int duration = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE.node(), 0, false) * hero.getLevel()) / 1000;
        duration = duration < 0 ? 0 : duration;
        int period = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.PERIOD.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, "period-increase", 0, false) * hero.getLevel()) / 1000;
        period = period < 0 ? 0 : period;
        int healthTick = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_TICK.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, "health-tick-increase", 0, false) * hero.getLevel());
        healthTick = healthTick < 0 ? 0 : healthTick;
        
        int manaTick = (SkillConfigManager.getUseSetting(hero, this, "mana-tick", 0, false)
                - SkillConfigManager.getUseSetting(hero, this, "mana-tick-increase", 0, false) * hero.getLevel());
        manaTick = manaTick < 0 ? 0 : manaTick;
        String description = getDescription().replace("$1", damage + "").replace("$2", damageTick + "").replace("$3", duration + "").replace("$4", healthTick + "").replace("$5", manaTick + "").replace("$6", period + "");
        
        //COOLDOWN
        int cooldown = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN_REDUCE.node(), 0, false) * hero.getLevel()) / 1000;
        cooldown = cooldown < 0 ? 0 : cooldown;
        if (cooldown > 0) {
            description += " CD:" + cooldown + "s";
        }
        
        //MANA
        int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA.node(), 10, false)
                - (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA_REDUCE.node(), 0, false) * hero.getLevel());
        if (mana > 0) {
            description += " M:" + mana;
        }
        
        //HEALTH_COST
        int healthCost = SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_COST, 0, false) - 
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_COST_REDUCE, mana, true) * hero.getLevel());
        if (healthCost > 0) {
            description += " HP:" + healthCost;
        }
        
        //STAMINA
        int staminaCost = SkillConfigManager.getUseSetting(hero, this, SkillSetting.STAMINA.node(), 0, false)
                - (SkillConfigManager.getUseSetting(hero, this, SkillSetting.STAMINA_REDUCE.node(), 0, false) * hero.getLevel());
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
    public void init() {
        super.init();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(SkillSetting.DAMAGE_TICK.node(), 1);
        node.set(SkillSetting.DAMAGE.node(), 0);
        node.set("damage-tick-increase", 0.0);
        node.set(SkillSetting.DAMAGE_INCREASE.node(), 0.0);
        node.set(SkillSetting.DURATION.node(), 10000);
        node.set(SkillSetting.DURATION_INCREASE.node(), 0);
        node.set(SkillSetting.PERIOD.node(), 1000);
        node.set("period-increase", 0);
        node.set(SkillSetting.HEALTH_TICK.node(), 0);
        node.set("health-tick-increase", 0);
        node.set("mana-tick", 0);
        node.set("mana-tick-increase", 0);
        node.set(SkillSetting.MAX_DISTANCE.node(),15);
        node.set(SkillSetting.MAX_DISTANCE_INCREASE.node(), 0);
        //node.set(Setting.APPLY_TEXT.node(), "%target% was gripped by %player%");
        node.set(SkillSetting.EXPIRE_TEXT.node(), "%player% released %target%");
        return node;
    }

    @Override
    public SkillResult use(Hero hero, LivingEntity le, String[] strings) {
        if (grippingPlayers.containsKey(hero)) {
            try {
                hero.removeEffect(hero.getEffect("Channel"));
            } catch (NullPointerException npe) {
                
            }
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        if (le.equals(hero.getPlayer()) || !(le instanceof Player)) {
            return SkillResult.INVALID_TARGET;
        }
        Player player = hero.getPlayer();
        Player tPlayer = (Player) le;
        Hero tHero = plugin.getCharacterManager().getHero(tPlayer);
        
        if (!damageCheck(player, tPlayer)) {
            player.sendMessage(ChatColor.GRAY + "You can't damage that target");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        double damage = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_INCREASE.node(), 0, false) * hero.getLevel());
        damage = damage < 0 ? 0 : damage;
        int damageTick = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE_TICK.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, "damage-tick-increase", 0, false) * hero.getLevel());
        damageTick = damageTick < 0 ? 0 : damageTick;
        int duration = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE.node(), 0, false) * hero.getLevel());
        duration = duration < 0 ? 0 : duration;
        int period = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.PERIOD.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, "period-increase", 0, false) * hero.getLevel());
        period = period < 0 ? 0 : period;
        int healthTick = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_TICK.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, "health-tick-increase", 0, false) * hero.getLevel());
        healthTick = healthTick < 0 ? 0 : healthTick;
        
        int manaTick = (SkillConfigManager.getUseSetting(hero, this, "mana-tick", 0, false)
                - SkillConfigManager.getUseSetting(hero, this, "mana-tick-increase", 0, false) * hero.getLevel());
        manaTick = manaTick < 0 ? 0 : manaTick;
        addSpellTarget(tPlayer,hero);
        damageEntity(tPlayer, player, damage);
        
        hero.addEffect(new ChannelEffect(this, duration, getReagentCost(hero)));
        tHero.addEffect(new GripEffect(this, duration, period, damageTick, healthTick, manaTick, hero));
        
        broadcastExecuteText(hero, tPlayer);
        grippingPlayers.put(hero, tHero);
        return SkillResult.INVALID_TARGET_NO_MSG;
    }
    
    public class ChannelEffect extends PeriodicExpirableEffect  {
        private Location loc;
        private final ItemStack reagent;
        public ChannelEffect(Skill skill, long duration, ItemStack reagent) {
            super(skill, "Channel", 100, duration);
            this.reagent = reagent;
            this.types.add(EffectType.BENEFICIAL);
        }

        @Override
        public void applyToHero(Hero hero) {
            super.applyToHero(hero);
            loc = hero.getPlayer().getLocation();
        }
        
        @SuppressWarnings("deprecation")
        @Override
        public void removeFromHero(Hero hero) {
            super.removeFromHero(hero);
            Player player = hero.getPlayer();
            long time = System.currentTimeMillis();
            int skillLevel = hero.getSkillLevel(skill);
            int cooldown = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN, 0, true);
            double coolReduce = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.COOLDOWN_REDUCE, 0.0, false) * skillLevel;
            cooldown -= (int) coolReduce;
            // Set cooldown
            if (cooldown > 0) {
                hero.setCooldown("OmegaGrip", time + cooldown);
                Heroes.debugLog(Level.INFO, hero.getName() + " used skill: " + getName() + " cooldown until: " + time + cooldown);
            }

            if (Heroes.properties.globalCooldown > 0) {
                hero.setCooldown("global", Heroes.properties.globalCooldown + time);
            }
            
            int manaCost = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA, 0, true);
            double manaReduce = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.MANA_REDUCE, 0.0, false) * skillLevel;
            manaCost -= (int) manaReduce;
            // Deduct mana
            hero.setMana(hero.getMana() - manaCost);
            if (hero.isVerbose() && manaCost > 0) {
                Messaging.send(hero.getPlayer(), ChatColor.BLUE + "MANA " + Messaging.createManaBar(hero.getMana(), hero.getMaxMana()));
            }

            int healthCost = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.HEALTH_COST, 0, true);
            double healthReduce = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.HEALTH_COST_REDUCE, 0.0, false) * skillLevel;
            healthCost -= (int) healthReduce;
            
            // Deduct health
            if (healthCost > 0) {
                player.setHealth(player.getHealth() - healthCost);
            }

            int staminaCost = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.STAMINA, 0, true);
            double stamReduce = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.STAMINA_REDUCE, 0.0, false) * skillLevel;
            staminaCost -= (int) stamReduce;
            
            if (staminaCost > 0) {
                player.setFoodLevel(player.getFoodLevel() - staminaCost);
            }

            // Only charge the item cost if it's non-null
            if (reagent != null && reagent.getAmount() > 0) {
                player.getInventory().removeItem(reagent);
                player.updateInventory();
            }
            Hero tHero = grippingPlayers.get(hero);
            grippingPlayers.remove(hero);
            if (tHero != null && tHero.hasEffect("OmegaGrip")) {
                tHero.removeEffect(tHero.getEffect("OmegaGrip"));
            }
        }

        @Override
        public void tickMonster(Monster mnstr) {
        }

        @Override
        public void tickHero(Hero hero) {
            Location location = hero.getPlayer().getLocation();
            if (location == null)
                return;
            if (location.getX() != loc.getX() || location.getY() != loc.getY() || location.getZ() != loc.getZ()) {
                loc.setYaw(location.getYaw());
                loc.setPitch(location.getPitch());
                hero.getPlayer().teleport(loc);
            }
        }
        
    }
    
    public class GripEffect extends PeriodicExpirableEffect {
        private final double damageTick;
        private final double healTick;
        private final int manaTick;
        private final Hero caster;
        private final long aDuration;

        public GripEffect(Skill skill, long duration, long period, double damageTick, double healTick, int manaTick, Hero caster) {
            super(skill, "OmegaGrip", period, duration);
            this.damageTick = damageTick;
            this.aDuration = duration;
            this.healTick = healTick;
            this.manaTick = manaTick;
            this.caster = caster;
            this.types.add(EffectType.DISABLE);
            this.types.add(EffectType.DISPELLABLE);
            this.types.add(EffectType.STUN);
        }

        @Override
        public void applyToHero(Hero hero) {
            super.applyToHero(hero);
            //Player player = hero.getPlayer();
            //String applyText = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.APPLY_TEXT.node(), "%target% was gripped by %player%");
            //applyText = applyText.replace("%player%", caster.getPlayer().getDisplayName()).replace("%target%", hero.getPlayer().getDisplayName());
            hero.addEffect(new StunEffect(skill, aDuration));
            //broadcast(player.getLocation(), applyText, player.getDisplayName());
        }
        
        @Override
        public void removeFromHero(Hero hero) {
            super.removeFromHero(hero);
            Player player = hero.getPlayer();
            String expireText = SkillConfigManager.getUseSetting(hero, skill, SkillSetting.EXPIRE_TEXT.node(), "%player% released %target%");
            expireText = expireText.replace("%player%", ChatColor.WHITE + caster.getPlayer().getDisplayName() + ChatColor.GRAY).replace("%target%", ChatColor.WHITE + hero.getPlayer().getDisplayName() + ChatColor.GRAY);
            broadcast(player.getLocation(), expireText, player.getDisplayName());
            try {
                hero.removeEffect(hero.getEffect("Stun"));
            } catch (Exception e) {
                
            }
            if (grippingPlayers.containsKey(caster) && caster.hasEffect("Channel")) {
                caster.removeEffect(caster.getEffect("Channel"));
                grippingPlayers.remove(caster);
            }
        }
        
        @Override
        public void tickMonster(Monster mnstr) {
            
        }

        @Override
        public void tickHero(Hero hero) {
            if (caster.hasEffect("Stun") || caster.hasEffect("Silence") || !caster.hasEffect("Channel")) {
                hero.removeEffect(this);
                return;
            }
            addSpellTarget(hero.getPlayer(), caster);
            skill.damageEntity(hero.getPlayer(), caster.getPlayer(), damageTick + healTick);
            Bukkit.getPluginManager().callEvent(new EntityRegainHealthEvent(caster.getPlayer(), healTick, RegainReason.MAGIC));
            if (caster.getMana() + manaTick > caster.getMaxMana()) {
                caster.setMana(caster.getMaxMana());
            } else {
                caster.setMana(caster.getMana() + manaTick);
            }
            if (hero.getMana() - manaTick < 0) {
                hero.setMana(0);
            } else {
                hero.setMana(hero.getMana() - manaTick);
            }
        }
    }
    
    @EventHandler
    public void onSkillUse(SkillUseEvent event) {
        if (event.isCancelled() || !event.getHero().hasEffect("Channel")) {
            return;
        }
        Hero hero = event.getHero();
        if (!event.getSkill().getName().equals("OmegaGrip")) {
            event.setCancelled(true);
            hero.getPlayer().sendMessage(ChatColor.GRAY + "You can't use that. Use /skill omegagrip");
        }
        
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getCause() != DamageCause.ENTITY_ATTACK || !(event instanceof EntityDamageByEntityEvent)) {
            return;
        }
        EntityDamageByEntityEvent edbye = (EntityDamageByEntityEvent) event;
        Entity damager = edbye.getDamager();
        if (edbye.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) {
            damager = (Entity) ((Projectile)damager).getShooter();
        }
        if (!(damager instanceof Player)) {
            return;
        }
        if (plugin.getCharacterManager().getHero((Player) damager).hasEffect("Channel")) {
            event.setCancelled(true);
            event.setDamage(0);
        }
    }
    

}