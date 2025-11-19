import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("Welcome to the OOP Arena!");
        System.out.print("Enter your Hero's name: ");
        String name = sc.nextLine();

        // Create Objects
        Player hero = new Player(name);
        Enemy monster = new Enemy("Goblin King", 80, 10);

        System.out.println("A wild " + monster.getName() + " appears!");

        // THE GAME LOOP
        while (hero.isAlive() && monster.isAlive()) {
            // 1. Hero attacks Monster
            hero.attack(monster);

            // Check if monster died before it can attack back
            if (!monster.isAlive()) {
                System.out.println("VICTORY! You defeated the " + monster.getName());
                break;
            }

            // 2. Monster attacks Hero
            monster.attack(hero);

            // Check if hero died
            if (!hero.isAlive()) {
                System.out.println("GAME OVER. You have fallen.");
                break;
            }
            
            // Formatting line
            System.out.println("-------------------------------");
        }
        sc.close();
    }
}