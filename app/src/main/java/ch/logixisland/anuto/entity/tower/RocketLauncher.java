package ch.logixisland.anuto.entity.tower;

import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.List;

import ch.logixisland.anuto.R;
import ch.logixisland.anuto.engine.logic.GameEngine;
import ch.logixisland.anuto.engine.logic.entity.Entity;
import ch.logixisland.anuto.engine.logic.entity.EntityFactory;
import ch.logixisland.anuto.engine.logic.loop.TickTimer;
import ch.logixisland.anuto.engine.render.Layers;
import ch.logixisland.anuto.engine.render.sprite.SpriteInstance;
import ch.logixisland.anuto.engine.render.sprite.SpriteTemplate;
import ch.logixisland.anuto.engine.render.sprite.SpriteTransformation;
import ch.logixisland.anuto.engine.render.sprite.SpriteTransformer;
import ch.logixisland.anuto.engine.render.sprite.StaticSprite;
import ch.logixisland.anuto.engine.sound.Sound;
import ch.logixisland.anuto.entity.enemy.WeaponType;
import ch.logixisland.anuto.entity.shot.Rocket;
import ch.logixisland.anuto.util.RandomUtils;

public class RocketLauncher extends Tower implements SpriteTransformation {

    public final static String ENTITY_NAME = "rocketLauncher";
    private final static float ROCKET_LOAD_TIME = 1.0f;
    private final static float EXPLOSION_RADIUS = 1.7f;
    private final static float ENHANCE_EXPLOSION_RADIUS = 0.05f;

    private final static TowerProperties TOWER_PROPERTIES = new TowerProperties.Builder()
            .setValue(113100)
            .setDamage(48000)
            .setRange(3.0f)
            .setReload(3.0f)
            .setMaxLevel(15)
            .setWeaponType(WeaponType.Explosive)
            .setEnhanceBase(1.5f)
            .setEnhanceCost(950)
            .setEnhanceDamage(410)
            .setEnhanceRange(0.1f)
            .setEnhanceReload(0.07f)
            .setUpgradeLevel(3)
            .build();

    public static class Factory extends EntityFactory {
        @Override
        public Entity create(GameEngine gameEngine) {
            return new RocketLauncher(gameEngine);
        }
    }

    public static class Persister extends TowerPersister {

    }

    private static class StaticData {
        SpriteTemplate mSpriteTemplate;
        SpriteTemplate mSpriteTemplateRocket; // used for preview only
    }

    private float mExplosionRadius;
    private float mAngle = 90f;
    private Rocket mRocket;
    private final TickTimer mRocketLoadTimer;
    private final Aimer mAimer = new Aimer(this);

    private final StaticSprite mSprite;
    private final StaticSprite mSpriteRocket; // used for preview only
    private final Sound mSound;

    private RocketLauncher(GameEngine gameEngine) {
        super(gameEngine, TOWER_PROPERTIES);
        StaticData s = (StaticData) getStaticData();

        mSprite = getSpriteFactory().createStatic(Layers.TOWER_BASE, s.mSpriteTemplate);
        mSprite.setListener(this);
        mSprite.setIndex(RandomUtils.next(4));

        mSpriteRocket = getSpriteFactory().createStatic(Layers.TOWER, s.mSpriteTemplateRocket);
        mSpriteRocket.setListener(this);
        mSpriteRocket.setIndex(RandomUtils.next(4));

        mExplosionRadius = EXPLOSION_RADIUS;
        mRocketLoadTimer = TickTimer.createInterval(ROCKET_LOAD_TIME);

        mSound = getSoundFactory().createSound(R.raw.explosive2_tsh);
    }

    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    @Override
    public Object initStatic() {
        StaticData s = new StaticData();

        s.mSpriteTemplate = getSpriteFactory().createTemplate(R.attr.rocketLauncher, 4);
        s.mSpriteTemplate.setMatrix(1.1f, 1.1f, null, -90f);

        s.mSpriteTemplateRocket = getSpriteFactory().createTemplate(R.attr.rocket, 4);
        s.mSpriteTemplateRocket.setMatrix(0.8f, 1f, null, -90f);

        return s;
    }

    @Override
    public void init() {
        super.init();

        getGameEngine().add(mSprite);
    }

    @Override
    public void clean() {
        super.clean();

        getGameEngine().remove(mSprite);

        if (mRocket != null) {
            mRocket.remove();
        }
    }

    @Override
    public void enhance() {
        super.enhance();
        mExplosionRadius += ENHANCE_EXPLOSION_RADIUS;
    }

    @Override
    public void tick() {
        super.tick();
        mAimer.tick();

        if (mRocket == null && mRocketLoadTimer.tick()) {
            mRocket = new Rocket(this, getPosition(), getDamage(), mExplosionRadius);
            mRocket.setAngle(mAngle);
            getGameEngine().add(mRocket);
        }

        if (mAimer.getTarget() != null) {
            mAngle = getAngleTo(mAimer.getTarget());

            if (mRocket != null) {
                mRocket.setAngle(mAngle);

                if (isReloaded()) {
                    mRocket.setTarget(mAimer.getTarget());
                    mRocket.setEnabled(true);
                    mRocket = null;
                    mSound.play();

                    setReloaded(false);
                }
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
        mSprite.draw(canvas);
        mSpriteRocket.draw(canvas);
    }

    @Override
    public List<TowerInfoValue> getTowerInfoValues() {
        List<TowerInfoValue> properties = new ArrayList<>();
        properties.add(new TowerInfoValue(R.string.damage, getDamage()));
        properties.add(new TowerInfoValue(R.string.splash, mExplosionRadius));
        properties.add(new TowerInfoValue(R.string.reload, getReloadTime()));
        properties.add(new TowerInfoValue(R.string.dps, getDamage() / getReloadTime()));
        properties.add(new TowerInfoValue(R.string.range, getRange()));
        properties.add(new TowerInfoValue(R.string.inflicted, getDamageInflicted()));
        return properties;
    }
}
