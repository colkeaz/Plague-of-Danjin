package controller;

import model.Enemy;
import model.Goblin;
import model.GoblinKing;
import model.Lich;
import model.Skeleton;
import model.enemies.BoneColossus;
import model.enemies.GoblinChieftain;
import model.enemies.PlagueElemental;
import model.enemies.PlagueGoblin;
import model.enemies.ShieldedSkeleton;
import model.enemies.Thornmother;
import model.world.Area;
import model.world.Encounter;

/**
 * Generates Enemy[] arrays for each encounter in story mode.
 * Each area has a specific set of encounters defined in the spec.
 * Enemy stat scaling uses a fixed "effective wave" for each area
 * to maintain consistent difficulty.
 */
public class AreaEncounterGenerator {

    // Effective wave values for scaling wave-based enemy constructors
    private static final int GOBLIN_WARRENS_WAVE = 3;
    private static final int BONE_CATHEDRAL_WAVE = 12;
    private static final int PLAGUE_GARDENS_WAVE = 8;
    private static final int LICHS_THRONE_WAVE = 16;
    private static final int DANJINS_CORE_WAVE = 5;

    /**
     * Generates the enemy array for a given area and encounter.
     * Uses the encounter's enemyTypes list to instantiate the correct enemy classes.
     *
     * @param area the area the encounter belongs to
     * @param encounter the encounter definition
     * @return array of enemies for this encounter, or empty array for EVENT encounters
     */
    public Enemy[] generateEnemies(Area area, Encounter encounter) {
        if (encounter.getType() == Encounter.EncounterType.EVENT) {
            return new Enemy[0];
        }

        java.util.List<String> types = encounter.getEnemyTypes();
        if (types == null || types.isEmpty()) {
            return new Enemy[0];
        }

        int wave = getEffectiveWave(area);
        Enemy[] enemies = new Enemy[types.size()];

        for (int i = 0; i < types.size(); i++) {
            enemies[i] = createEnemy(types.get(i), wave, area);
        }

        // Apply Lich's Throne buff: +50% stats for all enemies
        if (area == Area.LICHS_THRONE) {
            applyLichsThroneBuffs(enemies);
        }

        return enemies;
    }

    /**
     * Creates a single enemy instance based on its type string.
     *
     * @param type the enemy type identifier
     * @param wave the effective wave for scaling
     * @param area the area context
     * @return a new Enemy instance
     */
    private Enemy createEnemy(String type, int wave, Area area) {
        switch (type) {
            case "Goblin":
                return new Goblin(wave);
            case "PlagueGoblin":
                return new PlagueGoblin(wave);
            case "GoblinChieftain":
                return new GoblinChieftain();
            case "GoblinKing":
                return new GoblinKing();
            case "Skeleton":
                return new Skeleton(wave);
            case "ShieldedSkeleton":
                return new ShieldedSkeleton(wave);
            case "BoneColossus":
                return new BoneColossus();
            case "PlagueElemental":
                return new PlagueElemental();
            case "Thornmother":
                return new Thornmother();
            case "Lich":
                return new Lich();
            default:
                // Fallback: generic Goblin
                return new Goblin(wave);
        }
    }

    /**
     * Returns the effective wave number for scaling enemies in a given area.
     */
    private int getEffectiveWave(Area area) {
        switch (area) {
            case GOBLIN_WARRENS:
                return GOBLIN_WARRENS_WAVE;
            case BONE_CATHEDRAL:
                return BONE_CATHEDRAL_WAVE;
            case PLAGUE_GARDENS:
                return PLAGUE_GARDENS_WAVE;
            case LICHS_THRONE:
                return LICHS_THRONE_WAVE;
            case DANJINS_CORE:
                return DANJINS_CORE_WAVE;
            default:
                return DANJINS_CORE_WAVE;
        }
    }

    /**
     * Applies +50% stat buffs to all enemies in the Lich's Throne area.
     * This makes all encounters in the final area significantly more challenging.
     */
    private void applyLichsThroneBuffs(Enemy[] enemies) {
        for (Enemy enemy : enemies) {
            // +50% ATK
            int atkBonus = enemy.getAttackPower() / 2;
            enemy.upgradePower(atkBonus);
            // +50% DEF
            int defBonus = enemy.getDefense() / 2;
            enemy.upgradeDefense(defBonus);
        }
    }
}
