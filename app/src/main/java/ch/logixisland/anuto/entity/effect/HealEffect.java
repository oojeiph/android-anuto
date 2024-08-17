package ch.logixisland.anuto.entity.effect;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Collection;

import ch.logixisland.anuto.engine.logic.GameEngine;
import ch.logixisland.anuto.engine.logic.entity.Entity;
import ch.logixisland.anuto.engine.render.Drawable;
import ch.logixisland.anuto.engine.render.Layers;
import ch.logixisland.anuto.entity.EntityTypes;
import ch.logixisland.anuto.entity.enemy.Enemy;
import ch.logixisland.anuto.util.iterator.StreamIterator;
import ch.logixisland.anuto.util.math.Vector2;

public class HealEffect extends Effect {

    private static final float EFFECT_DURATION = 0.7f;

    private static class StaticData {
        private Paint mPaint;
    }


    private class HealDrawable implements Drawable {
        public HealDrawable() {
        }

        @Override
        public int getLayer() {
            return Layers.SHOT;
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawCircle(getPosition().x(), getPosition().y(), mDrawRadius, mStaticData.mPaint);
        }
    }

    private final float mRange;
    private float mDrawRadius;
    private final float mHealAmount;

    private final Drawable mDrawable;
    private final Collection<Enemy> mHealedEnemies;
    private StaticData mStaticData;

    public HealEffect(Entity origin, Vector2 position, float amount, float radius, Collection<Enemy> healedEnemies) {
        super(origin, EFFECT_DURATION);
        setPosition(position);

        mHealAmount = amount;
        mRange = radius;
        mDrawRadius = 0f;
        mHealedEnemies = healedEnemies;
        mDrawable = new HealDrawable();
    }

    @Override
    public Object initStatic() {
        StaticData s = new StaticData();
        s.mPaint = new Paint();

        s.mPaint.setStyle(Paint.Style.STROKE);
        s.mPaint.setStrokeWidth(0.05f);
        s.mPaint.setColor(Color.BLUE);
        s.mPaint.setAlpha(70);
        return s;
    }
    @Override
    public void init() {
        super.init();

        mStaticData = (StaticData) getStaticData();
        getGameEngine().add(mDrawable);
    }

    @Override
    public void clean() {
        super.clean();

        getGameEngine().remove(mDrawable);
    }

    @Override
    public void tick() {
        super.tick();

        mDrawRadius += mRange / (GameEngine.TARGET_FRAME_RATE * EFFECT_DURATION);
    }

    @Override
    protected void effectBegin() {
        StreamIterator<Enemy> enemies = getGameEngine().getEntitiesByType(EntityTypes.ENEMY)
                .filter(inRange(getPosition(), mRange))
                .filter(mHealedEnemies)
                .cast(Enemy.class);

        while (enemies.hasNext()) {
            Enemy enemy = enemies.next();
            enemy.heal(mHealAmount * enemy.getMaxHealth());
            mHealedEnemies.add(enemy);
        }
    }
}
