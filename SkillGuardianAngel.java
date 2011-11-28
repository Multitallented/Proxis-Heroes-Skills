package com.herocraftonline.dev.heroes.skill.skills;


import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillResult;
import com.herocraftonline.dev.heroes.effects.common.InvulnerabilityEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;
import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

public class SkillGuardianAngel extends ActiveSkill {
    private String applyText;
    private String expireText;

    public SkillGuardianAngel(Heroes plugin) {
        super(plugin, "GuardianAngel");
        setDescription("Gives you and your party invuln");
        setUsage("/skill guardianangel");
        setArgumentRange(0, 0);
        setIdentifiers(new String[]{"skill guardianangel", "skill ga"});

        setTypes(SkillType.BUFF, SkillType.SILENCABLE);
    }
    
    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty(Setting.RADIUS.node(), 10);
        node.setProperty(Setting.DURATION.node(), 12000); // in Milliseconds - 10 minutes
        return node;
    }

    @Override
    public void init() {
        super.init();
        applyText = getSetting(null, Setting.APPLY_TEXT.node(), "Your feel a bit wiser!");
        expireText = getSetting(null, Setting.EXPIRE_TEXT.node(), "You no longer feel as wise!");
    }

    @Override
    public SkillResult use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        int duration = getSetting(hero, Setting.DURATION.node(), 600000, false);

        InvulnerabilityEffect iEffect = new InvulnerabilityEffect(this, duration);
        if (!hero.hasParty()) {
            hero.addEffect(iEffect);
        } else {
            int rangeSquared = (int) Math.pow(getSetting(hero, Setting.RADIUS.node(), 10, false), 2);
            for (Hero pHero : hero.getParty().getMembers()) {
                Player pPlayer = pHero.getPlayer();
                if (!pPlayer.getWorld().equals(player.getWorld())) {
                    continue;
                }
                if (pPlayer.getLocation().distanceSquared(player.getLocation()) > rangeSquared) {
                    continue;
                }
                pHero.addEffect(iEffect);
            }
        }

        broadcastExecuteText(hero);
        return SkillResult.NORMAL;
    }

    

}