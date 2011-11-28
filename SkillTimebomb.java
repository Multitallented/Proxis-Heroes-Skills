package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Messaging;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class SkillTimebomb extends ActiveSkill {

    public SkillTimebomb(Heroes plugin) {
        super(plugin, "Timebomb");
        setDescription("Creates a pit of TNT");
        setUsage("/skill timebomb");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill timebomb"});

        
        setTypes(SkillType.PHYSICAL, SkillType.DAMAGING, SkillType.SILENCABLE, SkillType.FIRE, SkillType.EARTH);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("range", 40);
        node.setProperty("max-tnt", 4);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        final Block wTargetBlock = player.getTargetBlock(null, getSetting(hero, "range", 40, false));

        final Material matOne = wTargetBlock.getType();
        final Block wOneDown = wTargetBlock.getRelative(BlockFace.DOWN);
        final Material matTwo = wTargetBlock.getRelative(BlockFace.UP).getType();
        final Material matThree = wTargetBlock.getRelative(BlockFace.NORTH).getType();
        final Material matFour = wTargetBlock.getRelative(BlockFace.SOUTH).getType();
        final Material matFive = wTargetBlock.getRelative(BlockFace.EAST).getType();
        final Material matSix = wTargetBlock.getRelative(BlockFace.WEST).getType();
        final Material matSeven = wOneDown.getType();
        final Material matEight = wOneDown.getRelative(BlockFace.NORTH).getType();
        final Material matNine = wOneDown.getRelative(BlockFace.SOUTH).getType();
        final Material matTen = wOneDown.getRelative(BlockFace.EAST).getType();
        final Material matEleven = wOneDown.getRelative(BlockFace.WEST).getType();
        final Material matTwelve = wTargetBlock.getRelative(BlockFace.NORTH_EAST).getType();
        final Material matThirteen = wTargetBlock.getRelative(BlockFace.NORTH_WEST).getType();
        final Material matFourteen = wTargetBlock.getRelative(BlockFace.SOUTH_EAST).getType();
        final Material matFifteen = wTargetBlock.getRelative(BlockFace.SOUTH_WEST).getType();
        final Material matSixteen = wOneDown.getRelative(BlockFace.NORTH_EAST).getType();
        final Material matSeventeen = wOneDown.getRelative(BlockFace.NORTH_WEST).getType();
        final Material matEighteen = wOneDown.getRelative(BlockFace.SOUTH_EAST).getType();
        final Material matNineteen = wOneDown.getRelative(BlockFace.SOUTH_WEST).getType();


        if (wTargetBlock.getRelative(BlockFace.UP).getType() == Material.AIR) {
            wTargetBlock.setTypeId(0);
        }
        int count = 1;
        if (wTargetBlock.getRelative(BlockFace.NORTH).getRelative(BlockFace.UP).getType() == Material.AIR) {
            if (Math.random() > .5 && 2 <= getSetting(hero, "max-tnt", 4, false)) {
                wTargetBlock.getRelative(BlockFace.NORTH).setTypeId(46, false);
                count++;
            } else {
                wTargetBlock.getRelative(BlockFace.NORTH).setTypeId(0);
            }
        }
        if (wTargetBlock.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP).getType() == Material.AIR) {
            if (Math.random() > .5 && 3 <= getSetting(hero, "max-tnt", 4, false)) {
                wTargetBlock.getRelative(BlockFace.SOUTH).setTypeId(46, false);
                count++;
            } else {
                wTargetBlock.getRelative(BlockFace.SOUTH).setTypeId(0);
            }
        }
        if (wTargetBlock.getRelative(BlockFace.EAST).getRelative(BlockFace.UP).getType() == Material.AIR) {
            if (Math.random() > .5 && 4 <= getSetting(hero, "max-tnt", 4, false)) {
                wTargetBlock.getRelative(BlockFace.EAST).setTypeId(46, false);
                count++;
            } else {
                wTargetBlock.getRelative(BlockFace.EAST).setTypeId(0);
            }
        }
        if (wTargetBlock.getRelative(BlockFace.WEST).getRelative(BlockFace.UP).getType() == Material.AIR) {
            wTargetBlock.getRelative(BlockFace.WEST).setTypeId(46, false);
        }
        Messaging.send(player,"Summoned " + count + "TNT");
        wOneDown.setType(Material.AIR);
        wOneDown.getRelative(BlockFace.NORTH).setType(Material.REDSTONE_TORCH_ON);
        wOneDown.getRelative(BlockFace.SOUTH).setType(Material.REDSTONE_TORCH_ON);
        wOneDown.getRelative(BlockFace.EAST).setType(Material.REDSTONE_TORCH_ON);
        wOneDown.getRelative(BlockFace.WEST).setType(Material.REDSTONE_TORCH_ON);
        if (wTargetBlock.getRelative(BlockFace.NORTH_EAST).getRelative(BlockFace.UP).getType() == Material.AIR) {
            wTargetBlock.getRelative(BlockFace.NORTH_EAST).setType(Material.AIR);
        }
        if (wTargetBlock.getRelative(BlockFace.NORTH_WEST).getRelative(BlockFace.UP).getType() == Material.AIR) {
            wTargetBlock.getRelative(BlockFace.NORTH_WEST).setType(Material.AIR);
        }
        if (wTargetBlock.getRelative(BlockFace.SOUTH_EAST).getRelative(BlockFace.UP).getType() == Material.AIR) {
            wTargetBlock.getRelative(BlockFace.SOUTH_EAST).setType(Material.AIR);
        }
        if (wTargetBlock.getRelative(BlockFace.SOUTH_WEST).getRelative(BlockFace.UP).getType() == Material.AIR) {
            wTargetBlock.getRelative(BlockFace.SOUTH_WEST).setType(Material.AIR);
        }
        wOneDown.getRelative(BlockFace.NORTH_EAST).setType(Material.AIR);
        wOneDown.getRelative(BlockFace.NORTH_WEST).setType(Material.AIR);
        wOneDown.getRelative(BlockFace.SOUTH_EAST).setType(Material.AIR);
        wOneDown.getRelative(BlockFace.SOUTH_WEST).setType(Material.AIR);

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {

                wOneDown.setType(matSeven);
                wOneDown.getRelative(BlockFace.NORTH).setType(matEight);
                wOneDown.getRelative(BlockFace.SOUTH).setType(matNine);
                wOneDown.getRelative(BlockFace.EAST).setType(matTen);
                wOneDown.getRelative(BlockFace.WEST).setType(matEleven);
                wOneDown.getRelative(BlockFace.NORTH_EAST).setType(matSixteen);
                wOneDown.getRelative(BlockFace.NORTH_WEST).setType(matSeventeen);
                wOneDown.getRelative(BlockFace.SOUTH_EAST).setType(matEighteen);
                wOneDown.getRelative(BlockFace.SOUTH_WEST).setType(matNineteen);

                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        wTargetBlock.setType(matOne);
                        wTargetBlock.getFace(BlockFace.UP).setType(matTwo);
                        wTargetBlock.getRelative(BlockFace.NORTH).setType(matThree);
                        wTargetBlock.getRelative(BlockFace.SOUTH).setType(matFour);
                        wTargetBlock.getRelative(BlockFace.EAST).setType(matFive);
                        wTargetBlock.getRelative(BlockFace.WEST).setType(matSix);
                        wTargetBlock.getRelative(BlockFace.NORTH_EAST).setType(matTwelve);
                        wTargetBlock.getRelative(BlockFace.NORTH_WEST).setType(matThirteen);
                        wTargetBlock.getRelative(BlockFace.SOUTH_EAST).setType(matFourteen);
                        wTargetBlock.getRelative(BlockFace.SOUTH_WEST).setType(matFifteen);
                    }
                }, 200L);
            }
        }, 200L);

        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }

}