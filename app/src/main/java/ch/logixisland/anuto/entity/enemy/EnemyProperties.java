package ch.logixisland.anuto.entity.enemy;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class EnemyProperties {

    private int mHealth;
    private float mSpeed;
    private int mReward;
    private Collection<WeaponType> mWeakAgainst = Collections.emptyList();
    private Collection<WeaponType> mStrongAgainst = Collections.emptyList();

    public static class Builder {

        private final EnemyProperties mResult = new EnemyProperties();

        public Builder setHealth(int health) {
            mResult.mHealth = health;
            return this;
        }

        public Builder setSpeed(float speed) {
            mResult.mSpeed = speed;
            return this;
        }

        public Builder setReward(int reward) {
            mResult.mReward = reward;
            return this;
        }

        public Builder setWeakAgainst(WeaponType... weakAgainst) {
            mResult.mWeakAgainst = Arrays.asList(weakAgainst);
            return this;
        }

        public Builder setStrongAgainst(WeaponType... strongAgainst) {
            mResult.mStrongAgainst = Arrays.asList(strongAgainst);
            return this;
        }

        public EnemyProperties build() {
            return mResult;
        }

    }

    public int getHealth() {
        return mHealth;
    }

    public float getSpeed() {
        return mSpeed;
    }

    public int getReward() {
        return mReward;
    }

    public Collection<WeaponType> getWeakAgainst() {
        return mWeakAgainst;
    }

    public Collection<WeaponType> getStrongAgainst() {
        return mStrongAgainst;
    }

}
