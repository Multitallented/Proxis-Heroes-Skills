package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.HeroesEventListener;
import com.herocraftonline.dev.heroes.api.WeaponDamageEvent;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.PassiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillDrain extends PassiveSkill {

    public SkillDrain(Heroes plugin) {
        super(plugin, "Drain");
        setDescription("Passive mana drain on non-skill damage");
        setTypes(SkillType.COUNTER, SkillType.BUFF, SkillType.MANA);
        
        registerEvent(Type.CUSTOM_EVENT, new SkillHeroListener(), Priority.Normal);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("mana-drain-per-attack", 4);
        node.setProperty(Setting.COOLDOWN.node(), 500);
        return node;
    }
    
    public class SkillHeroListener extends HeroesEventListener {
        @Override
        public void onWeaponDamage(WeaponDamageEvent event) {
            if (event.getCause() != DamageCause.ENTITY_ATTACK || event.isCancelled() || event.getDamage() == 0)
                return;
            if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
                Player player = (Player) event.getDamager();
                Hero hero = plugin.getHeroManager().getHero(player);

                if (hero.hasEffect("Drain")) {
                    if (hero.getCooldown("Drain") == null || hero.getCooldown("Drain") <= System.currentTimeMillis()) {
                        Hero tHero = getPlugin().getHeroManager().getHero((Player) event.getEntity());
                        int stealAmount = (int) getSetting(hero, "mana-drain-per-attack", 4, false);
                        long cooldown = getSetting(hero, Setting.COOLDOWN.node(), 500, false);
                        hero.setCooldown("Drain", cooldown + System.currentTimeMillis());
                        if (tHero.getMana() - stealAmount <= 0) {
                            tHero.setMana(0);
                        } else {
                            tHero.setMana(tHero.getMana() - stealAmount);
                        }
                    }
                }
            } else if (event.getDamager() instanceof Projectile) {
                if (((Projectile) event.getDamager()).getShooter() instanceof Player) {
                    Player player = (Player) ((Projectile) event.getDamager()).getShooter();
                    Hero hero = plugin.getHeroManager().getHero(player);

                    if (hero.hasEffect("Drain")) {
                        if (hero.getCooldown("Drain") == null || hero.getCooldown("Drain") <= System.currentTimeMillis()) {
                            Hero tHero = getPlugin().getHeroManager().getHero((Player) event.getEntity());
                            int stealAmount = (int) getSetting(hero, "mana-drain-per-attack", 4, false);
                            long cooldown = getSetting(hero, Setting.COOLDOWN.node(), 500, false);
                            hero.setCooldown("Drain", cooldown + System.currentTimeMillis());
                            if (tHero.getMana() - stealAmount <= 0) {
                                tHero.setMana(0);
                            } else {
                                tHero.setMana(tHero.getMana() - stealAmount);
                            }
                        }
                    }
                }
            }
        }
    }
}