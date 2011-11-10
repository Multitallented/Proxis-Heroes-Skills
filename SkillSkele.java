package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Player;
import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.CreatureType;

public class SkillSkele extends ActiveSkill {

    public SkillSkele(Heroes plugin) {
        super(plugin, "Skele");
        setDescription("Spawns hostile skeletons");
        setUsage("/skill skele");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill skele"});

        this.setTypes(SkillType.SUMMON, SkillType.DARK, SkillType.SILENCABLE);
    }

    @Override
    public boolean use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        broadcastExecuteText(hero);
        Block wTargetBlock = player.getTargetBlock(null, 20).getFace(
                        BlockFace.UP);
        double rand = Math.random();
        player.getWorld().spawnCreature(wTargetBlock.getLocation(),
                        CreatureType.SKELETON);
        int count = 1;
        if (rand > .7) {
            player.getWorld().spawnCreature(wTargetBlock.getLocation(),
                        CreatureType.SKELETON);
            count++;
        }
        if (rand > .9) {
            player.getWorld().spawnCreature(wTargetBlock.getLocation(),
                        CreatureType.SKELETON);
            count++;
        }
        broadcast(player.getLocation(), "" + count + "x Multiplier!");
        return true;
    }
    

}