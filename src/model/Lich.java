package model;

import model.enemies.EnemyAbility;
import model.events.GameEvent;
import model.events.GameEventType;
import model.skills.Element;

public class Lich extends Enemy {
    private int turnCounter = 0;
    private int attackCounter = 0;
    private int minionsActive = 0;

    private static final EnemyAbility NECROTIC_BLAST = new EnemyAbility(
            "Necrotic Blast",
            4.0f,
            Element.DARK,
            "The Lich is channeling dark energy..."
    );

    public Lich() {
        super("The Necromancer Lich", 300, 25, 20, Element.DARK);
    }

    /**
     * Boss telegraph schedule: every 3rd attack action.
     */
    @Override
    public boolean shouldTelegraph(int turnCount) {
        return turnCount > 0 && turnCount % 3 == 0;
    }

    public int getTurnCounter() {
        return turnCounter;
    }

    public int getMinionsActive() {
        return minionsActive;
    }

    /**
     * Clears all active minions (used by QTE success effect).
     */
    public void clearMinions() {
        this.minionsActive = 0;
    }

    /**
     * Adds the specified number of minions (used by QTE failure effect).
     */
    public void addMinions(int count) {
        this.minionsActive += count;
    }

    @Override
    public void attack(GameCharacter target) {
        turnCounter++;

        // SPECIAL ABILITY: Every 3 turns, summon a minion instead of attacking
        if (turnCounter % 3 == 0) {
            minionsActive++;

            fireEvent(GameEvent.builder(GameEventType.LICH_SUMMON_MINION)
                    .put("attackerName", this.getName())
                    .put("turnNumber", turnCounter)
                    .put("minionsActive", minionsActive)
                    .build());
        } else {
            attackCounter++;
            // Telegraph system: if winding up, execute ability
            if (isWindingUp()) {
                executeAbility(target);
            } else if (shouldTelegraph(attackCounter)) {
                windUp(NECROTIC_BLAST);
                // Still performs normal attack on wind-up turn
                super.attack(target);
            } else {
                // Normal attack on non-summon turns
                super.attack(target);
            }
        }

        // PASSIVE: Minions attack every single turn
        if (minionsActive > 0) {
            int minionDamage = minionsActive * 8; // 8 damage per minion

            fireEvent(GameEvent.builder(GameEventType.LICH_MINION_ATTACK)
                    .put("attackerName", this.getName())
                    .put("minionsActive", minionsActive)
                    .put("totalDamage", minionDamage)
                    .put("targetName", target.getName())
                    .build());

            target.takeDamage(minionDamage);
        }
    }
}
