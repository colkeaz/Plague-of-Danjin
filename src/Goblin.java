public class Goblin extends Enemy {
    // Constructor: Goblin has low HP (50) and low Defense (5) and scales with wave count
    public Goblin(int wave) {
        super("Goblin Grunt", 40 + (wave * 10), 8 + (wave * 2), 0);
    }
}
