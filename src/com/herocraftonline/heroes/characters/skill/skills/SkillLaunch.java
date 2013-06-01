package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.common.InvulnerabilityEffect;
import com.herocraftonline.heroes.characters.effects.common.RootEffect;
import com.herocraftonline.heroes.characters.effects.common.SilenceEffect;
import com.herocraftonline.heroes.characters.effects.common.SlowEffect;
import com.herocraftonline.heroes.characters.effects.common.StunEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

public class SkillLaunch extends ActiveSkill
{
  private Map<Player, Location> chargingPlayers = new HashMap<Player, Location>();

  public SkillLaunch(Heroes paramHeroes)
  {
    super(paramHeroes, "Launch");
    setDescription("Jump up to $3 blocks. The farther you jump, the more damage you do.");
    setUsage("/skill launch");
    setArgumentRange(0, 1);
    setIdentifiers(new String[] { "skill launch" });
    setTypes(new SkillType[] { SkillType.PHYSICAL, SkillType.MOVEMENT, SkillType.HARMFUL });
    Bukkit.getServer().getPluginManager().registerEvents(new ChargeEntityListener(this), plugin);
    //registerEvent(Event.Type.ENTITY_DAMAGE, new ChargeEntityListener(this), Event.Priority.Normal);
  }

    @Override
    public String getDescription(Hero hero) {
        long stunDuration = (long) SkillConfigManager.getUseSetting(hero, this, "stun-duration", 10000, false);
        if (stunDuration > 0) {
            stunDuration = (long) (stunDuration + (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this))) / 1000;
            stunDuration = stunDuration > 0 ? stunDuration : 0;
        }
        long slowDuration = (long) SkillConfigManager.getUseSetting(hero, this, "slow-duration", 0, false);
        if (slowDuration > 0) {
            slowDuration = (long) (slowDuration + (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this))) / 1000;
            slowDuration = slowDuration > 0 ? slowDuration : 0;
        }
        long rootDuration = (long) SkillConfigManager.getUseSetting(hero, this, "root-duration", 0, false);
        if (rootDuration > 0) {
            rootDuration = (long) (rootDuration + (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this))) / 1000;
            rootDuration = rootDuration > 0 ? rootDuration : 0;
        }
        long silenceDuration = (long) SkillConfigManager.getUseSetting(hero, this, "silence-duration", 0, false);
        if (silenceDuration > 0) {
            silenceDuration = (long) (silenceDuration + (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this))) / 1000;
            silenceDuration = silenceDuration > 0 ? silenceDuration : 0;
        }
        long invulnDuration = (long) SkillConfigManager.getUseSetting(hero, this, "invuln-duration", 0, false);
        if (invulnDuration > 0) {
            invulnDuration = (long) (invulnDuration + (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this))) / 1000;
            invulnDuration = invulnDuration > 0 ? invulnDuration : 0;
        }
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE.node(), 0, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getSkillLevel(this)));
        damage = damage > 0 ? damage : 0;
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS.node(), 2, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.RADIUS_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        radius = radius > 0 ? radius : 0;
        int distance = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE.node(), 15, false) +
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MAX_DISTANCE_INCREASE.node(), 0.0, false) * hero.getSkillLevel(this)));
        distance = distance > 0 ? distance : 0;
        String description = getDescription().replace("$1", damage + "").replace("$2", radius + "").replace("$3", distance + "");
        if (stunDuration > 0) {
            description += " Stun:" + stunDuration + "s";
        }
        if (slowDuration > 0) {
            description += " Slow:" + slowDuration + "s";
        }
        if (rootDuration > 0) {
            description += " Root:" + rootDuration + "s";
        }
        if (silenceDuration > 0) {
            description += " Silence:" + silenceDuration + "s";
        }
        if (invulnDuration > 0) {
            description += " Invuln:" + invulnDuration + "s";
        }
        
        
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
  public ConfigurationSection getDefaultConfig()
  {
    ConfigurationSection localConfigurationSection = super.getDefaultConfig();
    localConfigurationSection.set("stun-duration", 5000);
    localConfigurationSection.set("slow-duration", 0);
    localConfigurationSection.set("root-duration", 0);
    localConfigurationSection.set("silence-duration", 0);
    localConfigurationSection.set("invuln-duration", 0);
    localConfigurationSection.set("duration-increase", 0);
    localConfigurationSection.set(SkillSetting.DAMAGE.node(), 0);
    localConfigurationSection.set("damage-increase", 0);
    localConfigurationSection.set(SkillSetting.RADIUS.node(), 2);
    localConfigurationSection.set(SkillSetting.RADIUS_INCREASE.node(), 0);
    localConfigurationSection.set(SkillSetting.MAX_DISTANCE.node(), 15);
    localConfigurationSection.set(SkillSetting.MAX_DISTANCE_INCREASE.node(), 0);
    localConfigurationSection.set(SkillSetting.USE_TEXT.node(), "%hero% used %skill%!");
    localConfigurationSection.set("damage-per-block-traveled", 0.1);
    localConfigurationSection.set("mana-per-block-traveled", 0.5);
    return localConfigurationSection;
  }

  @Override
  public SkillResult use(Hero paramHero, String[] paramArrayOfString)
  {
    final Player localPlayer = paramHero.getPlayer();
    Location localLocation1 = localPlayer.getLocation();
    int distance = (int) (SkillConfigManager.getUseSetting(paramHero, this, SkillSetting.MAX_DISTANCE.node(), 15, false) +
             (SkillConfigManager.getUseSetting(paramHero, this, SkillSetting.MAX_DISTANCE_INCREASE.node(), 0.0, false) * paramHero.getSkillLevel(this)));
    distance = distance > 0 ? distance : 0;
    Location localLocation2 = null;
    try {
        localLocation2 = localPlayer.getTargetBlock(null, distance).getLocation();
    } catch (IllegalArgumentException iae) {
        return SkillResult.INVALID_TARGET_NO_MSG;
    }
    double d1 = localLocation2.getX() - localLocation1.getX();
    double d2 = localLocation2.getZ() - localLocation1.getZ();
    double d3 = Math.sqrt(d1 * d1 + d2 * d2);
    double d4 = 0;
    try {
        d4 = localLocation2.distance(localLocation1) / 8.0D;
    } catch (IllegalArgumentException iae) {
        return SkillResult.INVALID_TARGET_NO_MSG;
    }
    d1 = d1 / d3 * d4;
    d2 = d2 / d3 * d4;
    localPlayer.setVelocity(new Vector(d1, 1.0D, d2));
    this.chargingPlayers.put(localPlayer, localLocation1);
    this.plugin.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable()
    {
            @Override
      public void run()
      {
        localPlayer.setFallDistance(8.0F);
      }
    }
    , 1L);
    broadcastExecuteText(paramHero);
    return SkillResult.NORMAL;
  }

  public class ChargeEntityListener implements Listener
  {
    private final Skill skill;

    public ChargeEntityListener(Skill arg2)
    {
      this.skill = arg2;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent paramEntityDamageEvent)
    {
      Heroes.debug.startTask("HeroesSkillListener");
      if ((!paramEntityDamageEvent.getCause().equals(EntityDamageEvent.DamageCause.FALL)) || (!(paramEntityDamageEvent.getEntity() instanceof Player)) || (!chargingPlayers.containsKey((Player) paramEntityDamageEvent.getEntity())))
      {
        Heroes.debug.stopTask("HeroesSkillListener");
        return;
      }
      Player localPlayer1 = (Player)paramEntityDamageEvent.getEntity();
      Hero localHero1 = plugin.getCharacterManager().getHero(localPlayer1);
      double distance = localPlayer1.getLocation().distance(chargingPlayers.get(localPlayer1));
      double damageMod = SkillConfigManager.getUseSetting(localHero1, skill, "damage-per-block-traveled", 0.1, false);
      double manaMod = SkillConfigManager.getUseSetting(localHero1, skill, "mana-per-block-traveled", 0.5, false);
      int currentMana = localHero1.getMana();
      if (currentMana < distance * manaMod) {
          localHero1.setMana(0);
      } else {
          localHero1.setMana(currentMana - (int) (distance * manaMod));
      }
      chargingPlayers.remove(localPlayer1);
      paramEntityDamageEvent.setDamage(0);
      paramEntityDamageEvent.setCancelled(true);
      int i = (int) (SkillConfigManager.getUseSetting(localHero1, this.skill, SkillSetting.RADIUS.node(), 2, false) +
              (SkillConfigManager.getUseSetting(localHero1, this.skill, SkillSetting.RADIUS_INCREASE.node(), 0.0, false) * localHero1.getSkillLevel(skill)));
      i = i > 0 ? i : 0;
      long l1 = (long) SkillConfigManager.getUseSetting(localHero1, this.skill, "stun-duration", 10000, false);
      if (l1 > 0) {
          l1 = (long) (l1 + (SkillConfigManager.getUseSetting(localHero1, this.skill, "duration-increase", 0.0, false) * localHero1.getSkillLevel(skill)));
          l1 = l1 > 0 ? l1 : 0;
      }
      long l2 = (long) SkillConfigManager.getUseSetting(localHero1, this.skill, "slow-duration", 0, false);
      if (l2 > 0) {
          l2 = (long) (l2 + (SkillConfigManager.getUseSetting(localHero1, this.skill, "duration-increase", 0.0, false) * localHero1.getSkillLevel(skill)));
          l2 = l2 > 0 ? l2 : 0;
      }
      long l3 = (long) SkillConfigManager.getUseSetting(localHero1, this.skill, "root-duration", 0, false);
      if (l3 > 0) {
          l3 = (long) (l3 + (SkillConfigManager.getUseSetting(localHero1, this.skill, "duration-increase", 0.0, false) * localHero1.getSkillLevel(skill)));
          l3 = l3 > 0 ? l3 : 0;
      }
      long l4 = (long) SkillConfigManager.getUseSetting(localHero1, this.skill, "silence-duration", 0, false);
      if (l4 > 0) {
          l4 = (long) (l4 + (SkillConfigManager.getUseSetting(localHero1, this.skill, "duration-increase", 0.0, false) * localHero1.getSkillLevel(skill)));
          l4 = l4 > 0 ? l4 : 0;
      }
      int j = (int) (SkillConfigManager.getUseSetting(localHero1, this.skill, SkillSetting.DAMAGE.node(), 0, false) +
              (SkillConfigManager.getUseSetting(localHero1, this.skill, "damage-increase", 0.0, false) * localHero1.getSkillLevel(skill)));
      j += damageMod * distance;
      j = j > 0 ? j : 0;
      long l5 = (long) SkillConfigManager.getUseSetting(localHero1, this.skill, "invuln-duration", 0, false);
      if (l5 > 0) {
          l5 = (long) (l5 + (SkillConfigManager.getUseSetting(localHero1, this.skill, "duration-increase", 0.0, false) * localHero1.getSkillLevel(skill)));
          l5 = l5 > 0 ? l5 : 0;
          if (l5 > 0) {
            localHero1.addEffect(new InvulnerabilityEffect(this.skill, l5));
          }
      }
      Iterator localIterator = localPlayer1.getNearbyEntities(i, i, i).iterator();
      while (localIterator.hasNext())
      {
        Entity localEntity = (Entity)localIterator.next();
        if (!(localEntity instanceof LivingEntity))
          continue;
        LivingEntity localLivingEntity = (LivingEntity)localEntity;
        if (!damageCheck(localPlayer1, localLivingEntity))
          continue;
        if ((localEntity instanceof Player))
        {
          Player localPlayer2 = (Player)localEntity;
          Hero localHero2 = plugin.getCharacterManager().getHero(localPlayer2);
          if (l1 > 0L)
            localHero2.addEffect(new StunEffect(this.skill, l1));
          if (l2 > 0L)
            localHero2.addEffect(new SlowEffect(this.skill, l2, 2, true, localPlayer2.getDisplayName() + " has been slowed by " + localPlayer1.getDisplayName(), localPlayer2.getDisplayName() + " is no longer slowed by " + localPlayer1.getDisplayName(), localHero1));
          if (l3 > 0L)
            localHero2.addEffect(new RootEffect(this.skill, l3));
          if (l4 > 0L)
            localHero2.addEffect(new SilenceEffect(this.skill, l4));
          if (j > 0)
            this.skill.damageEntity(localLivingEntity, localPlayer1, j, EntityDamageEvent.DamageCause.MAGIC);
        }
        if (j > 0)
          this.skill.damageEntity(localLivingEntity, localPlayer1, j, EntityDamageEvent.DamageCause.MAGIC);
      }
      Heroes.debug.stopTask("HeroesSkillListener");
    }
  }
}