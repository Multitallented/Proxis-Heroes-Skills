package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillVoid extends ActiveSkill {

    private String applyText;
    private String expireText;

    public SkillVoid(Heroes plugin) {
        super(plugin, "Void");
        setDescription("Your pvp gets disabled for $1s.");
        setUsage("/skill void");
        setArgumentRange(0, 0);
        setIdentifiers(new String[] { "skill void" });
        Bukkit.getServer().getPluginManager().registerEvents(new SkillEntityListener(), plugin);
        //registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);
        
        setTypes(SkillType.COUNTER, SkillType.BUFF, SkillType.DARK);
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
        node.set(SkillSetting.DURATION.node(), 5000);
        node.set("duration-increase", 0);
        node.set(SkillSetting.APPLY_TEXT.node(), "%hero% became intangible!");
        node.set(SkillSetting.EXPIRE_TEXT.node(), "%hero% became tangible again!");
        return node;
    }

    @Override
    public void init() {
        super.init();
        applyText = SkillConfigManager.getUseSetting(null, this, SkillSetting.APPLY_TEXT.node(), "%hero% became intangible!").replace("%hero%", "$1");
        expireText = SkillConfigManager.getUseSetting(null, this, SkillSetting.EXPIRE_TEXT.node(), "%hero% became tangible again!").replace("%hero%", "$1");
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        broadcastExecuteText(hero);

        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this)));
        duration = duration > 0 ? duration : 0;
        hero.addEffect(new VoidEffect(this, duration));

        return SkillResult.NORMAL;
    }

    public class VoidEffect extends ExpirableEffect {

        public VoidEffect(Skill skill, long duration) {
            super(skill, "Void", duration);
            this.types.add(EffectType.INVULNERABILITY);
        }

        @Override
        public void applyToHero(Hero hero) {
            super.applyToHero(hero);
            Player player = hero.getPlayer();
            broadcast(player.getLocation(), applyText, player.getDisplayName());
        }

        @Override
        public void removeFromHero(Hero hero) {
            super.removeFromHero(hero);
            Player player = hero.getPlayer();
            broadcast(player.getLocation(), expireText, player.getDisplayName());
        }

    }

    public class SkillEntityListener implements Listener {

        @EventHandler
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled())
                return;
            Entity defender = event.getEntity();
            if (defender instanceof Player) {
                if (event instanceof EntityDamageByEntityEvent) {
                    EntityDamageByEntityEvent edby = (EntityDamageByEntityEvent) event;
                    Entity damager = edby.getDamager(); 
                    if (event.getCause() == DamageCause.PROJECTILE) {
                        damager = (Entity) ((Projectile)damager).getShooter();
                    }
                    if (!(damager instanceof Player)) {
                        return;
                    }
                    Hero tHero = plugin.getCharacterManager().getHero((Player) damager);
                    if (tHero.hasEffect("Void")) {
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}