package controller;

import java.util.List;

import model.Enemy;
import model.Player;
import model.PlayerAction;
import model.events.GameEvent;
import model.events.GameEventDispatcher;
import model.events.GameEventListener;
import model.events.GameEventType;
import model.skills.Skill;
import model.skills.SkillEffect;
import model.skills.SkillTree;
import model.status.StatusEffect;
import model.status.StatusType;

/**
 * Main game controller that orchestrates combat flow.
 * This engine is NON-BLOCKING: each method call advances the state one step.
 * No while loops waiting for input, no Thread.sleep().
 *
 * The engine acts as a central event bus: it forwards events from all child objects
 * (player, enemies, WaveManager, ChestSystem, RunModifiers, EventRoomManager) so that
 * a view layer only needs to register a single listener on the engine to receive all
 * game events.
 */
public class CombatEngine extends GameEventDispatcher {
    private Player hero;
    private final WaveManager waveManager;
    private final ChestSystem chestSystem;
    private final RunModifiers runModifiers;
    private final EventRoomManager eventRoomManager;
    private int currentWave;
    private Enemy[] currentEnemies;
    private int currentEnemyIndex;
    private GameState currentState;

    // Track enemy turn counts for telegraph system
    private int[] enemyTurnCounts;

    // Skill choice state: holds offered skills during milestone waves
    private List<Skill> pendingSkillChoices;

    // Track ENRAGE stat modifications for reversal on expiry
    private int enrageAtkBonus = 0;
    private int enrageDefReduction = 0;
    private boolean enrageActive = false;

    /**
     * Internal listener that forwards all child events through this engine's dispatcher.
     */
    private final GameEventListener forwardingListener = this::fireEvent;

    /**
     * Internal listener that handles reflect damage from events like BoneColossus shield break.
     * Processes ENEMY_ABILITY_FIRED events that contain reflectDamage.
     */
    private final GameEventListener reflectHandler = event -> {
        if (event.getType() == GameEventType.ENEMY_ABILITY_FIRED) {
            int reflectDamage = event.getInt("reflectDamage");
            if (reflectDamage > 0 && hero != null && hero.isAlive()) {
                String elementName = event.getString("element");
                model.skills.Element reflectElement = model.skills.Element.PHYSICAL;
                if (elementName != null) {
                    try {
                        reflectElement = model.skills.Element.valueOf(elementName);
                    } catch (IllegalArgumentException e) {
                        // Fall back to PHYSICAL
                    }
                }
                hero.takeDamage(reflectDamage, reflectElement);
            }
        }
    };

    public CombatEngine() {
        this.waveManager = new WaveManager();
        this.chestSystem = new ChestSystem();
        this.runModifiers = new RunModifiers();
        this.eventRoomManager = new EventRoomManager();
        this.currentState = GameState.INTRO;
        this.currentWave = 0;

        // Register forwarding on subsystems so their events bubble up through the engine
        waveManager.addListener(forwardingListener);
        chestSystem.addListener(forwardingListener);
        runModifiers.addListener(forwardingListener);
        eventRoomManager.addListener(forwardingListener);
    }

    /**
     * Starts a new game with the given player name.
     * Transitions from INTRO to AWAITING_PLAYER_ACTION after setting up wave 1.
     */
    public void startGame(String playerName) {
        this.hero = new Player(playerName);
        // Forward player events through the engine
        hero.addListener(forwardingListener);
        // Forward inventory and skill tree events through the engine
        hero.getInventory().addListener(forwardingListener);
        hero.getSkillTree().addListener(forwardingListener);

        this.currentWave = 1;
        this.currentEnemies = waveManager.createWaveEnemies(currentWave);
        applyShatterEffect(currentEnemies);
        registerEnemyListeners(currentEnemies);
        this.currentEnemyIndex = 0;
        this.enemyTurnCounts = new int[currentEnemies.length];
        this.currentState = GameState.AWAITING_PLAYER_ACTION;

        // Apply wave modifiers for wave 1
        runModifiers.applyWaveModifiers(currentWave, hero);
    }

    /**
     * Processes a player action against the current enemy.
     * Ticks status effects at the start of the player's turn, checks stun,
     * applies mana regen, then executes the action.
     * Transitions to ENEMY_TURN, WAVE_TRANSITION, or GAME_OVER/VICTORY depending on outcome.
     */
    public void processPlayerAction(PlayerAction action) {
        if (currentState != GameState.AWAITING_PLAYER_ACTION) {
            return;
        }

        currentState = GameState.PROCESSING_ACTION;

        try {
            // Apply permanent damage per turn from Danjin's Heart absorption
            if (runModifiers.isDanjinHeartAbsorbed() && runModifiers.getPermanentDamagePerTurn() > 0) {
                runModifiers.applyPermanentDamagePerTurn(hero, runModifiers.getPermanentDamagePerTurn());
                if (isPlayerDeadAfterReviveCheck()) {
                    currentState = GameState.GAME_OVER;
                    fireEvent(GameEvent.builder(GameEventType.PLAYER_DEFEATED)
                            .put("playerName", hero.getName())
                            .put("waveNumber", currentWave)
                            .build());
                    return;
                }
            }

            // Tick player status effects at the start of their turn
            List<GameEvent> statusEvents = hero.getStatusManager().tickAll();
            applyPlayerStatusEffects(statusEvents);

            if (isPlayerDeadAfterReviveCheck()) {
                currentState = GameState.GAME_OVER;
                fireEvent(GameEvent.builder(GameEventType.PLAYER_DEFEATED)
                        .put("playerName", hero.getName())
                        .put("waveNumber", currentWave)
                        .build());
                return;
            }

            // Check if player is stunned - skip turn if so
            if (hero.getStatusManager().isStunned()) {
                hero.getStatusManager().removeEffect(StatusType.STUN);
                currentState = GameState.ENEMY_TURN;
                return;
            }

            // Regen mana at the start of each player turn
            hero.regenMana(10);

            // Handle multi-target skills specially
            if (isMultiTargetAction(action)) {
                boolean success = processMultiTargetAction(action);
                if (!success) {
                    currentState = GameState.AWAITING_PLAYER_ACTION;
                    return;
                }
                // Tick cooldowns after action
                hero.getSkillTree().tickAllCooldowns();
                // Check if all enemies are defeated
                if (allEnemiesDefeated()) {
                    handleAllEnemiesDefeated();
                } else {
                    advanceToNextAliveEnemy();
                    currentState = GameState.ENEMY_TURN;
                }
                return;
            }

            Enemy currentEnemy = getCurrentEnemy();
            boolean success = hero.executeAction(action, currentEnemy);

            if (!success) {
                // Action failed (e.g., insufficient mana), return to awaiting action
                currentState = GameState.AWAITING_PLAYER_ACTION;
                return;
            }

            // Tick cooldowns after action
            hero.getSkillTree().tickAllCooldowns();

            // Check if enemy was defeated
            if (!currentEnemy.isAlive()) {
                handleEnemyDefeated();
            } else {
                // Enemy still alive, proceed to enemy turn
                currentState = GameState.ENEMY_TURN;
            }
        } catch (RuntimeException e) {
            // On unexpected failure, revert to a safe state so the engine is not stuck
            currentState = GameState.AWAITING_PLAYER_ACTION;
            throw e;
        }
    }

    /**
     * Processes the current enemy's turn attacking the player.
     * Ticks enemy status effects, checks stun, handles telegraph system.
     * Transitions to AWAITING_PLAYER_ACTION or GAME_OVER.
     */
    public void processEnemyTurn() {
        if (currentState != GameState.ENEMY_TURN) {
            return;
        }

        Enemy currentEnemy = getCurrentEnemy();
        if (currentEnemy == null || !currentEnemy.isAlive()) {
            // Skip dead enemies, find next alive one or transition
            if (advanceToNextAliveEnemy()) {
                currentState = GameState.AWAITING_PLAYER_ACTION;
            } else {
                handleAllEnemiesDefeated();
            }
            return;
        }

        // Tick enemy status effects
        List<GameEvent> statusEvents = currentEnemy.getStatusManager().tickAll();
        applyEnemyStatusEffects(currentEnemy, statusEvents);

        if (!currentEnemy.isAlive()) {
            handleEnemyDefeated();
            return;
        }

        // Check if enemy is stunned - skip turn if so
        if (currentEnemy.getStatusManager().isStunned()) {
            currentEnemy.getStatusManager().removeEffect(StatusType.STUN);
            currentState = GameState.AWAITING_PLAYER_ACTION;
            return;
        }

        // Increment turn count for this enemy
        enemyTurnCounts[currentEnemyIndex]++;
        int turnCount = enemyTurnCounts[currentEnemyIndex];

        // Handle telegraph system
        if (currentEnemy.isWindingUp()) {
            // Execute the wound-up ability
            currentEnemy.executeAbility(hero);
        } else if (currentEnemy.shouldTelegraph(turnCount)) {
            // Start wind-up for next turn (enemy does NOT attack this turn)
            currentEnemy.windUp(getEnemyAbility(currentEnemy));
        } else {
            // Normal attack
            currentEnemy.attack(hero);
        }

        if (isPlayerDeadAfterReviveCheck()) {
            currentState = GameState.GAME_OVER;
            fireEvent(GameEvent.builder(GameEventType.PLAYER_DEFEATED)
                    .put("playerName", hero.getName())
                    .put("waveNumber", currentWave)
                    .build());
        } else {
            currentState = GameState.AWAITING_PLAYER_ACTION;
        }
    }

    /**
     * Advances to the next wave. Called when the current wave is complete.
     * Handles chest spawning (50% chance, not after wave 20), event rooms,
     * skill milestones, and sets up the next wave.
     */
    public void advanceWave() {
        if (currentState != GameState.WAVE_TRANSITION) {
            return;
        }

        fireEvent(GameEvent.builder(GameEventType.WAVE_COMPLETE)
                .put("waveNumber", currentWave)
                .build());

        // Check if all waves are complete
        if (currentWave >= waveManager.getTotalWaves()) {
            currentState = GameState.VICTORY;
            fireEvent(GameEvent.builder(GameEventType.GAME_VICTORY)
                    .put("playerName", hero.getName())
                    .build());
            return;
        }

        // Check if an event room should trigger after this wave
        if (eventRoomManager.shouldTrigger(currentWave)) {
            currentState = GameState.EVENT_ROOM;
            fireEvent(GameEvent.builder(GameEventType.EVENT_ROOM_ENTERED)
                    .put("roomName", eventRoomManager.getEventRoomDescription(currentWave))
                    .put("waveNumber", currentWave)
                    .build());
            return;
        }

        // Check for skill milestone (after clearing wave 4, 9, 14 -> offer skills for wave 5, 10, 15)
        int nextWave = currentWave + 1;
        if (isSkillMilestoneWave(nextWave)) {
            List<Skill> choices = hero.getSkillTree().getChoicesForMilestone(nextWave);
            if (!choices.isEmpty()) {
                pendingSkillChoices = choices;
                currentState = GameState.SKILL_CHOICE;
                fireEvent(GameEvent.builder(GameEventType.SKILL_CHOICE_OFFERED)
                        .put("waveNumber", nextWave)
                        .put("choiceCount", choices.size())
                        .build());
                return;
            }
        }

        // 50% chance of chest between waves (not after wave 20)
        boolean chestFound = chestSystem.tryFindChest(hero);
        if (chestFound) {
            currentState = GameState.CHEST_RESULT;
        } else {
            startNextWave();
        }
    }

    /**
     * Called after chest result is displayed to proceed to the next wave.
     */
    public void proceedAfterChest() {
        if (currentState != GameState.CHEST_RESULT) {
            return;
        }
        startNextWave();
    }

    /**
     * Processes a player's event room choice.
     * 
     * @param choice 0-based index of the chosen option
     */
    public void processEventRoomChoice(int choice) {
        if (currentState != GameState.EVENT_ROOM) {
            return;
        }

        int choiceCount = eventRoomManager.getChoiceCount(currentWave);
        if (choice < 0 || choice >= choiceCount) {
            return; // Invalid choice
        }

        eventRoomManager.applyChoice(currentWave, choice, hero, runModifiers);

        // After event room, check for skill milestone before proceeding
        int nextWave = currentWave + 1;
        if (isSkillMilestoneWave(nextWave)) {
            List<Skill> choices = hero.getSkillTree().getChoicesForMilestone(nextWave);
            if (!choices.isEmpty()) {
                pendingSkillChoices = choices;
                currentState = GameState.SKILL_CHOICE;
                fireEvent(GameEvent.builder(GameEventType.SKILL_CHOICE_OFFERED)
                        .put("waveNumber", nextWave)
                        .put("choiceCount", choices.size())
                        .build());
                return;
            }
        }

        // 50% chance of chest between waves
        boolean chestFound = chestSystem.tryFindChest(hero);
        if (chestFound) {
            currentState = GameState.CHEST_RESULT;
        } else {
            startNextWave();
        }
    }

    /**
     * Processes the player's skill choice at a milestone wave.
     * 
     * @param skillIndex 0-based index into the offered skill choices
     */
    public void processSkillChoice(int skillIndex) {
        if (currentState != GameState.SKILL_CHOICE) {
            return;
        }

        if (pendingSkillChoices == null || skillIndex < 0 || skillIndex >= pendingSkillChoices.size()) {
            return; // Invalid choice
        }

        Skill chosen = pendingSkillChoices.get(skillIndex);
        hero.getSkillTree().unlockSkill(chosen);
        pendingSkillChoices = null;

        // 50% chance of chest between waves
        boolean chestFound = chestSystem.tryFindChest(hero);
        if (chestFound) {
            currentState = GameState.CHEST_RESULT;
        } else {
            startNextWave();
        }
    }

    /**
     * Processes the equip screen state. Transitions back to awaiting player action
     * or wave transition depending on context.
     */
    public void processEquipScreen() {
        if (currentState != GameState.EQUIP_SCREEN) {
            return;
        }
        currentState = GameState.AWAITING_PLAYER_ACTION;
    }

    private void startNextWave() {
        currentWave++;
        currentEnemies = waveManager.createWaveEnemies(currentWave);
        applyShatterEffect(currentEnemies);
        registerEnemyListeners(currentEnemies);
        currentEnemyIndex = 0;
        enemyTurnCounts = new int[currentEnemies.length];
        currentState = GameState.AWAITING_PLAYER_ACTION;

        // Apply wave modifiers (Danjin's Curse at waves 5, 10, 15, 20)
        runModifiers.applyWaveModifiers(currentWave, hero);
    }

    private void handleEnemyDefeated() {
        // +5 HP heal on enemy kill
        hero.heal(5);

        fireEvent(GameEvent.builder(GameEventType.ENEMY_DEFEATED)
                .put("enemyName", currentEnemies[currentEnemyIndex].getName())
                .put("waveNumber", currentWave)
                .put("playerHp", hero.getHp())
                .build());

        // Check if there are more alive enemies in this wave
        if (advanceToNextAliveEnemy()) {
            // More enemies in this wave
            currentState = GameState.AWAITING_PLAYER_ACTION;
        } else {
            // All enemies in wave defeated, transition to next wave
            currentState = GameState.WAVE_TRANSITION;
        }
    }

    private void handleAllEnemiesDefeated() {
        currentState = GameState.WAVE_TRANSITION;
    }

    /**
     * Advances the currentEnemyIndex to the next alive enemy.
     * Returns true if a next alive enemy was found, false if all are dead.
     */
    private boolean advanceToNextAliveEnemy() {
        for (int i = currentEnemyIndex + 1; i < currentEnemies.length; i++) {
            if (currentEnemies[i].isAlive()) {
                currentEnemyIndex = i;
                return true;
            }
        }
        // Also check from start (in case multi-target killed some but not the current)
        for (int i = 0; i < currentEnemies.length; i++) {
            if (currentEnemies[i].isAlive()) {
                currentEnemyIndex = i;
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if all enemies in the current wave are defeated.
     */
    private boolean allEnemiesDefeated() {
        for (Enemy enemy : currentEnemies) {
            if (enemy.isAlive()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a player action uses a multi-target skill.
     */
    private boolean isMultiTargetAction(PlayerAction action) {
        int skillIndex = action.getSkillIndex();
        if (skillIndex < 0) return false;
        Skill skill = hero.getSkillTree().getSkillByIndex(skillIndex);
        return skill != null && skill.getSkillEffect() == SkillEffect.MULTI_TARGET;
    }

    /**
     * Processes a multi-target action - applies damage to all alive enemies.
     * Returns true if the skill was successfully cast, false otherwise.
     */
    private boolean processMultiTargetAction(PlayerAction action) {
        int skillIndex = action.getSkillIndex();
        Skill skill = hero.getSkillTree().getSkillByIndex(skillIndex);
        if (skill == null) return false;

        if (!skill.isReady()) {
            fireEvent(GameEvent.builder(GameEventType.SKILL_ON_COOLDOWN)
                    .put("skillName", skill.getName())
                    .put("turnsRemaining", skill.getCurrentCooldown())
                    .build());
            return false;
        }

        int spellCostReduction = hero.getInventory().getTotalStatBonus(
                model.items.ItemEffect.SPELL_COST_REDUCTION);
        int effectiveCost = skill.getEffectiveCost(spellCostReduction);

        if (effectiveCost > 0 && !hero.spendMana(effectiveCost)) {
            return false;
        }

        skill.use();

        fireEvent(GameEvent.builder(GameEventType.SPELL_CAST)
                .put("casterName", hero.getName())
                .put("spellName", skill.getName())
                .put("manaCost", effectiveCost)
                .build());

        // Apply damage to ALL alive enemies
        int damage = (int)(hero.getTotalAttackPower() * skill.getDamageMultiplier());
        for (Enemy enemy : currentEnemies) {
            if (enemy.isAlive()) {
                enemy.takeDamage(damage, skill.getElement());
            }
        }

        return true;
    }

    /**
     * Applies status effect consequences from tickAll (poison damage, regen heals, enrage, etc.)
     */
    private void applyPlayerStatusEffects(List<GameEvent> statusEvents) {
        for (GameEvent event : statusEvents) {
            if (event.getType() == GameEventType.STATUS_TICKED) {
                String statusType = event.getString("statusType");
                int potency = event.getInt("potency");
                if ("POISON".equals(statusType)) {
                    hero.takeDamage(potency);
                } else if ("REGEN".equals(statusType)) {
                    hero.heal(potency);
                }
            } else if (event.getType() == GameEventType.STATUS_EXPIRED) {
                String statusType = event.getString("statusType");
                if ("ENRAGE".equals(statusType) && enrageActive) {
                    // Revert enrage stat modifications
                    hero.upgradePower(-enrageAtkBonus);
                    hero.upgradeDefense(enrageDefReduction);
                    enrageAtkBonus = 0;
                    enrageDefReduction = 0;
                    enrageActive = false;
                }
            }
        }

        // Check if ENRAGE was just applied (status exists but we haven't tracked it yet)
        if (!enrageActive && hero.getStatusManager().hasEffect(StatusType.ENRAGE)) {
            // Apply +100% ATK, -50% DEF
            enrageAtkBonus = hero.getTotalAttackPower(); // +100% of current ATK
            enrageDefReduction = hero.getDefense() / 2; // -50% of current DEF
            hero.upgradePower(enrageAtkBonus);
            hero.upgradeDefense(-enrageDefReduction);
            enrageActive = true;
        }
    }

    /**
     * Applies status effect consequences for enemies (poison damage, etc.)
     */
    private void applyEnemyStatusEffects(Enemy enemy, List<GameEvent> statusEvents) {
        for (GameEvent event : statusEvents) {
            if (event.getType() == GameEventType.STATUS_TICKED) {
                String statusType = event.getString("statusType");
                int potency = event.getInt("potency");
                if ("POISON".equals(statusType)) {
                    enemy.takeDamage(potency);
                } else if ("REGEN".equals(statusType)) {
                    enemy.heal(potency);
                }
            }
        }
    }

    /**
     * Checks if the player has died and handles auto-revive if active.
     * Returns true if the player is actually dead (no revive available),
     * false if the player was revived or is still alive.
     */
    private boolean isPlayerDeadAfterReviveCheck() {
        if (!hero.isAlive() && hero.isAutoReviveActive()) {
            // Consume the auto-revive and restore to 50% HP
            hero.setAutoReviveActive(false);
            int reviveHp = hero.getMaxHp() / 2;
            hero.heal(reviveHp);
            fireEvent(GameEvent.builder(GameEventType.HEAL)
                    .put("targetName", hero.getName())
                    .put("amount", reviveHp)
                    .put("currentHp", hero.getHp())
                    .put("maxHp", hero.getMaxHp())
                    .build());
            return false;
        }
        return !hero.isAlive();
    }

    /**
     * Returns an EnemyAbility for the given enemy to telegraph.
     * Delegates to the enemy's getNextAbility or creates a default.
     */
    private model.enemies.EnemyAbility getEnemyAbility(Enemy enemy) {
        model.enemies.EnemyAbility ability = enemy.getNextAbility();
        if (ability != null) {
            return ability;
        }
        // Fallback: create a generic powerful attack ability
        return new model.enemies.EnemyAbility(
                "Power Strike", 2.0f, enemy.getElement(),
                enemy.getName() + " is charging a powerful attack!");
    }

    /**
     * Checks if a wave number is a skill milestone wave (5, 10, 15).
     */
    private boolean isSkillMilestoneWave(int wave) {
        return wave == 5 || wave == 10 || wave == 15;
    }

    /**
     * Applies the Shatter effect (from Danjin's Heart choice C) to enemies.
     * Reduces all enemy HP by 20% if the flag is set.
     */
    private void applyShatterEffect(Enemy[] enemies) {
        if (runModifiers.isDanjinHeartShattered()) {
            for (Enemy enemy : enemies) {
                enemy.reduceMaxHpByPercent(20);
            }
        }
    }

    /**
     * Registers the forwarding listener on each enemy so their events
     * are dispatched through the engine's central event bus.
     * Also registers the reflect handler so BoneColossus shield-break
     * damage is applied to the player.
     */
    private void registerEnemyListeners(Enemy[] enemies) {
        for (Enemy enemy : enemies) {
            enemy.addListener(forwardingListener);
            enemy.addListener(reflectHandler);
        }
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public Player getPlayer() {
        return hero;
    }

    public Enemy getCurrentEnemy() {
        if (currentEnemies != null && currentEnemyIndex < currentEnemies.length) {
            return currentEnemies[currentEnemyIndex];
        }
        return null;
    }

    public Enemy[] getCurrentEnemies() {
        return currentEnemies;
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public WaveManager getWaveManager() {
        return waveManager;
    }

    public ChestSystem getChestSystem() {
        return chestSystem;
    }

    public RunModifiers getRunModifiers() {
        return runModifiers;
    }

    public EventRoomManager getEventRoomManager() {
        return eventRoomManager;
    }

    public List<Skill> getPendingSkillChoices() {
        return pendingSkillChoices;
    }
}
