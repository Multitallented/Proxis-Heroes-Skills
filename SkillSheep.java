package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.CreatureType;

public class SkillSheep extends ActiveSkill {
    public final static int MAX_DISTANCE = 120;

    public SkillSheep(Heroes plugin) {
        super(plugin, "Sheep");
        setDescription("Create sheep");
        setUsage("/skill sheep");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill sheep"});
        
        setTypes(SkillType.DARK, SkillType.SUMMON, SkillType.SILENCABLE);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        broadcastExecuteText(hero);
        Block wTargetBlock = player.getTargetBlock(null, 20).getFace(
                        BlockFace.UP);
        double rand = Math.random();
        player.getWorld().spawnCreature(wTargetBlock.getLocation(),
                        CreatureType.SHEEP);
        int count = 1;
        if (rand > .7) {
            player.getWorld().spawnCreature(wTargetBlock.getLocation(),
                        CreatureType.SHEEP);
            count++;
        }
        if (rand > .9) {
            player.getWorld().spawnCreature(wTargetBlock.getLocation(),
                        CreatureType.SHEEP);
            count++;
        }
        broadcast(player.getLocation(), "" + count + "x Multiplier!");
        return SkillResult.NORMAL;
    }
    

}