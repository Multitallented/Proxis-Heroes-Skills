package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.LivingEntity;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.effects.EffectType;
import com.herocraftonline.dev.heroes.effects.PeriodicExpirableEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.skill.TargettedSkill;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SkillCripple extends TargettedSkill {
    private String applyText;
    private String removeText;

    public SkillCripple(Heroes plugin) {
        super(plugin, "Cripple");
        setDescription("Deal damage to the target, plus extra if they move");
        setUsage("/skill cripple <target>");
        setArgumentRange(0, 1);
        setIdentifiers("skill cripple");
        setTypes(SkillType.DEBUFF, SkillType.DAMAGING, SkillType.PHYSICAL);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty(Setting.DURATION.node(), 10000);
        node.setProperty(Setting.PERIOD.node(), 1000);
        node.setProperty("tick-damage", 2);
        node.setProperty(Setting.DAMAGE.node(), 5);
        node.setProperty(Setting.MAX_DISTANCE.node(), 15);
        node.setProperty(Setting.APPLY_TEXT.node(), "%target% has been Crippled by %hero%!");
        node.setProperty("remove-text", "%target% has recovered from %hero%s Crippling blow!");
        return node;
    }
    
    @Override
    public void init() {
        super.init();
        applyText = getSetting(null, Setting.APPLY_TEXT.node(), "%target% has been Crippled by %hero%!").replace("%target%", "$1").replace("%hero%", "$2");
        removeText = getSetting(null, "remove-text", "%target% has recovered from %hero%s Crippling blow!").replace("%target%", "$1").replace("%hero%", "$2");
    }

    @Override
    public boolean use(Hero hero, LivingEntity le, String[] strings) {
        Player player = hero.getPlayer();
        if (!(le.equals(player)) && le instanceof Player) {
            Hero tHero = getPlugin().getHeroManager().getHero((Player) le);
            if (hero.getParty() == null || !(hero.getParty().getMembers().contains(tHero))) {
                if (damageCheck(player, tHero.getPlayer())) {
                    broadcastExecuteText(hero, le);
                    int damage = getSetting(hero, Setting.DAMAGE.node(), 5, false);
                    tHero.getPlayer().damage(damage, player);
                    long duration = getSetting(hero, Setting.DURATION.node(), 10000, false);
                    long period = getSetting(hero, Setting.PERIOD.node(), 1000, false);
                    CrippleEffect cEffect = new CrippleEffect(this, period, duration, player);
                    tHero.addEffect(cEffect);
                    return true;
                }
            }
        }
        Messaging.send(player, "Invalid target.");
        return false;
    }

    public class CrippleEffect extends PeriodicExpirableEffect {
        private Player caster;
        private Location prevLocation;
        public CrippleEffect(Skill skill, long period, long duration, Player caster) {
            super(skill, "Cripple", period, duration);
            this.caster=caster;
            this.types.add(EffectType.BLEED);
            this.types.add(EffectType.DISPELLABLE);
            this.types.add(EffectType.PHYSICAL);
        }
        
        @Override
        public void tick(Hero hero) {
            super.tick(hero);
            if (prevLocation != null
                    && Math.abs(hero.getPlayer().getLocation().getX() - prevLocation.getX()) >= 1
                    && Math.abs(hero.getPlayer().getLocation().getZ() - prevLocation.getZ()) >= 1) {
                Hero eHero = getPlugin().getHeroManager().getHero(caster);
                int damage = getSetting(eHero, Setting.DAMAGE.node(),5,false);
                hero.getPlayer().damage(damage, caster);
            }
            prevLocation = hero.getPlayer().getLocation();
        }
        
        @Override
        public void apply(Hero hero) {
            super.apply(hero);
            broadcast(hero.getPlayer().getLocation(), applyText, hero.getPlayer().getDisplayName(), caster.getDisplayName());
            this.prevLocation = hero.getPlayer().getLocation();
        }
        
        @Override
        public void remove(Hero hero) {
            super.remove(hero);
            broadcast(hero.getPlayer().getLocation(), removeText, hero.getPlayer().getDisplayName(), caster.getDisplayName());
        }
    }
}