<h1 align = "center">𐔌 .⋮ Plague of Danjin  .ᐟ  ֹ   ₊ ꒱</h1>
<h3 align = "center">A turn-based Java Console RPG with Skills, Mana, and Boss Battles.</h3>
<p align = "center">
<b>CS 2105 </b> <br/>
Paala, Luke Andre <br/>
Caraig, Hans Gadiel <br/>
Fajiculay, John Cedric
</p>

## ‧₊˚ ┊ Overview
Plague of Danjin is a console-based Java RPG where the player controls a hero fighting through **20 progressive waves** of enemies to cleanse the land of corruption.
<br/><br/>

## ‧₊˚ ┊ OOP Concepts
### a. Encapsulation
Data integrity is maintained by keeping attributes private.
- **Implementation:** All attributes in `GameCharacter.java` (such as `hp`, `attackPower`, `defense`, `mana`) are `private`.
- **Access:** External classes interact with these variables only through public methods like `takeDamage()`, `heal()`, and `spendMana()`.

### b. Inheritance
The project uses a hierarchical structure to share code and define specific behaviors.
- **Implementation:** `GameCharacter` serves as the superclass. `Player` and `Enemy` inherit from it. Specific enemies like `Goblin`, `Skeleton`, and `Lich` further inherit from `Enemy`, creating a deep inheritance tree (Grandparent $\to$ Parent $\to$ Child).

### c. Polymorphism
The game uses Method Overriding to treat different objects as a generic type while executing specific behaviors.
- **Implementation:** The `attack()` method is defined abstractly in the parent.
    - `Player`: Overrides it to show a skills menu.
    - `Enemy`: Overrides it to perform random attacks.
    - `Lich`: Overrides it to implement complex boss logic (summoning minions).
- **Dynamic Usage:** The `GameMain` loop treats all opponents as `Enemy` objects, but they behave like Goblins, Skeletons, or Bosses at runtime.

### d. Abstraction
Abstract classes define the "blueprint" for all entities.
- **Implementation:** `GameCharacter` is an `abstract` class. It cannot be instantiated directly, forcing developers to create specific types of characters (like `Player` or `Goblin`) to implement the `abstract void attack()` method.

---
<br/>

### Players can:
⚔️ Perform basic and magic attacks  
🔥 Cast powerful spells using mana  
🛡️ Buff defense and regenerate mana  
🎯 Land critical hits  
📦 Open mysterious chests for upgrades  
👑 Defeat the Goblin King and the Necromancer Lich  

### Game Data Handling
💾 Player HP, defense, attack, and mana persist across all 20 waves.

---

## ‧₊˚ ┊ Project Structure
```
📂 src/
├── ☕ GameMain.java
├── ☕ GameCharacter.java
├── ☕ Player.java
├── ☕ Enemy.java
└── ☕ Enemy_Lich.java

```

- `GameMain.java` – Game loop, enemy phases, waves, chest system, and victory screen.  
- `GameCharacter.java` – Abstract parent class controlling stats, damage, healing, and mana.  
- `Player.java` – Player controls, skills system, mana management, and buffs.  
- `Enemy.java` – Standard enemy AI with random damage.  
- `Enemy_Lich.java` – Advanced boss enemy with minion summoning and passive attacks.  


## ‧₊˚ ┊ How to Run the Program

```
Open your terminal in the project src folder and run:

javac *.java

Run the game using:

java GameMain

```

## ‧₊˚ ┊ Features
1. **Turn-Based Combat System**
2. **20 Progressive Waves**
3. **Two Boss Battles**
   - Goblin King (Wave 10)
   - Necromancer Lich (Wave 20)
4. **Mana & Skill System**
   - Fireball (3x Damage)
   - Holy Light (Heal)
   - Iron Will (Defense Buff)
5. **Skeleton Minion System (Lich Boss)**
6. **Critical Hit System (15%)**
7. **Dynamic Defense & Damage Reduction**
8. **Chest & Loot Rewards**
9. **Auto Heal +5 After Every Kill**
10. **Animated Text & Victory Screen**

## ‧₊˚ ┊ Object-oriented Principles

- `💊 Encapsulation` 
   Data integrity is maintained by keeping attributes private to prevent invalid game states (e.g., negative HP).
    - **Implementation:** All attributes in `GameCharacter.java` (such as `hp`, `attackPower`, `defense`, `mana`) are `private`.
    - **Access:** External classes interact with these variables only through public methods like `takeDamage()`, `heal()`, and `spendMana()`.
<br/>

- `💡 Abstraction` - The `GameCharacter` class defines the abstract method: public abstract void attack(GameCharacter target);<br/>
  This forces all child classes to define their own attack styles.<br/>

- `🧬 Inheritance` - Player, Enemy, and Enemy_Lich all inherit from GameCharacter.<br/>
   Enemy_Lich also extends Enemy, forming a multi-level inheritance structure.<br/>
   
- `🎭 Polymorphism` -  The attack() method behaves differently based on the object:<br/>
  Player → user choices & skills<br/>
  Enemy → random attacks<br/>
  Enemy_Lich → summoning minions + passive damage<br/>
  Dynamic method dispatch is used at runtime.<br/>

## ‧₊˚ ┊ Enemy Phases

- `Phase 1` – Goblins (Waves 1–9)
  - Basic enemies with scaling HP and attack.
  
- `Boss 1` – Goblin King (Wave 10)
  - High HP, strong attack, increased defense.
  
- `Phase 2` – Skeleton Warriors (Waves 11–19)
  - Naturally armored enemies with higher stats.
  
- `Final Boss` – Necromancer Lich (Wave 20)
  - Summons skeleton minions every 3 turns
  - Minions deal passive damage every turn
  - Extremely high HP and strong defense

## ‧₊˚ ┊ Example Output

```

--- Your Turn (HP: 84 | MP: 55) ---
1. Basic Attack
2. Skills (Magic)
Choose an action: 2

--- Grimoire ---
1. Fireball (20 MP)
2. Holy Light (15 MP)
3. Iron Will (10 MP)
4. Back
Select Spell: 1

🔥 You cast FIREBALL! 🔥
Skeleton Warrior blocked 5 damage and took 85 damage!
Current HP: 12/140

```

## ‧₊˚ ┊ Victory Screen (Snippet)

```
  CONGRATULATIONS, traveler.
            The Corrupted Heart has been destroyed... and with it, the plague.
            Morthga breathes once more because of you.
            Your name shall echo through its restored halls. 
            Farewell, Hero Lan."
            Until the next descent.
################################################################################
#                                                                              #
#   ______  _                                       __                         #
#   | ___ \\| |                                     / _|                       #
#   | |_/ /| |  __ _   __ _  _   _   ___     ___  | |_                         #
#   |  __/ | | / _` | / _` || | | | / _ \\   / _ \\ |  _|                      #
#   | |    | || (_| || (_| || |_| ||  __/  | (_) || |                          #
#   \\_|    |_| \\__,_| \\__, | \\__,_| \\___|   \\___/ |_|                    #
#                      __/ |                                                   #
#                     |___/                                                    #
#    ______              _  _                                                  #
#    |  _  \\            (_)(_)                                                #
#    | | | |  __ _  _ __  _  _  _ __                                           #
#    | | | | / _` || '_ \\| || || '_ \\                                        #
#    | |/ / | (_| || | | || || || | | |                                        #
#    |___/   \\__,_||_| |_|| ||_||_| |_|                                       #
#                        _/ |                                                  #
#                       |__/                                                   #
#                                                                              #
################################################################################

```

## ‧₊˚ ┊ Authors and Acknowledgement
<table>
<tr>
<th> Name </th>
</tr>
<tr>
<td><strong>Paala, Luke Andre</strong></td>
</tr>
<tr>
<td><strong>Caraig, Hans Gadiel</strong></td>
</tr>
<tr>
<td><strong>Fajiculay, John Cedric</strong></td>
</tr>
</table>


# Acknowledgement: 
First of all, we would like to express our sincere gratitude to
our OOP teacher, Ma'am Fatima Marie Agdon, for teaching us and
providing the knowledge we needed to build this project. Through
her dedication and guidance, we were able to create a turn-based
game that reflects our interests—something we can proudly say
we made ourselves.

We would also like to dedicate this project to our Creator, for
granting us wisdom, strength, and perseverance throughout the
development process. Without His guidance, none of this would
have been possible.

To our friends and classmates, thank you for your ideas,
feedback, and moral support. Each of you contributed in your
own way to bringing this project to life.

Lastly, we acknowledge our own effort, patience, and teamwork.
This project strengthened both our technical skills and our collaboration
as a group.




## ‧₊˚ ┊ Future Enhancements
1. **Classes**
    - Classes with different archetypes such as Knight, Mage, Assassin, and etc.
2. **Graphic User Interface**
    - For the visual enhancement of the Player.
3. **More Enemies**
   - More Unique enemies and varrying stats.
4. **Items and INventory**
    - Improvement of the items and possibly an addition of an Inventory System.
5. **Lore and Area Expansion**
    - This project is open to explore more about the lore and have more areas to go through and fight.
6. **World Interaction**
    - Make the player able to explore around the dungeon and interact with npc's.
