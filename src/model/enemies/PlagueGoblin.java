package model.enemies;

import model.Enemy;
import model.GameCharacter;
import model.skills.Element;
import model.status.StatusEffect;
import model.status.StatusType;

/**
 * A goblin variant that deals POISON damage and applies POISON status on hit.
 * Stats: 60 + wave*8 HP, 12 + wave*2 ATK, 0 DEF.
 */
public class PlagueGoblin extends Enemy {

    public PlagueGoblin(int wave) {
        super("Plague Goblin",
              60 + wave * 8,
              12 + wave * 2,
              0,
              Element.POISON);
    }

    @Override
    public void attack(GameCharacter target) {
        // Perform normal attack (includes telegraph logic from base class)
        super.attack(target);

        // After dealing damage, apply POISON to target (3 turns, 5 dmg/tick)
        if (target.isAlive()) {
            StatusEffect poison = new StatusEffect(StatusType.POISON, 3, 5, this.getName());
            target.getStatusManager().addEffect(poison);
        }
    }
}
