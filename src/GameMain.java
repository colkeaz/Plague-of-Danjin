import java.util.Random;
import java.util.Scanner;

public class GameMain {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Random rand = new Random();

        System.out.println();
        System.out.println();
        typeText("Welcome, traveler... to the Plague of Danjin.", 100);
        typeText("Before you descend into the depths, speak your name, that it may be remembered.", 75);
        System.out.println();
        System.out.print("Enter your name: ");
        String name = sc.nextLine();
        System.out.println();
        
        // 1. Create Hero OUTSIDE the loop so HP persists between waves
        Player hero = new Player(name);

        typeText("A sturdy soul you are, " + hero.getName() + ".", 100);
        typeText("The kingdom of Morthga is withering under a cruel plague.", 100);
        typeText("All signs point to Danjin, the forgotten dungeon where corruption festers.", 100);
        typeText("If salvation still exists, it lies below.", 100);
        typeText("Steel yourself... your descent begins now.", 80);

        // --- WAVE LOOP (Runs 1 to 20) ---
        for (int wave = 1; wave <= 20; wave++) {

            // Check if Player is dead before starting a wave
            if (!hero.isAlive()) break;

            System.out.println("\n========================================");
            typeText("   --- YOU DESCEND INTO THE ABYSS ---", 25);
            typeText("               WAVE " + wave, 25);
            System.out.println("========================================\n");

            // 2. Determine enemies for this wave
            int numEnemies;
            if (wave == 10 || wave == 20) {
                numEnemies = 1; // Boss Waves
            } else if (wave >= 15) {
                numEnemies = rand.nextInt(2) + 2; // 2 or 3 Enemies (Hardest)
            } else if (wave >= 5) {
                numEnemies = rand.nextInt(2) + 1; // 1 or 2 Enemies
            } else {
                numEnemies = 1; // Easy start
            }

            // --- ENEMY LOOP (Sequential fights in one wave) ---
            for (int i = 0; i < numEnemies; i++) {

                // Check death again in case died in previous fight of same wave
                if (!hero.isAlive()) break;

                //  Spawn the correct enemy
                Enemy currentEnemy;

                if (wave < 10) {
                    // PHASE 1: GOBLINS (Waves 1-9)
                    int hp = 40 + (wave * 10);
                    int atk = 8 + (wave * 2);
                    currentEnemy = new Enemy("Goblin Grunt", hp, atk, 0);

                    typeText("A twisted Goblin Grunt claws its way from the dark. (" + (i + 1) + "/" + numEnemies + ")", 25);
                    typeText("Its hunger is older than light.", 75);
                    typeText("(Stats -> HP: " + currentEnemy.getHp() + " | ATK: " + currentEnemy.getAttackPower() + ")", 25);

                } else if (wave == 10) {
                    // BOSS 1: GOBLIN KING
                    typeText("⚠ A vile presence emerges...", 100);
                    typeText("The Goblin King stands before you... swollen with plague and rage. ⚠", 120);
                    typeText("Raise your weapon. This will be a tough battle.", 120);

                    currentEnemy = new Enemy("Goblin King", 150, 20, 15);

                } else if (wave < 20) {
                    // PHASE 2: SKELETONS (Waves 11-19)
                    int scale = wave - 10; // Reset scaling for Phase 2
                    int hp = 80 + (scale * 12);
                    int atk = 15 + (scale * 3);
                    int def = 5 + (scale * 1);
                    currentEnemy = new Enemy("Skeleton Warrior", hp, atk, def);

                    typeText("A Skeleton Warrior drags itself together from scattered bones. (" + (i + 1) + "/" + numEnemies + ")", 25);
                    typeText("Armor fused to bone... a knight long forgotten.", 75);
                    typeText("(Stats -> HP: " + currentEnemy.getHp()
                            + " | ATK: " + currentEnemy.getAttackPower()
                            + " | DEF: " + currentEnemy.getDefense() + ")", 25);

                } else {
                    // BOSS 2: THE LICH (Wave 20)
                    typeText("⚠ The air freezes...", 100);
                    typeText("A voice older than the plague itself whispers your name...", 120);
                    typeText("The Lich descends from the void. ⚠", 120);
                    typeText("You are at the end, traveler...", 120);
                    typeText("Every heartbeat echoes in the void.", 120);
                    typeText("Hope feels distant... yet this pestilence must be purged.", 120);
                    typeText("Steel your resolve, for the darkness itself is before you... watching.", 75);

                    currentEnemy = new EnemyLich();
                }

                // --- BATTLE LOOP (The Fight) ---
                while (hero.isAlive() && currentEnemy.isAlive()) {

                    // Hero Attacks
                    hero.attack(currentEnemy);

                    // Check if enemy died
                    if (!currentEnemy.isAlive()) {
                        typeText(">> The " + currentEnemy.getName() + " collapses into dust and silence.", 25);
                        typeText("A faint warmth returns to your body... +5 HP.", 25);
                        hero.heal(5);
                        break;
                    }

                    // Enemy Attacks
                    currentEnemy.attack(hero);

                    // Check if hero died
                    if (!hero.isAlive()) {
                        typeText(">> Your strength fades...", 150);
                        typeText("The darkness of Danjin claims another soul.", 150);
                        typeText("GAME OVER.", 150);
                        break;
                    }

                    System.out.println("-------------------------------");
                }

            }
            // --- POST-WAVE LOGIC (Chest & Pause) ---
            if (hero.isAlive()) {

                if (wave < 20) {
                    if (rand.nextInt(100) < 50) findChest(hero);
                    System.out.println("Press Enter to start the next wave...");
                    sc.nextLine();
                } else {
                    System.out.println("Press Enter to continue...");
                    sc.nextLine();
                }
            }
        }

        // Game End Message
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

    public static void findChest(Player p) {
        Random rand = new Random();
        int chance = rand.nextInt(100);

        System.out.println("-----------------------------------");
        System.out.println("You found a Mysterious Chest!");

        if (chance < 10) {
            typeText("...The chest radiates with forgotten divinity.", 75);
            typeText("You claim a LEGENDARY weapon, forged before the plague.", 90);
            typeText("Power surges through your veins.", 100);
            p.upgradePower(20);
        } else if (chance < 25) {
            typeText("You uncover ancient armor shards, still humming with sacred energy.", 75);
            typeText("Your body hardens against the dark.", 90);
            p.upgradeDefense(15);
            p.heal(10);
        } else if (chance < 45) {
            typeText("Inside lies tempered iron: blades and plates unmarred by rot.", 75);
            typeText("Reliable strength for the battles ahead.", 90);
            p.upgradePower(8);
            p.upgradeDefense(8);
        } else if (chance < 85) {
            typeText("The chest holds a simple vial of red vitality.", 75);
            typeText("Life returns to you, if only for a moment.", 90);
            p.heal(30);
        } else {
            typeText("The chest shudders… then bites!", 75);
            typeText("A Mimic tears into your arm before retreating into the dark!", 90);
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
            "#    ______              _  _                                                  #",
            "#    |  _  \\            (_)(_)                                                #",
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
