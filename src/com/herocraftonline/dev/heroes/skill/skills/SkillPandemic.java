package com.herocraftonline.dev.heroes.skill.skills;


import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.effects.PeriodicExpirableEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillPandemic extends ActiveSkill {
    public String applyText;
    public String expireText;

    public SkillPandemic(Heroes plugin) {
        super(plugin, "Pandemic");
        setDescription("Drains $2hp or to 1hp from everyone within $1 blocks over $3s.");
        setUsage("/skill pandemic");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill pandemic"});

        setTypes(SkillType.HARMFUL, SkillType.DAMAGING, SkillType.SILENCABLE, SkillType.DEBUFF);
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(Setting.RADIUS.node(), 10);
        node.set("radius-increase", 0);
        node.set(Setting.DAMAGE.node(), 16);
        node.set("damage-increase", 0);
        node.set(Setting.PERIOD.node(), 2000);
        node.set(Setting.APPLY_TEXT.node(), "%target% has caught %hero%s %skill%!");
        node.set(Setting.EXPIRE_TEXT.node(), "%target% has recovered from %hero%s %skill%!");
        return node;
    }
    
    @Override
    public void init() {
        super.init();
        applyText = SkillConfigManager.getRaw(this, Setting.APPLY_TEXT.node(), "%target% has caught %hero%s %skill%!").replace("%hero%", "$1").replace("%target%", "$2").replace("%skill%", "$3");
        expireText = SkillConfigManager.getRaw(this, Setting.EXPIRE_TEXT.node(), "%target% has recovered from %hero%s %skill%!").replace("%hero%", "$1").replace("%target%", "$2").replace("%skill%", "$3");
    }

    @Override
    public String getDescription(Hero hero) {
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 10, false) +
                (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0, false) * hero.getLevel()));
        radius = radius > 0 ? radius : 0;
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, Setting.DURATION.node(), 10, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0, false) * hero.getLevel())) / 1000;
        duration = duration > 0 ? duration : 0;
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE.node(), 16, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0, false) * hero.getLevel()));
        damage = damage > 0 ? damage : 0;
        String description = getDescription().replace("$1", radius + "").replace("$2", damage + "").replace("$3", duration + "");
        
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
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE.node(), 16, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0, false) * hero.getLevel()));
        damage = damage > 0 ? damage : 0;
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 10, false) +
                (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0, false) * hero.getLevel()));
        radius = radius > 0 ? radius : 0;
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, Setting.DURATION.node(), 10, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0, false) * hero.getLevel()));
        duration = duration > 0 ? duration : 0;
        long period = (long) SkillConfigManager.getUseSetting(hero, this, Setting.PERIOD.node(), 2000, false);
        PandemicEffect pe = new PandemicEffect(this, duration, period, damage, player);
        for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof Monster) {
                Monster m = (Monster) e;
                damageEntity(m, player, damage, DamageCause.MAGIC);
                //m.damage(damage, player);
            } else if (e instanceof Player) {
                Player p = (Player) e;
                plugin.getHeroManager().getHero(p).addEffect(pe);
            }
        }
        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }
    
    public class PandemicEffect extends PeriodicExpirableEffect {
        private final int damage;
        private final Player caster;
        public PandemicEffect(Skill skill, long duration, long period, int damage, Player caster) {
            super(skill, "Pandemic", period, duration);
            int numberOfTicks = (int) (duration / period);
            this.damage = damage / numberOfTicks;
            this.caster = caster;
        }
        
        @Override
        public void apply(Hero hero) {
            super.apply(hero);
            Player p = hero.getPlayer();
            broadcast(p.getLocation(), applyText, caster.getDisplayName(), p.getDisplayName(), "Pandemic");
        }
        
        @Override
        public void remove(Hero hero) {
            super.remove(hero);
            Player p = hero.getPlayer();
            broadcast(p.getLocation(), expireText, caster.getDisplayName(), p.getDisplayName(), "Pandemic");
        }
        
        @Override
        public void tick(Hero hero) {
            super.tick(hero);
            if (hero.getHealth() - damage > 1) {
                damageEntity(hero.getPlayer(), caster, damage, DamageCause.MAGIC);
                //hero.getPlayer().damage(damage, caster);
            }
        }
    }
    

}