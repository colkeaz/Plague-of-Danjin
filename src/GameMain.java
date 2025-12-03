import java.util.Random;
import java.util.Scanner;

public class GameMain {
    public static void main(String[] args) {
        // Initialize Scanner for user input and Random for RNG mechanics
        Scanner sc = new Scanner(System.in);
        Random rand = new Random();

        // --- INTRO SEQUENCE ---
        // We use typeText() instead of System.out.println() to create a cinematic "RPG" feel.
        // The delay (100ms) creates suspense before asking for the player's name.
        System.out.println();
        System.out.println();
        typeText("Welcome, traveler... to the Plague of Danjin.", 100);
        typeText("Before you descend into the depths, speak your name, that it may be remembered.", 75);
        System.out.println();
        System.out.print("Enter your name: ");
        String name = sc.nextLine();
        System.out.println();
        
        // 1. OBJECT CREATION (Persistence)
        // We create the 'hero' object OUTSIDE the game loop.
        // WHY? If we put this inside the loop, the player's HP would reset to 100 every wave.
        // Keeping it here ensures stats persist from Wave 1 to Wave 20.
        Player hero = new Player(name);

        typeText("A sturdy soul you are, " + hero.getName() + ".", 100);
        typeText("The kingdom of Morthga is withering under a cruel plague.", 100);
        typeText("All signs point to Danjin, the forgotten dungeon where corruption festers.", 100);
        typeText("If salvation still exists, it lies below.", 100);
        typeText("Steel yourself... your descent begins now.", 80);

        // --- MAIN GAME LOOP (Wave System) ---
        // This loop controls the flow of the entire game. It runs 20 times (20 Waves).
        for (int wave = 1; wave <= 20; wave++) {

            // FAIL-SAFE CHECK:
            // Before starting a new wave, we verify the player is actually alive.
            // This prevents the "Zombie Bug" where a dead player starts Wave 6.
            if (!hero.isAlive()) break;

            System.out.println("\n========================================");
            typeText("   --- YOU DESCEND INTO THE ABYSS ---", 25);
            typeText("               WAVE " + wave, 25);
            System.out.println("========================================\n");

            // 2. DYNAMIC DIFFICULTY SCALING
            // We determine how many enemies to spawn based on the current wave number.
            // This adds variety: Early waves are 1v1, later waves are 1v2 or 1v3.
            int numEnemies;
            if (wave == 10 || wave == 20) {
                numEnemies = 1; // Boss Waves are always 1v1 duel
            } else if (wave >= 15) {
                numEnemies = rand.nextInt(2) + 2; // Hardest: 2 or 3 Enemies
            } else if (wave >= 5) {
                numEnemies = rand.nextInt(2) + 1; // Medium: 1 or 2 Enemies
            } else {
                numEnemies = 1; // Easy: 1 Enemy
            }

            // 3. ARRAY MANAGEMENT 
            // We use an Array (waveEnemies) to manage multiple objects in a single turn.
            // This allows us to scale up to 100 enemies if we wanted to, without changing variable names.
            Enemy[] waveEnemies = new Enemy[numEnemies];

            for (int i = 0; i < numEnemies; i++) {
                // POLYMORPHISM IN ACTION:
                // We fill the array with specific subclasses (Goblin, Skeleton, Lich).
                // The main loop treats them all as generic "Enemy" objects, but they behave differently.
                if (wave < 10) {
                    // Phase 1: Goblins (Stats scale with wave number)
                    waveEnemies[i] = new Goblin(wave);

                } else if (wave == 10) {
                    // Boss 1: The Goblin King
                    waveEnemies[i] = new GoblinKing();

                } else if (wave < 20) {
                    // Phase 2: Skeletons (Stronger base stats)
                    waveEnemies[i] = new Skeleton(wave);

                } else {
                    // Boss 2: The Final Boss
                    waveEnemies[i] = new Lich();
                }
            }

            // --- BATTLE LOOP (Processing the Array) ---
            // We iterate through the array to fight enemies one by one (Sequential Combat).
            for (int i = 0; i < waveEnemies.length; i++) {

                // Safety check: Stop the fight immediately if the hero dies mid-wave.
                if (!hero.isAlive()) break;

                Enemy currentEnemy = waveEnemies[i];

                // --- FLAVOR TEXT ENGINE ---
                // Provides narrative context depending on who you are fighting.
                if (wave < 10) {
                    typeText("A twisted Goblin Grunt claws its way from the dark. (" + (i + 1) + "/" + numEnemies + ")", 25);
                    typeText("Its hunger is older than light.", 75);
                    // Shows the scaled stats to the player for strategy
                    typeText("(Stats -> HP: " + currentEnemy.getHp() + " | ATK: " + currentEnemy.getAttackPower() + ")", 25);

                } else if (wave == 10) {
                    typeText("⚠ A vile presence emerges...", 100);
                    typeText("The Goblin King stands before you... swollen with plague and rage. [!]", 120);
                    typeText("Raise your weapon. This will be a tough battle.", 120);

                } else if (wave < 20) {
                    typeText("A Skeleton Warrior drags itself together from scattered bones. (" + (i + 1) + "/" + numEnemies + ")", 25);
                    typeText("Armor fused to bone... a knight long forgotten.", 75);
                    typeText("(Stats -> HP: " + currentEnemy.getHp()
                            + " | ATK: " + currentEnemy.getAttackPower()
                            + " | DEF: " + currentEnemy.getDefense() + ")", 25);

                } else {
                    // The Lich Intro
                    typeText("[!] The air freezes...", 100);
                    typeText("A voice older than the plague itself whispers your name...", 120);
                    typeText("The Lich descends from the void. [!]", 120);
                    typeText("You are at the end, traveler...", 120);
                    typeText("Every heartbeat echoes in the void.", 120);
                    typeText("Hope feels distant... yet this pestilence must be purged.", 120);
                    typeText("Steel your resolve, for the darkness itself is before you... watching.", 75);
                }

                // --- COMBAT LOOP ---
                // Continues until one side is dead.
                while (hero.isAlive() && currentEnemy.isAlive()) {

                    // 1. Player Turn
                    // Calls the attack() method in Player.java which handles the menu system.
                    hero.attack(currentEnemy);

                    // 2. Win Condition Check
                    if (!currentEnemy.isAlive()) {
                        typeText(">> The " + currentEnemy.getName() + " collapses into dust and silence.", 25);
                        typeText("A faint warmth returns to your body... +5 HP.", 25);
                        hero.heal(5); // Reward for killing an enemy
                        break; // Exit combat loop
                    }

                    // 3. Enemy Turn
                    // Calls attack() in Enemy.java. If it's the Lich, it summons minions here.
                    currentEnemy.attack(hero);

                    // 4. Loss Condition Check
                    if (!hero.isAlive()) {
                        typeText(">> Your strength fades...", 150);
                        typeText("The darkness of Danjin claims another soul.", 150);
                        typeText("GAME OVER.", 150);
                        break; // Exit combat loop
                    }

                    System.out.println("-------------------------------");
                }
            }
            
            // --- POST-WAVE LOGIC ---
            // Occurs only if the player survived the entire wave.
            if (hero.isAlive()) {

                if (wave < 20) {
                    // CHEST SYSTEM: 50% chance to spawn a chest between waves.
                    // Provides risk/reward choices.
                    if (rand.nextInt(100) < 50) findChest(hero);
                    
                    // Pause for pacing
                    System.out.println("Press Enter to start the next wave...");
                    sc.nextLine();
                } else {
                    // End of Wave 20 (Victory)
                    System.out.println("Press Enter to continue...");
                    sc.nextLine();
                }
            }
        }

        // --- END GAME SEQUENCE ---
        if (hero.isAlive()) {
            typeText("CONGRATULATIONS, traveler.", 120);
            typeText("The Corrupted Heart has been destroyed... and with it, the plague.", 120);
            typeText("Morthga breathes once more because of you.", 120);
            typeText("Your name shall echo through its restored halls.", 120);
            typeText("Farewell, Hero " + hero.getName() + ".", 120);
            typeText("Until the next descent.", 120);
            System.out.println();

            printVictoryScreen();
        }

        sc.close();
    }

    // --- HELPER METHOD: CHEST SYSTEM ---
    // Contains the logic for RNG rewards. Demonstrates probability handling.
    public static void findChest(Player p) {
        Random rand = new Random();
        int chance = rand.nextInt(100);

        System.out.println("-----------------------------------");
        System.out.println("You found a Mysterious Chest!");

        // 1. LEGENDARY (0 to 9) -> 10% Chance
        if (chance < 10) {
            typeText("...The chest radiates with forgotten divinity.", 75);
            typeText("You claim a LEGENDARY weapon, forged before the plague.", 90);
            typeText("Power surges through your veins.", 100);
            p.upgradePower(20);
            
        // 2. EPIC / ANCIENT ARMOR (10 to 24) -> 15% Chance
        } else if (chance < 25) {
            typeText("You uncover ancient armor shards, still humming with sacred energy.", 75);
            typeText("Your body hardens against the dark.", 90);
            p.upgradeDefense(15);
            p.heal(10);
            
        // 3. RARE / TEMPERED IRON (25 to 44) -> 20% Chance
        } else if (chance < 45) {
            typeText("Inside lies tempered iron: blades and plates unmarred by rot.", 75);
            typeText("Reliable strength for the battles ahead.", 90);
            p.upgradePower(8);
            p.upgradeDefense(8);
            
        // 4. COMMON / VIAL (45 to 84) -> 40% Chance
        } else if (chance < 85) {
            typeText("The chest holds a simple vial of red vitality.", 75);
            typeText("Life returns to you, if only for a moment.", 90);
            p.heal(30);
            
        // 5. MIMIC / TRAP (85 to 99) -> 15% Chance
        } else {
            typeText("The chest shudders… then bites!", 75);
            typeText("A Mimic tears into your arm before retreating into the dark!", 90);
            p.takeDamage(15);
        }
    }

    // --- HELPER METHOD: TYPEWRITER EFFECT ---
    // Uses Thread.sleep() to print characters one by one.
    // Adds polish and immersion to the console output.
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

    // --- HELPER METHOD: VICTORY SCREEN ---
    // Prints the ASCII art line-by-line for a "Scanline" effect.
    public static void printVictoryScreen() {
        System.out.println("\n\n");
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

        for (String line : victoryArt) {
            System.out.println(line);
            try {
                Thread.sleep(60);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        try { Thread.sleep(500); } catch (Exception e) {}

        typeText("The shadows recede...", 150);
        typeText("Those who forged this tale:", 150);
        typeText("- Paala, Luke Andre", 75);
        typeText("- Caraig, Hans Gadiel", 75);
        typeText("- Fajiculay, Cedric", 75);
        System.out.println("\nThank you for braving the Plague of Danjin.");
    }
}