package ch.logixisland.anuto.entity.effect;

import java.util.HashSet;
import java.util.Iterator;

import ch.logixisland.anuto.engine.logic.GameEngine;
import ch.logixisland.anuto.engine.logic.entity.Entity;
import ch.logixisland.anuto.engine.logic.loop.TickTimer;
import ch.logixisland.anuto.entity.EntityTypes;
import ch.logixisland.anuto.entity.enemy.Enemy;
import ch.logixisland.anuto.util.math.Vector2;

public class AreaObserver implements Entity.Listener {

    private final Vector2 mPosition;
    private final float mRange;
    private boolean mFinished;

    private final GameEngine mGameEngine;
    private final Listener mListener;
    private final TickTimer mUpdateTimer = TickTimer.createInterval(0.1f);
    private final HashSet<Enemy> mEnemiesInArea = new HashSet<>();

    public interface Listener {
        void enemyEntered(Enemy enemy);

        void enemyExited(Enemy enemy);
    }

    public AreaObserver(GameEngine gameEngine, Vector2 position, float range, Listener listener) {
        mGameEngine = gameEngine;
        mPosition = position;
        mRange = range;
        mListener = listener;
        mFinished = false;
    }

    public void tick() {
        if (mFinished) {
            return;
        }

        if (!mUpdateTimer.tick()) {
            return;
        }

        checkForExitedEnemies();
        checkForEnteredEnemies();
    }

    public void clean() {
        for (Enemy enemy : mEnemiesInArea) {
            enemy.removeListener(this);
            mListener.enemyExited(enemy);
        }

        mEnemiesInArea.clear();
        mFinished = true;
    }

    private void checkForExitedEnemies() {
        for (Iterator<Enemy> it = mEnemiesInArea.iterator(); it.hasNext(); ) {
            Enemy enemy = it.next();
            if (enemy.getDistanceTo(mPosition) > mRange) {
                it.remove();
                enemy.removeListener(this);
                mListener.enemyExited(enemy);
            }
        }
    }

    private void checkForEnteredEnemies() {
        Iterator<Enemy> enemies = mGameEngine.getEntitiesByType(EntityTypes.ENEMY)
                .filter(Entity.inRange(mPosition, mRange))
                .cast(Enemy.class);

        while (enemies.hasNext()) {
            Enemy enemy = enemies.next();

            if (!mEnemiesInArea.contains(enemy)) {
                mEnemiesInArea.add(enemy);
                enemy.addListener(this);
                mListener.enemyEntered(enemy);
            }
        }
    }

    @Override
    public void entityRemoved(Entity entity) {
        Enemy enemy = (Enemy) entity;
        mEnemiesInArea.remove(enemy);
        enemy.removeListener(this);
        mListener.enemyExited(enemy);
    }
}
