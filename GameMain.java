import java.util.Scanner;
import java.util.Random;

public class GameMain {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Random rand = new Random();

        System.out.println("Welcome to the the Plague of Danjin!");
        System.out.print("Enter your Hero's name: ");
        String name = sc.nextLine();

        // 1. Create Hero OUTSIDE the loop so HP persists between waves
        Player hero = new Player(name);

        // --- WAVE LOOP (Runs 1 to 10) ---
        for (int wave = 1; wave <= 10; wave++) {
            
            System.out.println("\n=========================");
            System.out.println("      STARTING WAVE " + wave);
            System.out.println("=========================");

            // Check if Player is dead before starting a wave
            if (!hero.isAlive()) break;

            // 2. Determine enemies for this wave
            int numEnemies;
            if (wave == 10) {
                numEnemies = 1; // Only 1 Boss
            } else {
                // Randomly 1 or 2 enemies for normal waves
                numEnemies = rand.nextInt(2) + 1; 
            }

            // --- ENEMY LOOP (Sequential fights in one wave) ---
            for (int i = 0; i < numEnemies; i++) {
                
                // Check death again in case died in previous fight of same wave
                if (!hero.isAlive()) break; 

                // 3. Spawn the correct enemy
                Enemy currentEnemy;
                
                if (wave == 10) {
                    System.out.println("⚠ THE BOSS APPROACHES! ⚠");
                    // Boss has High HP (150) and High Attack (20)
                    currentEnemy = new Enemy("Goblin King", 150, 20); 
                } else {
                    // Normal enemy has Low HP (40) and Low Attack (8)
                    currentEnemy = new Enemy("Goblin Grunt", 40, 8);
                    System.out.println("A wild " + currentEnemy.getName() + " appears! (" + (i+1) + "/" + numEnemies + ")");
                }

                // --- BATTLE LOOP (The Fight) ---
                while (hero.isAlive() && currentEnemy.isAlive()) {
                    
                    // Hero Attacks
                    hero.attack(currentEnemy);

                    // Check if enemy died
                    if (!currentEnemy.isAlive()) {
                        System.out.println(">> You defeated the " + currentEnemy.getName() + "!");
                        
                        // Optional: Give a small heal after killing an enemy?
                        hero.heal(5); // Heal 5 HP after each kill
                        break; 
                    }

                    // Enemy Attacks
                    currentEnemy.attack(hero);

                    // Check if hero died
                    if (!hero.isAlive()) {
                        System.out.println(">> You have been defeated...");
                        break;
                    }
                    
                    System.out.println("-------------------------------");
                }
            }
        }
        
        // Game End Message
        if (hero.isAlive()) {
            System.out.println("\nCONGRATULATIONS! You cleared the dungeon!");
        } else {
            System.out.println("\nGAME OVER.");
        }
        
        sc.close();
    }
}