package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.effects.EffectType;
import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.skill.TargettedSkill;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;
import java.util.HashMap;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

public class SkillCreepingPain extends TargettedSkill {
    private String applyText;
    private String expireText;
    private String missText;
    public HashMap<Player, Integer> affectedPlayers = new HashMap<Player, Integer>();

    public SkillCreepingPain(Heroes plugin) {
        super(plugin, "CreepingPain");
        setDescription("multiplies the damage done to the target");
        setUsage("/skill creepingpain");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill creepingpain"});
        registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);
        //registerEvent(Type.CUSTOM_EVENT, new SkillEventListener(), Priority.Normal);
        
        setTypes(SkillType.DEBUFF, SkillType.DAMAGING, SkillType.DARK, SkillType.SILENCABLE);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty(Setting.DURATION.node(), 10000); // in milliseconds
        node.setProperty("no_effect_expire_text", "%target%s urge to throw up passes!");
        node.setProperty(Setting.APPLY_TEXT.node(), "%target% feels a sudden urge to throw up!");
        node.setProperty(Setting.EXPIRE_TEXT.node(), "%target% falls to his knees and pukes blood!");
        return node;
    }

    @Override
    public void init() {
        super.init();
        missText = getSetting(null, "no_effect_expire_text", "%target%s urge to throw up passes!").replace("%target%", "$1");
        applyText = getSetting(null, Setting.APPLY_TEXT.node(), "%target% feels a sudden urge to throw up!").replace("%target%", "$1");
        expireText = getSetting(null, Setting.EXPIRE_TEXT.node(), "%target% falls to his knees and pukes blood!").replace("%target%", "$1");
    }

    @Override
    public SkillResult use(Hero hero, LivingEntity target, String[] args) {
        Player player = hero.getPlayer();
        if (target.equals(player) || target instanceof Creature) {
            return SkillResult.INVALID_TARGET;
        }

        if (target instanceof Player && hero.getParty() != null) {
            for (Hero h : hero.getParty().getMembers()) {
                if (target.equals(h.getPlayer())) {
                    return SkillResult.INVALID_TARGET;
                }
            }
        }
        long duration = 10000;
        duration = getSetting(hero, Setting.DURATION.node(), 10000, false);
        CurseEffect cEffect = new CurseEffect(this, duration, player);
        if (target instanceof Player) {
            Hero tHero = getPlugin().getHeroManager().getHero((Player) target);
            tHero.addEffect(cEffect);
            return SkillResult.NORMAL;
        }
        return SkillResult.INVALID_TARGET;
    }

    public class CurseEffect extends ExpirableEffect {
        
        private Player caster;
        public CurseEffect(Skill skill, long duration, Player caster) {
            super(skill, "CreepingPain", duration);
            this.types.add(EffectType.DISEASE);
            this.types.add(EffectType.POISON);
            this.types.add(EffectType.HARMFUL);
            this.types.add(EffectType.BLEED);
            this.caster = caster;
        }

        @Override
        public void apply(Hero hero) {
            super.apply(hero);
            Player player = hero.getPlayer();
            affectedPlayers.put(player, 0);
            broadcast(player.getLocation(), applyText, player.getDisplayName());
        }

        @Override
        public void remove(Hero hero) {
            super.remove(hero);

            Player player = hero.getPlayer();
            if (affectedPlayers.containsKey(player)&& affectedPlayers.get(player) > 0) {
                player.damage(affectedPlayers.get(player), caster);
                affectedPlayers.remove(player);
                broadcast(player.getLocation(), expireText, player.getDisplayName());
            } else {
                if (affectedPlayers.containsKey(player)) {
                    affectedPlayers.remove(player);
                }
                broadcast(player.getLocation(), missText, player.getDisplayName());
            }
        }
    }

    public class SkillEntityListener extends EntityListener {

        @Override
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.getEntity() instanceof Player) {
                Hero hero = getPlugin().getHeroManager().getHero((Player) event.getEntity());
                Player player = hero.getPlayer();
                if (hero.hasEffect("CreepingPain")) {
                    int damage = event.getDamage();
                    if (affectedPlayers.containsKey(player)) {
                        affectedPlayers.put(player, damage + affectedPlayers.get(player));
                    } else {
                        affectedPlayers.put(player, damage);
                    }
                }
            }
        }
    }
    

}