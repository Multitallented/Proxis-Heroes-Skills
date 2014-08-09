package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.common.ImbueEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

public class SkillExplodingArrow extends ActiveSkill
{
    public SkillExplodingArrow(Heroes plugin)
    {
        super(plugin, "ExplodingArrow");
        setDescription("Shoots an exploding arrow");
        setUsage("/skill explodingarrow");
        setArgumentRange(0, 0);
        setIdentifiers(new String[] { "skill explodingarrow", "skill earrow" });
        setTypes(new SkillType[] { SkillType.FIRE, SkillType.BUFF });

        Bukkit.getServer().getPluginManager().registerEvents(new SkillEntityListener(this), plugin);
    }

    @Override
    public ConfigurationSection getDefaultConfig()
    {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(SkillSetting.DAMAGE.node(), Integer.valueOf(5));
        node.set("block-dmg", Integer.valueOf(0));
        node.set("mana-per-shot", Integer.valueOf(1));
        node.set("radius", Integer.valueOf(5));
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args)
    {
        long duration = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION, 60000, false);
        int numAttacks = SkillConfigManager.getUseSetting(hero, this, "attacks", 1, false);
        hero.addEffect(new ExplodingArrowBuff(this, duration, numAttacks));
        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }

    @Override
    public String getDescription(Hero hero)
    {
        return getDescription();
    }

    public class ExplodingArrowBuff extends ImbueEffect
    {
        public ExplodingArrowBuff(Skill skill, long duration, int numAttacks)
        {
            super(skill, "ExplodingArrowBuff");
            this.types.add(EffectType.FIRE);
            setDescription("exploding");
        }
    }

    public class SkillEntityListener implements Listener {
        private final Skill skill;

        public SkillEntityListener(Skill skill) {
            this.skill = skill;
        }

        @EventHandler
        public void onProjectileHit(ProjectileHitEvent projectile) {

            if (!(projectile.getEntity() instanceof Arrow)) {
                return;
            }

            Arrow arrow = (Arrow)projectile.getEntity();

            if (!(arrow.getShooter() instanceof Player)) {
                return;
            }

            Player player = (Player)arrow.getShooter();
            Hero hero = SkillExplodingArrow.this.plugin.getCharacterManager().getHero(player);
            if (!hero.hasEffect("ExplodingArrowBuff")) {
                return;
            }

            int radius = (int)Math.pow(SkillConfigManager.getUseSetting(hero, this.skill, "radius", 5, false), 2.0D);

            float damage = SkillConfigManager.getUseSetting(hero, this.skill, "DAMAGE", 5, false);
            float blockdamage = damage;
            int block_dmg = SkillConfigManager.getUseSetting(hero, this.skill, "block-dmg", 0, false);

            if (block_dmg == 0)
            {
                blockdamage = 0.0F;

                for (Entity t_entity : player.getWorld().getEntities()) {
                    if ((t_entity instanceof Player)) {
                        Player heroes = (Player)t_entity;
                        if ((heroes.equals(player)) || 
                                (heroes.getLocation().distanceSquared(arrow.getLocation()) > radius)) continue;
                        addSpellTarget(heroes,hero);
                        damageEntity(heroes, player, damage, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION);
                    }
                    else if ((t_entity instanceof Creature)) {
                        Creature mob = (Creature)t_entity;
                        if (t_entity.getLocation().distanceSquared(arrow.getLocation()) <= radius) {
                            addSpellTarget(mob,hero);
                            damageEntity(mob, player, damage, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION);
                        }
                    }

                }

            }

            arrow.getWorld().createExplosion(arrow.getLocation(), blockdamage);

        }

        @EventHandler
        public void onEntityDamage(EntityDamageEvent event) {
            if ((event.isCancelled()) || (!(event instanceof EntityDamageByEntityEvent))) {
                return;
            }

            Entity projectile = ((EntityDamageByEntityEvent)event).getDamager();
            if ((!(projectile instanceof Arrow)) || (!(((Projectile)projectile).getShooter() instanceof Player))) {
                return;
            }

            Player player = (Player)((Projectile)projectile).getShooter();
            Hero hero = SkillExplodingArrow.this.plugin.getCharacterManager().getHero(player);
            if (!hero.hasEffect("ExplodingArrowBuff")) {
                return;
            }

            int radius = (int)Math.pow(SkillConfigManager.getUseSetting(hero, this.skill, "radius", 5, false), 2.0D);

            float damage = SkillConfigManager.getUseSetting(hero, this.skill, "DAMAGE", 5, false);
            float blockdamage = damage;
            int block_dmg = SkillConfigManager.getUseSetting(hero, this.skill, "block-dmg", 0, false);

            if (block_dmg == 0)
            {
                blockdamage = 0.0F;

                for (Entity t_entity : player.getWorld().getEntities()) {
                    if ((t_entity instanceof Player)) {
                        Player heroes = (Player)t_entity;
                        if (heroes.getLocation().distanceSquared(projectile.getLocation()) <= radius)
                            addSpellTarget(heroes,hero);
                            SkillExplodingArrow.damageEntity(heroes, player, damage, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION);
                    }
                    else if ((t_entity instanceof Creature)) {
                        Creature mob = (Creature)t_entity;
                        if (t_entity.getLocation().distanceSquared(projectile.getLocation()) <= radius) {
                            addSpellTarget(mob,hero);
                            SkillExplodingArrow.damageEntity(mob, player, damage, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION);
                        }
                    }

                }

            }

            projectile.getWorld().createExplosion(projectile.getLocation(), blockdamage);

        }

        @EventHandler(priority=EventPriority.MONITOR)
        public void onEntityShootBow(EntityShootBowEvent event) {
            if ((event.isCancelled()) || (!(event.getEntity() instanceof Player)) || (!(event.getProjectile() instanceof Arrow))) {
                return;
            }
            Hero hero = SkillExplodingArrow.this.plugin.getCharacterManager().getHero((Player)event.getEntity());
            if (hero.hasEffect("ExplodingArrowBuff")) {
                int mana = SkillConfigManager.getUseSetting(hero, this.skill, "mana-per-shot", 1, true);
                if (hero.getMana() < mana)
                    hero.removeEffect(hero.getEffect("ExplodingArrowBuff"));
                else
                    hero.setMana(hero.getMana() - mana);
            }
        }
    }
}