package ch.logixisland.anuto.entity.effect;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.Collection;

import ch.logixisland.anuto.engine.logic.GameEngine;
import ch.logixisland.anuto.engine.logic.entity.Entity;
import ch.logixisland.anuto.engine.render.Drawable;
import ch.logixisland.anuto.engine.render.Layers;
import ch.logixisland.anuto.entity.EntityTypes;
import ch.logixisland.anuto.entity.enemy.Enemy;
import ch.logixisland.anuto.entity.enemy.Flyer;
import ch.logixisland.anuto.util.iterator.StreamIterator;
import ch.logixisland.anuto.util.math.Vector2;

public class StraightLaser extends Effect {

    private final static float LASER_WIDTH = 0.7f;

    private final static float FLYER_STUN_INTENSITY = 20.0f;
    private final static float EFFECT_DURATION = 1.0f;
    private final static float VISIBLE_EFFECT_DURATION = 0.5f;
    private final static int ALPHA_START = 180;
    private final static int ALPHA_STEP = (int) (ALPHA_START / (GameEngine.TARGET_FRAME_RATE * VISIBLE_EFFECT_DURATION));

    private class LaserDrawable implements Drawable {
        private final Paint mPaint;
        private int mAlpha = ALPHA_START;

        public LaserDrawable() {
            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(0.1f);
            mPaint.setColor(Color.RED);
        }

        public void decreaseVisibility() {
            mAlpha -= ALPHA_STEP;

            if (mAlpha < 0) {
                mAlpha = 0;
            }

            mPaint.setAlpha(mAlpha);
        }

        @Override
        public int getLayer() {
            return Layers.SHOT;
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawLine(getPosition().x(), getPosition().y(), mLaserTo.x(), mLaserTo.y(), mPaint);
        }
    }

    private final float mDamage;
    private final Vector2 mLaserTo;
    private final Collection<Flyer> mStunnedFliers = new ArrayList<>();

    private final LaserDrawable mDrawObject;

    public StraightLaser(Entity origin, Vector2 position, Vector2 laserTo, float damage) {
        super(origin, EFFECT_DURATION);
        setPosition(position);

        mLaserTo = laserTo;
        mDamage = damage;

        mDrawObject = new LaserDrawable();
    }

    @Override
    public void init() {
        super.init();

        getGameEngine().add(mDrawObject);
    }

    @Override
    public void clean() {
        super.clean();

        getGameEngine().remove(mDrawObject);
    }

    @Override
    public void tick() {
        super.tick();

        mDrawObject.decreaseVisibility();
    }

    @Override
    protected void effectBegin() {
        StreamIterator<Enemy> enemies = getGameEngine().getEntitiesByType(EntityTypes.ENEMY)
                .filter(onLine(getPosition(), mLaserTo, LASER_WIDTH))
                .cast(Enemy.class);

        while (enemies.hasNext()) {
            Enemy enemy = enemies.next();
            enemy.damage(mDamage, getOrigin());

            if (enemy instanceof Flyer) {
                Flyer flyer = (Flyer) enemy;
                flyer.modifySpeed(1.0f / FLYER_STUN_INTENSITY, getOrigin());
                mStunnedFliers.add(flyer);
            }
        }
    }

    @Override
    protected void effectEnd() {
        for (Flyer flyer : mStunnedFliers) {
            flyer.modifySpeed(FLYER_STUN_INTENSITY, getOrigin());
        }
    }
}
