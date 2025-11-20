public abstract class GameCharacter {
    // ENCAPSULATION: Fields are private so they can't be messed with directly
    private String name;
    private int hp;
    private int maxHp;
    private int attackPower;

    public GameCharacter(String name, int maxHp, int attackPower) {
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp; // Start at full health
        this.attackPower = attackPower;
    }

    // ABSTRACTION: This method is abstract. We know characters attack,
    // but we don't know HOW they attack yet. The children must decide.
    public abstract void attack(GameCharacter target);

    // Getters and Setters (Controlled access)
    public String getName() { return name; }
    
    public int getHp() { return hp; }

    public void takeDamage(int damage) {
        this.hp -= damage;
        if (this.hp < 0) {
            this.hp = 0;
        }
        System.out.println(this.name + " took " + damage + " damage!");
        System.out.println("Current HP: " + this.hp + "/" + this.maxHp);
    }
    
    public boolean isAlive() {
        return this.hp > 0;
    }
    
    public int getAttackPower() { 
        return attackPower; }

    public void heal(int amount) {
        this.hp += amount;
        if (this.hp > this.maxHp) {
            this.hp = this.maxHp;
        }
        System.out.println(this.name + " healed for " + amount + " HP!");
        System.out.println("Current HP: " + this.hp + "/" + this.maxHp);
    }
}