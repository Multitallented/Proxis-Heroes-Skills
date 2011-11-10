package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.HeroRegainHealthEvent;
import com.herocraftonline.dev.heroes.api.HeroesEventListener;
import com.herocraftonline.dev.heroes.effects.EffectType;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.effects.ExpirableEffect;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.skill.TargettedSkill;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class SkillMortalWound extends TargettedSkill {
    private String applyText;
    private String expireText;
    private String missText;

    public SkillMortalWound(Heroes plugin) {
        super(plugin, "MortalWound");
        setDescription("disables healing for the target + damage");
        setUsage("/skill mortalwound");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill mortalwound"});
        registerEvent(Type.ENTITY_REGAIN_HEALTH, new SkillEntityListener(), Priority.Normal);
        registerEvent(Type.CUSTOM_EVENT, new SkillEventListener(), Priority.Normal);
        
        setTypes(SkillType.PHYSICAL, SkillType.DEBUFF, SkillType.DAMAGING);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty(Setting.DURATION.node(), 10000); // in milliseconds
        node.setProperty("miss-text", "%target%s heal was blocked!");
        node.setProperty(Setting.APPLY_TEXT.node(), "%target% has been cursed!");
        node.setProperty(Setting.EXPIRE_TEXT.node(), "%target% has recovered from the curse!");
        node.setProperty(Setting.DAMAGE.node(), 7);
        return node;
    }

    @Override
    public void init() {
        super.init();
        missText = getSetting(null, "miss-text", "%target%s heal was blocked!").replace("%target%", "$1");
        applyText = getSetting(null, Setting.APPLY_TEXT.node(), "%target% has been cursed!").replace("%target%", "$1");
        expireText = getSetting(null, Setting.EXPIRE_TEXT.node(), "%target% has recovered from the curse!").replace("%target%", "$1");
    }

    @Override
    public boolean use(Hero hero, LivingEntity target, String[] args) {
        Player player = hero.getPlayer();
        if (target.equals(player) || target instanceof Creature) {
            Messaging.send(player, "You need a target!");
            return false;
        }

        if (target instanceof Player && hero.getParty() != null) {
            for (Hero h : hero.getParty().getMembers()) {
                if (target.equals(h.getPlayer())) {
                    Messaging.send(player, "You need a target!");
                    return false;
                }
            }
        }
        long duration = 10000;
        duration = getSetting(hero, Setting.DURATION.node(), 10000, false);
        CurseEffect cEffect = new CurseEffect(this, duration, player);
        if (target instanceof Player) {
            Hero tHero = getPlugin().getHeroManager().getHero((Player) target);
            tHero.addEffect(cEffect);
            return true;
        }

        Messaging.send(player, "Invalid target!");
        return false;
    }

    public class CurseEffect extends ExpirableEffect {
        private Player caster;

        public CurseEffect(Skill skill, long duration, Player caster) {
            super(skill, "MortalWound", duration);
            this.types.add(EffectType.WOUNDING);
            this.types.add(EffectType.PHYSICAL);
            int damage = (int) getSetting(getPlugin().getHeroManager().getHero(caster), Setting.DAMAGE.node(),7,false);
            if (damage != 0) {
                this.types.add(EffectType.HARMFUL);
            }
            this.caster = caster;
        }

        @Override
        public void apply(Hero hero) {
            super.apply(hero);
            Player player = hero.getPlayer();
            int damage = (int) getSetting(getPlugin().getHeroManager().getHero(caster), Setting.DAMAGE.node(),7, false);
            player.damage(damage, caster);
            hero.syncHealth();
            broadcast(player.getLocation(), applyText, player.getDisplayName());
        }

        @Override
        public void remove(Hero hero) {
            super.remove(hero);

            Player player = hero.getPlayer();
            broadcast(player.getLocation(), expireText, player.getDisplayName());
        }
    }

    public class SkillEntityListener extends EntityListener {

        @Override
        public void onEntityRegainHealth(EntityRegainHealthEvent event) {
            Hero hero = getPlugin().getHeroManager().getHero((Player) event.getEntity());
            if (hero.hasEffect("MortalWound")) {
                event.setAmount(0);
                event.setCancelled(true);
                broadcast(hero.getPlayer().getLocation(), missText, hero.getPlayer().getDisplayName());
            }
        }
    }
    
    public class SkillEventListener extends HeroesEventListener {
        
        @Override
        public void onHeroRegainHealth(HeroRegainHealthEvent event) {
            if (event.getHero().hasEffect("MortalWound")) {
                event.setAmount(0);
                event.setCancelled(true);
                broadcast(event.getHero().getPlayer().getLocation(), missText, event.getHero().getPlayer().getDisplayName());
            }
        }
    }
    

}