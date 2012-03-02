package com.herocraftonline.dev.heroes.skill.skills;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.effects.common.InvulnerabilityEffect;
import com.herocraftonline.dev.heroes.effects.common.RootEffect;
import com.herocraftonline.dev.heroes.effects.common.SilenceEffect;
import com.herocraftonline.dev.heroes.effects.common.SlowEffect;
import com.herocraftonline.dev.heroes.effects.common.StunEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

public class SkillPounce extends ActiveSkill
{
  private Set<Player> chargingPlayers = new HashSet();

  public SkillPounce(Heroes paramHeroes)
  {
    super(paramHeroes, "Pounce");
    setDescription("Jump up to $3 blocks to your target. AOE Radius:$2 Damage:$1");
    setUsage("/skill pounce");
    setArgumentRange(0, 1);
    setIdentifiers(new String[] { "skill pounce" });
    setTypes(new SkillType[] { SkillType.PHYSICAL, SkillType.MOVEMENT, SkillType.HARMFUL });
    Bukkit.getServer().getPluginManager().registerEvents(new ChargeEntityListener(this), plugin);
    //registerEvent(Event.Type.ENTITY_DAMAGE, new ChargeEntityListener(this), Event.Priority.Lowest);
  }

    @Override
    public String getDescription(Hero hero) {
        long stunDuration = (long) SkillConfigManager.getUseSetting(hero, this, "stun-duration", 10000, false);
        if (stunDuration > 0) {
            stunDuration = (long) (stunDuration + (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getLevel())) / 1000;
            stunDuration = stunDuration > 0 ? stunDuration : 0;
        }
        long slowDuration = (long) SkillConfigManager.getUseSetting(hero, this, "slow-duration", 0, false);
        if (slowDuration > 0) {
            slowDuration = (long) (slowDuration + (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getLevel())) / 1000;
            slowDuration = slowDuration > 0 ? slowDuration : 0;
        }
        long rootDuration = (long) SkillConfigManager.getUseSetting(hero, this, "root-duration", 0, false);
        if (rootDuration > 0) {
            rootDuration = (long) (rootDuration + (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getLevel())) / 1000;
            rootDuration = rootDuration > 0 ? rootDuration : 0;
        }
        long silenceDuration = (long) SkillConfigManager.getUseSetting(hero, this, "silence-duration", 0, false);
        if (silenceDuration > 0) {
            silenceDuration = (long) (silenceDuration + (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getLevel())) / 1000;
            silenceDuration = silenceDuration > 0 ? silenceDuration : 0;
        }
        long invulnDuration = (long) SkillConfigManager.getUseSetting(hero, this, "invuln-duration", 0, false);
        if (invulnDuration > 0) {
            invulnDuration = (long) (invulnDuration + (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getLevel())) / 1000;
            invulnDuration = invulnDuration > 0 ? invulnDuration : 0;
        }
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.DAMAGE.node(), 0, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getLevel()));
        damage = damage > 0 ? damage : 0;
        int radius = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS.node(), 2, false) +
                (SkillConfigManager.getUseSetting(hero, this, Setting.RADIUS_INCREASE.node(), 0.0, false) * hero.getLevel()));
        radius = radius > 0 ? radius : 0;
        int distance = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.MAX_DISTANCE.node(), 15, false) +
                (SkillConfigManager.getUseSetting(hero, this, Setting.MAX_DISTANCE_INCREASE.node(), 0.0, false) * hero.getLevel()));
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
  public ConfigurationSection getDefaultConfig()
  {
    ConfigurationSection localConfigurationSection = super.getDefaultConfig();
    localConfigurationSection.set("stun-duration", 5000);
    localConfigurationSection.set("slow-duration", 0);
    localConfigurationSection.set("root-duration", 0);
    localConfigurationSection.set("silence-duration", 0);
    localConfigurationSection.set("invuln-duration", 0);
    localConfigurationSection.set("duration-increase", 0);
    localConfigurationSection.set(Setting.DAMAGE.node(), 0);
    localConfigurationSection.set("damage-increase", 0);
    localConfigurationSection.set(Setting.RADIUS.node(), 2);
    localConfigurationSection.set(Setting.RADIUS_INCREASE.node(), 0);
    localConfigurationSection.set(Setting.MAX_DISTANCE.node(), 15);
    localConfigurationSection.set(Setting.MAX_DISTANCE_INCREASE.node(), 0);
    localConfigurationSection.set(Setting.USE_TEXT.node(), "%hero% used %skill%!");
    return localConfigurationSection;
  }

  @Override
  public SkillResult use(Hero paramHero, String[] paramArrayOfString)
  {
    final Player localPlayer = paramHero.getPlayer();
    Location localLocation1 = localPlayer.getLocation();
    int distance = (int) (SkillConfigManager.getUseSetting(paramHero, this, Setting.MAX_DISTANCE.node(), 15, false) +
             (SkillConfigManager.getUseSetting(paramHero, this, Setting.MAX_DISTANCE_INCREASE.node(), 0.0, false) * paramHero.getLevel()));
    distance = distance > 0 ? distance : 0;
    Location localLocation2 = localPlayer.getTargetBlock(null, distance).getLocation();
    double d1 = localLocation2.getX() - localLocation1.getX();
    double d2 = localLocation2.getZ() - localLocation1.getZ();
    double d3 = Math.sqrt(d1 * d1 + d2 * d2);
    double d4 = localLocation2.distance(localLocation1) / 8.0D;
    d1 = d1 / d3 * d4;
    d2 = d2 / d3 * d4;
    localPlayer.setVelocity(new Vector(d1, 1.0D, d2));
    this.chargingPlayers.add(localPlayer);
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

    @EventHandler()
    public void onEntityDamage(EntityDamageEvent paramEntityDamageEvent)
    {
      Heroes.debug.startTask("HeroesSkillListener");
      if ((!paramEntityDamageEvent.getCause().equals(EntityDamageEvent.DamageCause.FALL)) || (!(paramEntityDamageEvent.getEntity() instanceof Player)) || (!chargingPlayers.contains((Player) paramEntityDamageEvent.getEntity())))
      {
        Heroes.debug.stopTask("HeroesSkillListener");
        return;
      }
      Player localPlayer1 = (Player)paramEntityDamageEvent.getEntity();
      Hero localHero1 = plugin.getHeroManager().getHero(localPlayer1);
      chargingPlayers.remove(localPlayer1);
      paramEntityDamageEvent.setDamage(0);
      paramEntityDamageEvent.setCancelled(true);
      int i = (int) (SkillConfigManager.getUseSetting(localHero1, this.skill, Setting.RADIUS.node(), 2, false) +
              (SkillConfigManager.getUseSetting(localHero1, this.skill, Setting.RADIUS_INCREASE.node(), 0.0, false) * localHero1.getLevel()));
      i = i > 0 ? i : 0;
      long l1 = (long) SkillConfigManager.getUseSetting(localHero1, this.skill, "stun-duration", 10000, false);
      if (l1 > 0) {
          l1 = (long) (l1 + (SkillConfigManager.getUseSetting(localHero1, this.skill, "duration-increase", 0.0, false) * localHero1.getLevel()));
          l1 = l1 > 0 ? l1 : 0;
      }
      long l2 = (long) SkillConfigManager.getUseSetting(localHero1, this.skill, "slow-duration", 0, false);
      if (l2 > 0) {
          l2 = (long) (l2 + (SkillConfigManager.getUseSetting(localHero1, this.skill, "duration-increase", 0.0, false) * localHero1.getLevel()));
          l2 = l2 > 0 ? l2 : 0;
      }
      long l3 = (long) SkillConfigManager.getUseSetting(localHero1, this.skill, "root-duration", 0, false);
      if (l3 > 0) {
          l3 = (long) (l3 + (SkillConfigManager.getUseSetting(localHero1, this.skill, "duration-increase", 0.0, false) * localHero1.getLevel()));
          l3 = l3 > 0 ? l3 : 0;
      }
      long l4 = (long) SkillConfigManager.getUseSetting(localHero1, this.skill, "silence-duration", 0, false);
      if (l4 > 0) {
          l4 = (long) (l4 + (SkillConfigManager.getUseSetting(localHero1, this.skill, "duration-increase", 0.0, false) * localHero1.getLevel()));
          l4 = l4 > 0 ? l4 : 0;
      }
      int j = (int) (SkillConfigManager.getUseSetting(localHero1, this.skill, Setting.DAMAGE.node(), 0, false) +
              (SkillConfigManager.getUseSetting(localHero1, this.skill, "damage-increase", 0.0, false) * localHero1.getLevel()));
      j = j > 0 ? j : 0;
      long l5 = (long) SkillConfigManager.getUseSetting(localHero1, this.skill, "invuln-duration", 0, false);
      if (l5 > 0) {
          l5 = (long) (l5 + (SkillConfigManager.getUseSetting(localHero1, this.skill, "duration-increase", 0.0, false) * localHero1.getLevel()));
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
          Hero localHero2 = plugin.getHeroManager().getHero(localPlayer2);
          if (l1 > 0L)
            localHero2.addEffect(new StunEffect(this.skill, l1));
          if (l2 > 0L)
            localHero2.addEffect(new SlowEffect(this.skill, l2, 2, true, localPlayer2.getDisplayName() + " has been slowed by " + localPlayer1.getDisplayName(), localPlayer2.getDisplayName() + " is no longer slowed by " + localPlayer1.getDisplayName(), localHero1));
          if (l3 > 0L)
            localHero2.addEffect(new RootEffect(this.skill, l3));
          if (l4 > 0L)
            localHero2.addEffect(new SilenceEffect(this.skill, l4));
          if (j > 0)
            damageEntity(localLivingEntity, localPlayer1, j, EntityDamageEvent.DamageCause.ENTITY_ATTACK);
        }
        else if ((localEntity instanceof LivingEntity))
        {
          if (l2 > 0L)
            plugin.getEffectManager().addEntityEffect(localLivingEntity, new SlowEffect(this.skill, l2, 2, true, Messaging.getLivingEntityName(localLivingEntity) + " has been slowed by " + localPlayer1.getDisplayName(), Messaging.getLivingEntityName(localLivingEntity) + " is no longer slowed by " + localPlayer1.getDisplayName(), localHero1));
          if (l3 > 0L)
            plugin.getEffectManager().addEntityEffect(localLivingEntity, new RootEffect(this.skill, l3));
        }
        if (j > 0) {
            damageEntity(localLivingEntity, localPlayer1, j, DamageCause.MAGIC);
            //localLivingEntity.damage(j, localPlayer1);
        }
          //this.skill.damageEntity(localLivingEntity, localPlayer1, j, EntityDamageEvent.DamageCause.ENTITY_ATTACK);
      }
      Heroes.debug.stopTask("HeroesSkillListener");
    }
  }
}