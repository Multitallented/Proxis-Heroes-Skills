package com.herocraftonline.dev.heroes.skill.skills;

import java.util.List;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.effects.Effect;
import com.herocraftonline.dev.heroes.effects.EffectType;
import com.herocraftonline.dev.heroes.effects.PeriodicExpirableEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;
import java.util.HashMap;
import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

public class SkillTether extends ActiveSkill {

    private String applyText;
    private String expireText;
    private HashMap<Player, Player> affectedPlayers = new HashMap<Player, Player>();
    
    public SkillTether(Heroes plugin) {
        super(plugin, "Tether");
        setDescription("Tethers enemies around you");
        setUsage("/skill tether");
        setArgumentRange(0, 0);
        setIdentifiers(new String[] { "skill tether" });
        
        setTypes(SkillType.DEBUFF, SkillType.COUNTER, SkillType.MOVEMENT, SkillType.PHYSICAL);
    }
    
    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty(Setting.DURATION.node(), 10000); // in milliseconds
        node.setProperty(Setting.APPLY_TEXT.node(), "%target% was tethered!");
        node.setProperty(Setting.EXPIRE_TEXT.node(), "%target% got away!");
        return node;
    }
    
    @Override
    public void init() {
        super.init();
        applyText = getSetting(null, Setting.APPLY_TEXT.node(), "%target% was tethered!").replace("%target%", "$1");
        expireText = getSetting(null, Setting.EXPIRE_TEXT.node(), "%target% got away!").replace("%target%", "$1");
        
    }

    @Override
    public boolean use(Hero hero, String[] args) {
        List<Entity> entities = hero.getPlayer().getNearbyEntities(3, 3, 3);
        Player player = hero.getPlayer();
        for (Entity n : entities) {
            if (n instanceof Monster) {
                ((Monster) n).setTarget(hero.getPlayer());
            } else if (n instanceof Player && n != player) {
                long duration = 10000;
                duration = getSetting(hero, Setting.DURATION.node(), 10000, false);
                CurseEffect cEffect = new CurseEffect(this, duration, hero.getPlayer());
                Hero tHero = getPlugin().getHeroManager().getHero((Player) n);
                tHero.addEffect(cEffect);
            }
        }
        for (Effect e : hero.getEffects()) {
            if (e.isType(EffectType.DISABLE) || e.isType(EffectType.ROOT) || e.isType(EffectType.STUN)) {
                hero.removeEffect(e);
            }
            
        }
        broadcastExecuteText(hero);
        return true;
    }
    
    public class CurseEffect extends PeriodicExpirableEffect {
        
        private Player caster;
        public CurseEffect(Skill skill, long duration, Player caster) {
            super(skill, "Tether", 20L, duration);
            this.types.add(EffectType.HARMFUL);
            this.types.add(EffectType.PHYSICAL);
            this.caster = caster;
        }

        @Override
        public void apply(Hero hero) {
            super.apply(hero);
            Player player = hero.getPlayer();
            affectedPlayers.put(player, caster);
            broadcast(player.getLocation(), applyText, player.getDisplayName());
        }

        @Override
        public void remove(Hero hero) {
            super.remove(hero);

            Player player = hero.getPlayer();
            if (affectedPlayers.containsKey(player)) {
                affectedPlayers.remove(player);
                broadcast(player.getLocation(), expireText, player.getDisplayName());
            }
        }
        
        @Override
        public void tick(Hero hero) {
            super.tick(hero);
            Player player = hero.getPlayer();
            if (player.getLocation().distance(caster.getLocation()) > 5) {
                player.teleport(caster);
            }
        }
    }

}

