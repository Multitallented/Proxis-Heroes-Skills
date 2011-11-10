package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.CreatureType;

public class SkillSpider extends ActiveSkill {

    public SkillSpider(Heroes plugin) {
        super(plugin, "Spider");
        setDescription("Creates spiders");
        setUsage("/skill spider");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill spider"});
        
        setTypes(SkillType.SUMMON, SkillType.DARK, SkillType.SILENCABLE);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        return node;
    }

    @Override
    public boolean use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        broadcastExecuteText(hero);
        Block wTargetBlock = player.getTargetBlock(null, 20).getFace(
                        BlockFace.UP);
        double rand = Math.random();
        player.getWorld().spawnCreature(wTargetBlock.getLocation(),
                        CreatureType.SPIDER);
        int count = 1;
        if (rand > .7) {
            player.getWorld().spawnCreature(wTargetBlock.getLocation(),
                        CreatureType.SPIDER);
            count++;
        }
        if (rand > .9) {
            player.getWorld().spawnCreature(wTargetBlock.getLocation(),
                        CreatureType.SPIDER);
            count++;
        }
        broadcast(player.getLocation(), "" + count + "x Multiplier!");
        return true;
    }
    

}