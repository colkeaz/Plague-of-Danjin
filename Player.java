import java.util.Scanner;

public class Player extends GameCharacter {

    public Player(String name) {
        // Start with fixed stats: e.g., 100 HP, 15 Attack
        super(name, 100, 15); 
    }

    // POLYMORPHISM: We are defining exactly how a Player attacks
    @Override
    public void attack(GameCharacter target) {
        Scanner sc = new Scanner(System.in);
        
        System.out.println("--- Your Turn ---");
        System.out.println("1. Attack");
        System.out.println("2. Heal");
        System.out.print("Choose an action: ");
        int choice = sc.nextInt();

        if (choice == 1) {
            // Normal attack logic
            // TODO: You can add a "Critical Hit" chance calculation here later
            int damageDealt = this.getAttackPower();
            System.out.println("You struck the enemy!");
            target.takeDamage(damageDealt);
        } else if (choice == 2) {
            // Heal logic
            // TODO: Implement logic to increase this.hp (using a setter or new method)
            System.out.println("You drank a potion! (Logic to be added)");
        } else {
            System.out.println("You missed your turn by panicking!");
        }
    }
}
