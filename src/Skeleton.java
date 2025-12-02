public class Skeleton extends Enemy {
    // Constructor accepts the 'wave' to calculate scaling
    public Skeleton(int wave) {
        // Math: Reset scale so wave 11 counts as "1"
        // HP: 80 base + 12 per level
        // Atk: 15 base + 3 per level
        // Def: 5 base + 1 per level
        super("Skeleton Warrior", 
              80 + ((wave - 10) * 12), 
              15 + ((wave - 10) * 3), 
              5 + ((wave - 10) * 1)); 
    }
}