package com.herocraftonline.dev.heroes.skill.skills;

import com.herocraftonline.dev.heroes.api.SkillResult;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;

public class SkillDeconstruct extends ActiveSkill {
    
    public SkillDeconstruct(Heroes plugin) {
        super(plugin, "Deconstruct");
        setDescription("Deconstructs the object you are holding.");
        setUsage("/skill deconstruct");
        setArgumentRange(0, 0);
        setIdentifiers(new String[] { "skill deconstruct", "skill dstruct" });
    }
    
    @Override
    public String getDescription(Hero hero) {
        String description = getDescription();
        
        //COOLDOWN
        int cooldown = (SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN.node(), 0, false)
                - SkillConfigManager.getUseSetting(hero, this, Setting.COOLDOWN_REDUCE.node(), 0, false) * hero.getLevel()) / 1000;
        if (cooldown > 0) {
            description += " CD:" + cooldown + "s";
        }
        
        //MANA
        int mana = SkillConfigManager.getUseSetting(hero, this, Setting.MANA.node(), 10, false)
                - (SkillConfigManager.getUseSetting(hero, this, Setting.MANA_REDUCE.node(), 0, false) * hero.getLevel());
        if (mana > 0) {
            description += " M:" + mana;
        }
        
        //HEALTH_COST
        int healthCost = SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST, 0, false) - 
                (SkillConfigManager.getUseSetting(hero, this, Setting.HEALTH_COST_REDUCE, mana, true) * hero.getLevel());
        if (healthCost > 0) {
            description += " HP:" + healthCost;
        }
        
        //STAMINA
        int staminaCost = SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA.node(), 0, false)
                - (SkillConfigManager.getUseSetting(hero, this, Setting.STAMINA_REDUCE.node(), 0, false) * hero.getLevel());
        if (staminaCost > 0) {
            description += " FP:" + staminaCost;
        }
        
        //DELAY
        int delay = SkillConfigManager.getUseSetting(hero, this, Setting.DELAY.node(), 0, false) / 1000;
        if (delay > 0) {
            description += " W:" + delay + "s";
        }
        
        //EXP
        int exp = SkillConfigManager.getUseSetting(hero, this, Setting.EXP.node(), 0, false);
        if (exp > 0) {
            description += " XP:" + exp;
        }
        
        return description;
    }
    
    
    @Override
    public ConfigurationSection getDefaultConfig() {
        ConfigurationSection node = super.getDefaultConfig();
        String root = "IRON_AXE";
        node.set(root + "." + Setting.LEVEL.node(), 1);
        node.set(root + ".min-durability", .5);
        node.set(root + ".IRON_INGOT", 1);
        node.set(root + ".STICK", 1);
        node.set(Setting.USE_TEXT.node(), "%hero% has deconstructed a %item%");
        return node;
    }
    
    @Override
    public void init() {
        super.init();
        setUseText(SkillConfigManager.getRaw(this, Setting.USE_TEXT.node(), "%hero% has deconstructed a %item%").replace("%hero%", "$1").replace("%item%", "$2"));
    }
    
    @SuppressWarnings("deprecation")
    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        
        ItemStack item = player.getItemInHand();
        if (item.getType() == Material.AIR) {
            Messaging.send(player, "You must be holding the item you wish to deconstruct!");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        
        String matName = item.getType().name();
        if (!SkillConfigManager.getSettingKeys(hero.getHeroClass(), this, "").contains(matName)) {
            Messaging.send(player, "Found Keys: " + SkillConfigManager.getSettingKeys(hero.getHeroClass(), this, "").toString());
            Messaging.send(player, "You can't deconstruct that item!");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        
        int level = SkillConfigManager.getUseSetting(hero, this, matName + "." + Setting.LEVEL.node(), 1, false);
        if (level > hero.getLevel()) {
            Messaging.send(player, "You must be level " + level + " to deconstruct that item!");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        
        if (item.getType().getMaxDurability() > 16) {
            double minDurability = item.getType().getMaxDurability() * (1D - SkillConfigManager.getUseSetting(hero, this, matName + ".min-durability", .5, false));
            if (item.getDurability() > minDurability) {
                Messaging.send(player, "The item is too damaged to deconstruct!");
                return SkillResult.INVALID_TARGET_NO_MSG;
            }
        }
        
        Set<String> returned = SkillConfigManager.getSettingKeys(hero.getHeroClass(), this, matName);
        if (returned == null) {
            Messaging.send(player, "Unable to deconstruct that item!");
            return SkillResult.INVALID_TARGET_NO_MSG;
        }
        
        for (String s : returned) {
            if (s.equals("min-durability") || s.equals(Setting.LEVEL.node()))
                continue;
            
            Material m = Material.matchMaterial(s);
            if (m == null) {
                throw new IllegalArgumentException("Error with skill " + getName() + ": bad item definition " + s);
            }
            int amount = SkillConfigManager.getUseSetting(hero, this, matName + "." + s, 1, false);
            if (amount < 1) {
                throw new IllegalArgumentException("Error with skill " + getName() + ": bad amount definition for " + s + ": " + amount);
            }
            
            ItemStack stack = new ItemStack(m, amount);
            Map<Integer, ItemStack> leftOvers = player.getInventory().addItem(stack);
            //Just dump any leftover stacks onto the ground
            if (!leftOvers.isEmpty()) {
                for(ItemStack leftOver : leftOvers.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), leftOver);
                }
            }
        }
        
        player.getInventory().removeItem(new ItemStack(player.getItemInHand().getType(), 1));
        player.updateInventory();
        broadcast(player.getLocation(), getUseText(), new Object[] { player.getDisplayName(), matName.toLowerCase().replace("_", " ") });
        return SkillResult.NORMAL;
    }

}