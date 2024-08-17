package ch.logixisland.anuto.entity.tower;

import ch.logixisland.anuto.entity.enemy.WeaponType;

public class TowerProperties {

    private int mValue;
    private int mDamage;
    private float mRange;
    private float mReload;
    private int mMaxLevel;
    private WeaponType mWeaponType;
    private int mEnhanceCost;
    private float mEnhanceBase;
    private int mEnhanceDamage;
    private float mEnhanceRange;
    private float mEnhanceReload;
    private String mUpgradeTowerName;
    private int mUpgradeCost;
    private int mUpgradeLevel;

    public static class Builder {

        private final TowerProperties mResult = new TowerProperties();

        public Builder setValue(int value) {
            mResult.mValue = value;
            return this;
        }

        public Builder setDamage(int damage) {
            mResult.mDamage = damage;
            return this;
        }

        public Builder setRange(float range) {
            mResult.mRange = range;
            return this;
        }

        public Builder setReload(float reload) {
            mResult.mReload = reload;
            return this;
        }

        public Builder setMaxLevel(int maxLevel) {
            mResult.mMaxLevel = maxLevel;
            return this;
        }

        public Builder setWeaponType(WeaponType weaponType) {
            mResult.mWeaponType = weaponType;
            return this;
        }

        public Builder setEnhanceCost(int enhanceCost) {
            mResult.mEnhanceCost = enhanceCost;
            return this;
        }

        public Builder setEnhanceBase(float enhanceBase) {
            mResult.mEnhanceBase = enhanceBase;
            return this;
        }

        public Builder setEnhanceDamage(int enhanceDamage) {
            mResult.mEnhanceDamage = enhanceDamage;
            return this;
        }

        public Builder setEnhanceRange(float enhanceRange) {
            mResult.mEnhanceRange = enhanceRange;
            return this;
        }

        public Builder setEnhanceReload(float enhanceReload) {
            mResult.mEnhanceReload = enhanceReload;
            return this;
        }

        public Builder setUpgradeTowerName(String upgradeTowerName) {
            mResult.mUpgradeTowerName = upgradeTowerName;
            return this;
        }

        public Builder setUpgradeCost(int upgradeCost) {
            mResult.mUpgradeCost = upgradeCost;
            return this;
        }

        public Builder setUpgradeLevel(int upgradeLevel) {
            mResult.mUpgradeLevel = upgradeLevel;
            return this;
        }

        public TowerProperties build() {
            return mResult;
        }
    }

    public int getValue() {
        return mValue;
    }

    public int getDamage() {
        return mDamage;
    }

    public float getRange() {
        return mRange;
    }

    public float getReload() {
        return mReload;
    }

    public int getMaxLevel() {
        return mMaxLevel;
    }

    public WeaponType getWeaponType() {
        return mWeaponType;
    }

    public int getEnhanceCost() {
        return mEnhanceCost;
    }

    public float getEnhanceBase() {
        return mEnhanceBase;
    }

    public int getEnhanceDamage() {
        return mEnhanceDamage;
    }

    public float getEnhanceRange() {
        return mEnhanceRange;
    }

    public float getEnhanceReload() {
        return mEnhanceReload;
    }

    public String getUpgradeTowerName() {
        return mUpgradeTowerName;
    }

    public int getUpgradeCost() {
        return mUpgradeCost;
    }

    public int getUpgradeLevel() {
        return mUpgradeLevel;
    }

}
