package ch.logixisland.anuto.entity.effect;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import ch.logixisland.anuto.R;
import ch.logixisland.anuto.engine.logic.GameEngine;
import ch.logixisland.anuto.engine.logic.entity.Entity;
import ch.logixisland.anuto.engine.render.Drawable;
import ch.logixisland.anuto.engine.render.Layers;
import ch.logixisland.anuto.engine.sound.Sound;
import ch.logixisland.anuto.entity.EntityTypes;
import ch.logixisland.anuto.entity.enemy.Enemy;
import ch.logixisland.anuto.util.iterator.StreamIterator;
import ch.logixisland.anuto.util.math.Vector2;

public class Explosion extends Effect {

    private final static float EFFECT_DURATION = 0.2f;
    private final static int ALPHA_START = 180;
    private final static int ALPHA_STEP = (int) (ALPHA_START / (GameEngine.TARGET_FRAME_RATE * EFFECT_DURATION));

    private class ExplosionDrawable implements Drawable {
        private final Paint mPaint;
        private int mAlpha = ALPHA_START;

        private ExplosionDrawable() {
            mPaint = new Paint();
            mPaint.setColor(Color.YELLOW);
            mPaint.setAlpha(mAlpha);
        }

        private void decreaseVisibility() {
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
            canvas.drawCircle(getPosition().x(), getPosition().y(), mRadius, mPaint);
        }
    }

    private final float mDamage;
    private final float mRadius;

    private final ExplosionDrawable mDrawObject;
    private final Sound mSound;

    public Explosion(Entity origin, Vector2 position, float damage, float radius) {
        super(origin, EFFECT_DURATION);
        setPosition(position);

        mDamage = damage;
        mRadius = radius;

        mDrawObject = new ExplosionDrawable();
        mSound = getSoundFactory().createSound(R.raw.explosive3_bghgh);
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
        mSound.play();

        StreamIterator<Enemy> enemies = getGameEngine().getEntitiesByType(EntityTypes.ENEMY)
                .filter(inRange(getPosition(), mRadius))
                .cast(Enemy.class);

        while (enemies.hasNext()) {
            Enemy enemy = enemies.next();
            enemy.damage(mDamage, getOrigin());
        }
    }
}
