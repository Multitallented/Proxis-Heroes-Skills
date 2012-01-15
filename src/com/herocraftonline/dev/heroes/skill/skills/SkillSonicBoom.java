package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Player;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.effects.common.SilenceEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.Effect;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;

public class SkillSonicBoom extends ActiveSkill {

    public SkillSonicBoom(Heroes plugin) {
        super(plugin, "SonicBoom");
        setDescription("$3 damage + $2s silence everyone within $1 blocks of you.");
        setUsage("/skill sonicboom");
        setArgumentRange(0, 0);
        setIdentifiers("skill sonicboom");
        setTypes(SkillType.MOVEMENT, SkillType.PHYSICAL);
    }
    
    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(Setting.DURATION.node(), 10000);
        node.set("duration-increase", 0);
        node.set(Setting.RADIUS.node(), 10);
        node.set("radius-increase", 0);
        node.set(Setting.DAMAGE.node(), 0);
        node.set("damage-increase", 0);
        return node;
    }

    @Override
    public String getDescription(Hero hero) {
        int radius = SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 30, false)
                + (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0, false) * hero.getLevel());
        int duration = (SkillConfigManager.getUseSetting(hero, this, Setting.DURATION.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0, false) * hero.getLevel()) / 1000);
        int damage = (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE.node(), 0, false) + 
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0, false) * hero.getLevel()));
        String description = getDescription().replace("$1", radius + "").replace("$2", duration + "").replace("$3", damage + "");
        
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
        int radius = SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 30, false)
                + (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0, false) * hero.getLevel());
        int damage = (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE.node(), 0, false) + 
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0, false) * hero.getLevel()));
        long duration = SkillConfigManager.getUseSetting(hero, this, Setting.DURATION.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0, false) * hero.getLevel());
        Player player = hero.getPlayer();
        for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof Creature) {
                Creature c = (Creature) e;
                c.damage(damage, player);
            } else if (e instanceof Player) {
                Player p = (Player) e;
                if (damageCheck(player, p)) {
                    p.damage(damage, player);
                    Hero tHero = plugin.getHeroManager().getHero(p);
                    tHero.addEffect(new SilenceEffect(this, duration));
                }
            }
        }
        player.getWorld().playEffect(player.getLocation(), Effect.SMOKE, 3);
        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }
}