package ch.logixisland.anuto.entity.tower;

import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.List;

import ch.logixisland.anuto.R;
import ch.logixisland.anuto.engine.logic.GameEngine;
import ch.logixisland.anuto.engine.logic.entity.Entity;
import ch.logixisland.anuto.engine.logic.entity.EntityFactory;
import ch.logixisland.anuto.engine.render.Layers;
import ch.logixisland.anuto.engine.render.sprite.SpriteInstance;
import ch.logixisland.anuto.engine.render.sprite.SpriteTemplate;
import ch.logixisland.anuto.engine.render.sprite.SpriteTransformation;
import ch.logixisland.anuto.engine.render.sprite.SpriteTransformer;
import ch.logixisland.anuto.engine.render.sprite.StaticSprite;
import ch.logixisland.anuto.engine.sound.Sound;
import ch.logixisland.anuto.entity.effect.BouncingLaser;
import ch.logixisland.anuto.entity.enemy.WeaponType;
import ch.logixisland.anuto.util.RandomUtils;
import ch.logixisland.anuto.util.math.Vector2;

public class SimpleLaser extends Tower implements SpriteTransformation {

    public final static String ENTITY_NAME = "simpleLaser";
    private final static float LASER_SPAWN_OFFSET = 0.7f;

    private final static TowerProperties TOWER_PROPERTIES = new TowerProperties.Builder()
            .setValue(150)
            .setDamage(230)
            .setRange(3.0f)
            .setReload(1.5f)
            .setMaxLevel(10)
            .setWeaponType(WeaponType.Laser)
            .setEnhanceBase(1.2f)
            .setEnhanceCost(50)
            .setEnhanceDamage(40)
            .setEnhanceRange(0.05f)
            .setEnhanceReload(0.1f)
            .setUpgradeTowerName(ch.logixisland.anuto.entity.tower.BouncingLaser.ENTITY_NAME)
            .setUpgradeCost(7000)
            .setUpgradeLevel(1)
            .build();

    public static class Factory extends EntityFactory {
        @Override
        public Entity create(GameEngine gameEngine) {
            return new SimpleLaser(gameEngine);
        }
    }

    public static class Persister extends TowerPersister {

    }

    private static class StaticData {
        SpriteTemplate mSpriteTemplateBase;
        SpriteTemplate mSpriteTemplateCanon;
    }

    private float mAngle = 90f;
    private final Aimer mAimer = new Aimer(this);

    private final StaticSprite mSpriteBase;
    private final StaticSprite mSpriteCanon;
    private final Sound mSound;

    private SimpleLaser(GameEngine gameEngine) {
        super(gameEngine, TOWER_PROPERTIES);
        StaticData s = (StaticData) getStaticData();

        mSpriteBase = getSpriteFactory().createStatic(Layers.TOWER_BASE, s.mSpriteTemplateBase);
        mSpriteBase.setIndex(RandomUtils.next(4));
        mSpriteBase.setListener(this);

        mSpriteCanon = getSpriteFactory().createStatic(Layers.TOWER, s.mSpriteTemplateCanon);
        mSpriteCanon.setIndex(RandomUtils.next(4));
        mSpriteCanon.setListener(this);

        mSound = getSoundFactory().createSound(R.raw.laser1_zz);
    }

    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    @Override
    public Object initStatic() {
        StaticData s = new StaticData();

        s.mSpriteTemplateBase = getSpriteFactory().createTemplate(R.attr.base5, 4);
        s.mSpriteTemplateBase.setMatrix(1f, 1f, null, -90f);

        s.mSpriteTemplateCanon = getSpriteFactory().createTemplate(R.attr.laserTower1, 4);
        s.mSpriteTemplateCanon.setMatrix(0.4f, 0.9f, new Vector2(0.2f, 0.2f), -90f);

        return s;
    }

    @Override
    public void init() {
        super.init();

        getGameEngine().add(mSpriteBase);
        getGameEngine().add(mSpriteCanon);
    }

    @Override
    public void clean() {
        super.clean();

        getGameEngine().remove(mSpriteBase);
        getGameEngine().remove(mSpriteCanon);
    }

    @Override
    public void tick() {
        super.tick();
        mAimer.tick();

        if (mAimer.getTarget() != null) {
            mAngle = getAngleTo(mAimer.getTarget());

            if (isReloaded()) {
                Vector2 from = Vector2.polar(LASER_SPAWN_OFFSET, mAngle).add(getPosition());
                getGameEngine().add(new BouncingLaser(this, from, mAimer.getTarget(), getDamage()));
                setReloaded(false);
                mSound.play();
            }
        }
    }

    @Override
    public Aimer getAimer() {
        return mAimer;
    }

    @Override
    public void draw(SpriteInstance sprite, Canvas canvas) {
        SpriteTransformer.translate(canvas, getPosition());
        canvas.rotate(mAngle);
    }

    @Override
    public void preview(Canvas canvas) {
        mSpriteBase.draw(canvas);
        mSpriteCanon.draw(canvas);
    }

    @Override
    public List<TowerInfoValue> getTowerInfoValues() {
        List<TowerInfoValue> properties = new ArrayList<>();
        properties.add(new TowerInfoValue(R.string.damage, getDamage()));
        properties.add(new TowerInfoValue(R.string.reload, getReloadTime()));
        properties.add(new TowerInfoValue(R.string.dps, getDamage() / getReloadTime()));
        properties.add(new TowerInfoValue(R.string.range, getRange()));
        properties.add(new TowerInfoValue(R.string.inflicted, getDamageInflicted()));
        return properties;
    }
}
