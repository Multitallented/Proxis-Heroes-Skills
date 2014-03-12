package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.PassiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import org.bukkit.entity.Player;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class SkillDampen extends PassiveSkill {
    
    public String skillBlockText;

    public SkillDampen(Heroes plugin) {
        super(plugin, "Dampen");
        setDescription("Makes you invulnerable to opponents with < $1 mana. M:$2");
        setArgumentRange(0, 0);
        
        setTypes(SkillType.MANA, SkillType.COUNTER);
        Bukkit.getServer().getPluginManager().registerEvents(new EntityDamageListener(this), plugin);
        //registerEvent(Type.ENTITY_DAMAGE, new EntityDamageListener(this), Priority.Normal);
    }

    @Override
    public String getDescription(Hero hero) {
        int manaReq = (int) (SkillConfigManager.getUseSetting(hero, this, "block-if-mana-below", 15, false) +
                (SkillConfigManager.getUseSetting(hero, this, "block-increase", 0.0, false) * hero.getSkillLevel(this)));
        manaReq = manaReq > 0 ? manaReq : 0;
        int minMana = (int) (SkillConfigManager.getUseSetting(hero, this, "mana-required", 20, false) -
                (SkillConfigManager.getUseSetting(hero, this, "mana-required-decrease", 0.0, false) * hero.getSkillLevel(this)));
        minMana = minMana > 0 ? minMana : 0;
        String description = getDescription().replace("$1", manaReq + "").replace("$2", minMana + "");
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set("block-if-mana-below", 15);
        node.set("block-increase", 0);
        node.set("mana-required", 20);
        node.set("mana-required-decrease", 0);
        node.set("block-text", "%name%s dampening field stopped %target%s attack!");
        return node;
    }
    
    @Override
    public void init() {
        super.init();
        skillBlockText = SkillConfigManager.getUseSetting(null, this, "skill-block-text", "%name%s dampening field stopped %target%s attack!").replace("%name%", "$1").replace("%target%", "$2");
    }
    
    public class EntityDamageListener implements Listener {
        private Skill skill;
        public EntityDamageListener(Skill skill) {
            this.skill = skill;
        }
        @EventHandler
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.isCancelled())
                return;
            if (event instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent edBy = (EntityDamageByEntityEvent) event;
                if (event.getEntity() instanceof Player) {
                    Hero tHero = plugin.getCharacterManager().getHero((Player) event.getEntity());
                    Entity damager = edBy.getDamager();
                    if (edBy.getCause() == DamageCause.PROJECTILE) {
                        damager = (Entity) ((Projectile)damager).getShooter();
                    }
                    if (!(damager instanceof Player)) {
                        return;
                    }
                    Hero hero = plugin.getCharacterManager().getHero((Player) damager);
                    int manaReq = (int) (SkillConfigManager.getUseSetting(hero, skill, "block-if-mana-below", 15, false) +
                            (SkillConfigManager.getUseSetting(hero, skill, "block-increase", 0.0, false) * hero.getSkillLevel(skill)));
                    manaReq = manaReq > 0 ? manaReq : 0;
                    int minMana = (int) (SkillConfigManager.getUseSetting(hero, skill, "mana-required", 20, false) -
                            (SkillConfigManager.getUseSetting(hero, skill, "mana-required-decrease", 0.0, false) * hero.getSkillLevel(skill)));
                    minMana = minMana > 0 ? minMana : 0;
                    if (tHero.hasEffect("Dampen") && tHero.getMana() >= manaReq && hero.getMana() <= minMana) {
                        event.setDamage(0);
                        event.setCancelled(true);
                        broadcast(tHero.getPlayer().getLocation(), skillBlockText, tHero.getPlayer().getDisplayName(), hero.getPlayer().getDisplayName());
                    }
                }
            }

        }
    }
}