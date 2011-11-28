package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class SkillDetect extends ActiveSkill {
    public final static int MAX_DISTANCE = 120;

    public SkillDetect(Heroes plugin) {
        super(plugin, "Detect");
        setDescription("Detect diamonds up to 30 blocks in front of you");
        setUsage("/skill detect");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill detect"});
        
        setTypes(SkillType.ITEM, SkillType.SILENCABLE, SkillType.LIGHT, SkillType.EARTH);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("block-id", 56);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        int blockID = 56;
        if (Material.getMaterial((int) getSetting(hero, "block-id",56, false)) != null) {
            blockID = (int) getSetting(hero, "block-id", 56, false);
        }
        Material mat = Material.getMaterial(blockID);
        BlockFace wTargetDirection = getPlayerDirection(player);
        Block wTargetBlock = player.getLocation().getBlock().getRelative(BlockFace.UP);
        if (wTargetDirection == BlockFace.NORTH) {
            ArrayList<Integer> diamondlocations = new ArrayList<Integer>();
            for (int i=0; i<30; i++) {
                wTargetBlock = wTargetBlock.getRelative(BlockFace.NORTH);
                if (wTargetBlock.getType() == mat) {
                    diamondlocations.add(i);
                }
            }
            if (diamondlocations.size() > 0) {
                String message = mat.name().toLowerCase().replace("_", " ") +
                        " found at ";
                for (int i=0; i<diamondlocations.size(); i++) {
                    if (diamondlocations.size()-1 != i) {
                        message += diamondlocations.get(i) + ", ";
                    } else {
                        message += diamondlocations.get(i);
                    }
                }
                player.sendMessage(message);
            } else {
                String message = "No " + mat.name().toLowerCase().replace("_", " ") +
                        " found within 30 blocks North of you at eye level";
                player.sendMessage(message);
            }
        } else if (wTargetDirection == BlockFace.EAST) {
            ArrayList<Integer> diamondlocations = new ArrayList<Integer>();
            for (int i=0; i<30; i++) {
                wTargetBlock = wTargetBlock.getRelative(BlockFace.EAST);
                if (wTargetBlock.getType() == mat) {
                    diamondlocations.add(i);
                }
            }
            if (diamondlocations.size() > 0) {
                String message = mat.name().toLowerCase().replace("_", " ") + " found at ";
                for (int i=0; i<diamondlocations.size(); i++) {
                    if (diamondlocations.size()-1 != i) {
                        message += diamondlocations.get(i) + ", ";
                    } else {
                        message += diamondlocations.get(i);
                    }
                }
                player.sendMessage(message);
            } else {
                String message = "No " + mat.name().toLowerCase().replace("_", " ") +
                        "found within 30 blocks East of you at eye level";
                player.sendMessage(message);
            }
        } else if (wTargetDirection == BlockFace.SOUTH) {
            ArrayList<Integer> diamondlocations = new ArrayList<Integer>();
            for (int i=0; i<30; i++) {
                wTargetBlock = wTargetBlock.getRelative(BlockFace.SOUTH);
                if (wTargetBlock.getType() == mat) {
                    diamondlocations.add(i);
                }
            }
            if (diamondlocations.size() > 0) {
                String message = mat.name().toLowerCase().replace("_", " ") +
                        " found at ";
                for (int i=0; i<diamondlocations.size(); i++) {
                    if (diamondlocations.size()-1 != i) {
                        message += diamondlocations.get(i) + ", ";
                    } else {
                        message += diamondlocations.get(i);
                    }
                }
                player.sendMessage(message);
            } else {
                String message = "No " + mat.name().toLowerCase().replace("_", " ") +
                        " found within 30 blocks South of you at eye level";
                player.sendMessage(message);
            }
        } else if (wTargetDirection == BlockFace.WEST) {
            ArrayList<Integer> diamondlocations = new ArrayList<Integer>();
            for (int i=0; i<30; i++) {
                wTargetBlock = wTargetBlock.getRelative(BlockFace.WEST);
                if (wTargetBlock.getType() == mat) {
                    diamondlocations.add(i);
                }
            }
            if (diamondlocations.size() > 0) {
                String message = mat.name().toLowerCase().replace("_", " ") +
                        " found at ";
                for (int i=0; i<diamondlocations.size(); i++) {
                    if (diamondlocations.size()-1 != i) {
                        message += diamondlocations.get(i) + ", ";
                    } else {
                        message += diamondlocations.get(i);
                    }
                }
                player.sendMessage(message);
            } else {
                String message = "No " + mat.name().toLowerCase().replace("_", " ") +
                        " found within 30 blocks West of you at eye level";
                player.sendMessage(message);
            }
        } else {
            player.sendMessage(ChatColor.YELLOW + "Fizzle... face north east south or west");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        return SkillResult.NORMAL;
    }
    public static BlockFace getPlayerDirection(Player player) {
            Block wTargetBlock = player.getTargetBlock(
                    null, MAX_DISTANCE);
            double wTargetX = wTargetBlock.getX();
            double wTargetZ = wTargetBlock.getZ();

            double wCenterX = player.getLocation().getX();
            double wCenterZ = player.getLocation().getZ();

            double wAngle = Math
                            .atan((wTargetX - wCenterX) / (wCenterZ - wTargetZ))
                            * (180 / Math.PI);

            if (wTargetX > wCenterX && wTargetZ > wCenterZ) {
                    wAngle = (90 + wAngle) + 90;
            } else if (wTargetX < wCenterX && wTargetZ > wCenterZ) {
                    wAngle = wAngle + 180;
            } else if (wTargetX < wCenterX && wTargetZ < wCenterZ) {
                    wAngle = (90 + wAngle) + 270;
            }

            BlockFace wDirection = null;
            if (wAngle < 45) {
                    // player facing east.
                    wDirection = BlockFace.EAST;
            } else if (wAngle < 135) {
                    // player facing south.
                    wDirection = BlockFace.SOUTH;
            } else if (wAngle < 225) {
                    // player facing west.
                    wDirection = BlockFace.WEST;
            } else if (wAngle < 315) {
                    // player facing north.
                    wDirection = BlockFace.NORTH;
            } else if (wAngle < 360) {
                    // player facing east.
                    wDirection = BlockFace.EAST;
            }

            return wDirection;
    }
    

}