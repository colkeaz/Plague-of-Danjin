package model;

import java.util.Random;

import model.enemies.EnemyAbility;
import model.events.GameEvent;
import model.events.GameEventType;
import model.skills.Element;

public class Enemy extends GameCharacter {
    private final Random rand = new Random();

    // Telegraph system fields
    private EnemyAbility nextAbility;
    private boolean windingUp;

    public Enemy(String name, int hp, int attackPower, int defense) {
        super(name, hp, attackPower, defense);
    }

    public Enemy(String name, int hp, int attackPower, int defense, Element element) {
        super(name, hp, attackPower, defense);
        setElement(element);
    }

    @Override
    public void attack(GameCharacter target) {
        // Simple AI: The enemy just attacks with random variation
        int damageVar = rand.nextInt(6); // Random variation of 0-5
        int totalDamage = this.getAttackPower() + damageVar;

        fireEvent(GameEvent.builder(GameEventType.ENEMY_ATTACK)
                .put("attackerName", this.getName())
                .put("damage", totalDamage)
                .put("targetName", target.getName())
                .build());

        target.takeDamage(totalDamage);
    }

    // --- Telegraph System ---

    /**
     * Returns whether this enemy should telegraph on the given turn.
     * Normal enemies telegraph every 4th attack; bosses every 3rd.
     * Subclasses can override to change the schedule.
     */
    public boolean shouldTelegraph(int turnCount) {
        // Default: every 4th turn for normal enemies
        return turnCount > 0 && turnCount % 4 == 0;
    }

    /**
     * Begins winding up a telegraphed ability. Fires an ENEMY_TELEGRAPH event
     * so the view can warn the player.
     */
    public void windUp(EnemyAbility ability) {
        this.nextAbility = ability;
        this.windingUp = true;

        fireEvent(GameEvent.builder(GameEventType.ENEMY_TELEGRAPH)
                .put("attackerName", this.getName())
                .put("abilityName", ability.getName())
                .put("element", ability.getElement().name())
                .put("telegraphMessage", ability.getTelegraphMessage())
                .build());
    }

    /**
     * Executes the wound-up ability on the target. Fires ENEMY_ABILITY_FIRED event
     * and deals multiplied damage with the ability's element.
     */
    public void executeAbility(GameCharacter target) {
        if (nextAbility == null) return;

        int baseDamage = this.getAttackPower();
        int abilityDamage = (int)(baseDamage * nextAbility.getDamageMultiplier());

        fireEvent(GameEvent.builder(GameEventType.ENEMY_ABILITY_FIRED)
                .put("attackerName", this.getName())
                .put("abilityName", nextAbility.getName())
                .put("damage", abilityDamage)
                .put("element", nextAbility.getElement().name())
                .put("targetName", target.getName())
                .build());

        target.takeDamage(abilityDamage, nextAbility.getElement());

        // Clear wind-up state
        this.nextAbility = null;
        this.windingUp = false;
    }

    /**
     * Returns whether this enemy is currently winding up for a powerful attack.
     */
    public boolean isWindingUp() {
        return windingUp;
    }

    /**
     * Returns the next ability queued for execution (null if not winding up).
     */
    public EnemyAbility getNextAbility() {
        return nextAbility;
    }
}
