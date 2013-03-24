package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.common.SilenceEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import org.bukkit.entity.Player;
import org.bukkit.Effect;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

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
        int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 30, false)
                + (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0, false) * hero.getSkillLevel(this));
        int duration = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0, false) * hero.getSkillLevel(this))) / 1000;
        int damage = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE.node(), 0, false) + 
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0, false) * hero.getSkillLevel(this)));
        String description = getDescription().replace("$1", radius + "").replace("$2", duration + "").replace("$3", damage + "");
        
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
    public SkillResult use(Hero hero, String[] args) {
        int radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 30, false)
                + (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0, false) * hero.getSkillLevel(this));
        int damage = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE.node(), 0, false) + 
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0, false) * hero.getSkillLevel(this)));
        long duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0, false) * hero.getSkillLevel(this));
        Player player = hero.getPlayer();
        for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
            if (e instanceof Creature) {
                Creature c = (Creature) e;
                damageEntity(c, player, damage, DamageCause.MAGIC);
                //c.damage(damage, player);
            } else if (e instanceof Player) {
                Player p = (Player) e;
                if (hero.hasParty() && hero.getParty().isPartyMember(plugin.getCharacterManager().getHero(p))) {
                    continue;
                }
                if (damageCheck(player, p)) {
                    damageEntity(p, player, damage, DamageCause.MAGIC);
                    //p.damage(damage, player);
                    Hero tHero = plugin.getCharacterManager().getHero(p);
                    tHero.addEffect(new SilenceEffect(this, duration));
                }
            }
        }
        player.getWorld().createExplosion(player.getLocation(), 0.0F, false);
        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }
}