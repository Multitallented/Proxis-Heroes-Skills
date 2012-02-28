package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Player;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;
import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

public class SkillTree extends ActiveSkill {

    public SkillTree(Heroes plugin) {
        super(plugin, "Tree");
        setDescription("Create a small tree temporarily. R:$1");
        setUsage("/skill tree");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill tree"});

        setTypes(SkillType.EARTH, SkillType.SILENCABLE, SkillType.SUMMON);
    }

    @Override
    public String getDescription(Hero hero) {
        int distance = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.MAX_DISTANCE.node(), 100, false) +
                (SkillConfigManager.getUseSetting(hero, this, Setting.MAX_DISTANCE_INCREASE.node(), 0.0, false) * hero.getLevel()));
        distance = distance > 0 ? distance : 0;
        String description = getDescription().replace("$1", distance + "");
        
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
        node.set(Setting.MAX_DISTANCE.node(), 100);
        node.set(Setting.MAX_DISTANCE_INCREASE.node(),0);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        int distance = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.MAX_DISTANCE.node(), 100, false) +
                (SkillConfigManager.getUseSetting(hero, this, Setting.MAX_DISTANCE_INCREASE.node(), 0.0, false) * hero.getLevel()));
        distance = distance > 0 ? distance : 0;
        Block refBlock = player.getTargetBlock(null, distance);
        Block refBlockTop = refBlock.getRelative(BlockFace.UP).getRelative(BlockFace.UP)
                        .getRelative(BlockFace.UP).getRelative(BlockFace.UP)
                        .getRelative(BlockFace.UP);
        Material refMatNorthEast = refBlockTop.getRelative(BlockFace.NORTH_EAST).getRelative(BlockFace.NORTH_EAST).getType();
        Material refMatNorthWest = refBlockTop.getRelative(BlockFace.NORTH_WEST).getRelative(BlockFace.NORTH_WEST).getType();
        Material refMatSouthEast = refBlockTop.getRelative(BlockFace.SOUTH_EAST).getRelative(BlockFace.SOUTH_EAST).getType();
        Material refMatSouthWest = refBlockTop.getRelative(BlockFace.SOUTH_WEST).getRelative(BlockFace.SOUTH_WEST).getType();
        if (refBlock.getRelative(BlockFace.UP).getType() == Material.AIR &&
                (refBlock.getType() == Material.DIRT || refBlock.getType() == Material.GRASS) &&
                refMatNorthEast == Material.AIR &&
                refMatNorthWest == Material.AIR &&
                refMatSouthEast == Material.AIR &&
                refMatSouthWest == Material.AIR &&
                refBlock.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_EAST).getType() == Material.AIR &&
                refBlock.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_WEST).getType() == Material.AIR &&
                refBlock.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_EAST).getType() == Material.AIR &&
                refBlock.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_WEST).getType() == Material.AIR) {


            final Block wTargetBlock = player.getTargetBlock(null, 100).getRelative(BlockFace.UP);
            final Block wOneUp = wTargetBlock.getRelative(BlockFace.UP);
            final Block wTwoUp = wOneUp.getRelative(BlockFace.UP);
            final Block wThreeUp = wTwoUp.getRelative(BlockFace.UP);
            final Block wFourUp = wThreeUp.getRelative(BlockFace.UP);
            final Block wOneNorthUp = wOneUp.getRelative(BlockFace.NORTH);
            final Block wOneSouthUp = wOneUp.getRelative(BlockFace.SOUTH);
            final Block wOneEastUp = wOneUp.getRelative(BlockFace.EAST);
            final Block wOneWestUp = wOneUp.getRelative(BlockFace.WEST);
            final Block wTwoNorthUp = wTwoUp.getRelative(BlockFace.NORTH);
            final Block wTwoSouthUp = wTwoUp.getRelative(BlockFace.SOUTH);
            final Block wTwoEastUp = wTwoUp.getRelative(BlockFace.EAST);
            final Block wTwoWestUp = wTwoUp.getRelative(BlockFace.WEST);

            final ArrayList<Material> matList = new ArrayList<Material>();
            matList.add(wTargetBlock.getType());

            matList.add(wOneUp.getType());
            matList.add(wOneNorthUp.getType());
            matList.add(wOneNorthUp.getRelative(BlockFace.NORTH).getType());
            matList.add(wOneNorthUp.getRelative(BlockFace.NORTH_EAST).getType());
            matList.add(wOneNorthUp.getRelative(BlockFace.NORTH_WEST).getType());
            matList.add(wOneNorthUp.getRelative(BlockFace.NORTH_EAST).getRelative(BlockFace.EAST).getType());
            matList.add(wOneNorthUp.getRelative(BlockFace.NORTH_WEST).getRelative(BlockFace.WEST).getType());
            matList.add(wOneNorthUp.getRelative(BlockFace.EAST).getType());
            matList.add(wOneNorthUp.getRelative(BlockFace.WEST).getType());
            matList.add(wOneSouthUp.getType());
            matList.add(wOneSouthUp.getRelative(BlockFace.SOUTH).getType());
            matList.add(wOneSouthUp.getRelative(BlockFace.SOUTH_EAST).getType());
            matList.add(wOneSouthUp.getRelative(BlockFace.SOUTH_WEST).getType());
            matList.add(wOneSouthUp.getRelative(BlockFace.SOUTH_EAST).getRelative(BlockFace.EAST).getType());
            matList.add(wOneSouthUp.getRelative(BlockFace.SOUTH_WEST).getRelative(BlockFace.WEST).getType());
            matList.add(wOneSouthUp.getRelative(BlockFace.EAST).getType());
            matList.add(wOneSouthUp.getRelative(BlockFace.WEST).getType());
            matList.add(wOneEastUp.getType());
            matList.add(wOneEastUp.getRelative(BlockFace.EAST).getType());
            matList.add(wOneEastUp.getRelative(BlockFace.NORTH_EAST).getType());
            matList.add(wOneEastUp.getRelative(BlockFace.SOUTH_EAST).getType());
            matList.add(wOneWestUp.getType());
            matList.add(wOneWestUp.getRelative(BlockFace.WEST).getType());
            matList.add(wOneWestUp.getRelative(BlockFace.NORTH_WEST).getType());
            matList.add(wOneWestUp.getRelative(BlockFace.SOUTH_WEST).getType());

            matList.add(wTwoUp.getType());
            matList.add(wTwoNorthUp.getType());
            matList.add(wTwoNorthUp.getRelative(BlockFace.NORTH).getType());
            matList.add(wTwoNorthUp.getRelative(BlockFace.NORTH_EAST).getType());
            matList.add(wTwoNorthUp.getRelative(BlockFace.NORTH_WEST).getType());
            matList.add(wTwoNorthUp.getRelative(BlockFace.NORTH_EAST).getRelative(BlockFace.EAST).getType());
            matList.add(wTwoNorthUp.getRelative(BlockFace.NORTH_WEST).getRelative(BlockFace.WEST).getType());
            matList.add(wTwoNorthUp.getRelative(BlockFace.EAST).getType());
            matList.add(wTwoNorthUp.getRelative(BlockFace.WEST).getType());
            matList.add(wTwoSouthUp.getType());
            matList.add(wTwoSouthUp.getRelative(BlockFace.SOUTH).getType());
            matList.add(wTwoSouthUp.getRelative(BlockFace.SOUTH_EAST).getType());

            matList.add(wTwoSouthUp.getRelative(BlockFace.SOUTH_WEST).getType());
            matList.add(wTwoSouthUp.getRelative(BlockFace.SOUTH_EAST).getRelative(BlockFace.EAST).getType());
            matList.add(wTwoSouthUp.getRelative(BlockFace.SOUTH_WEST).getRelative(BlockFace.WEST).getType());
            matList.add(wTwoSouthUp.getRelative(BlockFace.EAST).getType());
            matList.add(wTwoSouthUp.getRelative(BlockFace.WEST).getType());
            matList.add(wTwoEastUp.getType());
            matList.add(wTwoEastUp.getRelative(BlockFace.EAST).getType());
            matList.add(wTwoEastUp.getRelative(BlockFace.NORTH_EAST).getType());
            matList.add(wTwoEastUp.getRelative(BlockFace.SOUTH_EAST).getType());
            matList.add(wTwoWestUp.getType());
            matList.add(wTwoWestUp.getRelative(BlockFace.WEST).getType());
            matList.add(wTwoWestUp.getRelative(BlockFace.NORTH_WEST).getType());
            matList.add(wTwoWestUp.getRelative(BlockFace.SOUTH_WEST).getType());

            matList.add(wThreeUp.getType());
            matList.add(wThreeUp.getRelative(BlockFace.NORTH).getType());
            matList.add(wThreeUp.getRelative(BlockFace.NORTH_EAST).getType());
            matList.add(wThreeUp.getRelative(BlockFace.SOUTH).getType());
            matList.add(wThreeUp.getRelative(BlockFace.EAST).getType());
            matList.add(wThreeUp.getRelative(BlockFace.WEST).getType());

            matList.add(wFourUp.getType());
            matList.add(wFourUp.getRelative(BlockFace.NORTH).getType());
            matList.add(wFourUp.getRelative(BlockFace.SOUTH).getType());
            matList.add(wFourUp.getRelative(BlockFace.EAST).getType());
            matList.add(wFourUp.getRelative(BlockFace.WEST).getType());

            wTargetBlock.setType(Material.SAPLING);
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    wTargetBlock.setType(Material.LOG);
                    wOneUp.setType(Material.LOG);
                    wOneNorthUp.setType(Material.LEAVES);
                    wOneSouthUp.setType(Material.LEAVES);
                    wOneEastUp.setType(Material.LEAVES);
                    wOneWestUp.setType(Material.LEAVES);
                    wTwoUp.setType(Material.LEAVES);

                    plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                        @Override
                        public void run() {
                            wOneNorthUp.getRelative(BlockFace.NORTH).setType(Material.LEAVES);
                            wOneNorthUp.getRelative(BlockFace.NORTH_EAST).setType(Material.LEAVES);
                            wOneNorthUp.getRelative(BlockFace.NORTH_WEST).setType(Material.LEAVES);
                            wOneNorthUp.getRelative(BlockFace.NORTH_EAST).getRelative(BlockFace.EAST).setType(Material.LEAVES);
                            wOneNorthUp.getRelative(BlockFace.NORTH_WEST).getRelative(BlockFace.WEST).setType(Material.LEAVES);
                            wOneNorthUp.getRelative(BlockFace.EAST).setType(Material.LEAVES);
                            wOneNorthUp.getRelative(BlockFace.WEST).setType(Material.LEAVES);
                            wOneSouthUp.getRelative(BlockFace.SOUTH).setType(Material.LEAVES);
                            wOneSouthUp.getRelative(BlockFace.SOUTH_EAST).setType(Material.LEAVES);
                            wOneSouthUp.getRelative(BlockFace.SOUTH_WEST).setType(Material.LEAVES);
                            wOneSouthUp.getRelative(BlockFace.SOUTH_EAST).getRelative(BlockFace.EAST).setType(Material.LEAVES);
                            wOneSouthUp.getRelative(BlockFace.SOUTH_WEST).getRelative(BlockFace.WEST).setType(Material.LEAVES);
                            wOneSouthUp.getRelative(BlockFace.EAST).setType(Material.LEAVES);
                            wOneSouthUp.getRelative(BlockFace.WEST).setType(Material.LEAVES);
                            wOneEastUp.getRelative(BlockFace.EAST).setType(Material.LEAVES);
                            wOneEastUp.getRelative(BlockFace.NORTH_EAST).setType(Material.LEAVES);
                            wOneEastUp.getRelative(BlockFace.SOUTH_EAST).setType(Material.LEAVES);
                            wOneWestUp.getRelative(BlockFace.WEST).setType(Material.LEAVES);
                            wOneWestUp.getRelative(BlockFace.NORTH_WEST).setType(Material.LEAVES);
                            wOneWestUp.getRelative(BlockFace.SOUTH_WEST).setType(Material.LEAVES);

                            wTwoUp.setType(Material.LOG);
                            wTwoNorthUp.setType(Material.LEAVES);
                            wTwoNorthUp.getRelative(BlockFace.NORTH).setType(Material.LEAVES);
                            wTwoNorthUp.getRelative(BlockFace.NORTH_EAST).setType(Material.LEAVES);
                            wTwoNorthUp.getRelative(BlockFace.NORTH_WEST).setType(Material.LEAVES);
                            wTwoNorthUp.getRelative(BlockFace.NORTH_EAST).getRelative(BlockFace.EAST).setType(Material.LEAVES);
                            wTwoNorthUp.getRelative(BlockFace.NORTH_WEST).getRelative(BlockFace.WEST).setType(Material.LEAVES);
                            wTwoNorthUp.getRelative(BlockFace.EAST).setType(Material.LEAVES);
                            wTwoNorthUp.getRelative(BlockFace.WEST).setType(Material.LEAVES);
                            wTwoSouthUp.setType(Material.LEAVES);
                            wTwoSouthUp.getRelative(BlockFace.SOUTH).setType(Material.LEAVES);
                            wTwoSouthUp.getRelative(BlockFace.SOUTH_EAST).setType(Material.LEAVES);
                            wTwoSouthUp.getRelative(BlockFace.SOUTH_WEST).setType(Material.LEAVES);
                            wTwoSouthUp.getRelative(BlockFace.SOUTH_EAST).getRelative(BlockFace.EAST).setType(Material.LEAVES);
                            wTwoSouthUp.getRelative(BlockFace.SOUTH_WEST).getRelative(BlockFace.WEST).setType(Material.LEAVES);
                            wTwoSouthUp.getRelative(BlockFace.EAST).setType(Material.LEAVES);
                            wTwoSouthUp.getRelative(BlockFace.WEST).setType(Material.LEAVES);
                            wTwoEastUp.setType(Material.LEAVES);
                            wTwoEastUp.getRelative(BlockFace.EAST).setType(Material.LEAVES);
                            wTwoEastUp.getRelative(BlockFace.NORTH_EAST).setType(Material.LEAVES);
                            wTwoEastUp.getRelative(BlockFace.SOUTH_EAST).setType(Material.LEAVES);
                            wTwoWestUp.setType(Material.LEAVES);
                            wTwoWestUp.getRelative(BlockFace.WEST).setType(Material.LEAVES);
                            wTwoWestUp.getRelative(BlockFace.NORTH_WEST).setType(Material.LEAVES);
                            wTwoWestUp.getRelative(BlockFace.SOUTH_WEST).setType(Material.LEAVES);

                            wThreeUp.setType(Material.LOG);
                            wThreeUp.getRelative(BlockFace.NORTH).setType(Material.LEAVES);
                            wThreeUp.getRelative(BlockFace.NORTH_EAST).setType(Material.LEAVES);
                            wThreeUp.getRelative(BlockFace.SOUTH).setType(Material.LEAVES);
                            wThreeUp.getRelative(BlockFace.EAST).setType(Material.LEAVES);
                            wThreeUp.getRelative(BlockFace.WEST).setType(Material.LEAVES);

                            wFourUp.setType(Material.LEAVES);
                            wFourUp.getRelative(BlockFace.NORTH).setType(Material.LEAVES);
                            wFourUp.getRelative(BlockFace.SOUTH).setType(Material.LEAVES);
                            wFourUp.getRelative(BlockFace.EAST).setType(Material.LEAVES);
                            wFourUp.getRelative(BlockFace.WEST).setType(Material.LEAVES);

                            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                                @Override
                                public void run() {
                                    wTargetBlock.setType(matList.get(0));
                                    wOneUp.setType(matList.get(1));
                                    wOneNorthUp.setType(matList.get(2));
                                    wOneNorthUp.getRelative(BlockFace.NORTH).setType(matList.get(3));
                                    wOneNorthUp.getRelative(BlockFace.NORTH_EAST).setType(matList.get(4));
                                    wOneNorthUp.getRelative(BlockFace.NORTH_WEST).setType(matList.get(5));
                                    wOneNorthUp.getRelative(BlockFace.NORTH_EAST).getRelative(BlockFace.EAST).setType(matList.get(6));
                                    wOneNorthUp.getRelative(BlockFace.NORTH_WEST).getRelative(BlockFace.WEST).setType(matList.get(7));
                                    wOneNorthUp.getRelative(BlockFace.EAST).setType(matList.get(8));
                                    wOneNorthUp.getRelative(BlockFace.WEST).setType(matList.get(9));
                                    wOneSouthUp.setType(matList.get(10));
                                    wOneSouthUp.getRelative(BlockFace.SOUTH).setType(matList.get(11));
                                    wOneSouthUp.getRelative(BlockFace.SOUTH_EAST).setType(matList.get(12));
                                    wOneSouthUp.getRelative(BlockFace.SOUTH_WEST).setType(matList.get(13));
                                    wOneSouthUp.getRelative(BlockFace.SOUTH_EAST).getRelative(BlockFace.EAST).setType(matList.get(14));
                                    wOneSouthUp.getRelative(BlockFace.SOUTH_WEST).getRelative(BlockFace.WEST).setType(matList.get(15));
                                    wOneSouthUp.getRelative(BlockFace.EAST).setType(matList.get(16));
                                    wOneSouthUp.getRelative(BlockFace.WEST).setType(matList.get(17));
                                    wOneEastUp.setType(matList.get(18));
                                    wOneEastUp.getRelative(BlockFace.EAST).setType(matList.get(19));
                                    wOneEastUp.getRelative(BlockFace.NORTH_EAST).setType(matList.get(20));
                                    wOneEastUp.getRelative(BlockFace.SOUTH_EAST).setType(matList.get(21));
                                    wOneWestUp.setType(matList.get(22));
                                    wOneWestUp.getRelative(BlockFace.WEST).setType(matList.get(23));
                                    wOneWestUp.getRelative(BlockFace.NORTH_WEST).setType(matList.get(24));
                                    wOneWestUp.getRelative(BlockFace.SOUTH_WEST).setType(matList.get(25));

                                    wTwoUp.setType(matList.get(26));
                                    wTwoNorthUp.setType(matList.get(27));
                                    wTwoNorthUp.getRelative(BlockFace.NORTH).setType(matList.get(28));
                                    wTwoNorthUp.getRelative(BlockFace.NORTH_EAST).setType(matList.get(29));
                                    wTwoNorthUp.getRelative(BlockFace.NORTH_WEST).setType(matList.get(30));
                                    wTwoNorthUp.getRelative(BlockFace.NORTH_EAST).getRelative(BlockFace.EAST).setType(matList.get(31));
                                    wTwoNorthUp.getRelative(BlockFace.NORTH_WEST).getRelative(BlockFace.WEST).setType(matList.get(32));
                                    wTwoNorthUp.getRelative(BlockFace.EAST).setType(matList.get(33));
                                    wTwoNorthUp.getRelative(BlockFace.WEST).setType(matList.get(34));
                                    wTwoSouthUp.setType(matList.get(35));
                                    wTwoSouthUp.getRelative(BlockFace.SOUTH).setType(matList.get(36));
                                    wTwoSouthUp.getRelative(BlockFace.SOUTH_EAST).setType(matList.get(37));
                                    wTwoSouthUp.getRelative(BlockFace.SOUTH_WEST).setType(matList.get(38));
                                    wTwoSouthUp.getRelative(BlockFace.SOUTH_EAST).getRelative(BlockFace.EAST).setType(matList.get(39));
                                    wTwoSouthUp.getRelative(BlockFace.SOUTH_WEST).getRelative(BlockFace.WEST).setType(matList.get(40));
                                    wTwoSouthUp.getRelative(BlockFace.EAST).setType(matList.get(41));
                                    wTwoSouthUp.getRelative(BlockFace.WEST).setType(matList.get(42));
                                    wTwoEastUp.setType(matList.get(43));
                                    wTwoEastUp.getRelative(BlockFace.EAST).setType(matList.get(44));
                                    wTwoEastUp.getRelative(BlockFace.NORTH_EAST).setType(matList.get(45));
                                    wTwoEastUp.getRelative(BlockFace.SOUTH_EAST).setType(matList.get(46));
                                    wTwoWestUp.setType(matList.get(47));
                                    wTwoWestUp.getRelative(BlockFace.WEST).setType(matList.get(48));
                                    wTwoWestUp.getRelative(BlockFace.NORTH_WEST).setType(matList.get(49));
                                    wTwoWestUp.getRelative(BlockFace.SOUTH_WEST).setType(matList.get(50));

                                    wThreeUp.setType(matList.get(51));
                                    wThreeUp.getRelative(BlockFace.NORTH).setType(matList.get(52));
                                    wThreeUp.getRelative(BlockFace.NORTH_EAST).setType(matList.get(53));
                                    wThreeUp.getRelative(BlockFace.SOUTH).setType(matList.get(54));
                                    wThreeUp.getRelative(BlockFace.EAST).setType(matList.get(55));
                                    wThreeUp.getRelative(BlockFace.WEST).setType(matList.get(56));

                                    wFourUp.setType(matList.get(57));
                                    wFourUp.getRelative(BlockFace.NORTH).setType(matList.get(58));
                                    wFourUp.getRelative(BlockFace.SOUTH).setType(matList.get(59));
                                    wFourUp.getRelative(BlockFace.EAST).setType(matList.get(60));
                                    wFourUp.getRelative(BlockFace.WEST).setType(matList.get(61));
                                }
                            }, 900L);
                        }
                    }, 6L);
                }
            }, 4L);
            //TreeType wType = TreeType.values()[new Random().nextInt(TreeType
            //		.values().length)];
            //return player.getWorld()
            //		.generateTree(wTargetBlock.getLocation(), wType);
            return SkillResult.NORMAL;
        } else {
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
    }

}