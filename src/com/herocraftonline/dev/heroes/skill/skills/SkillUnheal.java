package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Player;
import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.effects.EffectType;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.skill.TargettedSkill;
import com.herocraftonline.dev.heroes.util.Setting;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class SkillUnheal extends TargettedSkill {
    private String applyText;
    private String expireText;
    private String missText;
    public HashMap<Hero, Player> affectedPlayers = new HashMap<Hero, Player>();

    public SkillUnheal(Heroes plugin) {
        super(plugin, "Unheal");
        //this.plugin = plugin;
        setDescription("Causes all heals to become damage for $2s. R:$1");
        setUsage("/skill unheal");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill unheal"});
        Bukkit.getServer().getPluginManager().registerEvents(new SkillEntityListener(), plugin);
        //registerEvent(Type.ENTITY_REGAIN_HEALTH, new SkillEntityListener(), Priority.Normal);
        //registerEvent(Type.CUSTOM_EVENT, new SkillEventListener(), Priority.Normal);
        
        setTypes(SkillType.DEBUFF, SkillType.SILENCABLE, SkillType.DARK);
    }
    
    @Override
    public String getDescription(Hero hero) {
        int distance = SkillConfigManager.getUseSetting(hero, this, Setting.MAX_DISTANCE, 15, false) + 
                (SkillConfigManager.getUseSetting(hero, this, Setting.MAX_DISTANCE_INCREASE.node(), 0, false) * hero.getLevel());
        int duration = SkillConfigManager.getUseSetting(hero, this, Setting.DURATION.node(), 10000, false) -
                (SkillConfigManager.getUseSetting(hero, this, "duration-reduce", 0, false) * hero.getLevel());
        String description = getDescription().replace("$1", distance + "").replace("$2", duration + "");
        
        //COOLDOWN
        int cooldown = (SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN_REDUCE.node(), 0, false) * hero.getLevel()) / 1000;
        if (cooldown > 0) {
            description += " CD:" + cooldown + "s";
        }
        
        //MANA
        int mana = SkillConfigManager.getUseSetting(hero, this, Setting.MANA.node(), 10, false)
                - (SkillConfigManager.getUseSetting(hero, this, Setting.MANA_REDUCE.node(), 0, false) * hero.getLevel());
        if (mana > 0) {
            description += " M:" + mana;
        }
        
        //HEALTH_COST
        int healthCost = SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST, 0, false) - 
                (SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST_REDUCE, mana, true) * hero.getLevel());
        if (healthCost > 0) {
            description += " HP:" + healthCost;
        }
        
        //STAMINA
        int staminaCost = SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA.node(), 0, false)
                - (SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA_REDUCE.node(), 0, false) * hero.getLevel());
        if (staminaCost > 0) {
            description += " FP:" + staminaCost;
        }
        
        //DELAY
        int delay = SkillConfigManager.getUseSetting(hero, this, Setting.DELAY.node(), 0, false) / 1000;
        if (delay > 0) {
            description += " W:" + delay + "s";
        }
        
        //EXP
        int exp = SkillConfigManager.getUseSetting(hero, this, Setting.EXP.node(), 0, false);
        if (exp > 0) {
            description += " XP:" + exp;
        }
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(Setting.MAX_DISTANCE.node(), 15);
        node.set(Setting.MAX_DISTANCE_INCREASE.node(), 0);
        node.set(Setting.DURATION.node(), 10000);
        node.set("duration-reduce", 0);
        node.set("miss-text", "%target%s heal was inverted!");
        node.set(Setting.APPLY_TEXT.node(), "%target% has been cursed!");
        node.set(Setting.EXPIRE_TEXT.node(), "%target% has recovered from the curse!");
        return node;
    }

    @Override
    public void init() {
        super.init();
        missText = SkillConfigManager.getUseSetting(null, this, "miss-text", "%target%s heal was inverted!").replace("%target%", "$1");
        applyText = SkillConfigManager.getUseSetting(null, this, Setting.APPLY_TEXT.node(), "%target% has recovered from the curse!").replace("%target%", "$1");
        expireText = SkillConfigManager.getUseSetting(null, this, Setting.EXPIRE_TEXT.node(), "%target% has recovered from the poison!").replace("%target%", "$1");
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
        long duration = 10000;
        duration = SkillConfigManager.getUseSetting(hero, this, Setting.DURATION.node(), 10000, false) -
                (SkillConfigManager.getUseSetting(hero, this, "duration-reduce", 0, false) * hero.getLevel());
        CurseEffect cEffect = new CurseEffect(this, duration, player);
        if (target instanceof Player) {
            Hero tHero = plugin.getHeroManager().getHero((Player) target);
            tHero.addEffect(cEffect);
            return SkillResult.NORMAL;
        }
        return SkillResult.INVALID_TARGET;
    }

    public class CurseEffect extends ExpirableEffect {
        private final Player caster;

        public CurseEffect(Skill skill, long duration, Player caster) {
            super(skill, "Unheal", duration);
            this.caster = caster;
            this.types.add(EffectType.HARMFUL);
            this.types.add(EffectType.WOUNDING);
            this.types.add(EffectType.DARK);
            this.types.add(EffectType.DISPELLABLE);
        }

        @Override
        public void apply(Hero hero) {
            super.apply(hero);
            Player player = hero.getPlayer();
            affectedPlayers.put(hero, caster);
            broadcast(player.getLocation(), applyText, player.getDisplayName());
        }

        @Override
        public void remove(Hero hero) {
            super.remove(hero);

            Player player = hero.getPlayer();
            affectedPlayers.remove(hero);
            broadcast(player.getLocation(), expireText, player.getDisplayName());
        }
    }

    public class SkillEntityListener implements Listener {
        @EventHandler
        public void onEntityRegainHealth(EntityRegainHealthEvent event) {
            if (!(event.getEntity() instanceof Player))
                return;
            Hero hero = plugin.getHeroManager().getHero((Player) event.getEntity());
            if (hero.hasEffect("Unheal")) {
                int damage = event.getAmount();
                event.setAmount(0);
                event.setCancelled(true);
                addSpellTarget(hero.getPlayer(),hero);
                try {
                    damageEntity(hero.getPlayer(), affectedPlayers.get(hero), damage, DamageCause.MAGIC);
                } catch (Exception e) {
                    damageEntity(hero.getPlayer(), hero.getPlayer(), damage, DamageCause.MAGIC);
                }
                //hero.getPlayer().damage(damage, hero.getPlayer());
                broadcast(hero.getPlayer().getLocation(), missText, hero.getPlayer().getDisplayName());
            }
        }
    }
    

}