package model;

import model.events.GameEvent;
import model.events.GameEventType;

public class GoblinKing extends Enemy {

    public GoblinKing() {
        super("Goblin King", 150, 20, 15);
    }

    @Override
    public void attack(GameCharacter target) {
        // THE RAGE MECHANIC: permanently increases attack
        fireEvent(GameEvent.builder(GameEventType.GOBLIN_KING_RAGE)
                .put("attackerName", this.getName())
                .put("attackIncrease", 2)
                .build());

        this.upgradePower(2);

        // Now attack with the new, higher damage
        super.attack(target);
    }
}
