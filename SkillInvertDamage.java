package com.herocraftonline.dev.heroes.skill.skills;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.effects.EffectType;
import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.util.config.ConfigurationNode;

public class SkillInvertDamage extends ActiveSkill
{
  private String applyText;
  private String expireText;

  public SkillInvertDamage(Heroes paramHeroes)
  {
    super(paramHeroes, "InvertDamage");
    setDescription("Converts half the damage you take into mana");
    setUsage("/skill invertdamage");
    setArgumentRange(0, 0);
    setIdentifiers(new String[] { "skill invertdamage" });
    setTypes(new SkillType[] { SkillType.SILENCABLE, SkillType.BUFF, SkillType.MANA });
    registerEvent(Event.Type.ENTITY_DAMAGE, new SkillEntityListener(), Event.Priority.Normal);
  }

    @Override
  public ConfigurationNode getDefaultConfig()
  {
    ConfigurationNode localConfigurationNode = super.getDefaultConfig();
    localConfigurationNode.setProperty(Setting.DURATION.node(), 6000);
    localConfigurationNode.setProperty(Setting.APPLY_TEXT.node(), "%target% is absorbing damage!");
    localConfigurationNode.setProperty(Setting.EXPIRE_TEXT.node(), "InvertDamage faded from %target%!");
    return localConfigurationNode;
  }

    @Override
  public void init()
  {
    super.init();
    this.applyText = getSetting(null, Setting.APPLY_TEXT.node(), "%target% is absorbing damage!").replace("%target%", "$1");
    this.expireText = getSetting(null, Setting.EXPIRE_TEXT.node(), "InvertDamage faded from %target%!").replace("%target%", "$1");
  }

    @Override
  public SkillResult use(Hero paramHero, String[] paramArrayOfString)
  {
    long duration = getSetting(paramHero, Setting.DURATION.node(), 6000, false);
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
        Hero localHero = getPlugin().getHeroManager().getHero((Player) localEntity);
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