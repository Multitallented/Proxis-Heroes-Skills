package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.skill.TargettedSkill;
import com.herocraftonline.dev.heroes.util.Messaging;
import org.bukkit.Location;
import org.bukkit.entity.*;

public class SkillMultiply extends TargettedSkill {
    public final static int MAX_DISTANCE = 120;

    public SkillMultiply(Heroes plugin) {
        super(plugin, "Multiply");
        setDescription("Breeds an animal or mob at target block");
        setUsage("/skill multiply");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill multiply"});
        setTypes(SkillType.SUMMON, SkillType.DARK, SkillType.SILENCABLE);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        return node;
    }

    @Override
    public SkillResult use(Hero hero, LivingEntity target, String[] args) {
        Player player = hero.getPlayer();
        if (target == player) {
            return SkillResult.INVALID_TARGET;
        }
        Location targetLocation = target.getLocation();
        double rand = Math.random();
        int count = 1;
        if (target instanceof Zombie){
            player.getWorld().spawnCreature(targetLocation,
                        CreatureType.ZOMBIE);
            if (rand > .7) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.ZOMBIE);
                count++;
            }
            if (rand > .9) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.ZOMBIE);
                count++;
            }
        } else if (target instanceof Skeleton) {
            player.getWorld().spawnCreature(targetLocation,
                        CreatureType.SKELETON);
            if (rand > .7) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.SKELETON);
                count++;
            }
            if (rand > .9) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.SKELETON);
                count++;
            }
        } else if (target instanceof Spider) {
            player.getWorld().spawnCreature(targetLocation,
                        CreatureType.SPIDER);
            if (rand > .7) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.SPIDER);
                count++;
            }
            if (rand > .9) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.SPIDER);
                count++;
            }
        } else if (target instanceof Creeper) {
            player.getWorld().spawnCreature(targetLocation,
                        CreatureType.CREEPER);
            if (rand > .7) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.CREEPER);
                count++;
            }
            if (rand > .9) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.CREEPER);
                count++;
            }
        } else if (target instanceof PigZombie) {
            player.getWorld().spawnCreature(targetLocation,
                        CreatureType.PIG_ZOMBIE);
            if (rand > .7) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.PIG_ZOMBIE);
                count++;
            }
            if (rand > .9) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.PIG_ZOMBIE);
                count++;
            }
        } else if (target instanceof Slime) {
            player.getWorld().spawnCreature(targetLocation,
                        CreatureType.SLIME);
            if (rand > .7) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.SLIME);
                count++;
            }
            if (rand > .9) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.SLIME);
                count++;
            }
        } else if (target instanceof Pig) {
            player.getWorld().spawnCreature(targetLocation,
                        CreatureType.PIG);
            if (rand > .7) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.PIG);
                count++;
            }
            if (rand > .9) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.PIG);
                count++;
            }
        } else if (target instanceof Cow) {
            player.getWorld().spawnCreature(targetLocation,
                        CreatureType.COW);
            if (rand > .7) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.COW);
                count++;
            }
            if (rand > .9) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.COW);
                count++;
            }
        } else if (target instanceof Sheep) {
            player.getWorld().spawnCreature(targetLocation,
                        CreatureType.SHEEP);
            if (rand > .7) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.SHEEP);
                count++;
            }
            if (rand > .9) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.SHEEP);
                count++;
            }
        } else if (target instanceof Chicken) {
            player.getWorld().spawnCreature(targetLocation,
                        CreatureType.CHICKEN);
            if (rand > .7) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.CHICKEN);
                count++;
            }
            if (rand > .9) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.CHICKEN);
                count++;
            }
        } else if (target instanceof Player) {
            player.getWorld().spawnCreature(targetLocation,
                        CreatureType.MONSTER);
            if (rand > .7) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.MONSTER);
                count++;
            }
            if (rand > .9) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.MONSTER);
                count++;
            }
        } else if (target instanceof Enderman) {
            player.getWorld().spawnCreature(targetLocation,
                        CreatureType.ENDERMAN);
            if (rand > .7) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.ENDERMAN);
                count++;
            }
            if (rand > .9) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.ENDERMAN);
                count++;
            }
        } else if (target instanceof Silverfish) {
            player.getWorld().spawnCreature(targetLocation,
                        CreatureType.SILVERFISH);
            if (rand > .7) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.SILVERFISH);
                count++;
            }
            if (rand > .9) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.SILVERFISH);
                count++;
            }
        } else if (target instanceof CaveSpider) {
            player.getWorld().spawnCreature(targetLocation,
                        CreatureType.CAVE_SPIDER);
            if (rand > .7) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.CAVE_SPIDER);
                count++;
            }
            if (rand > .9) {
                player.getWorld().spawnCreature(targetLocation,
                            CreatureType.CAVE_SPIDER);
                count++;
            }
        } else {
            return SkillResult.INVALID_TARGET;
        }
        broadcastExecuteText(hero, target);
        broadcast(player.getLocation(), "" + count + "x Multiplier!");
        return SkillResult.NORMAL;
    }
    

}