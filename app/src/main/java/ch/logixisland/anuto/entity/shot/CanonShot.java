package ch.logixisland.anuto.entity.shot;

import android.graphics.Canvas;

import ch.logixisland.anuto.R;
import ch.logixisland.anuto.engine.logic.GameEngine;
import ch.logixisland.anuto.engine.logic.entity.Entity;
import ch.logixisland.anuto.engine.render.Layers;
import ch.logixisland.anuto.engine.render.sprite.SpriteInstance;
import ch.logixisland.anuto.engine.render.sprite.SpriteTemplate;
import ch.logixisland.anuto.engine.render.sprite.SpriteTransformation;
import ch.logixisland.anuto.engine.render.sprite.SpriteTransformer;
import ch.logixisland.anuto.engine.render.sprite.StaticSprite;
import ch.logixisland.anuto.entity.enemy.Enemy;
import ch.logixisland.anuto.util.RandomUtils;
import ch.logixisland.anuto.util.math.Vector2;

public class CanonShot extends Shot implements SpriteTransformation, TargetTracker.Listener {

    private final static float MOVEMENT_SPEED = 4.0f;
    private final static float ROTATION_SPEED = 1.0f;
    private final static float ROTATION_STEP = ROTATION_SPEED * 360f / GameEngine.TARGET_FRAME_RATE;

    private static class StaticData {
        public SpriteTemplate mSpriteTemplate;
    }

    private float mAngle;
    private final float mDamage;
    private final TargetTracker mTracker;

    private final StaticSprite mSprite;

    public CanonShot(Entity origin, Vector2 position, Enemy target, float damage) {
        super(origin);
        setPosition(position);
        setSpeed(MOVEMENT_SPEED);

        mDamage = damage;
        mTracker = new TargetTracker(target, this, this);

        StaticData s = (StaticData) getStaticData();

        mSprite = getSpriteFactory().createStatic(Layers.SHOT, s.mSpriteTemplate);
        mSprite.setListener(this);
        mSprite.setIndex(RandomUtils.next(4));
    }

    @Override
    public Object initStatic() {
        StaticData s = new StaticData();

        s.mSpriteTemplate = getSpriteFactory().createTemplate(R.attr.canonShot, 4);
        s.mSpriteTemplate.setMatrix(0.33f, 0.33f, null, null);

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
    }

    @Override
    public void tick() {
        setDirection(mTracker.getTargetDirection());
        mAngle += ROTATION_STEP;
        super.tick();
        mTracker.tick();
    }

    @Override
    public void draw(SpriteInstance sprite, Canvas canvas) {
        SpriteTransformer.translate(canvas, getPosition());
        canvas.rotate(mAngle);
    }

    @Override
    public void targetLost(Enemy target) {
        this.remove();
    }

    @Override
    public void targetReached(Enemy target) {
        target.damage(mDamage, getOrigin());
        this.remove();
    }
}
