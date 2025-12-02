import java.util.Random;
import java.util.Scanner;

public class Player extends GameCharacter {

    public Player(String name) {
        // Start with fixed stats: e.g., 100 HP, 30 Attack, 15 Defense
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
            
            // MAIN MENU TRY-CATCH
            try {
                // Read input as string first to prevent Scanner glitches
                String input = sc.next(); 
                int choice = Integer.parseInt(input);

                // 2. Determine Logic based on Number
                if (choice == 1) {
                    // --- OPTION 1: BASIC ATTACK ---
                    int damageDealt = this.getAttackPower();
                    Random rand = new Random(); 
                    System.out.println();
                    
                    if (rand.nextInt(100) < 15) { 
                        damageDealt *= 2;
                        System.out.println("*** CRITICAL HIT! ***");
                    }
                    
                    System.out.println("You struck the enemy!");
                    target.takeDamage(damageDealt);
                    turnOver = true; // Valid move, end turn

                } else if (choice == 2) {
                    // --- OPTION 2: SKILLS SUB-MENU ---
                    System.out.println("\n--- Grimoire ---");
                    System.out.println("1. Fireball (20 MP)   - 3x Damage");
                    System.out.println("2. Holy Light (15 MP) - Heal HP");
                    System.out.println("3. Iron Will (10 MP)  - Buff Defense");
                    System.out.println("4. Back");
                    System.out.print("Select Spell: ");
                    
                    // NESTED TRY-CATCH: Handles invalid input inside the skill menu
                    try {
                        String spellInput = sc.next();
                        int spell = Integer.parseInt(spellInput);

                        if (spell == 1) {
                            if (this.spendMana(20)) {
                                System.out.println("[!!] You cast FIREBALL! [!!]");
                                target.takeDamage(this.getAttackPower() * 3);
                                turnOver = true;
                            }
                        } else if (spell == 2) {
                            if (this.spendMana(15)) {
                                System.out.println("*~* You cast HOLY LIGHT! *~*");
                                this.heal(30); 
                                turnOver = true;
                            }
                        } else if (spell == 3) {
                            if (this.spendMana(10)) {
                                System.out.println("[O] You cast IRON WILL! [O]");
                                this.upgradeDefense(5);
                                turnOver = true;
                            }
                        } else if (spell == 4) {
                            System.out.println("Returning to main menu...");
                            // turnOver stays FALSE, so it loops back to start
                        } else {
                            System.out.println("Invalid Spell Selection!");
                        }

                    } catch (NumberFormatException e) {
                        System.out.println(">> Invalid Spell Input! Please enter a number.");
                    }

                } else {
                    System.out.println("Invalid choice! Please select 1 or 2.");
                }

            } catch (NumberFormatException e) {
                // THIS IS THE CATCH BLOCK FOR THE MAIN MENU
                // If they typed "hello" instead of "1", the code jumps here.
                System.out.println(">> Invalid Input! Please enter a number.");
                // turnOver is still FALSE, so the loop repeats automatically!
            } catch (Exception e) {
                 // Catch-all for other errors
                 System.out.println(">> Something went wrong. Try again.");
                 sc.nextLine(); // Clear scanner buffer just in case
            }
        }
    }
}