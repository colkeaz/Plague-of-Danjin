<h1 align = "center">𐔌 .⋮ Plague of Danjin  .ᐟ  ֹ   ₊ ꒱</h1>
<h3 align = "center">A turn-based Java Console RPG Adventure.</h3>
<p align = "center">
<b>CS 2106 </b> <br/>
Paala, Luke Andre <br/>
Caraig, Hans Gadiel <br/>
Fajiculay, John Cedric
</p>

## ‧₊˚ ┊ Overview
Plague of Danjin is a console-based role-playing game developed in Java where the player controls a hero who must survive ten waves of enemies to end the plague threatening the kingdom.
<br/><br/>
The game showcases the practical application of Object-Oriented Programming (OOP) principles such as encapsulation, inheritance, abstraction, and polymorphism, combined with turn-based combat mechanics, random encounters, and progression-based difficulty.
<br/>
### Players can:
⚔️ Fight goblin enemies in turn-based combat  
🩹 Heal using potions  
🎯 Deal critical hits  
📈 Improve stats through loot chests  
👑 Defeat the Goblin King boss  

### Game Data Handling
💾 Player stats persist across waves and are updated dynamically during gameplay.

---

## ‧₊˚ ┊ Project Structure


- `GameMain.java` – Controls the game loop, waves, battles, and overall flow.
- `GameCharacter.java` – Abstract parent class defining shared attributes and methods.
- `Player.java` – Handles user input, attacks, healing, and critical hits.
- `Enemy.java` – Controls enemy AI and randomized attacks.

### How to Run the Program
Open your terminal in the project folder and run:


Then execute:



---

## ‧₊˚ ┊ Features
1. **Turn-Based Combat** – Player and enemies take turns attacking.
2. **Critical Hit System** – 10% chance to deal double damage.
3. **Healing System** – Randomized healing with potions.
4. **Progressive Waves** – Enemy stats scale every wave.
5. **Boss Battle** – Final fight against the Goblin King at Wave 10.
6. **Chest & Loot System** – Random upgrades and traps after waves.
7. **Defense System** – Damage reduction based on defense stat.
8. **Auto-Heal Per Kill** – Player heals +5 HP after every enemy defeat.
9. **Animated Text & Victory Screen** – Typewriter effect and ASCII art ending.

---

## ‧₊˚ ┊ Object-oriented Principles

### 💊 Encapsulation  
All attributes such as HP, Attack Power, and Defense are private inside the `GameCharacter` class. Access and modification are controlled using public methods like `takeDamage()`, `heal()`, and getters. This protects the integrity of character data.

### 💡 Abstraction  
The `GameCharacter` class defines the abstract method `attack()`, forcing subclasses to implement their own attack behavior. This hides implementation details while defining a common interface.

### 🧬 Inheritance  
Both `Player` and `Enemy` inherit from `GameCharacter`, allowing them to reuse shared properties and behaviors such as HP, damage handling, and healing.

### 🎭 Polymorphism  
The `attack()` method is overridden by both `Player` and `Enemy`, allowing different attack behaviors to be executed at runtime depending on the object type.

---

## ‧₊˚ ┊ Example Output



---

##  ‧₊˚ ┊ Victory Screen (Snippet)



---

##  ‧₊˚ ┊ Contributors

<table>
<tr>
    <th> &nbsp; </th>
    <th> Name </th>
    <th> Role </th>
</tr>
<tr>
    <td>⚔️</td>
    <td><strong>Paala, Luke Andre</strong></td>
    <td>Main Developer / Game Logic</td>
</tr>
<tr>
    <td>🛡️</td>
    <td><strong>Caraig, Hans Gadiel</strong></td>
    <td>System Design / Testing</td>
</tr>
<tr>
    <td>🎮</td>
    <td><strong>Fajiculay, John Cedric</strong></td>
    <td>Combat System & Balancing</td>
</tr>
</table>

---

##  ‧₊˚ ┊ Acknowledgment
We sincerely thank our instructor for guidance throughout the development of this project. We also appreciate our classmates for their feedback and encouragement during testing and refinement.

---

<small>
<b>DISCLAIMER</b><br/>
This project is created strictly for educational and academic purposes. It is intended to demonstrate Java programming and object-oriented design concepts.
</small>
