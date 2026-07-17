package model.enemies;

import model.Enemy;
import model.GameCharacter;
import model.skills.Element;
import model.status.StatusEffect;
import model.status.StatusType;

/**
 * A skeleton variant that regenerates a SHIELD at the start of each turn.
 * The shield blocks the first hit each turn. After being broken, takes damage normally.
 * Stats: 100 + (wave-10)*10 HP, 12 + (wave-10)*2 ATK, 8 + (wave-10)*1 DEF.
 */
public class ShieldedSkeleton extends Enemy {

    public ShieldedSkeleton(int wave) {
        super("Shielded Skeleton",
              100 + (wave - 10) * 10,
              12 + (wave - 10) * 2,
              8 + (wave - 10) * 1,
              Element.DARK);
    }

    @Override
    public void attack(GameCharacter target) {
        // At the start of this enemy's turn, reset its shield
        resetShield();

        // Perform normal attack (includes telegraph logic from base class)
        super.attack(target);
    }

    /**
     * Resets the shield by applying a SHIELD status effect.
     * Duration of 1 means it lasts for this turn cycle only.
     */
    private void resetShield() {
        if (!getStatusManager().hasEffect(StatusType.SHIELD)) {
            StatusEffect shield = new StatusEffect(StatusType.SHIELD, 99, 0, this.getName());
            getStatusManager().addEffect(shield);
        }
    }
}
