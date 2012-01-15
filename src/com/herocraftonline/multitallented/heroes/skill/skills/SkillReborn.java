package com.herocraftonline.dev.heroes.skill.skills;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.PassiveSkill;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.Effect;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

public class SkillReborn extends PassiveSkill {
    private String rebornText;

    public SkillReborn(Heroes plugin) {
        super(plugin, "Reborn");
        setDescription("Passive gives you $1% hp if not you were about to die. CD:$2");
        setTypes(SkillType.COUNTER, SkillType.DARK);
        
        registerEvent(Type.ENTITY_DAMAGE, new RebornListener(this), Priority.Normal);
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
                + (SkillConfigManager.getUseSetting(hero, this, "health-increase", 0.0, false) * hero.getLevel())) * 100);
        int cooldown = SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN.node(), 600000, false)
                + (SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN_REDUCE.node(), 0, false) * hero.getLevel());
        String description = getDescription().replace("$1", health + "").replace("$2", cooldown + "");
        return description;
    }
    
    @Override
    public void init() {
        super.init();
        rebornText = SkillConfigManager.getUseSetting(null, this, "on-reborn-text", "%hero% is saved from death, but weakened!").replace("%hero%", "$1");
    }
    
    public class RebornListener extends EntityListener {
        private Skill skill;
        public RebornListener(Skill skill) {
            this.skill = skill;
        }
        @Override
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled() || !(event.getEntity() instanceof Player) || event.getDamage() == 0 ||
                    event.getDamage() < plugin.getHeroManager().getHero((Player) event.getEntity()).getHealth()) {
                return;
            }
            Player player = (Player) event.getEntity();
            Hero hero = plugin.getHeroManager().getHero(player);
            if (hero.hasEffect("Reborn")) {
                if (hero.getCooldown("Reborn") == null || hero.getCooldown("Reborn") <= System.currentTimeMillis()) {
                    event.setDamage(0);
                    event.setCancelled(true);
                    hero.setHealth((double) Math.round(hero.getMaxHealth() * (SkillConfigManager.getUseSetting(hero, skill, "health-percent-on-rebirth", 0.5, false)
                            + (SkillConfigManager.getUseSetting(hero, skill, "health-increase", 0.0, false) * hero.getLevel()))));
                    hero.syncHealth();
                    long cooldown = (long) (SkillConfigManager.getUseSetting(hero, skill, Setting.COOLDOWN.node(), 600000, false)
                            + (SkillConfigManager.getUseSetting(hero, skill, Setting.COOLDOWN_REDUCE.node(), 0, false) * hero.getLevel()));
                    hero.setCooldown("Reborn", cooldown + System.currentTimeMillis());
                    broadcast(player.getLocation(),rebornText,player.getDisplayName());
                    player.getWorld().playEffect(player.getLocation(), Effect.SMOKE, 3);
                }
            }
        }
    }
}