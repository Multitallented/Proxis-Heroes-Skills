package com.herocraftonline.dev.heroes.skill.skills;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.configuration.ConfigurationSection;

public class SkillTrack extends ActiveSkill {

    private static final Random random = new Random();

    public SkillTrack(Heroes plugin) {
        super(plugin, "Track");
        setDescription("Gives you tons of info on a player.");
        setUsage("/skill track [player]");
        setArgumentRange(1, 1);
        setIdentifiers(new String[] { "skill track" });
        
        setTypes(SkillType.EARTH);
    }

    @Override
    public String getDescription(Hero hero) {
        String description = getDescription();
        
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
        node.set("randomness", 50);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();

        Player target = plugin.getServer().getPlayer(args[0]);
        if (target == null) {
            return SkillResult.INVALID_TARGET;
        }

        Location location = target.getLocation();
        int randomness = SkillConfigManager.getUseSetting(hero, this, "randomness", 50, false);
        int x = location.getBlockX() + random.nextInt(randomness);
        int y = location.getBlockY() + random.nextInt(randomness / 10);
        y = y < 1 ? 1 : y;
        int z = location.getBlockZ() + random.nextInt(randomness);
        Hero tHero = plugin.getHeroManager().getHero((Player) target);
        Messaging.send(player, "$1 is a level $2 $3 with $4 health!", tHero.getPlayer().getDisplayName(), tHero.getTieredLevel(tHero.getHeroClass()), tHero.getHeroClass().getName(), tHero.getHealth());
        Messaging.send(player, "$5 pos: $1: $2,$3,$4", location.getWorld().getName(), x, y, z, tHero.getPlayer().getDisplayName());
        Location hLoc = hero.getPlayer().getLocation();
        int hX = hLoc.getBlockX();
        int hY = hLoc.getBlockY();
        int hZ = hLoc.getBlockZ();
        
        Messaging.send(player, "Your current pos: $1: $2, $3, $4", hLoc.getWorld().getName(), hX, hY, hZ);
        if (damageCheck (player, tHero.getPlayer())) {
            Messaging.send(player, "You $1 hurt $2", "can", tHero.getPlayer().getDisplayName());
        } else {
            Messaging.send(player, "You $1 hurt $2", "cannot", tHero.getPlayer().getDisplayName());
        }
        player.setCompassTarget(location);
        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }

}