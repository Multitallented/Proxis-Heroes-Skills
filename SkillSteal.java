package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.skill.TargettedSkill;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class SkillSteal extends TargettedSkill {
    private String failMessage;
    private String noisySuccessMessage;
    public SkillSteal(Heroes plugin) {
        super(plugin, "Steal");
        setDescription("Gives you a chance to steal from the target");
        setUsage("/skill steal");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill arrowstorm"});
        
        setTypes(SkillType.PHYSICAL, SkillType.DAMAGING);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("base-chance", 0.1);
        node.setProperty("chance-per-level", 0.02);
        node.setProperty("failure-message", "%hero% failed to steal from %target%!");
        node.setProperty("steal-held-item-only", "false");
        node.setProperty("noisy-success-message", "%hero% stole %target%s %item%!");
        node.setProperty(Setting.USE_TEXT.node(), "None");
        node.setProperty("steal-amount", 64);
        return node;
    }
    
    @Override
    public void init() {
        super.init();
        //the hero has to be null here cause we dont' know who's casting the skill at this point
        //what this means is your text will be read from skills.yml and not from a setting in classes
        failMessage = getSetting(null, "failure-message","%hero% failed to steal from %target%!").replace("%hero%", "$1").replace("%target%", "$2");
        //While I'm here I might as well make the other message too
        noisySuccessMessage = getSetting(null, "noisy-success-message", "%hero% stole %target%s %item%!").replace("%hero", "$1").replace("%target%", "$2").replace("%item%", "$3");
    }
    
    //So basically I took SkillArrowstorm and I'm re-writing it
    //I'm using Arrowstorm cause it deals with player inventory
    //and I want this skill to steal a random item from the target's inventory
    @Override
    public SkillResult use(Hero hero, LivingEntity target, String[] args) {
        Player player = hero.getPlayer();
        //first let's make it so the target is actually an enemy player
        if (!(target instanceof Player) || target.equals(player))
            return SkillResult.INVALID_TARGET;
        Player tPlayer = (Player) target;
        if (damageCheck(player, tPlayer))
            return SkillResult.INVALID_TARGET;
        
        
        if (!getSetting(hero, Setting.USE_TEXT.node(), "None").equals("None"))
            broadcastExecuteText(hero, target);
        //So basically I just made it so that you can only target players, not yourself
        //and if the target isn't in your party then it won't work.
        
        //Now let's check to see if the player is successful in stealing an item
        //I'll do this by comparing their skill-chance with a random number
        double chance = getSetting(hero, "base-chance", 0.1, false) + (getSetting(hero, "chance-per-level", 0.02, false) * hero.getLevel());
        if (Math.random() >= chance) {
            //Actually we need to make it so that there's a chance that they failed so badly that
            //It will broadcast the failure if configured to do so
            //actually I need to make this message in the init function first
            if (Math.random() >= chance) {
                broadcast(player.getLocation(), failMessage, player.getDisplayName(), tPlayer.getDisplayName());
            }
            return SkillResult.FAIL;
        }
        //ok so if you're just joining us, I've just made it so if the player
        //fails to steal from the target, they have a chance to broadcast that failure
        //in local chat.
        
        //Now let's start doing the fun stuff of stealing from the target
        //First let's check to see if they can only steal what the opponent is holding
        ItemStack toBeStolen = null;
        ItemStack toSendToThief = null;
        PlayerInventory inv = tPlayer.getInventory();
        int randItemIndex = -1;
        boolean stealHeldItem = (getSetting(hero, "steal-held-item-only", "false").equals("true"));
        if (stealHeldItem) {
            toBeStolen = tPlayer.getItemInHand();
        } else {
            //Now we need to do some hard work here (sort of)
            //We need to find a random item in the targets inventory
            //and hijack it
            //ok so I googled bukkit javadocs and found exactly what I was
            //looking for ^_^ so i is the index of the slot. That's what I thought
            //but I wanted to be sure. I think there are only 27 slots in the inventory
            //I could be wrong though.... I'll figure this out later
            randItemIndex = (int) Math.random()*27;
            toBeStolen = inv.getItem(randItemIndex);
        }
        //Alright it was requested that I let the player only steal a certain amount
        //So let me add the node for that now
        int maxAmount = getSetting(hero, "steal-amount", 64, false); //btw I skill don't know what the false is for at the end....\
        //hold up I need to check to see if they're even stealing anything fist
        if (toBeStolen == null || toBeStolen.getType().equals(Material.AIR)) {
            Messaging.send(player, "The pocket you checked was empty!");
            return SkillResult.FAIL;
        }
        if (toBeStolen.getAmount() - maxAmount <= 0) {
            //soo toSendToThief is the itemstack I'm going to drop at the theif's feet
            toSendToThief = toBeStolen.clone();
            toSendToThief.setAmount(toBeStolen.getAmount());
            if (randItemIndex == -1)
                inv.clear(inv.getHeldItemSlot());
            inv.clear(randItemIndex);
        } else {
            toSendToThief = toBeStolen.clone(); //let me tell you why I did clone();
            //whenever you have a variable that starts with an uppercase like String or Object
            //that variable's value is a reference to something whereas if it's lowercase like
            //int or long, it's just a value. When you have a reference to something it means that
            //if I just did toBeStolen, it would still point to the ItemStack in the targets inventory
            //if I do .clone(), it now points to a new ItemStack that I just created
            //... at least I think that's right...
            toSendToThief.setAmount(maxAmount);
            toBeStolen.setAmount(toBeStolen.getAmount() - maxAmount);
        }
        tPlayer.updateInventory();
        //anyway, so we've set the amounts, now I have to drop the item at the theif's feet
        player.getWorld().dropItemNaturally(player.getLocation(), toSendToThief);
        //Lastly I want to have a chance that they weren't that good at stealing
        //and it broadcasts that they stole the item
        if (Math.random() >= chance)
            broadcast(player.getLocation(), noisySuccessMessage, player.getDisplayName(), tPlayer.getDisplayName(), toSendToThief.getType().name().replace("_", " ").toLowerCase());
   
        
        
        return SkillResult.NORMAL;
        //and that's all folks!
    }
    

}