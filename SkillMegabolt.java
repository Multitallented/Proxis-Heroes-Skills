package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class SkillMegabolt extends ActiveSkill {
    public SkillMegabolt(Heroes plugin) {
        super(plugin, "Megabolt");
        setDescription("Strikes lightning on every mob/player around you");
        setUsage("/skill megabolt");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill megabolt"});

        setTypes(SkillType.LIGHTNING, SkillType.DAMAGING, SkillType.SILENCABLE);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("radius", 30);
        node.setProperty(Setting.DAMAGE.node(), 14);
        return node;
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        int maxDistance = getSetting(hero, "radius", 30, false);
        int yDistance = maxDistance < 128 ? maxDistance : 128;
        int damage = (int) getSetting(hero, Setting.DAMAGE.node(), 14, false);
        for ( Entity wMonster : player.getNearbyEntities(maxDistance, yDistance, maxDistance) ) {
            if (wMonster instanceof Player) {
                Hero friendly = getPlugin().getHeroManager().getHero((Player) wMonster);
                if (hero.getParty() != null && !hero.getParty().getMembers().contains(friendly)) {
                    if (damageCheck(friendly.getPlayer(), player)) {
                        Player tPlayer = friendly.getPlayer();
                        addSpellTarget(tPlayer, hero);
                        player.getWorld().strikeLightningEffect(tPlayer.getLocation());
                        tPlayer.damage(damage, player);
                    }
                }
            } else if (wMonster instanceof LivingEntity) {
                wMonster.getWorld().strikeLightningEffect(wMonster.getLocation());
                ((LivingEntity) wMonster).damage(damage, player);
            }
        }
        return SkillResult.NORMAL;
    }

}