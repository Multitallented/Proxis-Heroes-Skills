package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.common.InvulnerabilityEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class SkillGuardianAngel extends ActiveSkill {

    public SkillGuardianAngel(Heroes plugin) {
        super(plugin, "GuardianAngel");
        setDescription("Gives you and your party within $1 blocks invuln for $2s");
        setUsage("/skill guardianangel");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill guardianangel"});

        setTypes(SkillType.BUFF, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        double radius = SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 10, false) +
                (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0.0, false) * hero.getSkillLevel(this));
        double duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 12000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0, false) * hero.getSkillLevel(this));
        String description = getDescription().replace("$1", radius + "").replace("$2", duration + "");
        
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
        node.set(SkillSetting.RADIUS.node(), 10);
        node.set("radius-increase", 0);
        node.set(SkillSetting.DURATION.node(), 12000); // in Milliseconds - 10 minutes
        node.set("duration-increase", 0);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        long duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 12000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0, false) * hero.getSkillLevel(this));

        InvulnerabilityEffect iEffect = new InvulnerabilityEffect(this, duration);
        if (!hero.hasParty()) {
            hero.addEffect(iEffect);
        } else {
            int rangeSquared = (int) Math.pow(SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 10, false) +
                (SkillConfigManager.getUseSetting(hero, this, "radius-increase", 0.0, false) * hero.getSkillLevel(this)), 2);
            for (Hero pHero : hero.getParty().getMembers()) {
                Player pPlayer = pHero.getPlayer();
                if (!pPlayer.getWorld().equals(player.getWorld())) {
                    continue;
                }
                if (pPlayer.getLocation().distanceSquared(player.getLocation()) > rangeSquared) {
                    continue;
                }
                pHero.addEffect(iEffect);
            }
        }

        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }

    

}