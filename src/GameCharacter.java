public abstract class GameCharacter {
    // ENCAPSULATION: Fields are private so they can't be messed with directly
    private String name;
    private int hp;
    private int maxHp;
    private int attackPower;
    private int defense;
    private int mana;
    private int maxMana;

    public GameCharacter(String name, int maxHp, int attackPower, int defense) {
        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp; // Start at full health
        this.attackPower = attackPower;
        this.defense = defense;

        this.maxMana = 100; // Default max mana
        this.mana = 75;      // Start with some mana
    }

    // ABSTRACTION: We know characters attack, but we don't know HOW they attack yet. The children must decide.
    public abstract void attack(GameCharacter target);

    // Getters and Setters (Controlled access)
    public String getName() { return name; }
    
    public int getHp() { return hp; }

    public void takeDamage(int damage) {
       // 1. Calculate reduction (e.g., 15 becomes 0.15)
        double reductionPercentage = this.defense / 100.0;
        
        // 2. Calculate how much damage is blocked
        double blockedAmount = damage * reductionPercentage;
        
        // 3. Subtract blocked amount from incoming damage
        int finalDamage = (int)(damage - blockedAmount);
        
        // Safety check: Damage cannot be negative
        if (finalDamage < 0) finalDamage = 0;

        this.hp -= finalDamage;
        if (this.hp < 0) this.hp = 0;

        // Visual Feedback
        System.out.println(this.name + " blocked " + (int)blockedAmount + " damage and took " + finalDamage + " damage!");
        System.out.println("Current HP: " + this.hp + "/" + this.maxHp);
    }
    
    public boolean isAlive() {
        return this.hp > 0;
    }
    
    public int getAttackPower() { 
        return attackPower; 
    }
    
    public int getDefense() { 
        return defense; 
    }    
    
    public void heal(int amount) {
        this.hp += amount;
        if (this.hp > this.maxHp) {
            this.hp = this.maxHp;
        }
        System.out.println(this.name + " healed for " + amount + " HP!");
        System.out.println("Current HP: " + this.hp + "/" + this.maxHp);
    }

    public void upgradePower(int amount) {
        this.attackPower += amount;
        System.out.println(this.name + "'s Attack Power increased by " + amount + "!");
        System.out.println("Current Attack: " + this.attackPower);
    }

    public void upgradeDefense(int amount) {
        this.defense += amount;
        // Cap defense at 75 so they don't become invincible
        if (this.defense > 75) this.defense = 75; 
        System.out.println(this.name + "'s Defense hardened by " + amount + "!");
    }

    public int getMana() { return mana; }

    public boolean spendMana(int cost) {
        if (this.mana >= cost) {
            this.mana -= cost;
            return true; // Success!
        }
        System.out.println(">> Not enough Mana! (Need " + cost + ", Have " + this.mana + ")");
        return false; // Failed
    }

    public void regenMana(int amount) {
        this.mana += amount;
        if (this.mana > maxMana) this.mana = maxMana;
    }
}