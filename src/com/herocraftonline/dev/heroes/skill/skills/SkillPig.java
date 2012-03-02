package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Player;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.CreatureType;

public class SkillPig extends ActiveSkill {
    public SkillPig(Heroes plugin) {
        super(plugin, "Pig");
        setDescription("$1% chance to spawn 1 pig, $2% for 2, and $3% for 3.");
        setUsage("/skill pig");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill pig"});
        
        setTypes(SkillType.DARK, SkillType.SUMMON, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        int chance2x = (int) (SkillConfigManager.getUseSetting(hero, this, "chance-2x", 0.2, false) * 100 +
                SkillConfigManager.getUseSetting(hero, this, "added-chance-2x-per-level", 0.0, false) * hero.getLevel());
        int chance3x = (int) (SkillConfigManager.getUseSetting(hero, this, "chance-3x", 0.1, false) * 100 +
                SkillConfigManager.getUseSetting(hero, this, "added-chance-3x-per-level", 0.0, false) * hero.getLevel());
        String description = getDescription().replace("$1", (100 - (chance2x + chance3x)) + "").replace("$2", chance2x + "").replace("$3", chance3x + "");
        
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
        player.getWorld().spawnCreature(wTargetBlock.getLocation(),
                        CreatureType.PIG);
        int count = 1;
        if (rand > (1 - chance2x - chance3x)) {
            player.getWorld().spawnCreature(wTargetBlock.getLocation(),
                        CreatureType.PIG);
            count++;
        }
        if (rand > (1 - chance3x)) {
            player.getWorld().spawnCreature(wTargetBlock.getLocation(),
                        CreatureType.PIG);
            count++;
        }
        broadcast(player.getLocation(), "" + count + "x Multiplier!");
        return SkillResult.NORMAL;
    }
    

}