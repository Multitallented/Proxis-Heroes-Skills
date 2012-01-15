package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Player;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillConfigManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;
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
        int amount = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.AMOUNT.node(), 1, false) +
                (SkillConfigManager.getUseSetting(hero, this, "amount-increase", 0.0, false) * hero.getLevel()));
        amount = amount > 0 ? amount : 0;
        String item = Material.getMaterial(SkillConfigManager.getUseSetting(hero, this, "item-id", 17, false)).name().replace("_", " ").toLowerCase();
        String description = getDescription().replace("$1", amount + "").replace("$2", item + "");
        
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
        node.set("item-id", 17);
        node.set("damage-value", 2);
        node.set(Setting.AMOUNT.node(), 1);
        node.set("amount-increase", 0);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        int id = SkillConfigManager.getUseSetting(hero, this, "item-id", 17, false);
        int damageValue = SkillConfigManager.getUseSetting(hero, this, "damage-value", 2, false);
        int amount = (int) (SkillConfigManager.getUseSetting(hero, this, Setting.AMOUNT.node(), 1, false) +
                (SkillConfigManager.getUseSetting(hero, this, "amount-increase", 0.0, false) * hero.getLevel()));
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