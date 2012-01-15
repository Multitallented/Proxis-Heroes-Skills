package com.herocraftonline.dev.heroes.skill.skills;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.effects.EffectType;
import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

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
    registerEvent(Event.Type.ENTITY_DAMAGE, new SkillEntityListener(), Event.Priority.Normal);
  }

    @Override
    public String getDescription(Hero hero) {
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, Setting.DURATION.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getLevel())) / 1000;
        duration = duration > 0 ? duration : 0;
        String description = getDescription().replace("$1", duration + "");
        
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
    ConfigurationSection localConfigurationNode = super.getDefaultConfig();
    localConfigurationNode.set(Setting.DURATION.node(), 6000);
    localConfigurationNode.set("duration-increase", 0);
    localConfigurationNode.set(Setting.APPLY_TEXT.node(), "%target% is absorbing damage!");
    localConfigurationNode.set(Setting.EXPIRE_TEXT.node(), "InvertDamage faded from %target%!");
    return localConfigurationNode;
  }

    @Override
  public void init()
  {
    super.init();
    this.applyText = SkillConfigManager.getUseSetting(null, this, Setting.APPLY_TEXT.node(), "%target% is absorbing damage!").replace("%target%", "$1");
    this.expireText = SkillConfigManager.getUseSetting(null, this, Setting.EXPIRE_TEXT.node(), "InvertDamage faded from %target%!").replace("%target%", "$1");
  }

    @Override
  public SkillResult use(Hero paramHero, String[] paramArrayOfString)
  {
    long duration = (long) (SkillConfigManager.getUseSetting(paramHero, this, Setting.DURATION.node(), 10000, false) +
            (SkillConfigManager.getUseSetting(paramHero, this, "duration-increase", 0.0, false) * paramHero.getLevel()));
    duration = duration > 0 ? duration : 0;
    broadcastExecuteText(paramHero);
    paramHero.addEffect(new InvertDamageEffect(this, duration));
    return SkillResult.NORMAL;
  }

  public class SkillEntityListener extends EntityListener
  {
    public SkillEntityListener()
    {
    }

        @Override
    public void onEntityDamage(EntityDamageEvent paramEntityDamageEvent)
    {
      if (paramEntityDamageEvent.isCancelled())
      {
        return;
      }
      Entity localEntity = paramEntityDamageEvent.getEntity();
      if (localEntity instanceof Player) {
        Hero localHero = plugin.getHeroManager().getHero((Player) localEntity);
        if (localHero.hasEffect("InvertDamage"))
        {
          double maxHealth = localHero.getMaxHealth(); 
          double damage = paramEntityDamageEvent.getDamage();
          double j = localHero.getHealth();
          if (j + damage > maxHealth) {
              damage = maxHealth - j;
          }
          localHero.setHealth(damage + j);
          localHero.syncHealth();
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
    public void apply(Hero paramHero)
    {
      super.apply(paramHero);
      Player localPlayer = paramHero.getPlayer();
      broadcast(localPlayer.getLocation(), applyText, paramHero.getPlayer().getDisplayName());
    }

    @Override
    public void remove(Hero paramHero)
    {
      super.remove(paramHero);
      Player localPlayer = paramHero.getPlayer();
      broadcast(localPlayer.getLocation(), expireText, paramHero.getPlayer().getDisplayName());
    }
  }
}