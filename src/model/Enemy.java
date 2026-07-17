package model;

import java.util.Random;

import model.events.GameEvent;
import model.events.GameEventType;

public class Enemy extends GameCharacter {

    public Enemy(String name, int hp, int attackPower, int defense) {
        super(name, hp, attackPower, defense);
    }

    @Override
    public void attack(GameCharacter target) {
        // Simple AI: The enemy just attacks with random variation
        Random rand = new Random();
        int damageVar = rand.nextInt(6); // Random variation of 0-5
        int totalDamage = this.getAttackPower() + damageVar;

        fireEvent(GameEvent.builder(GameEventType.ENEMY_ATTACK)
                .put("attackerName", this.getName())
                .put("damage", totalDamage)
                .put("targetName", target.getName())
                .build());

        target.takeDamage(totalDamage);
    }
}
