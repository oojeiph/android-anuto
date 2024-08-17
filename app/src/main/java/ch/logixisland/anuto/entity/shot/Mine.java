package ch.logixisland.anuto.entity.shot;

import android.graphics.Canvas;

import ch.logixisland.anuto.R;
import ch.logixisland.anuto.engine.logic.GameEngine;
import ch.logixisland.anuto.engine.logic.entity.Entity;
import ch.logixisland.anuto.engine.logic.loop.TickTimer;
import ch.logixisland.anuto.engine.render.Layers;
import ch.logixisland.anuto.engine.render.sprite.SpriteInstance;
import ch.logixisland.anuto.engine.render.sprite.SpriteTemplate;
import ch.logixisland.anuto.engine.render.sprite.SpriteTransformation;
import ch.logixisland.anuto.engine.render.sprite.SpriteTransformer;
import ch.logixisland.anuto.engine.render.sprite.StaticSprite;
import ch.logixisland.anuto.entity.EntityTypes;
import ch.logixisland.anuto.entity.effect.Explosion;
import ch.logixisland.anuto.entity.enemy.Enemy;
import ch.logixisland.anuto.entity.enemy.Flyer;
import ch.logixisland.anuto.util.RandomUtils;
import ch.logixisland.anuto.util.iterator.StreamIterator;
import ch.logixisland.anuto.util.math.Function;
import ch.logixisland.anuto.util.math.SampledFunction;
import ch.logixisland.anuto.util.math.Vector2;

public class Mine extends Shot implements SpriteTransformation {

    private final static float TRIGGER_RADIUS = 0.7f;

    private final static float TIME_TO_TARGET = 1.5f;
    private final static float ROTATION_RATE_MIN = 0.5f;
    private final static float ROTATION_RATE_MAX = 2.0f;
    private final static float HEIGHT_SCALING_START = 0.5f;
    private final static float HEIGHT_SCALING_STOP = 1.0f;
    private final static float HEIGHT_SCALING_PEAK = 1.5f;

    private static class StaticData {
        SpriteTemplate mSpriteTemplate;
    }

    private final float mDamage;
    private final float mRadius;
    private float mAngle;
    private boolean mFlying;
    private float mRotationStep;
    private final SampledFunction mHeightScalingFunction;
    private Vector2 mTarget;

    private StaticSprite mSpriteFlying;
    private StaticSprite mSpriteMine;

    private final TickTimer mUpdateTimer = TickTimer.createInterval(0.1f);

    public Mine(Entity origin, Vector2 position, Vector2 target, float damage, float radius) {
        super(origin);

        setPosition(position);
        setSpeed(getDistanceTo(target) / TIME_TO_TARGET);
        setDirection(getDirectionTo(target));

        mFlying = true;
        mDamage = damage;
        mRadius = radius;
        mTarget = target;

        mRotationStep = RandomUtils.next(ROTATION_RATE_MIN, ROTATION_RATE_MAX) * 360f / GameEngine.TARGET_FRAME_RATE;

        float x1 = (float) Math.sqrt(HEIGHT_SCALING_PEAK - HEIGHT_SCALING_START);
        float x2 = (float) Math.sqrt(HEIGHT_SCALING_PEAK - HEIGHT_SCALING_STOP);
        mHeightScalingFunction = Function.quadratic()
                .multiply(-1f)
                .offset(HEIGHT_SCALING_PEAK)
                .shift(-x1)
                .stretch(GameEngine.TARGET_FRAME_RATE * TIME_TO_TARGET / (x1 + x2))
                .sample();

        createAssets();
    }

    public Mine(Entity origin, Vector2 position, float damage, float radius) {
        super(origin);

        setPosition(position);
        setDirection(new Vector2());

        mFlying = false;
        mDamage = damage;
        mRadius = radius;
        mAngle = RandomUtils.next(0f, 360f);

        mHeightScalingFunction = Function.constant(HEIGHT_SCALING_STOP).sample();

        createAssets();
    }

    private void createAssets() {
        StaticData s = (StaticData) getStaticData();

        int index = RandomUtils.next(4);

        mSpriteFlying = getSpriteFactory().createStatic(Layers.SHOT, s.mSpriteTemplate);
        mSpriteFlying.setListener(this);
        mSpriteFlying.setIndex(index);

        mSpriteMine = getSpriteFactory().createStatic(Layers.BOTTOM, s.mSpriteTemplate);
        mSpriteMine.setListener(this);
        mSpriteMine.setIndex(index);
    }

    @Override
    public Object initStatic() {
        StaticData s = new StaticData();

        s.mSpriteTemplate = getSpriteFactory().createTemplate(R.attr.mine, 4);
        s.mSpriteTemplate.setMatrix(0.7f, 0.7f, null, null);

        return s;
    }

    @Override
    public void init() {
        super.init();

        if (mFlying) {
            getGameEngine().add(mSpriteFlying);
        } else {
            getGameEngine().add(mSpriteMine);
        }
    }

    @Override
    public void clean() {
        super.clean();

        if (mFlying) {
            getGameEngine().remove(mSpriteFlying);
        } else {
            getGameEngine().remove(mSpriteMine);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (mFlying) {
            mAngle += mRotationStep;
            mHeightScalingFunction.step();

            if (mHeightScalingFunction.getPosition() >= GameEngine.TARGET_FRAME_RATE * TIME_TO_TARGET) {
                getGameEngine().remove(mSpriteFlying);
                getGameEngine().add(mSpriteMine);

                mFlying = false;
                setSpeed(0f);
            }
        } else if (mUpdateTimer.tick()) {
            StreamIterator<Enemy> enemiesInRange = getGameEngine().getEntitiesByType(EntityTypes.ENEMY)
                    .filter(inRange(getPosition(), TRIGGER_RADIUS))
                    .cast(Enemy.class)
                    .filter(value -> !(value instanceof Flyer));

            if (!enemiesInRange.isEmpty()) {
                getGameEngine().add(new Explosion(getOrigin(), getPosition(), mDamage, mRadius));
                this.remove();
            }
        }
    }

    @Override
    public void draw(SpriteInstance sprite, Canvas canvas) {
        float s = mHeightScalingFunction.getValue();
        SpriteTransformer.translate(canvas, getPosition());
        SpriteTransformer.scale(canvas, s);
        canvas.rotate(mAngle);
    }

    public boolean isFlying() {
        return mFlying;
    }
}
