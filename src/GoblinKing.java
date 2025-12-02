public class GoblinKing extends Enemy {
    public GoblinKing() {
        super("Goblin King", 150, 20, 15);
    }

    // POLYMORPHISM: The King gets angrier every turn!
    @Override
    public void attack(GameCharacter target) {
        System.out.println("--- Goblin King's Turn ---");
        
        // 1. THE RAGE MECHANIC
        System.out.println(" The Goblin King roars in fury!");
        System.out.println(">> His muscles swell with rage! (Attack +2)");
        
        // This permanently increases his attack for the rest of the fight.
        this.upgradePower(2); 

        // 2. THE ATTACK    
        // Now he hits you with the new, higher damage
        super.attack(target);
    }
}
