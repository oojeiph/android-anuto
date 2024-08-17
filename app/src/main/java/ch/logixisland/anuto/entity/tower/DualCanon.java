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
import ch.logixisland.anuto.entity.enemy.WeaponType;
import ch.logixisland.anuto.entity.shot.CanonShot;
import ch.logixisland.anuto.entity.shot.Shot;
import ch.logixisland.anuto.util.RandomUtils;
import ch.logixisland.anuto.util.math.Function;
import ch.logixisland.anuto.util.math.SampledFunction;
import ch.logixisland.anuto.util.math.Vector2;

public class DualCanon extends Tower implements SpriteTransformation {

    public final static String ENTITY_NAME = "dualCanon";
    private final static float SHOT_SPAWN_OFFSET = 0.7f;
    private final static float REBOUND_RANGE = 0.25f;
    private final static float REBOUND_DURATION = 0.2f;

    private final static TowerProperties TOWER_PROPERTIES = new TowerProperties.Builder()
            .setValue(5700)
            .setDamage(3400)
            .setRange(3.0f)
            .setReload(0.5f)
            .setMaxLevel(10)
            .setWeaponType(WeaponType.Bullet)
            .setEnhanceBase(1.4f)
            .setEnhanceCost(470)
            .setEnhanceDamage(160)
            .setEnhanceRange(0.05f)
            .setEnhanceReload(0.03f)
            .setUpgradeTowerName(MachineGun.ENTITY_NAME)
            .setUpgradeCost(88500)
            .setUpgradeLevel(2)
            .build();

    public static class Factory extends EntityFactory {
        @Override
        public Entity create(GameEngine gameEngine) {
            return new DualCanon(gameEngine);
        }
    }

    public static class Persister extends TowerPersister {

    }

    private static class StaticData {
        SpriteTemplate mSpriteTemplateBase;
        SpriteTemplate mSpriteTemplateTower;
        SpriteTemplate mSpriteTemplateCanon;
    }

    private static class SubCanon {
        boolean reboundActive;
        SampledFunction reboundFunction;
        StaticSprite sprite;
    }

    private float mAngle = 90f;
    private boolean mShoot2 = false;
    private SubCanon[] mCanons = new SubCanon[2];
    private final Aimer mAimer = new Aimer(this);

    private final StaticSprite mSpriteBase;
    private final StaticSprite mSpriteTower;

    private final Sound mSound;

    private DualCanon(GameEngine gameEngine) {
        super(gameEngine, TOWER_PROPERTIES);
        StaticData s = (StaticData) getStaticData();

        Function reboundFunction = Function.sine()
                .multiply(REBOUND_RANGE)
                .stretch(GameEngine.TARGET_FRAME_RATE * REBOUND_DURATION / (float) Math.PI);

        mSpriteBase = getSpriteFactory().createStatic(Layers.TOWER_BASE, s.mSpriteTemplateBase);
        mSpriteBase.setListener(this);
        mSpriteBase.setIndex(RandomUtils.next(4));

        mSpriteTower = getSpriteFactory().createStatic(Layers.TOWER_LOWER, s.mSpriteTemplateTower);
        mSpriteTower.setListener(this);
        mSpriteTower.setIndex(RandomUtils.next(4));

        for (int i = 0; i < mCanons.length; i++) {
            mCanons[i] = new SubCanon();
            mCanons[i].reboundFunction = reboundFunction.sample();
            mCanons[i].reboundActive = false;

            mCanons[i].sprite = getSpriteFactory().createStatic(Layers.TOWER, s.mSpriteTemplateCanon);
            mCanons[i].sprite.setListener(this);
            mCanons[i].sprite.setIndex(RandomUtils.next(4));
        }

        mSound = getSoundFactory().createSound(R.raw.gun3_dit);
        mSound.setVolume(0.5f);
    }

    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    @Override
    public Object initStatic() {
        StaticData s = new StaticData();

        s.mSpriteTemplateBase = getSpriteFactory().createTemplate(R.attr.base1, 4);
        s.mSpriteTemplateBase.setMatrix(1f, 1f, null, null);

        s.mSpriteTemplateTower = getSpriteFactory().createTemplate(R.attr.canonDual, 4);
        s.mSpriteTemplateTower.setMatrix(0.5f, 0.5f, null, -90f);

        s.mSpriteTemplateCanon = getSpriteFactory().createTemplate(R.attr.canon, 4);
        s.mSpriteTemplateCanon.setMatrix(0.3f, 1.0f, new Vector2(0.15f, 0.4f), -90f);

        return s;
    }

    @Override
    public void init() {
        super.init();

        getGameEngine().add(mSpriteBase);
        getGameEngine().add(mSpriteTower);

        for (SubCanon c : mCanons) {
            getGameEngine().add(c.sprite);
        }
    }

    @Override
    public void clean() {
        super.clean();

        getGameEngine().remove(mSpriteBase);
        getGameEngine().remove(mSpriteTower);

        for (SubCanon c : mCanons) {
            getGameEngine().remove(c.sprite);
        }

        mCanons = null;
    }

    @Override
    public void tick() {
        super.tick();
        mAimer.tick();

        if (mAimer.getTarget() != null) {
            mAngle = getAngleTo(mAimer.getTarget());

            if (isReloaded()) {
                if (!mShoot2) {
                    Shot shot = new CanonShot(this, getPosition(), mAimer.getTarget(), getDamage());
                    shot.move(Vector2.polar(SHOT_SPAWN_OFFSET, mAngle));
                    shot.move(Vector2.polar(0.3f, mAngle + 90f));
                    getGameEngine().add(shot);

                    setReloaded(false);
                    mCanons[0].reboundActive = true;
                    mShoot2 = true;
                } else {
                    Shot shot = new CanonShot(this, getPosition(), mAimer.getTarget(), getDamage());
                    shot.move(Vector2.polar(SHOT_SPAWN_OFFSET, mAngle));
                    shot.move(Vector2.polar(0.3f, mAngle - 90f));
                    getGameEngine().add(shot);

                    setReloaded(false);
                    mCanons[1].reboundActive = true;
                    mShoot2 = false;
                }

                mSound.play();
            }
        }

        if (mCanons[0].reboundActive) {
            mCanons[0].reboundFunction.step();
            if (mCanons[0].reboundFunction.getPosition() >= GameEngine.TARGET_FRAME_RATE * REBOUND_DURATION) {
                mCanons[0].reboundFunction.reset();
                mCanons[0].reboundActive = false;
            }
        }

        if (mCanons[1].reboundActive) {
            mCanons[1].reboundFunction.step();
            if (mCanons[1].reboundFunction.getPosition() >= GameEngine.TARGET_FRAME_RATE * REBOUND_DURATION) {
                mCanons[1].reboundFunction.reset();
                mCanons[1].reboundActive = false;
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

        if (sprite == mCanons[0].sprite) {
            canvas.translate(0, 0.3f);

            if (mCanons[0].reboundActive) {
                canvas.translate(-mCanons[0].reboundFunction.getValue(), 0);
            }
        }

        if (sprite == mCanons[1].sprite) {
            canvas.translate(0, -0.3f);

            if (mCanons[1].reboundActive) {
                canvas.translate(-mCanons[1].reboundFunction.getValue(), 0);
            }
        }
    }

    @Override
    public void preview(Canvas canvas) {
        mSpriteBase.draw(canvas);
        mSpriteTower.draw(canvas);
        mCanons[0].sprite.draw(canvas);
        mCanons[1].sprite.draw(canvas);
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
