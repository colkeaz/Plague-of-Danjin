package controller;

public enum GameState {
    INTRO,
    AWAITING_PLAYER_ACTION,
    PROCESSING_ACTION,
    ENEMY_TURN,
    WAVE_TRANSITION,
    CHEST_RESULT,
    GAME_OVER,
    VICTORY
}
