public class Lich extends Enemy {
    private int turnCounter = 0;
    private int minionsActive = 0;

    // Constructor: Lich has huge HP (300) and good Defense (20)
    public Lich() {
        super("The Necromancer Lich", 300, 25, 20);
    }

    // POLYMORPHISM: The Lich attacks differently than a normal Enemy
    @Override
    public void attack(GameCharacter target) {
        turnCounter++;
        System.out.println("--- Lich's Turn (Turn " + turnCounter + ") ---");

        // SPECIAL ABILITY: Every 3 turns, summon a minion
        if (turnCounter % 3 == 0) {
            minionsActive++;
            System.out.println("[X] THE LICH RAISES HIS STAFF! [X]");
            System.out.println("The ground trembles... A Skeleton Minion rises from the earth!");
            System.out.println("Total Minions Active: " + minionsActive);
        } else {
            // Normal Attack on non-summon turns
            super.attack(target);
        }

        // PASSIVE: Minions attack every single turn
        if (minionsActive > 0) {
            int minionDamage = minionsActive * 8; // 8 damage per minion
            System.out.println("The skeleton minions swarm you!");
            target.takeDamage(minionDamage);
        }
    }
}
