package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.api.events.HeroRegainHealthEvent;
import com.herocraftonline.heroes.characters.CharacterTemplate;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.TargettedSkill;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class SkillMortalWound extends TargettedSkill implements Listener {
    private String applyText;
    private String expireText;
    private String missText;

    public SkillMortalWound(Heroes plugin) {
        super(plugin, "MortalWound");
        setDescription("$2 damage and disables target's heals for $1s.");
        setUsage("/skill mortalwound");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill mortalwound"});
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);

        setTypes(SkillType.PHYSICAL, SkillType.DEBUFF, SkillType.DAMAGING);
    }

    @Override
    public String getDescription(Hero hero) {
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this))) / 1000;
        duration = duration > 0 ? duration : 0;
        int damage = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE.node(), 7, false) +
                (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getSkillLevel(this)));
        damage = damage > 0 ? damage : 0;
        String description = getDescription().replace("$1", duration + "").replace("$2", damage + "");

        //COOLDOWN
        int cooldown = (SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, SkillSetting.COOLDOWN_REDUCE.node(), 0, false) * hero.getSkillLevel(this)) / 1000;
        if (cooldown > 0) {
            description += " CD:" + cooldown + "s";
        }

        //MANA
        int mana = SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA.node(), 10, false)
                - (SkillConfigManager.getUseSetting(hero, this, SkillSetting.MANA_REDUCE.node(), 0, false) * hero.getSkillLevel(this));
        if (mana > 0) {
            description += " M:" + mana;
        }

        //HEALTH_COST
        int healthCost = SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_COST, 0, false) -
                (SkillConfigManager.getUseSetting(hero, this, SkillSetting.HEALTH_COST_REDUCE, mana, true) * hero.getSkillLevel(this));
        if (healthCost > 0) {
            description += " HP:" + healthCost;
        }

        //STAMINA
        int staminaCost = SkillConfigManager.getUseSetting(hero, this, SkillSetting.STAMINA.node(), 0, false)
                - (SkillConfigManager.getUseSetting(hero, this, SkillSetting.STAMINA_REDUCE.node(), 0, false) * hero.getSkillLevel(this));
        if (staminaCost > 0) {
            description += " FP:" + staminaCost;
        }

        //DELAY
        int delay = SkillConfigManager.getUseSetting(hero, this, SkillSetting.DELAY.node(), 0, false) / 1000;
        if (delay > 0) {
            description += " W:" + delay + "s";
        }

        //EXP
        int exp = SkillConfigManager.getUseSetting(hero, this, SkillSetting.EXP.node(), 0, false);
        if (exp > 0) {
            description += " XP:" + exp;
        }
        return description;
    }

    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        node.set(SkillSetting.DURATION.node(), 10000);
        node.set("duration-increase", 0);
        node.set("miss-text", "%target%s heal was blocked!");
        node.set(SkillSetting.APPLY_TEXT.node(), "%target% has been cursed!");
        node.set(SkillSetting.EXPIRE_TEXT.node(), "%target% has recovered from the curse!");
        node.set(SkillSetting.DAMAGE.node(), 7);
        return node;
    }

    @Override
    public void init() {
        super.init();
        missText = SkillConfigManager.getUseSetting(null, this, "miss-text", "%target%s heal was blocked!").replace("%target%", "$1");
        applyText = SkillConfigManager.getUseSetting(null, this, SkillSetting.APPLY_TEXT.node(), "%target% has been cursed!").replace("%target%", "$1");
        expireText = SkillConfigManager.getUseSetting(null, this, SkillSetting.EXPIRE_TEXT.node(), "%target% has recovered from the curse!").replace("%target%", "$1");
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
        if (target instanceof Player) {
            Hero tHero = plugin.getCharacterManager().getHero((Player) target);
            long duration = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 10000, false) +
                    (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this)));
            duration = duration > 0 ? duration : 0;
            CurseEffect cEffect = new CurseEffect(this, duration);
            tHero.addEffect(cEffect);
            int damage = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DAMAGE.node(), 7, false) +
                    (SkillConfigManager.getUseSetting(hero, this, "damage-increase", 0.0, false) * hero.getSkillLevel(this)));
            damage = damage > 0 ? damage : 0;
            player.damage(damage, tHero.getPlayer());
            return SkillResult.NORMAL;
        }

        return SkillResult.INVALID_TARGET;
    }

    public class CurseEffect extends ExpirableEffect {

        public CurseEffect(Skill skill, long duration) {
            super(skill, "MortalWound", duration);
            this.types.add(EffectType.WOUNDING);
            this.types.add(EffectType.PHYSICAL);
        }

        @Override
        public void applyToHero(Hero hero) {
            super.applyToHero(hero);
            Player player = hero.getPlayer();
            broadcast(player.getLocation(), applyText, player.getDisplayName());
        }

        @Override
        public void removeFromHero(Hero hero) {
            super.removeFromHero(hero);

            Player player = hero.getPlayer();
            broadcast(player.getLocation(), expireText, player.getDisplayName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) {
            return;
        }
        CharacterTemplate cT = plugin.getCharacterManager().getCharacter((LivingEntity) event.getEntity());
        if (cT.hasEffect("MortalWound")) {
            event.setAmount(0);
            event.setCancelled(true);
            if (cT instanceof Hero) {
                Hero hero = (Hero)cT;
                broadcast(hero.getPlayer().getLocation(), missText, hero.getPlayer().getDisplayName());
            }
        }
    }


}