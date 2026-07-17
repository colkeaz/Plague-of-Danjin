package model;

import model.enemies.EnemyAbility;
import model.events.GameEvent;
import model.events.GameEventType;
import model.skills.Element;

public class GoblinKing extends Enemy {
    private int turnCounter = 0;

    private static final EnemyAbility CRUSHING_BLOW = new EnemyAbility(
            "Crushing Blow",
            3.0f,
            Element.PHYSICAL,
            "The Goblin King raises his massive fist..."
    );

    public GoblinKing() {
        super("Goblin King", 150, 20, 15, Element.PHYSICAL);
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

        // THE RAGE MECHANIC: permanently increases attack
        fireEvent(GameEvent.builder(GameEventType.GOBLIN_KING_RAGE)
                .put("attackerName", this.getName())
                .put("attackIncrease", 2)
                .build());

        this.upgradePower(2);

        // Telegraph system: if winding up, execute ability
        if (isWindingUp()) {
            executeAbility(target);
        } else if (shouldTelegraph(turnCounter)) {
            windUp(CRUSHING_BLOW);
            // Still performs normal attack on wind-up turn
            super.attack(target);
        } else {
            // Normal attack with the new, higher damage
            super.attack(target);
        }
    }
}
