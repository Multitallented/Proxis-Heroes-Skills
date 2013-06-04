package com.herocraftonline.heroes.characters.skill.skills;

import org.bukkit.entity.Player;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.api.events.SkillUseEvent;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.ExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class SkillDisguise extends ActiveSkill {

    public SkillDisguise(Heroes plugin) {
        super(plugin, "Disguise");
        setDescription("Disguises you as target mob for $1s");
        setUsage("/skill disguise <mobname>");
        setArgumentRange(0, 1);
        setIdentifiers(new String[]{"skill disguise"});
        Bukkit.getServer().getPluginManager().registerEvents(new SkillEntityListener(), plugin);
        
        setTypes(SkillType.BUFF, SkillType.SUMMON, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this))) / 1000;
        duration = duration > 0 ? duration : 0;
        String description = getDescription().replace("$1", duration + "");
        
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
        node.set(SkillSetting.DURATION.node(), 30000);
        node.set("duration-increase", 0);
        List<String> tempList = new ArrayList<String>();
        tempList.add("ZOMBIE");
        tempList.add("SKELETON");
        tempList.add("SPIDER");
        tempList.add("CREEPER");
        node.set("allowed-mobs", tempList);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        if (hero.hasEffect("Disguise")) {
            hero.removeEffect(hero.getEffect("Disguise"));
            player.sendMessage("You have returned to human form.");
            return SkillResult.NORMAL;
        }
        
        long duration = (long) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.DURATION.node(), 10000, false) +
                (SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0.0, false) * hero.getSkillLevel(this)));
        duration = duration > 0 ? duration : 0;
        if (args.length < 1) {
            player.sendMessage(ChatColor.GRAY + "No mob type specified. /skill disguise <mobname>");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        List<String> tempList = new ArrayList<String>();
        tempList.add("ZOMBIE");
        tempList.add("SKELETON");
        tempList.add("SPIDER");
        tempList.add("CREEPER");
        boolean valid = false;
        for (String s : SkillConfigManager.getUseSetting(hero, this, "allowed-mobs", tempList)) {
            if (s.equalsIgnoreCase(args[0])) {
                valid = true;
            }
        }
        if (!valid) {
            player.sendMessage(ChatColor.GRAY + "You're not allowed to disguise as a " + args[0]);
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        
        
        CurseEffect cEffect = new CurseEffect(this, duration, args[0]);
        hero.addEffect(cEffect);
        return SkillResult.INVALID_TARGET_NO_MSG;
    }

    public class CurseEffect extends ExpirableEffect {
        private final String mobtype;
        
        public CurseEffect(Skill skill, long duration, String mobtype) {
            super(skill, "Disguise", duration);
            this.types.add(EffectType.BENEFICIAL);
            this.types.add(EffectType.DISPELLABLE);
            this.types.add(EffectType.FORM);
            this.types.add(EffectType.MAGIC);
            this.mobtype = mobtype;
        }

        @Override
        public void applyToHero(Hero hero) {
            super.applyToHero(hero);
            Player player = hero.getPlayer();
            if (player.isOp()) {
                player.performCommand("btm mob " + mobtype);
            } else {
                player.setOp(true);
                player.performCommand("btm mob " + mobtype);
                player.setOp(false);
            }
            //broadcast(player.getLocation(), applyText, player.getDisplayName());
        }

        @Override
        public void removeFromHero(Hero hero) {
            super.removeFromHero(hero);
            Player player = hero.getPlayer();
            if (player.isOp()) {
                player.performCommand("btm off");
            } else {
                player.setOp(true);
                player.performCommand("btm off");
                player.setOp(false);
            }
        }
    }

    public class SkillEntityListener implements Listener {

        @EventHandler
        public void onSkillUse(SkillUseEvent event) {
            if (!event.getHero().hasEffect("Disguise")) {
                return;
            }
            if (!event.getSkill().getName().equals("Disguise")) {
                event.setCancelled(true);
                event.getHero().getPlayer().sendMessage(ChatColor.GRAY + "You can't use skills while disguised!");
                event.getHero().getPlayer().sendMessage(ChatColor.GRAY + "use /skill disguise to undisguise");
            }
        }
    }
    

}