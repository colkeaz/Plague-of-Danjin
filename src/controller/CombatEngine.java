package controller;

import model.Enemy;
import model.Player;
import model.PlayerAction;
import model.events.GameEvent;
import model.events.GameEventDispatcher;
import model.events.GameEventType;

/**
 * Main game controller that orchestrates combat flow.
 * This engine is NON-BLOCKING: each method call advances the state one step.
 * No while loops waiting for input, no Thread.sleep().
 */
public class CombatEngine extends GameEventDispatcher {
    private Player hero;
    private final WaveManager waveManager;
    private final ChestSystem chestSystem;
    private int currentWave;
    private Enemy[] currentEnemies;
    private int currentEnemyIndex;
    private GameState currentState;

    public CombatEngine() {
        this.waveManager = new WaveManager();
        this.chestSystem = new ChestSystem();
        this.currentState = GameState.INTRO;
        this.currentWave = 0;
    }

    /**
     * Starts a new game with the given player name.
     * Transitions from INTRO to AWAITING_PLAYER_ACTION after setting up wave 1.
     */
    public void startGame(String playerName) {
        this.hero = new Player(playerName);
        this.currentWave = 1;
        this.currentEnemies = waveManager.createWaveEnemies(currentWave);
        this.currentEnemyIndex = 0;
        this.currentState = GameState.AWAITING_PLAYER_ACTION;
    }

    /**
     * Processes a player action against the current enemy.
     * Applies mana regen (10) at the start of the player's turn, then executes the action.
     * Transitions to ENEMY_TURN, WAVE_TRANSITION, or GAME_OVER/VICTORY depending on outcome.
     */
    public void processPlayerAction(PlayerAction action) {
        if (currentState != GameState.AWAITING_PLAYER_ACTION) {
            return;
        }

        currentState = GameState.PROCESSING_ACTION;

        // Regen mana at the start of each player turn
        hero.regenMana(10);

        Enemy currentEnemy = getCurrentEnemy();
        boolean success = hero.executeAction(action, currentEnemy);

        if (!success) {
            // Action failed (e.g., insufficient mana), return to awaiting action
            currentState = GameState.AWAITING_PLAYER_ACTION;
            return;
        }

        // Check if enemy was defeated
        if (!currentEnemy.isAlive()) {
            handleEnemyDefeated();
        } else {
            // Enemy still alive, proceed to enemy turn
            currentState = GameState.ENEMY_TURN;
        }
    }

    /**
     * Processes the current enemy's turn attacking the player.
     * Transitions to AWAITING_PLAYER_ACTION or GAME_OVER.
     */
    public void processEnemyTurn() {
        if (currentState != GameState.ENEMY_TURN) {
            return;
        }

        Enemy currentEnemy = getCurrentEnemy();
        currentEnemy.attack(hero);

        if (!hero.isAlive()) {
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
     * Handles chest spawning (50% chance, not after wave 20), and sets up the next wave.
     * Transitions to CHEST_RESULT, AWAITING_PLAYER_ACTION, or VICTORY.
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

    private void startNextWave() {
        currentWave++;
        currentEnemies = waveManager.createWaveEnemies(currentWave);
        currentEnemyIndex = 0;
        currentState = GameState.AWAITING_PLAYER_ACTION;
    }

    private void handleEnemyDefeated() {
        // +5 HP heal on enemy kill
        hero.heal(5);

        fireEvent(GameEvent.builder(GameEventType.ENEMY_DEFEATED)
                .put("enemyName", currentEnemies[currentEnemyIndex].getName())
                .put("waveNumber", currentWave)
                .put("playerHp", hero.getHp())
                .build());

        // Check if there are more enemies in this wave
        currentEnemyIndex++;
        if (currentEnemyIndex < currentEnemies.length) {
            // More enemies in this wave
            currentState = GameState.AWAITING_PLAYER_ACTION;
        } else {
            // All enemies in wave defeated, transition to next wave
            currentState = GameState.WAVE_TRANSITION;
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

    public int getCurrentWave() {
        return currentWave;
    }

    public WaveManager getWaveManager() {
        return waveManager;
    }

    public ChestSystem getChestSystem() {
        return chestSystem;
    }
}
