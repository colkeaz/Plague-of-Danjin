Plague of Danjin – Java Console RPG

A simple turn-based RPG project written in Java, showcasing Object-Oriented Programming concepts such as encapsulation, inheritance, abstraction, and polymorphism.

This game runs in the console and features:

* A Hero character controlled by the player
* Randomly generated enemies
* A boss battle at Wave 10
* Turn-based combat system (Attack / Heal)
* Progressive waves with healing after each victory


Features

1. Object-Oriented Design

* GameCharacter (Abstract Class)
  Defines the general blueprint for all characters (HP, attack, healing).

* Player (Subclass)
  Handles user actions, attack choice, healing, and critical hits.

* Enemy (Subclass)
  Contains simple AI that performs random-variation attacks.


Gameplay

* Enter your hero’s name.
* Survive 10 waves of enemies.
* Each normal wave has 1–2 Goblin Grunts.
* The final wave pits you against the Goblin King (Boss).
* After defeating an enemy, the player automatically heals for +5 HP.
* If the player’s HP reaches 0 → GAME OVER.


Player Actions

During your turn, choose:

1 — Attack

* Deals your base attack power (15).
* 10% chance to land a Critical Hit, doubling damage.

2 — Heal

Drinks a potion to restore 15–25 HP (random).

Other Input

* If invalid, the player panics and skips the turn.


File Structure

 ├── Enemy.java           // Enemy AI logic
 |
 ├── GameCharacter.java   // Abstract parent class
 |
 ├── GameMain.java        // Main game loop
 |
 ├── Player.java          // Player behavior + UI input
 


How the Waves Work

* The game loops from wave 1 to wave 10.
* Each wave generates either:

  * 1–2 random enemies, OR
  * 1 boss on wave 10.
    
* Player HP persists between waves, so resource management matters.
* After every victory, you automatically heal +5 HP.



OOP Concepts Used

Concept             How It Is Used                                                                              
  
Encapsulation       Attributes like HP, attackPower are private and accessed through methods.                   
Inheritance         Player and Enemy inherit from GameCharacter.                                                
Abstraction         The abstract `attack()` method forces subclasses to implement their own style of attacking. 
Polymorphism        Player overrides `attack()` differently than Enemy.      

How to run this project:
1. Make sure you have Visual Studio Code or any Java compiler.
2. Run the main file:
   GameMain.java

Developers

* Paala, Luke Andre
* Caraig, Hans Gadiel
* Fajiculay, John Cedric

