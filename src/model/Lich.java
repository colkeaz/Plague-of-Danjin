package model;

import model.events.GameEvent;
import model.events.GameEventType;
import model.skills.Element;

public class Lich extends Enemy {
    private int turnCounter = 0;
    private int minionsActive = 0;

    public Lich() {
        super("The Necromancer Lich", 300, 25, 20, Element.DARK);
    }

    public int getTurnCounter() {
        return turnCounter;
    }

    public int getMinionsActive() {
        return minionsActive;
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
            // Normal attack on non-summon turns
            super.attack(target);
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
