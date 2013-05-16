package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

public class SkillInvertDamage extends ActiveSkill
{
  private String applyText;
  private String expireText;

  public SkillInvertDamage(Heroes paramHeroes)
  {
    super(paramHeroes, "InvertDamage");
    setDescription("Converts all damage for $1s into heals.");
    setUsage("/skill invertdamage");
    setArgumentRange(0, 0);
    setIdentifiers(new String[] { "skill invertdamage" });
    setTypes(new SkillType[] { SkillType.SILENCABLE, SkillType.BUFF, SkillType.MANA });
    Bukkit.getServer().getPluginManager().registerEvents(new SkillEntityListener(), plugin);
    //registerEvent(Event.Type.ENTITY_DAMAGE, new SkillEntityListener(), Event.Priority.Normal);
  }

    @Override
    public String getDescription(Hero hero) {
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this))) / 1000;
        duration = duration > 0 ? duration : 0;
        String description = getDescription().replace("$1", duration + "");
        
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
    ConfigurationSection localConfigurationNode = super.getDefaultConfig();
    localConfigurationNode.set(SkillSetting.DURATION.node(), 6000);
    localConfigurationNode.set("duration-increase", 0);
    localConfigurationNode.set(SkillSetting.APPLY_TEXT.node(), "%target% is absorbing damage!");
    localConfigurationNode.set(SkillSetting.EXPIRE_TEXT.node(), "InvertDamage faded from %target%!");
    return localConfigurationNode;
  }

    @Override
  public void init()
  {
    super.init();
    this.applyText = SkillConfigManager.getUseSetting(null, this, SkillSetting.APPLY_TEXT.node(), "%target% is absorbing damage!").replace("%target%", "$1");
    this.expireText = SkillConfigManager.getUseSetting(null, this, SkillSetting.EXPIRE_TEXT.node(), "InvertDamage faded from %target%!").replace("%target%", "$1");
  }

    @Override
  public SkillResult use(Hero paramHero, String[] paramArrayOfString)
  {
    long duration = (long) (SkillConfigManager.getUseSetting(paramHero, this, SkillSetting.DURATION.node(), 10000, false) +
            (SkillConfigManager.getUseSetting(paramHero, this, "duration-increase", 0.0, false) * paramHero.getSkillLevel(this)));
    duration = duration > 0 ? duration : 0;
    broadcastExecuteText(paramHero);
    paramHero.addEffect(new InvertDamageEffect(this, duration));
    return SkillResult.NORMAL;
  }

  public class SkillEntityListener implements Listener
  {
    public SkillEntityListener()
    {
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent paramEntityDamageEvent)
    {
      if (paramEntityDamageEvent.isCancelled())
      {
        return;
      }
      Entity localEntity = paramEntityDamageEvent.getEntity();
      if (localEntity instanceof Player) {
        Hero localHero = plugin.getCharacterManager().getHero((Player) localEntity);
        if (localHero.hasEffect("InvertDamage"))
        {
            Bukkit.getServer().getPluginManager().callEvent(new EntityRegainHealthEvent(localHero.getPlayer(), paramEntityDamageEvent.getDamage(), RegainReason.CUSTOM));
            paramEntityDamageEvent.setCancelled(true);
        }
      }
    }
  }

  public class InvertDamageEffect extends ExpirableEffect
  {
    public InvertDamageEffect(Skill arg2, long duration)
    {
      super(arg2, "InvertDamage", duration);
      this.types.add(EffectType.BENEFICIAL);
      this.types.add(EffectType.DISPELLABLE);
    }

    @Override
    public void applyToHero(Hero paramHero)
    {
      super.applyToHero(paramHero);
      Player localPlayer = paramHero.getPlayer();
      broadcast(localPlayer.getLocation(), applyText, paramHero.getPlayer().getDisplayName());
    }

    @Override
    public void removeFromHero(Hero paramHero)
    {
      super.removeFromHero(paramHero);
      Player localPlayer = paramHero.getPlayer();
      broadcast(localPlayer.getLocation(), expireText, paramHero.getPlayer().getDisplayName());
    }
  }
}