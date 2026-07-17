package model.enemies;

import model.Enemy;
import model.GameCharacter;
import model.events.GameEvent;
import model.events.GameEventType;
import model.skills.Element;
import model.status.StatusEffect;
import model.status.StatusType;

/**
 * Mini-boss appearing at wave 15. High DEF, low ATK.
 * Ability: "Bone Shield" - gains SHIELD status every 3 turns.
 * When shield breaks, deals 20 PHYSICAL damage to attacker (reflected).
 * Stats: 200 HP, 15 ATK, 25 DEF.
 */
public class BoneColossus extends Enemy {
    private int turnCounter = 0;
    private boolean shieldActiveThisCycle = false;

    private static final EnemyAbility BONE_CRUSH = new EnemyAbility(
            "Bone Crush",
            3.0f,
            Element.DARK,
            "The Bone Colossus is winding up a massive swing..."
    );

    public BoneColossus() {
        super("Bone Colossus", 200, 15, 25, Element.DARK);
    }

    /**
     * Boss telegraph schedule: every 3rd turn.
     */
    @Override
    public boolean shouldTelegraph(int turnCount) {
        return turnCount > 0 && turnCount % 3 == 0;
    }

    @Override
    public void attack(GameCharacter target) {
        turnCounter++;

        // Bone Shield every 3 turns
        if (turnCounter % 3 == 0) {
            StatusEffect shield = new StatusEffect(StatusType.SHIELD, 99, 0, this.getName());
            getStatusManager().addEffect(shield);
            shieldActiveThisCycle = true;
        }

        // Telegraph system: boss schedule (every 3rd attack)
        if (isWindingUp()) {
            executeAbility(target);
        } else if (shouldTelegraph(turnCounter)) {
            windUp(BONE_CRUSH);
            super.attack(target);
        } else {
            super.attack(target);
        }
    }

    /**
     * Override takeDamage to implement shield-break reflection.
     * When this enemy has a shield and it gets broken by an incoming hit,
     * deal 20 PHYSICAL damage reflected (via event for the controller to handle).
     */
    @Override
    public void takeDamage(int damage) {
        boolean hadShield = getStatusManager().hasEffect(StatusType.SHIELD);
        super.takeDamage(damage);
        boolean hasShieldNow = getStatusManager().hasEffect(StatusType.SHIELD);

        // If shield was broken by this hit, reflect 20 damage
        if (hadShield && !hasShieldNow) {
            shieldActiveThisCycle = false;
            fireEvent(GameEvent.builder(GameEventType.ENEMY_ABILITY_FIRED)
                    .put("attackerName", this.getName())
                    .put("abilityName", "Shield Reflect")
                    .put("reflectDamage", 20)
                    .put("element", Element.PHYSICAL.name())
                    .build());
        }
    }

    public boolean isShieldActiveThisCycle() {
        return shieldActiveThisCycle;
    }
}
