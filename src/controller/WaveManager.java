package controller;

import java.util.Random;

import model.Enemy;
import model.Goblin;
import model.GoblinKing;
import model.Lich;
import model.Skeleton;
import model.enemies.BoneColossus;
import model.enemies.GoblinChieftain;
import model.enemies.PlagueGoblin;
import model.enemies.ShieldedSkeleton;
import model.events.GameEvent;
import model.events.GameEventDispatcher;
import model.events.GameEventType;

public class WaveManager extends GameEventDispatcher {
    private static final int TOTAL_WAVES = 20;
    private final Random rand = new Random();

    /**
     * Creates the enemies for the given wave number.
     * Wave composition:
     *   Waves 1-4: Goblins (1 enemy each)
     *   Wave 5: GoblinChieftain (mini-boss, 1v1)
     *   Waves 6-9: Mix of Goblins and PlagueGoblins (1-2 enemies)
     *   Wave 10: GoblinKing (boss, 1v1)
     *   Waves 11-14: Skeletons (2-3 enemies)
     *   Wave 15: BoneColossus (mini-boss, 1v1)
     *   Waves 16-19: Mix of Skeletons and ShieldedSkeletons (2-3 enemies)
     *   Wave 20: Lich (boss, 1v1)
     */
    public Enemy[] createWaveEnemies(int wave) {
        int count = getEnemyCount(wave);
        Enemy[] enemies = new Enemy[count];

        for (int i = 0; i < count; i++) {
            enemies[i] = createEnemy(wave);
        }

        fireEvent(GameEvent.builder(GameEventType.WAVE_START)
                .put("waveNumber", wave)
                .put("totalWaves", TOTAL_WAVES)
                .put("enemyCount", count)
                .put("enemyType", enemies[0].getName())
                .build());

        return enemies;
    }

    private int getEnemyCount(int wave) {
        // Boss and mini-boss waves are always 1v1
        if (wave == 5 || wave == 10 || wave == 15 || wave == 20) {
            return 1;
        } else if (wave >= 16) {
            return rand.nextInt(2) + 2; // 2-3 enemies
        } else if (wave >= 11) {
            return rand.nextInt(2) + 2; // 2-3 enemies
        } else if (wave >= 6) {
            return rand.nextInt(2) + 1; // 1-2 enemies
        } else {
            return 1;
        }
    }

    private Enemy createEnemy(int wave) {
        if (wave <= 4) {
            return new Goblin(wave);
        } else if (wave == 5) {
            return new GoblinChieftain();
        } else if (wave <= 9) {
            // Mix of Goblins and PlagueGoblins
            if (rand.nextBoolean()) {
                return new PlagueGoblin(wave);
            } else {
                return new Goblin(wave);
            }
        } else if (wave == 10) {
            return new GoblinKing();
        } else if (wave <= 14) {
            return new Skeleton(wave);
        } else if (wave == 15) {
            return new BoneColossus();
        } else if (wave <= 19) {
            // Mix of Skeletons and ShieldedSkeletons
            if (rand.nextBoolean()) {
                return new ShieldedSkeleton(wave);
            } else {
                return new Skeleton(wave);
            }
        } else {
            return new Lich();
        }
    }

    public int getTotalWaves() {
        return TOTAL_WAVES;
    }
}
