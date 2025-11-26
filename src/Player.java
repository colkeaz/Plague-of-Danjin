import java.util.Random;
import java.util.Scanner;

public class Player extends GameCharacter {

    public Player(String name) {
        // Start with fixed stats: e.g., 100 HP, 15 Attack
        super(name, 100, 30, 15); 
    }

    // POLYMORPHISM: We are defining exactly how a Player attacks
    @Override
    public void attack(GameCharacter target) {
        Scanner sc = new Scanner(System.in);

        // 1. REGEN MANA AT START OF TURN
        // We do this first so the player sees the updated amount in the menu
        this.regenMana(10); // Regenerate 10 MP each turn

        boolean turnOver = false;

        // 2. THE LOOP (Keeps menu open until an action is actually taken)
        while (!turnOver) {
            System.out.println();
            System.out.println("+++++++++++++++++++++++++++++++++");
            // Show HP and MP so they know what they can afford
            System.out.println("--- Your Turn (HP: " + this.getHp() + " | MP: " + this.getMana() + ") ---");
            System.out.println("1. Basic Attack");
            System.out.println("2. Skills (Magic)");
            System.out.print("Choose an action: ");
            
            if (sc.hasNextInt()) {
                int choice = sc.nextInt();

                if (choice == 1) {
                    // --- OPTION 1: BASIC ATTACK (Your existing logic) ---
                    int damageDealt = this.getAttackPower();
                    Random rand = new Random(); 
                    System.out.println();
                    
                    if (rand.nextInt(100) < 15) { // Adjusted to 15% Crit
                        damageDealt *= 2;
                        System.out.println("*** CRITICAL HIT! ***");
                    }
                    
                    System.out.println("You struck the enemy!");
                    target.takeDamage(damageDealt);
                    
                    // END THE TURN
                    turnOver = true;

                } else if (choice == 2) {
                    // --- OPTION 2: SKILLS SUB-MENU ---
                    System.out.println("\n--- Grimoire ---");
                    System.out.println("1. Fireball (20 MP)   - 3x Damage");
                    System.out.println("2. Holy Light (15 MP) - Heal HP");
                    System.out.println("3. Iron Will (10 MP)  - Buff Defense");
                    System.out.println("4. Back"); // Important!
                    System.out.print("Select Spell: ");
                    
                    int spell = sc.nextInt();

                    if (spell == 1) {
                        // FIREBALL
                        if (this.spendMana(20)) {
                            System.out.println("🔥 You cast FIREBALL! 🔥");
                            int magicDmg = this.getAttackPower() * 3;
                            target.takeDamage(magicDmg);
                            turnOver = true;
                        }
                    } else if (spell == 2) {
                        // HEAL (Moved your old Heal logic here)
                        if (this.spendMana(15)) {
                            Random rand = new Random();
                            int bonusHeal = rand.nextInt(11); 
                            int totalheal = 30 + bonusHeal; // Buffed slightly since it costs mana
                            
                            System.out.println("✨ You cast HOLY LIGHT! ✨");
                            this.heal(totalheal); 
                            turnOver = true;
                        }
                    } else if (spell == 3) {
                        // DEFENSE BUFF
                        if (this.spendMana(10)) {
                            System.out.println("🛡️ You cast IRON WILL! 🛡️");
                            this.upgradeDefense(5); // Adds +5 Defense permanently
                            turnOver = true;
                        }
                    } else if (spell == 4) {
                        // BACK BUTTON
                        System.out.println("Returning to main menu...");
                        // We do NOT set turnOver = true, so the loop repeats!
                    } else {
                        System.out.println("Invalid Spell!");
                    }

                } else {
                    System.out.println("Invalid choice!");
                }
            } else {
                System.out.println("Invalid Input!");
                sc.next(); // Clear inputs
            }
        }
    }
    
}
