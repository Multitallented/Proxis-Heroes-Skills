package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.SkillSetting;

import org.bukkit.entity.Player;
import org.bukkit.Effect;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Spider;

public class SkillSpider extends ActiveSkill {
    public SkillSpider(Heroes plugin) {
        super(plugin, "Spider");
        setDescription("$1% chance to spawn 1 spider, $2% for 2, and $3% for 3.");
        setUsage("/skill spider");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill spider"});
        
        setTypes(SkillType.DARK, SkillType.SUMMON, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        int chance2x = (int) (SkillConfigManager.getUseSetting(hero, this, "chance-2x", 0.2, false) * 100 +
                SkillConfigManager.getUseSetting(hero, this, "added-chance-2x-per-level", 0.0, false) * hero.getSkillLevel(this));
        int chance3x = (int) (SkillConfigManager.getUseSetting(hero, this, "chance-3x", 0.1, false) * 100 +
                SkillConfigManager.getUseSetting(hero, this, "added-chance-3x-per-level", 0.0, false) * hero.getSkillLevel(this));
        String description = getDescription().replace("$1", (100 - (chance2x + chance3x)) + "").replace("$2", chance2x + "").replace("$3", chance3x + "");
        
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
        node.set("chance-2x", 0.2);
        node.set("chance-3x", 0.1);
        node.set("added-chance-2x-per-level", 0.0);
        node.set("added-chance-3x-per-level", 0.0);
        return node;
    }
    
    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        broadcastExecuteText(hero);
        double chance2x = SkillConfigManager.getUseSetting(hero, this, "chance-2x", 0.2, false);
        double chance3x = SkillConfigManager.getUseSetting(hero, this, "chance-3x", 0.1, false);
        Block wTargetBlock = player.getTargetBlock(null, 20).getRelative(
                        BlockFace.UP);
        double rand = Math.random();
        LivingEntity le = player.getWorld().spawn(wTargetBlock.getLocation(),
                        Spider.class);
        le.getWorld().playEffect(le.getLocation(), Effect.SMOKE, 3);
        int count = 1;
        if (rand > (1 - chance2x - chance3x)) {
            LivingEntity le1 = player.getWorld().spawn(wTargetBlock.getLocation(),
                        Spider.class);
            le1.getWorld().playEffect(le1.getLocation(), Effect.SMOKE, 3);
            count++;
        }
        if (rand > (1 - chance3x)) {
            LivingEntity le1 = player.getWorld().spawn(wTargetBlock.getLocation(),
                        Spider.class);
            le1.getWorld().playEffect(le1.getLocation(), Effect.SMOKE, 3);
            count++;
        }
        broadcast(player.getLocation(), "" + count + "x Multiplier!");
        return SkillResult.NORMAL;
    }
    

}