package model.enemies;

import model.Enemy;
import model.GameCharacter;
import model.Player;
import model.skills.Element;
import model.status.StatusEffect;
import model.status.StatusType;

/**
 * An elemental creature born of concentrated plague energy.
 * Every attack applies 2 stacks of POISON. Telegraphed ability "Toxic Eruption"
 * deals 3x POISON damage.
 * Stats: HP 100, ATK 12, DEF 5, Element POISON.
 */
public class PlagueElemental extends Enemy {
    private int turnCounter = 0;

    private static final EnemyAbility TOXIC_ERUPTION = new EnemyAbility(
            "Toxic Eruption",
            3.0f,
            Element.POISON,
            "The Plague Elemental swells with toxic energy..."
    );

    public PlagueElemental() {
        super("Plague Elemental", 100, 12, 5, Element.POISON);
    }

    /**
     * Telegraph schedule: every 3rd turn.
     */
    @Override
    public boolean shouldTelegraph(int turnCount) {
        return turnCount > 0 && turnCount % 3 == 0;
    }

    @Override
    public void attack(GameCharacter target) {
        turnCounter++;

        // Telegraph system
        if (isWindingUp()) {
            executeAbility(target);
        } else if (shouldTelegraph(turnCounter)) {
            windUp(TOXIC_ERUPTION);
            // Still performs a normal attack on the wind-up turn
            performPoisonAttack(target);
        } else {
            performPoisonAttack(target);
        }
    }

    /**
     * Performs a normal attack and applies 2 stacks of POISON (3 turns, 5 dmg each).
     */
    private void performPoisonAttack(GameCharacter target) {
        super.attack(target);

        // Apply 2 stacks of POISON if target is alive
        if (target.isAlive()) {
            boolean immune = false;
            if (target instanceof Player) {
                immune = ((Player) target).getInventory().hasPoisonImmunity();
            }
            if (!immune) {
                StatusEffect poison1 = new StatusEffect(StatusType.POISON, 3, 5, this.getName());
                StatusEffect poison2 = new StatusEffect(StatusType.POISON, 3, 5, this.getName());
                target.getStatusManager().addEffect(poison1);
                target.getStatusManager().addEffect(poison2);
            }
        }
    }
}
