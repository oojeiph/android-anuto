package ch.logixisland.anuto.entity.shot;

import android.graphics.Canvas;

import ch.logixisland.anuto.R;
import ch.logixisland.anuto.engine.logic.GameEngine;
import ch.logixisland.anuto.engine.logic.entity.Entity;
import ch.logixisland.anuto.engine.render.Layers;
import ch.logixisland.anuto.engine.render.sprite.AnimatedSprite;
import ch.logixisland.anuto.engine.render.sprite.SpriteInstance;
import ch.logixisland.anuto.engine.render.sprite.SpriteTemplate;
import ch.logixisland.anuto.engine.render.sprite.SpriteTransformation;
import ch.logixisland.anuto.engine.render.sprite.SpriteTransformer;
import ch.logixisland.anuto.engine.sound.Sound;
import ch.logixisland.anuto.entity.effect.GlueEffect;
import ch.logixisland.anuto.util.math.Vector2;

public class GlueShot extends Shot implements SpriteTransformation {

    public final static float MOVEMENT_SPEED = 4.0f;
    private final static float ANIMATION_SPEED = 1.0f;

    private static class StaticData {

        public SpriteTemplate mSpriteTemplate;
    }

    private final float mIntensity;

    private final float mDuration;
    private final Vector2 mTarget;
    private final AnimatedSprite mSprite;

    private final Sound mSound;

    public GlueShot(Entity origin, Vector2 position, Vector2 target, float intensity, float duration) {
        super(origin);
        setPosition(position);
        mTarget = target;

        setSpeed(MOVEMENT_SPEED);
        setDirection(getDirectionTo(target));

        mIntensity = intensity;
        mDuration = duration;

        StaticData s = (StaticData) getStaticData();

        mSprite = getSpriteFactory().createAnimated(Layers.SHOT, s.mSpriteTemplate);
        mSprite.setListener(this);
        mSprite.setSequenceForward();
        mSprite.setFrequency(ANIMATION_SPEED);

        mSound = getSoundFactory().createSound(R.raw.gas1_pff);
    }

    @Override
    public Object initStatic() {
        StaticData s = new StaticData();

        s.mSpriteTemplate = getSpriteFactory().createTemplate(R.attr.glueShot, 6);
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
        super.tick();

        mSprite.tick();

        if (getDistanceTo(mTarget) < getSpeed() / GameEngine.TARGET_FRAME_RATE) {
            getGameEngine().add(new GlueEffect(getOrigin(), mTarget, mIntensity, mDuration));
            mSound.play();
            this.remove();
        }
    }

    @Override
    public void draw(SpriteInstance sprite, Canvas canvas) {
        SpriteTransformer.translate(canvas, getPosition());
    }
}
