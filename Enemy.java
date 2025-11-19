import java.util.Random;

public class Enemy extends GameCharacter {

    public Enemy(String name, int hp, int attackPower) {
        super(name, hp, attackPower);
    }

    @Override
    public void attack(GameCharacter target) {
        System.out.println("--- Enemy Turn ---");
        
        // Simple AI: The enemy just attacks randomly
        Random rand = new Random();
        int damageVar = rand.nextInt(5); // Random variation of 0-4
        int totalDamage = this.getAttackPower() + damageVar;
        
        System.out.println(this.getName() + " attacks you savagely!");
        target.takeDamage(totalDamage);
    }
}