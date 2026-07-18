<h1 align = "center">𐔌 .⋮ Plague of Danjin  .ᐟ  ֹ   ₊ ꒱</h1>
<h3 align = "center">A graphical 8-bit roguelike dungeon crawler built with Java and libGDX.</h3>
<p align = "center">


## ‧₊˚ ┊ Overview
Plague of Danjin is a graphical 8-bit roguelike dungeon crawler built with Java and libGDX. The player descends into the Dungeon of Danjin to cleanse the kingdom of Morthga from a spreading plague. Features non-linear exploration, 3 character classes, turn-based combat with elemental systems, and procedural pixel art.
<br/><br/>

## ‧₊˚ ┊ Object-oriented Principles

### 💊 Encapsulation
Data integrity is maintained by keeping attributes private.
- **Implementation:** All attributes in `GameCharacter.java` (such as `hp`, `attackPower`, `defense`, `mana`) are `private`.
- **Access:** External classes interact with these variables only through public methods like `takeDamage()`, `heal()`, and `spendMana()`.

### 🧬 Inheritance
The project uses a hierarchical structure to share code and define specific behaviors.
- **Implementation:** `GameCharacter` serves as the superclass. `Player` and `Enemy` inherit from it. Specific enemies like `Goblin`, `Skeleton`, and `Lich` further inherit from `Enemy`, creating a deep inheritance tree (Grandparent $\to$ Parent $\to$ Child).

### 🎭 Polymorphism
The game uses Method Overriding to treat different objects as a generic type while executing specific behaviors.
- **Implementation:** The `attack()` method is defined abstractly in the parent.
    - `Player`: Overrides it to show a skills menu.
    - `Enemy`: Overrides it to perform random attacks.
    - `GoblinKing`: Overrides it to trigger a "Rage" buff before attacking (Infinite Scaling).
    - `Lich`: Overrides it to implement complex boss logic (summoning minions).
- **Dynamic Usage:** The `GameMain` loop treats all opponents as `Enemy` objects, but they behave like Goblins, Skeletons, or Bosses at runtime.

### 💡 Abstraction
Abstract classes define the "blueprint" for all entities.
- **Implementation:** `GameCharacter` is an `abstract` class. It cannot be instantiated directly, forcing developers to create specific types of characters (like `Player` or `Goblin`) to implement the `abstract void attack()` method.

---
<br/>

## ‧₊˚ ┊ Features
1. **Turn-Based Combat System** with Elemental Damage (Fire, Holy, Dark, Physical, Poison)
2. **3 Character Classes** — Knight (tank), Mage (glass cannon), Rogue (crit/poison)
3. **Non-Linear World Map** — 4 branching dungeon areas explorable in any order
4. **Story Mode + Classic Mode** (original 20-wave gauntlet preserved)
5. **39 Unique Skills** with cooldowns and class-specific milestone unlocks
6. **Items & Equipment** — 20+ items across 3 slots (Weapon, Armor, Accessory)
7. **8 Enemy Types** with unique mechanics (telegraphs, shields, poison, minions, rage)
8. **4 Boss Encounters** with QTE (Quick-Time Event) phases
9. **Status Effects** — Poison, Regen, Shield, Stun, Enrage, Curse
10. **Procedural 8-Bit Audio** — Chiptune BGM and 18 SFX generated from waveforms
11. **Save System** with Meta-Progression and 6 Unlockable Starting Bonuses
12. **Event Rooms** with moral choices affecting 3 possible story endings
13. **Pixel-Perfect 8-Bit Rendering** (320×240, 3× scaled to 960×720)
14. **Dynamic Difficulty** — Danjin's Curse escalates danger over time

---

## ‧₊˚ ┊ Game Modes

### 🗺️ Story Mode
Non-linear dungeon exploration. Explore the Goblin Warrens, Bone Cathedral, and Plague Gardens in any order. Collect 3 keys to face the Lich. Choices affect which of 3 endings you receive.

### ⚔️ Classic Mode
The original 20-wave gauntlet — fight through progressive waves of increasing difficulty.

---

## ‧₊˚ ┊ Character Classes

| Class | HP | Specialty | Passive |
|-------|-----|-----------|---------|
| 🛡️ **Knight** | 130 HP, 25 DEF | Shield Slam, Rally, Fortress | Thick Skin (10% damage reduction) |
| 🔮 **Mage** | High MP (120) | Fireball, Ice Shard, Arcane Shield | Arcane Affinity (-3 MP costs, +5 mana regen) |
| 🗡️ **Rogue** | High Crit (25%) | Poison Strike, Shadow Step, Backstab | Keen Edge (2.5× crit damage) |

---

## ‧₊˚ ┊ World Map

```
              ┌────────────────────┐
              │   THE LICH'S THRONE │ (Requires 3 Keys)
              └────────┬───────────┘
                       │
              ┌────────┴───────────┐
              │   DANJIN'S CORE    │ (Central Hub)
              └─┬──────┬──────┬───┘
                │      │      │
     [GOBLIN WARRENS] [BONE CATHEDRAL] [PLAGUE GARDENS]
       Boss: GobKing    Boss: Colossus   Boss: Thornmother
```

---

## ‧₊˚ ┊ Enemy Types

| Enemy | Element | Mechanic |
|-------|---------|----------|
| Goblin Grunt | Physical | Scaling melee attacker |
| Plague Goblin | Poison | Poison on hit, toxic aura |
| Goblin King | Physical | Boss, rage mechanic +2 ATK/turn |
| Goblin Chieftain | Physical | Mini-boss, war cry buff |
| Skeleton Warrior | Dark | Armored undead |
| Shielded Skeleton | Dark | Blocks first hit per turn |
| Bone Colossus | Dark | Mini-boss, reflects damage on shield break |
| Necromancer Lich | Dark | Final boss, summons minions, 3 QTE phases |
| Plague Elemental | Poison | Double poison stacks |
| Thornmother | Poison | Summons vine minions, self-heals |

---

## ‧₊˚ ┊ Architecture (MVC)

- **Model:** Pure game logic with zero I/O. Communicates via an event-driven system (Observer pattern).
- **Controller:** Non-blocking state machines. CombatEngine advances one step per call — designed for 60 FPS integration.
- **View:** libGDX rendering layer. Completely replaceable without touching game logic.

---

## ‧₊˚ ┊ Project Structure
```
src/
├── model/                  (Game Logic — Zero I/O)
│   ├── GameCharacter.java, Player.java, Enemy.java
│   ├── CharacterClass.java, ClassAbility.java, ClassSkillTree.java
│   ├── PlayerAction.java, SaveData.java
│   ├── items/             (Item, Inventory, ItemRegistry — 20+ items)
│   ├── skills/            (Element, Skill, SkillTree — 39 skills)
│   ├── status/            (StatusManager — 6 effect types)
│   ├── enemies/           (8 enemy types + telegraph system)
│   ├── events/            (GameEvent system — 35+ event types)
│   └── world/             (Area, Encounter, WorldState — dungeon map)
├── controller/            (State Machines & Game Flow)
│   ├── CombatEngine.java, WaveManager.java, WorldManager.java
│   ├── QTEManager.java, SaveManager.java, MetaProgression.java
│   ├── ChestSystem.java, EventRoomManager.java, RunModifiers.java
│   └── AreaEncounterGenerator.java, GameState.java
└── view/                  (libGDX Rendering — Replaceable)
    ├── PlagueOfDanjinGame.java, DesktopLauncher.java
    ├── screens/           (9 screens: WorldMap, Combat, QTE, etc.)
    ├── sprites/           (Pixmap pixel-art generator, animations)
    ├── effects/           (Particles, damage numbers, screen shake)
    ├── audio/             (Procedural chiptune SFX + BGM)
    ├── ui/                (HUD, CombatMenu, MessageLog)
    └── rendering/         (PixelRenderer — 320×240 FitViewport)
```

87 Java files across 17 packages.

---

## ‧₊˚ ┊ Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 17 |
| Framework | libGDX 1.12.1 (LWJGL3 desktop backend) |
| Build | Gradle 8.11 |
| Rendering | SpriteBatch + Pixmap (procedural pixel art) |
| Audio | Procedural chiptune (square/triangle/sawtooth waveforms at 22050 Hz) |
| Resolution | 320×240 internal, 960×720 window (nearest-neighbor scaling) |

---

## ‧₊˚ ┊ How to Run

### Quick Start (Recommended)
```bash
# Clone the repository
git clone https://github.com/colkeaz/Plague-of-Danjin.git
cd Plague-of-Danjin

# Run the setup script (detects/installs prerequisites automatically)
./setup.sh          # Linux/Mac
setup.bat           # Windows

# Play the game
./gradlew run
```

### Prerequisites
- **Java 17+** — The setup script will detect and guide installation if needed
- **Gradle** — Included via wrapper (no manual install required)

### Manual Setup
If you prefer to set things up manually:
1. Install Java 17 or higher from [Adoptium](https://adoptium.net/temurin/releases/)
2. Set `JAVA_HOME` to your JDK installation path
3. Build: `./gradlew classes`
4. Run: `./gradlew run`

---

## ‧₊˚ ┊ Authors and Acknowledgement
<table>
<tr>
<th> Authors </th>
</tr>
<tr>
<td><strong>Paala, Luke Andre V.</strong></td>
</tr>
<tr>
<td><strong>Caraig, Hans Gadiel P.</strong></td>
</tr>
<tr>
<td><strong>Fajiculay, John Cedric F.</strong></td>
</tr>
</table>


### Acknowledgement: 
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
