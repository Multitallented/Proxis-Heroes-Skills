package com.herocraftonline.dev.heroes.skill.skills;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Messaging;

public class SkillTrack extends ActiveSkill {

    private static final Random random = new Random();

    public SkillTrack(Heroes plugin) {
        super(plugin, "Track");
        setDescription("Locates a player");
        setUsage("/skill track <player>");
        setArgumentRange(1, 1);
        setIdentifiers(new String[] { "skill track" });
        
        setTypes(SkillType.EARTH);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("randomness", 50);
        return node;
    }

    @Override
    public boolean use(Hero hero, String[] args) {
        Player player = hero.getPlayer();

        Player target = getPlugin().getServer().getPlayer(args[0]);
        if (target == null) {
            Messaging.send(player, "Target not found.");
            return false;
        }

        Location location = target.getLocation();
        int randomness = getSetting(hero, "randomness", 50, false);
        int x = location.getBlockX() + random.nextInt(randomness);
        int y = location.getBlockY() + random.nextInt(randomness / 10);
        y = y < 1 ? 1 : y;
        int z = location.getBlockZ() + random.nextInt(randomness);
        Hero tHero = getPlugin().getHeroManager().getHero((Player) target);
        Messaging.send(player, "$1 is a level $2 $3 with $4 health!", tHero.getPlayer().getDisplayName(), tHero.getLevel(), tHero.getHeroClass().getName(), tHero.getHealth());
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
        return true;
    }

}