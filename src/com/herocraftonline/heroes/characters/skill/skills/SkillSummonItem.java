package com.herocraftonline.heroes.characters.skill.skills;

import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.api.SkillResult;
import com.herocraftonline.heroes.characters.Hero;
import com.herocraftonline.heroes.characters.skill.ActiveSkill;
import com.herocraftonline.heroes.characters.skill.SkillConfigManager;
import com.herocraftonline.heroes.characters.skill.SkillType;
import com.herocraftonline.heroes.characters.skill.SkillSetting;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
public class SkillSummonItem extends ActiveSkill {

    public SkillSummonItem(Heroes plugin) {
        super(plugin, "SummonItem");
        setDescription("summons $1 $2 for you");
        setUsage("/skill summonitem");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill summonitem"});
        
        setTypes(SkillType.SUMMON, SkillType.SILENCABLE, SkillType.ITEM);
    }

    @Override
    public String getDescription(Hero hero) {
        int amount = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.AMOUNT.node(), 1, false) +
                (SkillConfigManager.getUseSetting(hero, this, "amount-increase", 0.0, false) * hero.getSkillLevel(this)));
        amount = amount > 0 ? amount : 0;
        String item = Material.getMaterial(SkillConfigManager.getUseSetting(hero, this, "item-id", 17, false)).name().replace("_", " ").toLowerCase();
        String description = getDescription().replace("$1", amount + "").replace("$2", item + "");
        
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
        node.set("item-id", 17);
        node.set("damage-value", 2);
        node.set(SkillSetting.AMOUNT.node(), 1);
        node.set("amount-increase", 0);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        int id = SkillConfigManager.getUseSetting(hero, this, "item-id", 17, false);
        int damageValue = SkillConfigManager.getUseSetting(hero, this, "damage-value", 2, false);
        int amount = (int) (SkillConfigManager.getUseSetting(hero, this, SkillSetting.AMOUNT.node(), 1, false) +
                (SkillConfigManager.getUseSetting(hero, this, "amount-increase", 0.0, false) * hero.getSkillLevel(this)));
        amount = amount > 0 ? amount : 0;
        ItemStack is = null;
        if (damageValue == 0) {
            is = new ItemStack(id, amount);
        } else {
            is = new ItemStack(id, amount, (short) damageValue);
        }
        player.getWorld().dropItemNaturally(player.getLocation(), is);
        return SkillResult.NORMAL;
    }
    

}