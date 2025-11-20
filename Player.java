import java.util.Scanner;
import java.util.Random;

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
            // attack logic
            int damageDealt = this.getAttackPower();
            Random rand = new Random(); 
            if (rand.nextInt(100) < 10) { // 10% chance for critical hit
                damageDealt *= 2;
                System.out.println("Critical Hit!");
            }
            System.out.println("You struck the enemy!");
            target.takeDamage(damageDealt);
        } else if (choice == 2) {
            // Heal logic
            Random rand = new Random();
            int bonusHeal = rand.nextInt(11); // Random bonus between 0-10
            int totalheal = 15 + bonusHeal;
            System.out.println("You drank a potion! (Logic to be added)");
            this.heal(totalheal); // Heal between 10-20 HP


        } else {
            System.out.println("You missed your turn by panicking!");
        }
    }
    
}
