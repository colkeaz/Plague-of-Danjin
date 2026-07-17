package model.events;

public enum GameEventType {
    DAMAGE_DEALT,
    DAMAGE_BLOCKED,
    HEAL,
    MANA_SPENT,
    MANA_INSUFFICIENT,
    MANA_REGEN,
    POWER_UPGRADED,
    DEFENSE_UPGRADED,
    CRITICAL_HIT,
    PLAYER_BASIC_ATTACK,
    SPELL_CAST,
    ENEMY_ATTACK,
    GOBLIN_KING_RAGE,
    LICH_SUMMON_MINION,
    LICH_MINION_ATTACK,
    ENEMY_DEFEATED,
    PLAYER_DEFEATED,
    WAVE_START,
    WAVE_COMPLETE,
    CHEST_FOUND,
    CHEST_LEGENDARY,
    CHEST_EPIC,
    CHEST_RARE,
    CHEST_COMMON,
    CHEST_MIMIC,
    GAME_VICTORY,
    FLAVOR_TEXT,

    // Item system events
    ITEM_EQUIPPED,
    ITEM_UNEQUIPPED,
    ITEM_DROPPED,

    // Status effect events
    STATUS_APPLIED,
    STATUS_TICKED,
    STATUS_EXPIRED,

    // Enemy ability events
    ENEMY_TELEGRAPH,
    ENEMY_ABILITY_FIRED,

    // Skill system events
    SKILL_UNLOCKED,
    SKILL_CHOICE_OFFERED,
    SKILL_ON_COOLDOWN,

    // Event room events
    EVENT_ROOM_ENTERED,
    EVENT_ROOM_CHOICE_MADE,

    // Run modifier events
    CURSE_APPLIED,
    RUN_MODIFIER_APPLIED,

    // Shield events
    SHIELD_BLOCKED,
    SHIELD_BROKEN,

    // Enemy formation events
    ENEMY_FORMATION_SPAWNED,

    // QTE (Quick-Time Event) events
    QTE_TRIGGERED,
    QTE_SUCCESS,
    QTE_FAILURE
}
