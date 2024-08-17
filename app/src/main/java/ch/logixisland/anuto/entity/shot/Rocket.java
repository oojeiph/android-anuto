package ch.logixisland.anuto.entity.shot;

import android.graphics.Canvas;

import ch.logixisland.anuto.R;
import ch.logixisland.anuto.engine.logic.entity.Entity;
import ch.logixisland.anuto.engine.render.Layers;
import ch.logixisland.anuto.engine.render.sprite.AnimatedSprite;
import ch.logixisland.anuto.engine.render.sprite.SpriteInstance;
import ch.logixisland.anuto.engine.render.sprite.SpriteTemplate;
import ch.logixisland.anuto.engine.render.sprite.SpriteTransformation;
import ch.logixisland.anuto.engine.render.sprite.SpriteTransformer;
import ch.logixisland.anuto.engine.render.sprite.StaticSprite;
import ch.logixisland.anuto.entity.EntityTypes;
import ch.logixisland.anuto.entity.effect.Explosion;
import ch.logixisland.anuto.entity.enemy.Enemy;
import ch.logixisland.anuto.util.RandomUtils;
import ch.logixisland.anuto.util.math.Vector2;

public class Rocket extends Shot implements SpriteTransformation, TargetTracker.Listener {

    private final static float MOVEMENT_SPEED = 2.5f;
    private final static float ANIMATION_SPEED = 3f;

    private static class StaticData {
        SpriteTemplate mSpriteTemplate;
        SpriteTemplate mSpriteTemplateFire;
    }

    private final float mDamage;
    private final float mRadius;
    private float mAngle;
    private final TargetTracker mTracker;

    private final StaticSprite mSprite;
    private final AnimatedSprite mSpriteFire;

    public Rocket(Entity origin, Vector2 position, float damage, float radius) {
        super(origin);
        setPosition(position);
        setSpeed(MOVEMENT_SPEED);
        setEnabled(false);

        mDamage = damage;
        mRadius = radius;
        mTracker = new TargetTracker(this, this);

        StaticData s = (StaticData) getStaticData();

        mSprite = getSpriteFactory().createStatic(Layers.SHOT, s.mSpriteTemplate);
        mSprite.setListener(this);
        mSprite.setIndex(RandomUtils.next(4));

        mSpriteFire = getSpriteFactory().createAnimated(Layers.SHOT, s.mSpriteTemplateFire);
        mSpriteFire.setListener(this);
        mSpriteFire.setSequenceForward();
        mSpriteFire.setFrequency(ANIMATION_SPEED);
    }

    public void setAngle(float angle) {
        mAngle = angle;
    }

    public void setTarget(Enemy target) {
        mTracker.setTarget(target);
    }

    @Override
    public Object initStatic() {
        StaticData s = new StaticData();

        s.mSpriteTemplate = getSpriteFactory().createTemplate(R.attr.rocket, 4);
        s.mSpriteTemplate.setMatrix(0.8f, 1f, null, -90f);

        s.mSpriteTemplateFire = getSpriteFactory().createTemplate(R.attr.rocketFire, 4);
        s.mSpriteTemplateFire.setMatrix(0.3f, 0.3f, new Vector2(0.15f, 0.6f), -90f);

        return s;
    }

    @Override
    public void init() {
        super.init();
        getGameEngine().add(mSprite);

        if (isEnabled()) {
            getGameEngine().add(mSpriteFire);
        }
    }

    @Override
    public void clean() {
        super.clean();
        getGameEngine().remove(mSprite);

        if (isEnabled()) {
            getGameEngine().remove(mSpriteFire);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if (!isEnabled() && mSpriteFire != null) {
            getGameEngine().remove(mSpriteFire);
        }

        if (isEnabled() && mSpriteFire != null) {
            getGameEngine().add(mSpriteFire);
        }
    }

    public void draw(SpriteInstance sprite, Canvas canvas) {
        SpriteTransformer.translate(canvas, getPosition());
        canvas.rotate(mAngle);
    }

    @Override
    public void tick() {
        if (isEnabled()) {
            Vector2 direction = mTracker.getTargetDirection();
            setDirection(direction);
            mAngle = direction.angle();

            mSpriteFire.tick();
        }

        super.tick();
        mTracker.tick();
    }

    @Override
    public void targetLost(Enemy target) {
        Enemy closest = (Enemy) getGameEngine().getEntitiesByType(EntityTypes.ENEMY)
                .min(distanceTo(getPosition()));

        if (closest == null) {
            this.remove();
        } else {
            mTracker.setTarget(closest);
        }
    }

    @Override
    public void targetReached(Enemy target) {
        getGameEngine().add(new Explosion(getOrigin(), target.getPosition(), mDamage, mRadius));
        this.remove();
    }
}
