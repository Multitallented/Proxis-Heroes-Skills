package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.effects.EffectType;
import com.herocraftonline.dev.heroes.effects.PeriodicExpirableEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Messaging;
import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class SkillGlassshield extends ActiveSkill {

    public String applyText;
    public String expireText;
    
    public SkillGlassshield(Heroes plugin) {
        super(plugin, "Glassshield");
        setDescription("Create moving shield of glass");
        setUsage("/skill glassshield");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill glassshield"});

        setTypes(SkillType.BUFF, SkillType.COUNTER, SkillType.SILENCABLE);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("min_duration", 20000);
        node.setProperty("max_duration", 40000);
        node.setProperty("apply-text", "%hero% uses the air to create a %skill%!");
        node.setProperty("expire-text", "%hero%s %skill% fades as the wind dies down!");
        return node;
    }

    @Override
    public void init() {
        super.init();
        applyText = getSetting(null, "apply-text", "%hero% uses the air to create a %skill%!").replace("%hero%", "$1").replace("%skill%", "$2");
        expireText = getSetting(null, "expire-text", "%hero%s %skill% fades as the wind dies down!").replace("%hero%", "$1").replace("%skill%", "$2");
    }
    @Override
    public boolean use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        int minDuration = (int) getSetting(hero, "min_duration", 20000,false);
        if (minDuration < 1000 || minDuration > 60000) {
            minDuration = 20000;
        }
        int maxDuration = (int) getSetting(hero, "max_duration", 40000, false);
        if (maxDuration > 60000) {
            maxDuration = 40000;
        }
        if (maxDuration < minDuration) {
            maxDuration = minDuration;
        }
        int randDuration = maxDuration - minDuration;
        long duration = (long) (Math.random()*randDuration+minDuration);
        broadcastExecuteText(hero);
        Messaging.send(player, "Duration " + duration/1000 + "s");
        GlassshieldEffect gEffect = new GlassshieldEffect(this, "Glassshield", 200, duration);
        hero.addEffect(gEffect);
        return true;
    }
    
    public class GlassshieldEffect extends PeriodicExpirableEffect{
        private boolean seed;
        private Block prevRefBlock;
        private ArrayList<Block> glassList = new ArrayList<Block>();
        public GlassshieldEffect(Skill skill, String name, long period, long duration) {
           super(skill, name, period, duration);
           this.types.add(EffectType.BENEFICIAL);
           this.types.add(EffectType.DISPELLABLE);
           this.types.add(EffectType.ICE);
           this.types.add(EffectType.LIGHT);
           
           seed = true;
        }
        
        @Override
        public void apply(Hero hero) {
            super.apply(hero);
            Player player = hero.getPlayer();
            broadcast(player.getLocation(), applyText, player.getDisplayName(), "Glassshield");
        }
        
        @Override
        public void remove(Hero hero) {
            super.remove(hero);
            Player player = hero.getPlayer();
            broadcast(player.getLocation(), expireText, player.getDisplayName(), "Glassshield");
            updateShield(null, seed);
        }
        
        @Override
        public void tick(Hero hero) {
            super.tick(hero);
            updateShield(hero.getPlayer().getLocation().getBlock(), seed);
            seed = !seed;
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