package ch.logixisland.anuto.business.wave;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.logixisland.anuto.business.game.ScoreBoard;
import ch.logixisland.anuto.engine.logic.GameEngine;
import ch.logixisland.anuto.engine.logic.entity.EntityRegistry;
import ch.logixisland.anuto.engine.logic.map.EnemyInfo;
import ch.logixisland.anuto.engine.logic.map.MapPath;
import ch.logixisland.anuto.engine.logic.map.WaveInfo;
import ch.logixisland.anuto.entity.EntityTypes;
import ch.logixisland.anuto.entity.enemy.Enemy;
import ch.logixisland.anuto.util.container.KeyValueStore;
import ch.logixisland.anuto.util.iterator.StreamIterator;
import ch.logixisland.anuto.util.math.MathUtils;
import ch.logixisland.anuto.util.math.Vector2;

class WaveAttender implements Enemy.Listener {

    private final GameEngine mGameEngine;
    private final ScoreBoard mScoreBoard;
    private final EntityRegistry mEntityRegistry;
    private final WaveManager mWaveManager;
    private final WaveInfo mWaveInfo;

    private final List<MapPath> mPaths;
    private final Collection<Enemy> mRemainingEnemies = new ArrayList<>();

    private final int mWaveNumber;
    private int mWaveStartTickCount;

    private int mExtend;
    private int mWaveReward;
    private float mEnemyHealthModifier;
    private float mEnemyRewardModifier;

    WaveAttender(GameEngine gameEngine, ScoreBoard scoreBoard, EntityRegistry entityRegistry,
                 WaveManager waveManager, WaveInfo waveInfo, List<MapPath> paths, int waveNumber) {
        mGameEngine = gameEngine;
        mScoreBoard = scoreBoard;
        mEntityRegistry = entityRegistry;
        mWaveManager = waveManager;
        mWaveInfo = waveInfo;
        mPaths = paths;
        mWaveNumber = waveNumber;

        mExtend = 1;
        mEnemyHealthModifier = 1;
        mEnemyRewardModifier = 1;
        mWaveReward = mWaveInfo.getWaveReward();
    }

    float getWaveDefaultHealth(EnemyDefaultHealth enemyDefaultHealth) {
        float waveHealth = 0f;
        for (EnemyInfo d : mWaveInfo.getEnemies()) {
            waveHealth += enemyDefaultHealth.getDefaultHealth(d.getName());
        }
        waveHealth *= mExtend + 1;
        return waveHealth;
    }

    int getWaveReward() {
        return mWaveReward;
    }

    void setExtend(int extend) {
        mExtend = extend;
    }

    void modifyEnemyHealth(float modifier) {
        mEnemyHealthModifier *= modifier;
    }

    void modifyEnemyReward(float modifier) {
        mEnemyRewardModifier *= modifier;
    }

    void modifyWaveReward(float modifier) {
        mWaveReward = (int) ((float) mWaveReward * modifier);
    }

    void start() {
        if (mWaveStartTickCount == 0) {
            mWaveStartTickCount = mGameEngine.getTickCount();
        }

        scheduleEnemies();
    }

    void giveWaveReward() {
        mScoreBoard.giveCredits(mWaveReward, true);
        mWaveReward = 0;
    }

    int getRemainingEnemiesCount() {
        return mRemainingEnemies.size();
    }

    int getWaveStartTickCount() {
        return mWaveStartTickCount;
    }

    float getRemainingEnemiesReward() {
        float totalReward = 0f;

        for (Enemy enemy : mRemainingEnemies) {
            totalReward += enemy.getReward();
        }

        return totalReward;
    }

    KeyValueStore writeActiveWaveData() {
        KeyValueStore data = new KeyValueStore();
        data.putInt("waveNumber", mWaveNumber);
        data.putInt("waveStartTickCount", mWaveStartTickCount);
        data.putInt("extend", mExtend);
        data.putInt("waveReward", mWaveReward);
        data.putFloat("enemyHealthModifier", mEnemyHealthModifier);
        data.putFloat("enemyRewardModifier", mEnemyRewardModifier);
        return data;
    }

    void readActiveWaveData(KeyValueStore data) {
        mExtend = data.getInt("extend");
        mWaveReward = data.getInt("waveReward");
        mEnemyHealthModifier = data.getFloat("enemyHealthModifier");
        mEnemyRewardModifier = data.getFloat("enemyRewardModifier");
        mWaveStartTickCount = data.getInt("waveStartTickCount");

        StreamIterator<Enemy> enemyIterator = mGameEngine.getEntitiesByType(EntityTypes.ENEMY).cast(Enemy.class);
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();

            if (enemy.getWaveNumber() == mWaveNumber) {
                mRemainingEnemies.add(enemy);
                enemy.addListener(this);
            }
        }
    }

    private void scheduleEnemies() {
        int delayTicks = mWaveStartTickCount - mGameEngine.getTickCount();
        float offset = 0;

        List<EnemyInfo> enemyInfos = mWaveInfo.getEnemies();

        for (int extendIndex = 0; extendIndex < mExtend + 1; extendIndex++) {
            for (int enemyIndex = 0; enemyIndex < enemyInfos.size(); enemyIndex++) {
                EnemyInfo info = enemyInfos.get(enemyIndex);

                if (MathUtils.equals(info.getDelay(), 0f, 0.1f)) {
                    offset += info.getOffset();
                } else {
                    offset = info.getOffset();
                }

                if (enemyIndex > 0 || extendIndex > 0) {
                    delayTicks += Math.round(info.getDelay() * GameEngine.TARGET_FRAME_RATE);
                }

                if (delayTicks >= 0) {
                    Enemy enemy = createAndConfigureEnemy(info, offset);
                    addEnemy(enemy, delayTicks);
                }
            }
        }
    }

    private Enemy createAndConfigureEnemy(EnemyInfo info, float offset) {
        MapPath path = mPaths.get(info.getPathIndex());
        Enemy enemy = (Enemy) mEntityRegistry.createEntity(info.getName());
        enemy.setWaveNumber(mWaveNumber);
        enemy.modifyHealth(mEnemyHealthModifier);
        enemy.modifyReward(mEnemyRewardModifier);
        enemy.setupPath(path.getWayPoints());

        Vector2 startPosition = path.getWayPoints().get(0);
        Vector2 startDirection = startPosition.directionTo(path.getWayPoints().get(1));
        enemy.setPosition(Vector2.mul(startDirection, -offset).add(startPosition));

        return enemy;
    }

    private void addEnemy(final Enemy enemy, int delayTicks) {
        mRemainingEnemies.add(enemy);
        enemy.addListener(this);

        mGameEngine.postAfterTicks(() -> mGameEngine.add(enemy), delayTicks);
    }

    @Override
    public void enemyKilled(Enemy enemy) {
        mScoreBoard.giveCredits(enemy.getReward(), true);
    }

    @Override
    public void enemyFinished(Enemy enemy) {
        mScoreBoard.takeLives(1);
    }

    @Override
    public void enemyRemoved(Enemy enemy) {
        mRemainingEnemies.remove(enemy);
        mWaveManager.enemyRemoved();

        if (getRemainingEnemiesCount() == 0) {
            giveWaveReward();
            mWaveManager.waveFinished(this);
        }
    }
}
