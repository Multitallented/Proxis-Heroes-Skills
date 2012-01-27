package com.herocraftonline.dev.heroes.skill.skills;


import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.PassiveSkill;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

public class SkillDodge extends PassiveSkill {

    public SkillDodge(Heroes plugin) {
        super(plugin, "Dodge");
        setDescription("Passive $1% chance to dodge enemy attacks.");
        setTypes(SkillType.COUNTER, SkillType.BUFF);
        Bukkit.getServer().getPluginManager().registerEvents(new SkillHeroListener(this), plugin);
        //registerEvent(Type.ENTITY_DAMAGE, new SkillHeroListener(this), Priority.Normal);
    }

    @Override
    public String getDescription(Hero hero) {
        double chance = (SkillConfigManager.getUseSetting(hero, this, Setting.CHANCE.node(), 0.2, false) +
                (SkillConfigManager.getUseSetting(hero, this, Setting.CHANCE_LEVEL.node(), 0.0, false) * hero.getLevel())) * 100;
        chance = chance > 0 ? chance : 0;
        String description = getDescription().replace("$1", chance + "");
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(Setting.CHANCE.node(), 0.1);
        node.set(Setting.CHANCE_LEVEL.node(), 0);
        return node;
    }
    
    public class SkillHeroListener implements Listener {
        private Skill skill;
        public SkillHeroListener(Skill skill) {
            this.skill=skill;
        }
        @EventHandler
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled() || !(event.getEntity() instanceof Player))
                return;
            Player player = (Player) event.getEntity();
            Hero hero = plugin.getHeroManager().getHero(player);
            if (hero.hasEffect("Dodge")) {
                double chance = (SkillConfigManager.getUseSetting(hero, skill, Setting.CHANCE.node(), 0.2, false) +
                        (SkillConfigManager.getUseSetting(hero, skill, Setting.CHANCE_LEVEL.node(), 0.0, false) * hero.getLevel()));
                chance = chance > 0 ? chance : 0;
                if (Math.random() <= chance) {
                    event.setDamage(0);
                    event.setCancelled(true);
                    broadcast(player.getLocation(), "$1 dodged an attack!", player.getDisplayName());
                }
            }
        }
    }
}