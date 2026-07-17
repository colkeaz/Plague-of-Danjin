package controller;

import java.util.Random;

import model.Enemy;
import model.Goblin;
import model.GoblinKing;
import model.Lich;
import model.Skeleton;
import model.events.GameEvent;
import model.events.GameEventDispatcher;
import model.events.GameEventType;

public class WaveManager extends GameEventDispatcher {
    private static final int TOTAL_WAVES = 20;
    private final Random rand = new Random();

    /**
     * Creates the enemies for the given wave number.
     * Enemy count logic:
     *   wave 10 or 20 -> 1 (boss waves)
     *   wave >= 15 -> rand.nextInt(2) + 2 (2-3 enemies)
     *   wave >= 5 -> rand.nextInt(2) + 1 (1-2 enemies)
     *   else -> 1
     *
     * Enemy type logic:
     *   wave < 10 -> Goblin(wave)
     *   wave == 10 -> GoblinKing()
     *   wave < 20 -> Skeleton(wave)
     *   wave == 20 -> Lich()
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
        if (wave == 10 || wave == 20) {
            return 1;
        } else if (wave >= 15) {
            return rand.nextInt(2) + 2;
        } else if (wave >= 5) {
            return rand.nextInt(2) + 1;
        } else {
            return 1;
        }
    }

    private Enemy createEnemy(int wave) {
        if (wave < 10) {
            return new Goblin(wave);
        } else if (wave == 10) {
            return new GoblinKing();
        } else if (wave < 20) {
            return new Skeleton(wave);
        } else {
            return new Lich();
        }
    }

    public int getTotalWaves() {
        return TOTAL_WAVES;
    }
}
