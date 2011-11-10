package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Messaging;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class SkillFirespin extends ActiveSkill {
    public final static int MAX_DISTANCE = 120;

    public SkillFirespin(Heroes plugin) {
        super(plugin, "Firespin");
        setDescription("Create a ring of fire");
        setUsage("/skill firespin");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill firespin"});

        setTypes(SkillType.FIRE, SkillType.DAMAGING, SkillType.SILENCABLE);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("max-distance", 30);
        return node;
    }

    @Override
    public boolean use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        final Block wTarget = player.getTargetBlock(null, getSetting(hero, "max-distance", 30, false));
        final long duration = (long) (Math.rint(Math.random()*5)*20 + 100);
        broadcastExecuteText(hero);
        Messaging.send(player, "Duration: " + duration/20 + "s");
        final Material matOne = wTarget.getRelative(BlockFace.UP).getType();
        if (retrieveBlock(wTarget,0,0,2).getType() == Material.AIR) {
            wTarget.getFace(BlockFace.UP).setType(Material.FIRE);
        }

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
        @Override
        public void run() {
            final Material matTwo = wTarget.getRelative(BlockFace.EAST).getRelative(BlockFace.UP).getType();
            if (wTarget.getRelative(BlockFace.EAST).getFace(BlockFace.UP).getType() == Material.AIR) {
                wTarget.getRelative(BlockFace.EAST).getFace(BlockFace.UP).setType(Material.FIRE);
            }

            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
            public void run() {
                final Material matSix = wTarget.getRelative(BlockFace.NORTH_EAST).getRelative(BlockFace.UP).getType();
                if (wTarget.getRelative(BlockFace.NORTH_EAST).getFace(BlockFace.UP).getType() == Material.AIR) {
                    wTarget.getRelative(BlockFace.NORTH_EAST).getFace(BlockFace.UP).setType(Material.FIRE);
                }

                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                        @Override
                public void run() {
                    final Material matFour = wTarget.getRelative(BlockFace.NORTH).getRelative(BlockFace.UP).getType();
                    if (wTarget.getRelative(BlockFace.NORTH).getFace(BlockFace.UP).getType() == Material.AIR) {
                        wTarget.getRelative(BlockFace.NORTH).getFace(BlockFace.UP).setType(Material.FIRE);
                    }

                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                @Override
                    public void run() {
                        final Material matSeven = wTarget.getRelative(BlockFace.NORTH_WEST).getRelative(BlockFace.UP).getType();
                        if (wTarget.getRelative(BlockFace.NORTH_WEST).getFace(BlockFace.UP).getType() == Material.AIR) {
                            wTarget.getRelative(BlockFace.NORTH_WEST).getFace(BlockFace.UP).setType(Material.FIRE);
                        }

                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                        @Override
                        public void run() {
                            final Material matThree = wTarget.getRelative(BlockFace.WEST).getRelative(BlockFace.UP).getType();
                            if (wTarget.getRelative(BlockFace.WEST).getFace(BlockFace.UP).getType() == Material.AIR) {
                                wTarget.getRelative(BlockFace.WEST).getFace(BlockFace.UP).setType(Material.FIRE);
                            }

                            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                @Override
                            public void run() {
                                final Material matNine = wTarget.getRelative(BlockFace.SOUTH_WEST).getRelative(BlockFace.UP).getType();
                                if (wTarget.getRelative(BlockFace.SOUTH_WEST).getFace(BlockFace.UP).getType() == Material.AIR) {
                                    wTarget.getRelative(BlockFace.SOUTH_WEST).getFace(BlockFace.UP).setType(Material.FIRE);
                                }

                                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                        @Override
                                public void run() {
                                    final Material matFive = wTarget.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP).getType();
                                    if (wTarget.getRelative(BlockFace.SOUTH).getFace(BlockFace.UP).getType() == Material.AIR) {
                                        wTarget.getRelative(BlockFace.SOUTH).getFace(BlockFace.UP).setType(Material.FIRE);
                                    }

                                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                                @Override
                                    public void run() {
                                        final Material matEight = wTarget.getRelative(BlockFace.SOUTH_EAST).getRelative(BlockFace.UP).getType();
                                        if (wTarget.getRelative(BlockFace.SOUTH_EAST).getFace(BlockFace.UP).getType() == Material.AIR) {
                                            wTarget.getRelative(BlockFace.SOUTH_EAST).getFace(BlockFace.UP).setType(Material.FIRE);
                                        }

                                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                                        @Override
                                        public void run() {
                                            wTarget.getRelative(BlockFace.UP).setType(matOne);
                                            wTarget.getRelative(BlockFace.EAST).getRelative(BlockFace.UP)
                                                            .setType(matTwo);
                                            wTarget.getRelative(BlockFace.WEST).getRelative(BlockFace.UP)
                                                            .setType(matThree);
                                            wTarget.getRelative(BlockFace.NORTH).getRelative(BlockFace.UP)
                                                            .setType(matFour);
                                            wTarget.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP)
                                                            .setType(matFive);
                                            wTarget.getRelative(BlockFace.NORTH_EAST).getRelative(BlockFace.UP)
                                                            .setType(matSix);
                                            wTarget.getRelative(BlockFace.NORTH_WEST).getRelative(BlockFace.UP)
                                                            .setType(matSeven);
                                            wTarget.getRelative(BlockFace.SOUTH_EAST).getRelative(BlockFace.UP)
                                                            .setType(matEight);
                                            wTarget.getRelative(BlockFace.SOUTH_WEST).getRelative(BlockFace.UP)
                                                            .setType(matNine);
                                        }
                                        }, duration);
                                    }
                                    }, 1L);
                                }
                                }, 1L);
                            }
                            }, 1L);
                        }
                        }, 1L);
                    }
                    }, 1L);
                }
                }, 1L);
            }
            }, 1L);
        }
        }, 1L);

        return true;
    }
    
    
    
    
    private Block retrieveBlock(Block refBlock, int relX, int relY, int relZ) {
        Block returnBlock = refBlock;
            while (0<Math.abs(relX)) {
                if (relX > 0) {
                    returnBlock = returnBlock.getRelative(BlockFace.NORTH);
                    relX--;
                } else {
                    returnBlock = returnBlock.getRelative(BlockFace.SOUTH);
                    relX++;
                }
            }
            while (0<Math.abs(relY)) {
                if (relY > 0) {
                    returnBlock = returnBlock.getRelative(BlockFace.EAST);
                    relY--;
                } else {
                    returnBlock = returnBlock.getRelative(BlockFace.WEST);
                    relY++;
                }
            }
            while (0<Math.abs(relZ)) {
                if (relZ > 0) {
                    returnBlock = returnBlock.getRelative(BlockFace.UP);
                    relZ--;
                } else {
                    returnBlock = returnBlock.getRelative(BlockFace.DOWN);
                    relZ++;
                }
            }
            return returnBlock;
        }
    

}