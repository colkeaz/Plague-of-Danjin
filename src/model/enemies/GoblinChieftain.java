package model.enemies;

import model.Enemy;
import model.GameCharacter;
import model.skills.Element;

/**
 * Mini-boss appearing at wave 5. Uses "War Cry" every 2 turns to buff ATK by +5
 * and heal for 15 HP. Also has a telegraphed ability.
 * Stats: 120 HP, 18 ATK, 10 DEF.
 */
public class GoblinChieftain extends Enemy {
    private int turnCounter = 0;

    private static final EnemyAbility WAR_CRY_TELEGRAPH = new EnemyAbility(
            "War Cry",
            2.0f,
            Element.PHYSICAL,
            "The Goblin Chieftain beats his chest and roars..."
    );

    public GoblinChieftain() {
        super("Goblin Chieftain", 120, 18, 10, Element.PHYSICAL);
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

        // War Cry every 2 turns: buff ATK +5 and heal 15 HP
        if (turnCounter % 2 == 0) {
            this.upgradePower(5);
            this.heal(15);
        }

        // Telegraph system: boss schedule (every 3rd attack)
        if (isWindingUp()) {
            executeAbility(target);
        } else if (shouldTelegraph(turnCounter)) {
            windUp(WAR_CRY_TELEGRAPH);
            // Still performs a normal attack on the wind-up turn
            super.attack(target);
        } else {
            super.attack(target);
        }
    }
}
