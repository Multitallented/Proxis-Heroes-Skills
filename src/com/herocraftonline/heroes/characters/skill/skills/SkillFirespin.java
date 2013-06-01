package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.BlockBreakEvent;

public class SkillFirespin extends ActiveSkill {
    public final static int MAX_DISTANCE = 120;

    public SkillFirespin(Heroes plugin) {
        super(plugin, "Firespin");
        setDescription("Creates a ring of Fire. R:$1");
        setUsage("/skill firespin");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill firespin"});

        setTypes(SkillType.FIRE, SkillType.DAMAGING, SkillType.SILENCABLE);
    }
    
    @Override
    public String getDescription(Hero hero) {
        int distance = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE, 15, false) + 
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE_INCREASE.node(), 0, false) * hero.getSkillLevel(this));
        distance = distance > 0 ? distance : 0;
        String description = getDescription().replace("$1", distance + "");
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
        node.set(SkillSetting.MAX_DISTANCE.node(), 30);
        node.set(SkillSetting.MAX_DISTANCE_INCREASE.node(), 0);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        final Player player = hero.getPlayer();
        final Block wTarget = player.getTargetBlock(null, (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE, 15, false) + 
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE_INCREASE.node(), 0, false) * hero.getSkillLevel(this))));
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (p.getWorld().equals(wTarget.getWorld()) && Math.sqrt(p.getLocation().distanceSquared(wTarget.getLocation())) <= 2 && !p.equals(player)
                    && !damageCheck(p, player)) {
                return SkillResult.INVALID_TARGET;
            }
        }
        final long duration = (long) (Math.rint(Math.random()*5)*20 + 100);
        broadcastExecuteText(hero);
        Messaging.send(player, "Duration: " + duration/20 + "s");
        final Material matOne = wTarget.getRelative(BlockFace.UP).getType();
        if (retrieveBlock(wTarget,0,0,2).getType() == Material.AIR) {
            setBlock(player, wTarget.getRelative(BlockFace.UP));
        }

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
        @Override
        public void run() {
            final Material matTwo = wTarget.getRelative(BlockFace.EAST).getRelative(BlockFace.UP).getType();
            if (wTarget.getRelative(BlockFace.EAST).getRelative(BlockFace.UP).getType() == Material.AIR) {
                setBlock(player, wTarget.getRelative(BlockFace.EAST).getRelative(BlockFace.UP));
            }

            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
            public void run() {
                final Material matSix = wTarget.getRelative(BlockFace.NORTH_EAST).getRelative(BlockFace.UP).getType();
                if (wTarget.getRelative(BlockFace.NORTH_EAST).getRelative(BlockFace.UP).getType() == Material.AIR) {
                    setBlock(player, wTarget.getRelative(BlockFace.NORTH_EAST).getRelative(BlockFace.UP));
                }

                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                        @Override
                public void run() {
                    final Material matFour = wTarget.getRelative(BlockFace.NORTH).getRelative(BlockFace.UP).getType();
                    if (wTarget.getRelative(BlockFace.NORTH).getRelative(BlockFace.UP).getType() == Material.AIR) {
                        setBlock(player, wTarget.getRelative(BlockFace.NORTH).getRelative(BlockFace.UP));
                    }

                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                @Override
                    public void run() {
                        final Material matSeven = wTarget.getRelative(BlockFace.NORTH_WEST).getRelative(BlockFace.UP).getType();
                        if (wTarget.getRelative(BlockFace.NORTH_WEST).getRelative(BlockFace.UP).getType() == Material.AIR) {
                            setBlock(player, wTarget.getRelative(BlockFace.NORTH_WEST).getRelative(BlockFace.UP));
                        }

                        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                        @Override
                        public void run() {
                            final Material matThree = wTarget.getRelative(BlockFace.WEST).getRelative(BlockFace.UP).getType();
                            if (wTarget.getRelative(BlockFace.WEST).getRelative(BlockFace.UP).getType() == Material.AIR) {
                                setBlock(player, wTarget.getRelative(BlockFace.WEST).getRelative(BlockFace.UP));
                            }

                            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                @Override
                            public void run() {
                                final Material matNine = wTarget.getRelative(BlockFace.SOUTH_WEST).getRelative(BlockFace.UP).getType();
                                if (wTarget.getRelative(BlockFace.SOUTH_WEST).getRelative(BlockFace.UP).getType() == Material.AIR) {
                                    setBlock(player, wTarget.getRelative(BlockFace.SOUTH_WEST).getRelative(BlockFace.UP));
                                }

                                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                        @Override
                                public void run() {
                                    final Material matFive = wTarget.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP).getType();
                                    if (wTarget.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP).getType() == Material.AIR) {
                                        setBlock(player, wTarget.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP));
                                    }

                                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                                                @Override
                                    public void run() {
                                        final Material matEight = wTarget.getRelative(BlockFace.SOUTH_EAST).getRelative(BlockFace.UP).getType();
                                        if (wTarget.getRelative(BlockFace.SOUTH_EAST).getRelative(BlockFace.UP).getType() == Material.AIR) {
                                            setBlock(player, wTarget.getRelative(BlockFace.SOUTH_EAST).getRelative(BlockFace.UP));
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

        return SkillResult.NORMAL;
    }
    
    private boolean setBlock(Player player, Block block) {
        BlockBreakEvent testEvent = new BlockBreakEvent(block, player);
        Bukkit.getPluginManager().callEvent(testEvent);
        if (testEvent.isCancelled()) {
            return false;
        }
        block.setType(Material.FIRE);
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