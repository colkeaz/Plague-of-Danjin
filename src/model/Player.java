package model;

import java.util.Random;

import model.events.GameEvent;
import model.events.GameEventType;
import model.skills.Element;

public class Player extends GameCharacter {
    private final Random rand = new Random();

    public Player(String name) {
        // Start with fixed stats: 100 HP, 30 Attack, 15 Defense
        super(name, 100, 30, 15);
        setElement(Element.PHYSICAL);
    }

    /**
     * Execute a player action programmatically without any I/O.
     * Returns true if the action was successfully executed, false if it failed (e.g., insufficient mana).
     */
    public boolean executeAction(PlayerAction action, GameCharacter target) {
        switch (action) {
            case BASIC_ATTACK:
                performBasicAttack(target);
                return true;

            case FIREBALL:
                if (spendMana(20)) {
                    fireEvent(GameEvent.builder(GameEventType.SPELL_CAST)
                            .put("casterName", getName())
                            .put("spellName", "Fireball")
                            .put("manaCost", 20)
                            .build());
                    target.takeDamage(getAttackPower() * 3);
                    return true;
                }
                return false;

            case HOLY_LIGHT:
                if (spendMana(15)) {
                    fireEvent(GameEvent.builder(GameEventType.SPELL_CAST)
                            .put("casterName", getName())
                            .put("spellName", "Holy Light")
                            .put("manaCost", 15)
                            .build());
                    this.heal(30);
                    return true;
                }
                return false;

            case IRON_WILL:
                if (spendMana(10)) {
                    fireEvent(GameEvent.builder(GameEventType.SPELL_CAST)
                            .put("casterName", getName())
                            .put("spellName", "Iron Will")
                            .put("manaCost", 10)
                            .build());
                    this.upgradeDefense(5);
                    return true;
                }
                return false;

            default:
                return false;
        }
    }

    private void performBasicAttack(GameCharacter target) {
        int damageDealt = this.getAttackPower();

        boolean isCrit = rand.nextInt(100) < 15;
        if (isCrit) {
            damageDealt *= 2;
            fireEvent(GameEvent.builder(GameEventType.CRITICAL_HIT)
                    .put("attackerName", getName())
                    .put("damage", damageDealt)
                    .build());
        }

        fireEvent(GameEvent.builder(GameEventType.PLAYER_BASIC_ATTACK)
                .put("attackerName", getName())
                .put("damage", damageDealt)
                .put("targetName", target.getName())
                .put("isCritical", isCrit)
                .build());

        target.takeDamage(damageDealt);
    }

    /**
     * Default attack() implementation calls executeAction with BASIC_ATTACK
     * for AI compatibility and polymorphism support.
     * Note: Mana regen is the controller's responsibility (CombatEngine.processPlayerAction),
     * not the model's. This method does not regen mana.
     */
    @Override
    public void attack(GameCharacter target) {
        executeAction(PlayerAction.BASIC_ATTACK, target);
    }
}
