package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.PeriodicExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import java.util.HashSet;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 *
 * @author Multitallented
 */
public class SkillSpeed extends ActiveSkill {

    public SkillSpeed(Heroes plugin) {
        super(plugin, "Speed");
        setDescription("Lets you move faster on certain terrain");
        setUsage("/skill speed");
        setArgumentRange(0, 0);
        setIdentifiers("skill speed");
        setTypes(SkillType.ICE, SkillType.SILENCABLE, SkillType.BUFF);
    }
    
    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(Setting.DURATION.node(), 600000);
        node.set(Setting.MAX_DISTANCE.node(), 5);
        node.set("terrain-type", "STATIONARY_WATER");
        return node;
    }
    
    @Override
    public SkillResult use(Hero hero, String[] strings) {
        long duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 600000, false);
        int distance = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE.node(), 5, false);
        String block = SkillConfigManager.getUseSetting(hero, this, "terrain-type", "STATIONARY_WATER");
        Material mat = Material.getMaterial(block);
        if (mat == null) {
            hero.getPlayer().sendMessage(ChatColor.RED + "SkillSpeed has invalid terrain-type.");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        if (hero.hasEffect("Speed")) {
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        hero.addEffect(new OlympicSwimmerEffect(this, plugin, 500, duration, distance, mat));
        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }

    @Override
    public String getDescription(Hero hero) {
        String terrainType = SkillConfigManager.getUseSetting(hero, this, "terrain-type", "STATIONARY_WATER");
        terrainType = terrainType.toLowerCase().replace("_", " ");
        String message = "You move faster on $1";
        message = message.replace("$1", terrainType);
        return message;
    }
    
    public class OlympicSwimmerEffect extends PeriodicExpirableEffect {
        public HashSet<Block> previousBlocks = new HashSet<Block>();
        private final int distance;
        private final Material mat;
        
        public OlympicSwimmerEffect(Skill skill, Heroes plugin, long period, long duration, int distance, Material mat) {
            super(skill, "OlympicSwimmer", period, duration);
            this.types.add(EffectType.BENEFICIAL);
            this.types.add(EffectType.DISPELLABLE);
            this.distance = distance;
            this.mat = mat;
        }

        @Override
        public void tickMonster(Monster mnstr) {
            // :P
        }

        @Override
        public void tickHero(Hero hero) {
            //Turn water to ice under their feet
            //Turn previous ice into water
            //done!
            Player player = hero.getPlayer();
            Block b = player.getLocation().getBlock();
            if (b.getType() != mat) {
                return;
            }
            Location l = player.getTargetBlock(null, distance).getLocation();
            Location pL = player.getLocation().getBlock().getRelative(BlockFace.DOWN).getLocation();
            Vector vc = new Vector(l.getX() - pL.getX(), 0.5D, l.getZ() - pL.getZ());
            vc.multiply(0.2);
            player.setVelocity(vc);
        }
        

    }
    
}













