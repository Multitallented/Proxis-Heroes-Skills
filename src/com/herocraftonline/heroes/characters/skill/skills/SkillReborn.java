package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.Effect;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Setting;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class SkillReborn extends PassiveSkill {
    private String rebornText;

    public SkillReborn(Heroes plugin) {
        super(plugin, "Reborn");
        setDescription("Passive gives you $1% hp if not you were about to die. CD:$2");
        setTypes(SkillType.COUNTER, SkillType.DARK);
        Bukkit.getServer().getPluginManager().registerEvents(new RebornListener(this), plugin);
        //registerEvent(Type.ENTITY_DAMAGE, new RebornListener(this), Priority.Normal);
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set("health-percent-on-rebirth", .5);
        node.set("health-increase", 0.0);
        node.set("on-reborn-text", "%hero% is saved from death, but weakened!");
        node.set(Setting.COOLDOWN.node(), 600000);
        node.set(Setting.COOLDOWN_REDUCE.node(), 0);
        return node;
    }

    @Override
    public String getDescription(Hero hero) {
        int health = (int) ((SkillConfigManager.getUseSetting(hero, this, "health-percent-on-rebirth", 0.5, false)
                + (SkillConfigManager.getUseSetting(hero, this, "health-increase", 0.0, false) * hero.getSkillLevel(this))) * 100);
        int cooldown = SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN.node(), 600000, false)
                + (SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN_REDUCE.node(), 0, false) * hero.getSkillLevel(this));
        String description = getDescription().replace("$1", health + "").replace("$2", cooldown + "");
        return description;
    }
    
    @Override
    public void init() {
        super.init();
        rebornText = SkillConfigManager.getUseSetting(null, this, "on-reborn-text", "%hero% is saved from death, but weakened!").replace("%hero%", "$1");
    }
    
    public class RebornListener implements Listener {
        private Skill skill;
        public RebornListener(Skill skill) {
            this.skill = skill;
        }
        @EventHandler
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled() || !(event.getEntity() instanceof Player) || event.getDamage() == 0 ||
                    event.getDamage() < plugin.getCharacterManager().getHero((Player) event.getEntity()).getHealth()) {
                return;
            }
            Player player = (Player) event.getEntity();
            Hero hero = plugin.getCharacterManager().getHero(player);
            if (hero.hasEffect("Reborn")) {
                if (hero.getCooldown("Reborn") == null || hero.getCooldown("Reborn") <= System.currentTimeMillis()) {
                    for (Effect e : hero.getEffects()) {
                        if (e.isType(EffectType.STUN) || e.isType(EffectType.DISPELLABLE) || e.isType(EffectType.HARMFUL)
                                || e.isType(EffectType.ROOT) || e.isType(EffectType.WOUNDING) || e.isType(EffectType.BLIND)) {
                            hero.removeEffect(e);
                        }
                    }
                    event.setDamage(0);
                    event.setCancelled(true);
                    hero.setHealth((int) Math.round(hero.getMaxHealth() * (SkillConfigManager.getUseSetting(hero, skill, "health-percent-on-rebirth", 0.5, false)
                            + (SkillConfigManager.getUseSetting(hero, skill, "health-increase", 0.0, false) * hero.getSkillLevel(skill)))));
                    hero.syncHealth();
                    long cooldown = (long) (SkillConfigManager.getUseSetting(hero, skill, Setting.COOLDOWN.node(), 600000, false)
                            + (SkillConfigManager.getUseSetting(hero, skill, Setting.COOLDOWN_REDUCE.node(), 0, false) * hero.getSkillLevel(skill)));
                    hero.setCooldown("Reborn", cooldown + System.currentTimeMillis());
                    broadcast(player.getLocation(),rebornText,player.getDisplayName());
                    Location le2 = player.getLocation();
                    for (int i = 0; i < 9; i++) {
                        le2.getWorld().playEffect(le2, org.bukkit.Effect.SMOKE, i);
                    }
                    for (int i = 0; i < 9; i++) {
                        le2.getWorld().playEffect(le2, org.bukkit.Effect.SMOKE, i);
                    }
                    for (int i = 0; i < 9; i++) {
                        le2.getWorld().playEffect(le2, org.bukkit.Effect.SMOKE, i);
                    }
                }
            }
        }
    }
}