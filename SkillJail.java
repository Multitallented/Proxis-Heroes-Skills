package com.herocraftonline.dev.heroes.skill.skills;


import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.skill.TargettedSkill;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class SkillJail extends TargettedSkill {
    private String jailText;
    private Map<Player, Location> jailedPlayers = new HashMap<Player, Location>();
    private Set<Location> jailLocations;

    public SkillJail(Heroes plugin) {
        super(plugin, "Jail");
        setDescription("Sends players who die near you to jail");
        setUsage("/skill jail");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill jail"});

        setTypes(SkillType.HARMFUL, SkillType.TELEPORT);
        registerEvent(Type.PLAYER_RESPAWN, new RespawnListener(), Priority.Highest);
        registerEvent(Type.ENTITY_DAMAGE, new JailListener(), Priority.Monitor);
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set("jail-text", "%target% was jailed!");
        node.set(Setting.MAX_DISTANCE.node(), 25);
        return node;
    }
    
    @Override
    public void init() {
        super.init();
        jailLocations = new HashSet<Location>();
        jailText = SkillConfigManager.getRaw(this, "jail-text",  "%target% was jailed!").replace("%target%", "$1");
        for (String n : SkillConfigManager.getRawKeys(this, null)) {
            System.out.println(n == null);
            String retrievedNode = SkillConfigManager.getRaw(this, n, (String) null);
            String[] splitArg = retrievedNode.split(":");
            if (retrievedNode != null && splitArg.length == 4) {
                World world = plugin.getServer().getWorld(splitArg[0]);
                System.out.println(world.getName() + ":" + Double.parseDouble(splitArg[1]) + ":" + Double.parseDouble(splitArg[2]) + ":" + Double.parseDouble(splitArg[3]));
                jailLocations.add(new Location(world, Double.parseDouble(splitArg[1]), Double.parseDouble(splitArg[2]), Double.parseDouble(splitArg[3])));
            }
        }
        
    }

    @Override
    public SkillResult use(Hero hero, LivingEntity target, String[] args) {
        if (jailLocations.isEmpty()) {
            Messaging.send(hero.getPlayer(), "There are no jails setup yet.");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        Player player = hero.getPlayer();
        if (target.equals(player)) {
            return SkillResult.INVALID_TARGET;
        } else if (target instanceof Player && damageCheck((Player) target, player)) {
            Hero tHero = plugin.getHeroManager().getHero((Player) target);
            long duration = SkillConfigManager.getUseSetting(hero, this, Setting.DURATION.node(), 60000, false);
            tHero.addEffect(new JailEffect(this, duration));
            broadcastExecuteText(hero, target);
        }
        
        return SkillResult.NORMAL;
    }
    
    public class JailEffect extends ExpirableEffect {
        public JailEffect(Skill skill, long duration) {
            super(skill, "Jail", duration);
        }
    }
    
    public class JailListener extends EntityListener {
        @Override
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled() || !(event.getEntity() instanceof Player) || event.getDamage() == 0 || jailLocations.isEmpty() ||
                    event.getDamage() < plugin.getHeroManager().getHero((Player) event.getEntity()).getHealth()) {
                return;
            }
            Player player = (Player) event.getEntity();
            Hero hero = plugin.getHeroManager().getHero(player);
            if (hero.hasEffect("Jail")) {
                broadcast(player.getLocation(),jailText,player.getDisplayName());
                Location tempLocation = null;
                for (Location l : jailLocations) {
                    if (tempLocation == null || tempLocation.distanceSquared(player.getLocation()) > l.distanceSquared(player.getLocation())) {
                        tempLocation = l;
                    }
                }
                jailedPlayers.put(player, tempLocation);
            }
        }
    }
    
    public class RespawnListener extends PlayerListener {
        @Override
        public void onPlayerRespawn(final PlayerRespawnEvent event) {
            if (!jailedPlayers.isEmpty() && jailedPlayers.containsKey(event.getPlayer())) {
                event.setRespawnLocation(jailedPlayers.get(event.getPlayer()));
                jailedPlayers.remove(event.getPlayer());
            }
        }
    }

}