package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.Monster;
import com.herocraftonline.heroes.characters.effects.EffectType;
import com.herocraftonline.heroes.characters.effects.PeriodicExpirableEffect;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.Skill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.util.Messaging;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

public class SkillGlassshield extends ActiveSkill {

    public String applyText;
    public String expireText;
    
    public SkillGlassshield(Heroes plugin) {
        super(plugin, "Glassshield");
        setDescription("Create moving shield of glass for $1-$2s");
        setUsage("/skill glassshield");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill glassshield"});

        setTypes(SkillType.BUFF, SkillType.COUNTER, SkillType.SILENCABLE);
    }

    @Override
    public String getDescription(Hero hero) {
        long addDur = SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0, false) * hero.getSkillLevel(this);
        long minDur = SkillConfigManager.getUseSetting(hero, this, "min_duration", 20000, false) + addDur;
        long maxDur = SkillConfigManager.getUseSetting(hero, this, "max_duration", 40000, false) + addDur;
        String description = getDescription().replace("$1", minDur + "").replace("$2", maxDur + "");
        
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
        node.set("min_duration", 20000);
        node.set("max_duration", 40000);
        node.set("duration-increase", 0);
        node.set("apply-text", "%hero% uses the air to create a %skill%!");
        node.set("expire-text", "%hero%s %skill% fades as the wind dies down!");
        return node;
    }

    @Override
    public void init() {
        super.init();
        applyText = SkillConfigManager.getUseSetting(null, this, "apply-text", "%hero% uses the air to create a %skill%!").replace("%hero%", "$1").replace("%skill%", "$2");
        expireText = SkillConfigManager.getUseSetting(null, this, "expire-text", "%hero%s %skill% fades as the wind dies down!").replace("%hero%", "$1").replace("%skill%", "$2");
    }
    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        long addDur = SkillConfigManager.getUseSetting(hero, this, "duration-increase", 0, false) * hero.getSkillLevel(this);
        long minDuration = SkillConfigManager.getUseSetting(hero, this, "min_duration", 20000,false) + addDur;
        if (minDuration < 1000 || minDuration > 60000) {
            minDuration = 20000;
        }
        long maxDuration = SkillConfigManager.getUseSetting(hero, this, "max_duration", 40000, false) + addDur;
        if (maxDuration > 60000) {
            maxDuration = 40000;
        }
        if (maxDuration < minDuration) {
            maxDuration = minDuration;
        }
        long randDuration = maxDuration - minDuration;
        long duration = (long) ((Math.random()*randDuration)+minDuration);
        broadcastExecuteText(hero);
        Messaging.send(player, "Duration " + duration/1000 + "s");
        GlassshieldEffect gEffect = new GlassshieldEffect(this, "Glassshield", duration);
        hero.addEffect(gEffect);
        return SkillResult.NORMAL;
    }
    
    public class GlassshieldEffect extends PeriodicExpirableEffect{
        private boolean seed;
        private Block prevRefBlock;
        private ArrayList<Block> glassList = new ArrayList<Block>();
        public GlassshieldEffect(Skill skill, String name, long duration) {
           super(skill, name, 200, duration);
           this.types.add(EffectType.BENEFICIAL);
           this.types.add(EffectType.DISPELLABLE);
           this.types.add(EffectType.ICE);
           this.types.add(EffectType.LIGHT);
           
           seed = true;
        }
        
        @Override
        public void applyToHero(Hero hero) {
            super.applyToHero(hero);
            Player player = hero.getPlayer();
            broadcast(player.getLocation(), applyText, player.getDisplayName(), "Glassshield");
        }
        
        @Override
        public void removeFromHero(Hero hero) {
            super.removeFromHero(hero);
            Player player = hero.getPlayer();
            broadcast(player.getLocation(), expireText, player.getDisplayName(), "Glassshield");
            updateShield(null, seed);
        }
        
        @Override
        public void tickHero(Hero hero) {
            if (!hero.hasEffect("Glassshield")) {
                return;
            }
            updateShield(hero.getPlayer().getLocation().getBlock(), seed);
            seed = !seed;
        }
        
        @Override
        public void tickMonster(Monster mnstr) {
            //:P I'm really tired
        }

        private void updateShield(Block refBlock, boolean seed) {
            if (prevRefBlock != null) {
                //////////NORTH
                replaceGlass(retrieveBlock(prevRefBlock,3,0,-1));
                replaceGlass(retrieveBlock(prevRefBlock,3,0,0));
                replaceGlass(retrieveBlock(prevRefBlock,3,0,1));
                replaceGlass(retrieveBlock(prevRefBlock,3,0,2));
                replaceGlass(retrieveBlock(prevRefBlock,3,0,3));

                replaceGlass(retrieveBlock(prevRefBlock,3,1,-1));
                replaceGlass(retrieveBlock(prevRefBlock,3,1,0));
                replaceGlass(retrieveBlock(prevRefBlock,3,1,1));
                replaceGlass(retrieveBlock(prevRefBlock,3,1,2));
                replaceGlass(retrieveBlock(prevRefBlock,3,1,3));

                replaceGlass(retrieveBlock(prevRefBlock,3,2,-1));
                replaceGlass(retrieveBlock(prevRefBlock,3,2,0));
                replaceGlass(retrieveBlock(prevRefBlock,3,2,1));
                replaceGlass(retrieveBlock(prevRefBlock,3,2,2));
                replaceGlass(retrieveBlock(prevRefBlock,3,2,3));

                replaceGlass(retrieveBlock(prevRefBlock,3,-1,-1));
                replaceGlass(retrieveBlock(prevRefBlock,3,-1,0));
                replaceGlass(retrieveBlock(prevRefBlock,3,-1,1));
                replaceGlass(retrieveBlock(prevRefBlock,3,-1,2));
                replaceGlass(retrieveBlock(prevRefBlock,3,-1,3));

                replaceGlass(retrieveBlock(prevRefBlock,3,-2,-1));
                replaceGlass(retrieveBlock(prevRefBlock,3,-2,0));
                replaceGlass(retrieveBlock(prevRefBlock,3,-2,1));
                replaceGlass(retrieveBlock(prevRefBlock,3,-2,2));
                replaceGlass(retrieveBlock(prevRefBlock,3,-2,3));

                /////////EAST
                replaceGlass(retrieveBlock(prevRefBlock,0,3,-1));
                replaceGlass(retrieveBlock(prevRefBlock,0,3,0));
                replaceGlass(retrieveBlock(prevRefBlock,0,3,1));
                replaceGlass(retrieveBlock(prevRefBlock,0,3,2));
                replaceGlass(retrieveBlock(prevRefBlock,0,3,3));

                replaceGlass(retrieveBlock(prevRefBlock,1,3,-1));
                replaceGlass(retrieveBlock(prevRefBlock,1,3,0));
                replaceGlass(retrieveBlock(prevRefBlock,1,3,1));
                replaceGlass(retrieveBlock(prevRefBlock,1,3,2));
                replaceGlass(retrieveBlock(prevRefBlock,1,3,3));

                replaceGlass(retrieveBlock(prevRefBlock,2,3,-1));
                replaceGlass(retrieveBlock(prevRefBlock,2,3,0));
                replaceGlass(retrieveBlock(prevRefBlock,2,3,1));
                replaceGlass(retrieveBlock(prevRefBlock,2,3,2));
                replaceGlass(retrieveBlock(prevRefBlock,2,3,3));

                replaceGlass(retrieveBlock(prevRefBlock,-1,3,-1));
                replaceGlass(retrieveBlock(prevRefBlock,-1,3,0));
                replaceGlass(retrieveBlock(prevRefBlock,-1,3,1));
                replaceGlass(retrieveBlock(prevRefBlock,-1,3,2));
                replaceGlass(retrieveBlock(prevRefBlock,-1,3,3));

                replaceGlass(retrieveBlock(prevRefBlock,-2,3,-1));
                replaceGlass(retrieveBlock(prevRefBlock,-2,3,0));
                replaceGlass(retrieveBlock(prevRefBlock,-2,3,1));
                replaceGlass(retrieveBlock(prevRefBlock,-2,3,2));
                replaceGlass(retrieveBlock(prevRefBlock,-2,3,3));

                ///////////WEST
                replaceGlass(retrieveBlock(prevRefBlock,0,-3,-1));
                replaceGlass(retrieveBlock(prevRefBlock,0,-3,0));
                replaceGlass(retrieveBlock(prevRefBlock,0,-3,1));
                replaceGlass(retrieveBlock(prevRefBlock,0,-3,2));
                replaceGlass(retrieveBlock(prevRefBlock,0,-3,3));

                replaceGlass(retrieveBlock(prevRefBlock,1,-3,-1));
                replaceGlass(retrieveBlock(prevRefBlock,1,-3,0));
                replaceGlass(retrieveBlock(prevRefBlock,1,-3,1));
                replaceGlass(retrieveBlock(prevRefBlock,1,-3,2));
                replaceGlass(retrieveBlock(prevRefBlock,1,-3,3));

                replaceGlass(retrieveBlock(prevRefBlock,2,-3,-1));
                replaceGlass(retrieveBlock(prevRefBlock,2,-3,0));
                replaceGlass(retrieveBlock(prevRefBlock,2,-3,1));
                replaceGlass(retrieveBlock(prevRefBlock,2,-3,2));
                replaceGlass(retrieveBlock(prevRefBlock,2,-3,3));

                replaceGlass(retrieveBlock(prevRefBlock,-1,-3,-1));
                replaceGlass(retrieveBlock(prevRefBlock,-1,-3,0));
                replaceGlass(retrieveBlock(prevRefBlock,-1,-3,1));
                replaceGlass(retrieveBlock(prevRefBlock,-1,-3,2));
                replaceGlass(retrieveBlock(prevRefBlock,-1,-3,3));

                replaceGlass(retrieveBlock(prevRefBlock,-2,-3,-1));
                replaceGlass(retrieveBlock(prevRefBlock,-2,-3,0));
                replaceGlass(retrieveBlock(prevRefBlock,-2,-3,1));
                replaceGlass(retrieveBlock(prevRefBlock,-2,-3,2));
                replaceGlass(retrieveBlock(prevRefBlock,-2,-3,3));

                //////////SOUTH
                replaceGlass(retrieveBlock(prevRefBlock,-3,0,-1));
                replaceGlass(retrieveBlock(prevRefBlock,-3,0,0));
                replaceGlass(retrieveBlock(prevRefBlock,-3,0,1));
                replaceGlass(retrieveBlock(prevRefBlock,-3,0,2));
                replaceGlass(retrieveBlock(prevRefBlock,-3,0,3));

                replaceGlass(retrieveBlock(prevRefBlock,-3,1,-1));
                replaceGlass(retrieveBlock(prevRefBlock,-3,1,0));
                replaceGlass(retrieveBlock(prevRefBlock,-3,1,1));
                replaceGlass(retrieveBlock(prevRefBlock,-3,1,2));
                replaceGlass(retrieveBlock(prevRefBlock,-3,1,3));

                replaceGlass(retrieveBlock(prevRefBlock,-3,2,-1));
                replaceGlass(retrieveBlock(prevRefBlock,-3,2,0));
                replaceGlass(retrieveBlock(prevRefBlock,-3,2,1));
                replaceGlass(retrieveBlock(prevRefBlock,-3,2,2));
                replaceGlass(retrieveBlock(prevRefBlock,-3,2,3));

                replaceGlass(retrieveBlock(prevRefBlock,-3,-1,-1));
                replaceGlass(retrieveBlock(prevRefBlock,-3,-1,0));
                replaceGlass(retrieveBlock(prevRefBlock,-3,-1,1));
                replaceGlass(retrieveBlock(prevRefBlock,-3,-1,2));
                replaceGlass(retrieveBlock(prevRefBlock,-3,-1,3));

                replaceGlass(retrieveBlock(prevRefBlock,-3,-2,-1));
                replaceGlass(retrieveBlock(prevRefBlock,-3,-2,0));
                replaceGlass(retrieveBlock(prevRefBlock,-3,-2,1));
                replaceGlass(retrieveBlock(prevRefBlock,-3,-2,2));
                replaceGlass(retrieveBlock(prevRefBlock,-3,-2,3));

                //////////UP
                replaceGlass(retrieveBlock(prevRefBlock,0,0,4));
                replaceGlass(retrieveBlock(prevRefBlock,-1,0,4));
                replaceGlass(retrieveBlock(prevRefBlock,-2,0,4));
                replaceGlass(retrieveBlock(prevRefBlock,1,0,4));
                replaceGlass(retrieveBlock(prevRefBlock,2,0,4));

                replaceGlass(retrieveBlock(prevRefBlock,0,1,4));
                replaceGlass(retrieveBlock(prevRefBlock,1,1,4));
                replaceGlass(retrieveBlock(prevRefBlock,2,1,4));
                replaceGlass(retrieveBlock(prevRefBlock,-1,1,4));
                replaceGlass(retrieveBlock(prevRefBlock,-2,1,4));

                replaceGlass(retrieveBlock(prevRefBlock,0,2,4));
                replaceGlass(retrieveBlock(prevRefBlock,-1,2,4));
                replaceGlass(retrieveBlock(prevRefBlock,-2,2,4));
                replaceGlass(retrieveBlock(prevRefBlock,1,2,4));
                replaceGlass(retrieveBlock(prevRefBlock,2,2,4));

                replaceGlass(retrieveBlock(prevRefBlock,0,-1,4));
                replaceGlass(retrieveBlock(prevRefBlock,1,-1,4));
                replaceGlass(retrieveBlock(prevRefBlock,2,-1,4));
                replaceGlass(retrieveBlock(prevRefBlock,-1,-1,4));
                replaceGlass(retrieveBlock(prevRefBlock,-2,-1,4));

                replaceGlass(retrieveBlock(prevRefBlock,0,-2,4));
                replaceGlass(retrieveBlock(prevRefBlock,-1,-2,4));
                replaceGlass(retrieveBlock(prevRefBlock,-2,-2,4));
                replaceGlass(retrieveBlock(prevRefBlock,1,-2,4));
                replaceGlass(retrieveBlock(prevRefBlock,2,-2,4));

                /////////DOWN
                replaceGlass(retrieveBlock(prevRefBlock,0,0,-2));
                replaceGlass(retrieveBlock(prevRefBlock,-2,0,-2));
                replaceGlass(retrieveBlock(prevRefBlock,-1,0,-2));
                replaceGlass(retrieveBlock(prevRefBlock,1,0,-2));
                replaceGlass(retrieveBlock(prevRefBlock,2,0,-2));

                replaceGlass(retrieveBlock(prevRefBlock,0,1,-2));
                replaceGlass(retrieveBlock(prevRefBlock,1,1,-2));
                replaceGlass(retrieveBlock(prevRefBlock,2,1,-2));
                replaceGlass(retrieveBlock(prevRefBlock,-1,1,-2));
                replaceGlass(retrieveBlock(prevRefBlock,-2,1,-2));

                replaceGlass(retrieveBlock(prevRefBlock,0,2,-2));
                replaceGlass(retrieveBlock(prevRefBlock,-2,2,-2));
                replaceGlass(retrieveBlock(prevRefBlock,-1,2,-2));
                replaceGlass(retrieveBlock(prevRefBlock,1,2,-2));
                replaceGlass(retrieveBlock(prevRefBlock,2,2,-2));

                replaceGlass(retrieveBlock(prevRefBlock,0,-1,-2));
                replaceGlass(retrieveBlock(prevRefBlock,1,-1,-2));
                replaceGlass(retrieveBlock(prevRefBlock,2,-1,-2));
                replaceGlass(retrieveBlock(prevRefBlock,-1,-1,-2));
                replaceGlass(retrieveBlock(prevRefBlock,-2,-1,-2));

                replaceGlass(retrieveBlock(prevRefBlock,0,-2,-2));
                replaceGlass(retrieveBlock(prevRefBlock,-2,-2,-2));
                replaceGlass(retrieveBlock(prevRefBlock,-1,-2,-2));
                replaceGlass(retrieveBlock(prevRefBlock,1,-2,-2));
                replaceGlass(retrieveBlock(prevRefBlock,2,-2,-2));
                fixGlass();
            }
            if (refBlock != null) {
                if (seed) {
                    /////////NORTH
                    replaceIfAir(retrieveBlock(refBlock,3,0,-1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,3,0,0),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,3,0,1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,3,0,2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,3,0,3),Material.AIR);

                    replaceIfAir(retrieveBlock(refBlock,3,1,-1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,3,1,0),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,3,1,1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,3,1,2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,3,1,3),Material.GLASS);

                    replaceIfAir(retrieveBlock(refBlock,3,2,-1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,3,2,0),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,3,2,1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,3,2,2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,3,2,3),Material.AIR);

                    replaceIfAir(retrieveBlock(refBlock,3,-1,-1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,3,-1,0),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,3,-1,1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,3,-1,2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,3,-1,3),Material.GLASS);

                    replaceIfAir(retrieveBlock(refBlock,3,-2,-1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,3,-2,0),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,3,-2,1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,3,-2,2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,3,-2,3),Material.AIR);

                    //////////EAST
                    replaceIfAir(retrieveBlock(refBlock,0,3,-1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,0,3,0),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,0,3,1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,0,3,2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,0,3,3),Material.GLASS);

                    replaceIfAir(retrieveBlock(refBlock,1,3,-1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,1,3,0),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,1,3,1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,1,3,2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,1,3,3),Material.AIR);

                    replaceIfAir(retrieveBlock(refBlock,2,3,-1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,2,3,0),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,2,3,1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,2,3,2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,2,3,3),Material.GLASS);

                    replaceIfAir(retrieveBlock(refBlock,-1,3,-1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-1,3,0),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-1,3,1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-1,3,2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-1,3,3),Material.AIR);

                    replaceIfAir(retrieveBlock(refBlock,-2,3,-1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-2,3,0),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-2,3,1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-2,3,2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-2,3,3),Material.GLASS);

                    //////////WEST
                    replaceIfAir(retrieveBlock(refBlock,0,-3,-1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,0,-3,0),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,0,-3,1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,0,-3,2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,0,-3,3),Material.GLASS);

                    replaceIfAir(retrieveBlock(refBlock,1,-3,-1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,1,-3,0),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,1,-3,1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,1,-3,2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,1,-3,3),Material.AIR);

                    replaceIfAir(retrieveBlock(refBlock,2,-3,-1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,2,-3,0),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,2,-3,1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,2,-3,2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,2,-3,3),Material.GLASS);

                    replaceIfAir(retrieveBlock(refBlock,-1,-3,-1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-1,-3,0),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-1,-3,1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-1,-3,2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-1,-3,3),Material.AIR);

                    replaceIfAir(retrieveBlock(refBlock,-2,-3,-1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-2,-3,0),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-2,-3,1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-2,-3,2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-2,-3,3),Material.GLASS);

                    //////////SOUTH
                    replaceIfAir(retrieveBlock(refBlock,-3,0,-1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-3,0,0),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-3,0,1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-3,0,2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-3,0,3),Material.AIR);

                    replaceIfAir(retrieveBlock(refBlock,-3,1,-1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-3,1,0),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-3,1,1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-3,1,2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-3,1,3),Material.GLASS);

                    replaceIfAir(retrieveBlock(refBlock,-3,2,-1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-3,2,0),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-3,2,1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-3,2,2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-3,2,3),Material.AIR);

                    replaceIfAir(retrieveBlock(refBlock,-3,-1,-1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-3,-1,0),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-3,-1,1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-3,-1,2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-3,-1,3),Material.GLASS);

                    replaceIfAir(retrieveBlock(refBlock,-3,-2,-1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-3,-2,0),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-3,-2,1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-3,-2,2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-3,-2,3),Material.AIR);

                    /////////UP
                    replaceIfAir(retrieveBlock(refBlock,0,0,4),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-1,0,4),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-2,0,4),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,1,0,4),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,2,0,4),Material.AIR);

                    replaceIfAir(retrieveBlock(refBlock,0,1,4),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,1,1,4),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,2,1,4),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-1,1,4),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-2,1,4),Material.GLASS);


                    replaceIfAir(retrieveBlock(refBlock,0,2,4),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-1,2,4),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-2,2,4),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,1,2,4),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,2,2,4),Material.AIR);

                    replaceIfAir(retrieveBlock(refBlock,0,-1,4),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,1,-1,4),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,2,-1,4),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-1,-1,4),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-2,-1,4),Material.AIR);

                    replaceIfAir(retrieveBlock(refBlock,0,-2,4),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-1,-2,4),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-2,-2,4),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,1,-2,4),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,2,-2,4),Material.AIR);

                    //////////DOWN
                    replaceIfAir(retrieveBlock(refBlock,0,0,-2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-1,0,-2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-2,0,-2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,1,0,-2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,2,0,-2),Material.AIR);

                    replaceIfAir(retrieveBlock(refBlock,0,1,-2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,1,1,-2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,2,1,-2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-1,1,-2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-2,1,-2),Material.GLASS);

                    replaceIfAir(retrieveBlock(refBlock,0,2,-2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-1,2,-2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-2,2,-2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,1,2,-2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,2,2,-2),Material.AIR);

                    replaceIfAir(retrieveBlock(refBlock,0,-1,-2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,1,-1,-2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,2,-1,-2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-1,-1,-2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-2,-1,-2),Material.GLASS);

                    replaceIfAir(retrieveBlock(refBlock,0,-2,-2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-1,-2,-2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-2,-2,-2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,1,-2,-2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,2,-2,-2),Material.AIR);
                } else {
                    /////NORTH
                    replaceIfAir(retrieveBlock(refBlock,3,0,-1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,3,0,0),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,3,0,1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,3,0,2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,3,0,3),Material.GLASS);

                    replaceIfAir(retrieveBlock(refBlock,3,1,-1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,3,1,0),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,3,1,1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,3,1,2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,3,1,3),Material.AIR);

                    replaceIfAir(retrieveBlock(refBlock,3,2,-1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,3,2,0),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,3,2,1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,3,2,2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,3,2,3),Material.GLASS);

                    replaceIfAir(retrieveBlock(refBlock,3,1,-1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,3,-1,0),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,3,-1,1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,3,-1,2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,3,1,3),Material.AIR);

                    replaceIfAir(retrieveBlock(refBlock,3,-2,-1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,3,-2,0),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,3,-2,1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,3,-2,2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,3,-2,3),Material.GLASS);

                    //////EAST
                    replaceIfAir(retrieveBlock(refBlock,0,3,-1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,0,3,0),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,0,3,1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,0,3,2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,0,3,3),Material.AIR);

                    replaceIfAir(retrieveBlock(refBlock,1,3,-1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,1,3,0),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,1,3,1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,1,3,2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,1,3,3),Material.GLASS);

                    replaceIfAir(retrieveBlock(refBlock,2,3,-1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,2,3,0),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,2,3,1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,2,3,2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,2,3,3),Material.AIR);

                    replaceIfAir(retrieveBlock(refBlock,-1,3,-1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-1,3,0),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-1,3,1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-1,3,2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-1,3,3),Material.GLASS);

                    replaceIfAir(retrieveBlock(refBlock,-2,3,-1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-2,3,0),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-2,3,1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-2,3,2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-2,3,3),Material.AIR);

                    ///////WEST
                    replaceIfAir(retrieveBlock(refBlock,0,-3,-1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,0,-3,0),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,0,-3,1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,0,-3,2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,0,-3,3),Material.AIR);

                    replaceIfAir(retrieveBlock(refBlock,1,-3,-1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,1,-3,0),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,1,-3,1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,1,-3,2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,1,-3,3),Material.GLASS);

                    replaceIfAir(retrieveBlock(refBlock,2,-3,-1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,2,-3,0),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,2,-3,1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,2,-3,2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,2,-3,3),Material.AIR);

                    replaceIfAir(retrieveBlock(refBlock,-1,-3,-1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-1,-3,0),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-1,-3,1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-1,-3,2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-1,-3,3),Material.GLASS);

                    replaceIfAir(retrieveBlock(refBlock,-2,-3,-1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-2,-3,0),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-2,-3,1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-2,-3,2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-2,-3,3),Material.AIR);

                    ///////SOUTH
                    replaceIfAir(retrieveBlock(refBlock,-3,0,-1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-3,0,0),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-3,0,1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-3,0,2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-3,0,3),Material.GLASS);

                    replaceIfAir(retrieveBlock(refBlock,-3,1,-1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-3,1,0),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-3,1,1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-3,1,2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-3,1,3),Material.AIR);

                    replaceIfAir(retrieveBlock(refBlock,-3,2,-1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-3,2,0),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-3,2,1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-3,2,2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-3,2,3),Material.GLASS);

                    replaceIfAir(retrieveBlock(refBlock,-3,-1,-1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-3,-1,0),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-3,-1,1),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-3,-1,2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-3,-1,3),Material.AIR);

                    replaceIfAir(retrieveBlock(refBlock,-3,-2,-1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-3,-2,0),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-3,-2,1),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-3,-2,2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-3,-2,3),Material.GLASS);

                    ///////UP
                    replaceIfAir(retrieveBlock(refBlock,0,0,4),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-1,0,4),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-2,0,4),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,1,0,4),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,2,0,4),Material.GLASS);

                    replaceIfAir(retrieveBlock(refBlock,0,1,4),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,1,1,4),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,2,1,4),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-1,1,4),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-2,1,4),Material.AIR);

                    replaceIfAir(retrieveBlock(refBlock,0,2,4),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-1,2,4),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-2,2,4),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,1,2,4),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,2,2,4),Material.GLASS);

                    replaceIfAir(retrieveBlock(refBlock,0,-1,4),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,1,-1,4),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,2,-1,4),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-1,-1,4),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-2,-1,4),Material.AIR);

                    replaceIfAir(retrieveBlock(refBlock,0,-2,4),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-1,-2,4),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-2,-2,4),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,1,-2,4),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,2,-2,4),Material.GLASS);

                    ////////DOWN
                    replaceIfAir(retrieveBlock(refBlock,0,0,-2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-1,0,-2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-2,0,-2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,1,0,-2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,2,0,-2),Material.GLASS);

                    replaceIfAir(retrieveBlock(refBlock,0,1,-2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,1,1,-2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,2,1,-2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-1,1,-2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-2,1,-2),Material.AIR);

                    replaceIfAir(retrieveBlock(refBlock,0,2,-2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-1,2,-2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-2,2,-2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,1,2,-2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,2,2,-2),Material.GLASS);

                    replaceIfAir(retrieveBlock(refBlock,0,-1,-2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,1,-1,-2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,2,-1,-2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-1,-1,-2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-2,-1,-2),Material.AIR);

                    replaceIfAir(retrieveBlock(refBlock,0,-2,-2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,-1,-2,-2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,-2,-2,-2),Material.GLASS);
                    replaceIfAir(retrieveBlock(refBlock,1,-2,-2),Material.AIR);
                    replaceIfAir(retrieveBlock(refBlock,2,-2,-2),Material.GLASS);
                }
            }
            prevRefBlock = refBlock;
        }

        /**
         * Retrieves the block at the relative co-ordinates.
         * 
         * @param referenceBlock, relativeX, relativeY, relativeZ
         * @return Block
         */
        private void replaceIfAir(Block checkBlock, Material replaceWith) {
            if (checkBlock.getType() == Material.GLASS) {
                glassList.add(checkBlock);
            }
            if (checkBlock.getType() == Material.AIR) {
                checkBlock.setType(replaceWith);
            }
        }

        /**
         * Sets the block material to AIR if the block was GLASS.
         * 
         * @param referenceBlock
         * @return Block
         */
        private void replaceGlass(Block checkBlock) {
            if (checkBlock.getType() == Material.GLASS) {
                checkBlock.setType(Material.AIR);
            }
        }

        /**
         * Corrects missing GLASS from the replaceGlass Method
         * 
         * @param referenceBlock
         * @return Block
         */
        private void fixGlass() {
            for (int i=0; i<glassList.size(); i++) {
                glassList.get(i).setType(Material.GLASS);
            }
            glassList.clear();
        }

        private Block retrieveBlock(Block refBlock, int relX, int relY, int relZ) {
            Block returnBlock = refBlock;
                while (0<Math.abs(relX)) {
                    if (relX > 0) {
                        returnBlock = returnBlock.getRelative(BlockFace.NORTH);
                        relX--;
                    } else {
                        returnBlock = returnBlock.getRelative(BlockFace.SOUTH);
                        relX++;
                    }
                }
                while (0<Math.abs(relY)) {
                    if (relY > 0) {
                        returnBlock = returnBlock.getRelative(BlockFace.EAST);
                        relY--;
                    } else {
                        returnBlock = returnBlock.getRelative(BlockFace.WEST);
                        relY++;
                    }
                }
                while (0<Math.abs(relZ)) {
                    if (relZ > 0) {
                        returnBlock = returnBlock.getRelative(BlockFace.UP);
                        relZ--;
                    } else {
                        returnBlock = returnBlock.getRelative(BlockFace.DOWN);
                        relZ++;
                    }
                }
                return returnBlock;
           }

        }
    }