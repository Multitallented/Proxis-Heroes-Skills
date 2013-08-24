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
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class SkillReflect extends ActiveSkill implements Listener {
    
    public SkillReflect(Heroes plugin) {
        super(plugin, "Reflect");
        setDescription("Reflects damage dealt to you back on its source");
        setUsage("/skill reflect");
        setArgumentRange(0, 0);
        setIdentifiers(new String[] { "skill reflect" });
        Bukkit.getPluginManager().registerEvents(this, plugin);
        setTypes(SkillType.PHYSICAL, SkillType.HARMFUL);
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
        node.get(SkillSetting.DURATION.node(), 5000);
        node.get(SkillSetting.DURATION_INCREASE.node(), 0);
        node.get("percent-reflected", 100);
        node.get("percent-reflected-increase", 0);
        return node;
    }
    
    @Override
    public void init() {
        super.init();
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        long duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 5000, false)
                + (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION_INCREASE.node(), 0, false) * hero.getSkillLevel(this));
        duration = duration < 0 ? 0 : duration;
        hero.addEffect(new ReflectEffect(this, duration));
        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }

    public class ReflectEffect extends ExpirableEffect {
        public ReflectEffect(Skill skill, long duration) {
            super(skill, "Reflect", duration);
        }
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled() || (!(event.getEntity() instanceof Player)) || 
                (!(event instanceof EntityDamageByEntityEvent))) {
            return;
        }
        Player dPlayer = null;
        EntityDamageByEntityEvent edby = (EntityDamageByEntityEvent) event;
        if (edby.getDamager() instanceof Player) {
            dPlayer = (Player) edby.getDamager();
        } else if (edby.getDamager() instanceof Projectile) {
            if (((Projectile) edby.getDamager()).getShooter() instanceof Player) {
                dPlayer = (Player) ((Projectile) edby.getDamager()).getShooter();
            }
        }
        if (dPlayer == null) {
            return;
        }
        Player player = (Player) event.getEntity();
        Hero hero = plugin.getCharacterManager().getHero(player);
        if (!hero.hasEffect("Reflect")) {
            return;
        }
        double percent = (SkillConfigManager.getUseSetting(hero, this, "percent-reflected", 100, false)
                + (SkillConfigManager.getUseSetting(hero, this, "percent-reflected-increase", 0, false) * hero.getSkillLevel(this))) / 100;
        percent = percent < 0 ? 0 : percent;
        percent = percent > 1 ? 1 : percent;
        addSpellTarget(dPlayer, hero);
        damageEntity(dPlayer, player, (event.getDamage() * percent));
        if (event.getDamage() * percent >= event.getDamage()) {
            event.setCancelled(true);
        } else {
            event.setDamage((event.getDamage() * (1 - percent)));
        }
    }
}
