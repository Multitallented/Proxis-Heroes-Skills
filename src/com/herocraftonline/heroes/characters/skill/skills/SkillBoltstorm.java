package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.PeriodicExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Util;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

public class SkillBoltstorm extends ActiveSkill
{
  private String applyText;
  private String expireText;

  public SkillBoltstorm(Heroes plugin)
  {
    super(plugin, "Boltstorm");
    setDescription("You call bolts of lightning down upon nearby enemies for $1 seconds, each bolt will deal $2 damage.");
    setUsage("/skill boltstorm");
    setArgumentRange(0, 0);
    setIdentifiers(new String[] { "skill boltstorm" });
    setTypes(new SkillType[] { SkillType.LIGHTNING, SkillType.SILENCABLE, SkillType.DAMAGING });
  }

    @Override
  public ConfigurationSection getDefaultConfig()
  {
    ConfigurationSection node = super.getDefaultConfig();
    node.set(SkillSetting.RADIUS.node(), Integer.valueOf(7));
    node.set(SkillSetting.DURATION.node(), Integer.valueOf(10000));
    node.set(SkillSetting.PERIOD.node(), Integer.valueOf(1000));
    node.set(SkillSetting.DAMAGE.node(), Integer.valueOf(4));
    node.set(SkillSetting.APPLY_TEXT.node(), "%hero% has summoned a boltstorm!");
    node.set(SkillSetting.EXPIRE_TEXT.node(), "%hero%'s boltstorm has subsided!");
    return node;
  }

    @Override
  public void init()
  {
    super.init();
    this.applyText = SkillConfigManager.getRaw(this, SkillSetting.APPLY_TEXT, "%hero% has summoned a boltstorm!").replace("%hero%", "$1");
    this.expireText = SkillConfigManager.getRaw(this, SkillSetting.EXPIRE_TEXT, "%hero%'s boltstorm has subsided!").replace("%hero%", "$1");
  }

    @Override
  public SkillResult use(Hero hero, String[] args)
  {
    int period = SkillConfigManager.getUseSetting(hero, this, SkillSetting.PERIOD, 1000, true);
    int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 10000, false);
    hero.addEffect(new BoltStormEffect(this, period, duration));
    return SkillResult.NORMAL;
  }

    @Override
  public String getDescription(Hero hero)
  {
    int duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 10000, false);
    int damage = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE, 4, false);
    return getDescription().replace("$1", duration / 1000 + "").replace("$2", damage + "");
  }

  public class BoltStormEffect extends PeriodicExpirableEffect
  {
    public BoltStormEffect(Skill skill, long period, long duration)
    {
      super(skill, "Boltstorm", period, duration);
      this.types.add(EffectType.BENEFICIAL);
      this.types.add(EffectType.DISPELLABLE);
      this.types.add(EffectType.LIGHTNING);
    }

      @Override
    public void applyToHero(Hero hero)
    {
      super.applyToHero(hero);
      Player player = hero.getPlayer();
      broadcast(player.getLocation(), SkillBoltstorm.this.applyText, player.getDisplayName());
    }

      @Override
    public void removeFromHero(Hero hero)
    {
      super.removeFromHero(hero);
      Player player = hero.getPlayer();
      broadcast(player.getLocation(), SkillBoltstorm.this.expireText, player.getDisplayName());
    }

      @Override
    public void tickHero(Hero hero)
    {
      Player player = hero.getPlayer();
      int range = SkillConfigManager.getUseSetting(hero, this.skill, SkillSetting.RADIUS, 7, false);

      List<LivingEntity> targets = new ArrayList<LivingEntity>();
      for (Entity entity : player.getNearbyEntities(range, range, range)) {
        if (!(entity instanceof LivingEntity))
        {
          continue;
        }
        LivingEntity target = (LivingEntity)entity;

        if ((target.equals(player)) || (hero.getSummons().contains(target)) || 
          (!Skill.damageCheck(player, target)))
        {
          continue;
        }
        targets.add(target);
      }

      if (targets.isEmpty()) {
        return;
      }
      double damage = SkillConfigManager.getUseSetting(hero, this.skill, SkillSetting.DAMAGE, 4, false);
      LivingEntity target = targets.get(Util.nextInt(targets.size()));
      target.getWorld().strikeLightningEffect(target.getLocation());

      SkillBoltstorm.this.addSpellTarget(target, hero);
      Skill.damageEntity(target, player, damage, EntityDamageEvent.DamageCause.MAGIC);
    }

      @Override
    public void tickMonster(Monster monster) {
    }
  }
}