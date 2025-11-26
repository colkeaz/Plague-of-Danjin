import java.util.Scanner;
import java.util.Random;

public class GameMain {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Random rand = new Random();

        System.out.println();
        System.out.println();
        typeText("Welcome to the Plague of Danjin!", 200);
        typeText("Enter your Hero's Name: ", 100);
        String name = sc.nextLine();

        // 1. Create Hero OUTSIDE the loop so HP persists between waves
        Player hero = new Player(name);

        System.out.println("\nGreetings, " + hero.getName() + "!");
        typeText("The land of Danjin is plagued by vicious Goblins.", 120);
        typeText("Defeat them all and restore peace to the kingdom!", 120);
        typeText("Prepare yourself for battle!", 120);

        typeText("Let's Begin your adventure!", 75);
        // --- WAVE LOOP (Runs 1 to 10) ---
        for (int wave = 1; wave <= 10; wave++) {

             // Check if Player is dead before starting a wave
            if (!hero.isAlive()) break;

            
            System.out.println("\n=========================");
            typeText("      STARTING WAVE " + wave, 65);
            System.out.println("=========================");
            System.out.println();


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
                    // Boss has High HP (150), High Attack (20), and Moderate Defense (20)
                    currentEnemy = new Enemy("Goblin King", 150, 20, 20); 
                } else {
                    //NORMAL ENEMIES (Waves 1-9)
                    // Scale enemy stats based on wave number
                    // HP increases by 10 per wave (Wave 1 = 50hp, Wave 9 = 130hp)
                    int scaledHp = 40 + (wave * 10);
                    
                    // Attack increases by 2 per wave (Wave 1 = 10atk, Wave 9 = 26atk)
                    int scaledAtk = 8 + (wave * 2);
    
                    // Defense increases slightly every 3 waves
                    int scaledDef = wave / 3; 
    
                    // Create the enemy with these new numbers
                    currentEnemy = new Enemy("Goblin Grunt", scaledHp, scaledAtk, scaledDef);
    
                    System.out.println();
                    System.out.println("--------------------------------------------------------------------");
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
            // --- POST-WAVE LOGIC (Chest & Pause) ---
            if (hero.isAlive()) {
        
                if (wave < 10) {
                    // If it is NOT the last wave
                    if (rand.nextInt(100) < 50) {
                        findChest(hero);
                    }
                    System.out.println("Press Enter to start the next wave...");
                    sc.nextLine(); 
                } else {
                    // If it IS the last wave (Wave 10)
                    System.out.println("Press Enter to continue...");
                    sc.nextLine();
                }
            }
        }
        
        // Game End Message
        if (hero.isAlive()) {
            System.out.println();
            typeText("CONGRATULATIONS! You have defeated the boss and eradicated the Plague!", 120); 
            typeText("You are a true hero, " + hero.getName() + "!", 120);
            typeText("Thank you for playing!", 120); 
            typeText("See you next time, Hero "+ hero.getName()+ "! ", 120); 
            System.out.println();

            printVictoryScreen();            

        } else {
            typeText("GAME OVER!", 150);
        }
        
        sc.close();
    }

    public static void findChest(Player p) {
        Random rand = new Random();
        int chance = rand.nextInt(100); // 0-99

        System.out.println("-----------------------------------");
        System.out.println("You found a Mysterious Chest!");

         // 1. LEGENDARY (0 to 9) -> 10%
        if (chance < 10) {
            System.out.println("✨ AMAZING! You found a LEGENDARY Sword! ✨");
            p.upgradePower(20); // Huge upgrade

        // 2. EPIC (11 to 29) -> 15% 
        } else if (chance < 25) {
            System.out.println("Wow! You found Epic Armor polishing kits!");
            p.upgradeDefense(15); // Significant upgrade to defense
            p.heal(10);; // Small heal

        // 3. Uncommon (30 to 49) -> 20%
        }else if(chance < 45){
            System.out.println("You found an iron sword and armor!");
            p.upgradePower(8);
            p.upgradeDefense(8);
        
        //3. COMMON (50 to 84) -> 40%
        }else if (chance < 85) {
            System.out.println("It contains a  Health Potion.");
            p.heal(30);

        // 4. Trap (85 to 99) -> 15%
        } else {
            System.out.println("It was a Mimic! It bites your hand!");
            p.takeDamage(15);
        }
    }

    public static void typeText(String text, int delay) {
        for (char c : text.toCharArray()) {
            System.out.print(c); 
            try {
                Thread.sleep(delay); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println(); 
    }

    public static void printVictoryScreen() {
    System.out.println("\n\n");
    
    // 1. The Art (Scanline Effect)
    String[] victoryArt = {
        "################################################################################",
        "#                                                                              #",
        "#   ______  _                                       __                         #",
        "#   | ___ \\| |                                     / _|                        #",
        "#   | |_/ /| |  __ _   __ _  _   _   ___     ___  | |_                         #",
        "#   |  __/ | | / _` | / _` || | | | / _ \\   / _ \\ |  _|                        #",
        "#   | |    | || (_| || (_| || |_| ||  __/  | (_) || |                          #",
        "#   \\_|    |_| \\__,_| \\__, | \\__,_| \\___|   \\___/ |_|                          #",
        "#                      __/ |                                                   #",
        "#                     |___/                                                    #",
        "#    ______              _  _        _                                         #",
        "#    |  _  \\            (_)(_)      (_)                                        #",
        "#    | | | |  __ _  _ __  _  _  _ __                                           #",
        "#    | | | | / _` || '_ \\| || || '_ \\                                          #",
        "#    | |/ / | (_| || | | || || || | | |                                        #",
        "#    |___/   \\__,_||_| |_|| ||_||_| |_|                                        #",
        "#                        _/ |                                                  #",
        "#                       |__/                                                   #",
        "#                                                                              #",
        "################################################################################"
    };

    // Print the art Line-by-Line (Fast scan: 30ms)
    for (String line : victoryArt) {
        System.out.println(line);
        try {
            Thread.sleep(30); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // 2. The Credits (Typewriter Effect)
    System.out.println();
    // Slightly longer pause before names start
    try { Thread.sleep(500); } catch (Exception e) {} 

    typeText("Developed by:", 150);
    typeText("- Paala, Luke Andre", 75);
    typeText("- Caraig, Hans Gadiel", 75);
    typeText("- Fajiculay, Cedric", 75);
    
    System.out.println("\nThank you for playing!");
}
}