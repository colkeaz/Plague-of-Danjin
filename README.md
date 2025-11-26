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
The game applies **Object-Oriented Programming (OOP)** principles such as encapsulation, inheritance, abstraction, and polymorphism, while integrating **turn-based combat, mana-based skills, scaling difficulty, loot rewards, and multi-phase boss battles**.
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

📂 src/
├── ☕ GameMain.java
├── ☕ GameCharacter.java
├── ☕ Player.java
├── ☕ Enemy.java
└── ☕ Enemy_Lich.java


- `GameMain.java` – Game loop, enemy phases, waves, chest system, and victory screen.  
- `GameCharacter.java` – Abstract parent class controlling stats, damage, healing, and mana.  
- `Player.java` – Player controls, skills system, mana management, and buffs.  
- `Enemy.java` – Standard enemy AI with random damage.  
- `Enemy_Lich.java` – Advanced boss enemy with minion summoning and passive attacks.  

---

## ‧₊˚ ┊ How to Run the Program
Open your terminal in the project src folder and run:

javac *.java

Run the game using:

java GameMain

---

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

---

## ‧₊˚ ┊ Object-oriented Principles

💊 Encapsulation
All attributes such as `HP`, `attackPower`, `defense`, and `mana` are private in `GameCharacter`. Controlled access is done using getters and methods like `takeDamage()`, `heal()`, and `spendMana()`.

💡 Abstraction
The `GameCharacter` class defines the abstract method:

public abstract void attack(GameCharacter target);

This forces all child classes to define their own attack styles.

🧬 Inheritance
Player, Enemy, and Enemy_Lich all inherit from GameCharacter.
 Enemy_Lich also extends Enemy, forming a multi-level inheritance structure.

🎭 Polymorphism
The attack() method behaves differently based on the object:
Player → user choices & skills


Enemy → random attacks


Enemy_Lich → summoning minions + passive damage


Dynamic method dispatch is used at runtime.

‧₊˚ ┊ Enemy Phases
Phase 1 – Goblins (Waves 1–9)
Basic enemies with scaling HP and attack.
Boss 1 – Goblin King (Wave 10)
High HP, strong attack, increased defense.
Phase 2 – Skeleton Warriors (Waves 11–19)
Naturally armored enemies with higher stats.
Final Boss – Necromancer Lich (Wave 20)
☠️ Summons skeleton minions every 3 turns
☠️ Minions deal passive damage every turn
☠️ Extremely high HP and strong defense

‧₊˚ ┊ Example Output

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

‧₊˚ ┊ Victory Screen (Snippet)
################################################################################
#                                                                              #
#   ______  _                                       __                         #
#   | ___ \| |                                     / _|                        #
#                                                                              #
################################################################################

## ‧₊˚ ┊ Contributors
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
