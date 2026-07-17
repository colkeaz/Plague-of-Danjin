package model.enemies;

import model.Enemy;
import model.GameCharacter;
import model.skills.Element;

/**
 * Boss enemy of the Plague Gardens. Commands vine minions and channels
 * the power of corrupted nature.
 * Every 3 turns summons a vine minion (tracked by vineCount). Heals 10 HP
 * when a vine is alive. Telegraphed ability "Nature's Wrath" deals 2.5x POISON
 * damage and heals self for 30 HP.
 * Stats: HP 180, ATK 20, DEF 15, Element POISON.
 */
public class Thornmother extends Enemy {
    private int turnCounter = 0;
    private int vineCount = 0;

    private static final EnemyAbility NATURES_WRATH = new EnemyAbility(
            "Nature's Wrath",
            2.5f,
            Element.POISON,
            "The Thornmother channels the fury of corrupted nature..."
    );

    public Thornmother() {
        super("Thornmother", 180, 20, 15, Element.POISON);
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

        // Summon a vine every 3 turns
        if (turnCounter % 3 == 0) {
            vineCount++;
        }

        // Heal 10 HP if at least one vine is alive
        if (vineCount > 0) {
            this.heal(10);
        }

        // Telegraph system
        if (isWindingUp()) {
            executeAbility(target);
            // Nature's Wrath also heals self for 30
            this.heal(30);
        } else if (shouldTelegraph(turnCounter)) {
            windUp(NATURES_WRATH);
            // Still performs a normal attack on the wind-up turn
            super.attack(target);
        } else {
            super.attack(target);
        }
    }

    /**
     * Returns the current number of active vine minions.
     */
    public int getVineCount() {
        return vineCount;
    }

    /**
     * Destroys a vine minion (called when player kills a vine).
     */
    public void destroyVine() {
        if (vineCount > 0) {
            vineCount--;
        }
    }
}
