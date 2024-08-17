package ch.logixisland.anuto.entity.enemy;

import android.graphics.Canvas;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import ch.logixisland.anuto.GameSettings;
import ch.logixisland.anuto.engine.logic.GameEngine;
import ch.logixisland.anuto.engine.logic.entity.Entity;
import ch.logixisland.anuto.entity.EntityTypes;
import ch.logixisland.anuto.entity.effect.TeleportedMarker;
import ch.logixisland.anuto.entity.tower.Tower;
import ch.logixisland.anuto.util.iterator.Function;
import ch.logixisland.anuto.util.math.Vector2;


public abstract class Enemy extends Entity {

    public static Function<Enemy, Float> health() {
        return input -> input.mHealth;
    }

    public static Function<Enemy, Float> distanceRemaining() {
        return Enemy::getDistanceRemaining;
    }

    public interface Listener {
        void enemyKilled(Enemy enemy);

        void enemyFinished(Enemy enemy);

        void enemyRemoved(Enemy enemy);
    }

    private final EnemyProperties mEnemyProperties;
    private float mHealth;
    private float mMaxHealth;
    private float mSpeedModifier;
    private int mReward;
    private int mWaveNumber;
    private List<Vector2> mWayPoints;
    private int mWayPointIndex;
    private boolean mBeingTeleported;
    private boolean mWasTeleported;

    private final HealthBar mHealthBar;

    private final List<Listener> mListeners = new CopyOnWriteArrayList<>();

    Enemy(GameEngine gameEngine, EnemyProperties enemyProperties) {
        super(gameEngine);

        mEnemyProperties = enemyProperties;
        mSpeedModifier = 1f;
        mHealth = enemyProperties.getHealth();
        mMaxHealth = enemyProperties.getHealth();
        mReward = enemyProperties.getReward();

        mHealthBar = new HealthBar(getTheme(), this);
    }

    public abstract int getTextId();

    public abstract void drawPreview(Canvas canvas);

    @Override
    public final int getEntityType() {
        return EntityTypes.ENEMY;
    }

    @Override
    public void init() {
        super.init();
        getGameEngine().add(mHealthBar);
    }

    @Override
    public void clean() {
        super.clean();
        getGameEngine().remove(mHealthBar);

        for (Listener listener : mListeners) {
            listener.enemyRemoved(this);
        }

        mListeners.clear();
    }

    @Override
    public void tick() {
        super.tick();

        if (mBeingTeleported) {
            return;
        }

        if (!hasWayPoint()) {
            for (Listener listener : mListeners) {
                listener.enemyFinished(this);
            }
            remove();
            return;
        }

        float stepSize = getSpeed() / GameEngine.TARGET_FRAME_RATE;
        if (getDistanceTo(getCurrentWayPoint()) >= stepSize) {
            move(getDirection().mul(stepSize));
        } else {
            setPosition(getCurrentWayPoint());
            mWayPointIndex++;
        }
    }

    public EnemyProperties getEnemyProperties() {
        return mEnemyProperties;
    }

    public void startTeleport() {
        mBeingTeleported = true;
    }

    public void finishTeleport() {
        mBeingTeleported = false;
        mWasTeleported = true;
        getGameEngine().add(new TeleportedMarker(this));
    }

    public boolean isBeingTeleported() {
        return mBeingTeleported;
    }

    public boolean wasTeleported() {
        return mWasTeleported;
    }

    public int getWaveNumber() {
        return mWaveNumber;
    }

    public void setWaveNumber(int waveNumber) {
        mWaveNumber = waveNumber;
    }

    public void setupPath(List<Vector2> wayPoints) {
        setupPath(wayPoints, 0);
    }

    void setupPath(List<Vector2> wayPoints, int wayPointIndex) {
        mWayPoints = wayPoints;
        mWayPointIndex = wayPointIndex;
    }

    private Vector2 getCurrentWayPoint() {
        return mWayPoints.get(mWayPointIndex);
    }

    List<Vector2> getWayPoints() {
        return mWayPoints;
    }

    int getWayPointIndex() {
        return mWayPointIndex;
    }

    boolean hasWayPoint() {
        return mWayPoints != null && mWayPointIndex < mWayPoints.size();
    }

    public Vector2 getDirection() {
        if (!hasWayPoint()) {
            return null;
        }

        return getDirectionTo(getCurrentWayPoint());
    }

    public float getSpeed() {
        return mEnemyProperties.getSpeed() * Math.max(mSpeedModifier, GameSettings.MIN_SPEED_MODIFIER);
    }

    public void modifySpeed(float f, Entity origin) {
        if (origin instanceof Tower) {
            Tower originTower = (Tower) origin;

            if (mEnemyProperties.getStrongAgainst().contains(originTower.getWeaponType())) {
                return;
            }
        }

        mSpeedModifier = mSpeedModifier * f;
    }

    private float getDistanceRemaining() {
        if (!hasWayPoint()) {
            return 0;
        }

        float dist = getDistanceTo(getCurrentWayPoint());

        for (int i = mWayPointIndex + 1; i < mWayPoints.size(); i++) {
            Vector2 wThis = mWayPoints.get(i);
            Vector2 wLast = mWayPoints.get(i - 1);

            dist += wLast.distanceTo(wThis);
        }

        return dist;
    }

    public Vector2 getPositionAfter(float sec) {
        if (mWayPoints == null) {
            return getPosition();
        }

        float distance = sec * getSpeed();
        int index = mWayPointIndex;
        Vector2 position = getPosition();

        while (index < mWayPoints.size()) {
            Vector2 wayPoint = mWayPoints.get(index);
            float toWaypointDist = position.distanceTo(wayPoint);

            if (distance < toWaypointDist) {
                return Vector2.to(position, wayPoint)
                              .mul(distance / toWaypointDist)
                              .add(position);
            }
            distance -= toWaypointDist;
            index++;
        }

        return position;
    }

    public void sendBack(float dist) {
        int index = mWayPointIndex - 1;
        Vector2 pos = getPosition();

        while (index > 0) {
            Vector2 wp = mWayPoints.get(index);
            float toWpDist = pos.distanceTo(wp);

            if (dist > toWpDist) {
                dist -= toWpDist;
                pos = wp;
                index--;
            } else {
                pos = pos.directionTo(wp)
                         .mul(dist)
                         .add(pos);
                setPosition(pos);
                mWayPointIndex = index + 1;
                return;
            }
        }

        setPosition(mWayPoints.get(0));
        mWayPointIndex = 1;
    }

    float getHealth() {
        return mHealth;
    }

    public float getMaxHealth() {
        return mMaxHealth;
    }

    public void damage(float amount, Entity origin) {
        if (origin instanceof Tower) {
            Tower originTower = (Tower) origin;

            if (mEnemyProperties.getWeakAgainst().contains(originTower.getWeaponType())) {
                amount *= GameSettings.WEAK_AGAINST_DAMAGE_MODIFIER;
            }

            if (mEnemyProperties.getStrongAgainst().contains(originTower.getWeaponType())) {
                amount *= GameSettings.STRONG_AGAINST_DAMAGE_MODIFIER;
            }

            originTower.reportDamageInflicted(amount);
        }

        mHealth -= amount;

        if (mHealth <= 0) {
            for (Listener listener : mListeners) {
                listener.enemyKilled(this);
            }

            remove();
        }
    }

    public void modifyHealth(float f) {
        mHealth *= f;
        mMaxHealth *= f;
    }

    void setHealth(float health, float maxHealth) {
        mHealth = health;
        mMaxHealth = maxHealth;
    }

    public void heal(float amount) {
        mHealth += amount;

        if (mHealth > mMaxHealth) {
            mHealth = mMaxHealth;
        }
    }

    public int getReward() {
        return mReward;
    }

    public void modifyReward(float f) {
        mReward = Math.round(mReward * f);
    }

    void setReward(int reward) {
        mReward = reward;
    }

    public void addListener(Listener listener) {
        mListeners.add(listener);
    }

    public void removeListener(Listener listener) {
        mListeners.remove(listener);
    }

}
