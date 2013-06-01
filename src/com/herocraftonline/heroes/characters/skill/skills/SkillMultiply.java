package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;

public class SkillMultiply extends TargettedSkill {
    public final static int MAX_DISTANCE = 120;

    public SkillMultiply(Heroes plugin) {
        super(plugin, "Multiply");
        setDescription("Clones target creature. R:$1");
        setUsage("/skill multiply");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill multiply"});
        setTypes(SkillType.SUMMON, SkillType.DARK, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        int distance = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE.node(), 15, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
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
        node.set(SkillSetting.MAX_DISTANCE.node(), 15);
        node.set(SkillSetting.MAX_DISTANCE_INCREASE.node(), 0.0);
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
        /*} else if (target instanceof Player) {
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
            }*/
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