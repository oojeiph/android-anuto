package ch.logixisland.anuto.entity.enemy;

import android.graphics.Canvas;

import ch.logixisland.anuto.R;
import ch.logixisland.anuto.engine.logic.GameEngine;
import ch.logixisland.anuto.engine.logic.entity.Entity;
import ch.logixisland.anuto.engine.logic.entity.EntityFactory;
import ch.logixisland.anuto.engine.logic.loop.TickListener;
import ch.logixisland.anuto.engine.render.Layers;
import ch.logixisland.anuto.engine.render.sprite.AnimatedSprite;
import ch.logixisland.anuto.engine.render.sprite.ReplicatedSprite;
import ch.logixisland.anuto.engine.render.sprite.SpriteInstance;
import ch.logixisland.anuto.engine.render.sprite.SpriteTemplate;
import ch.logixisland.anuto.engine.render.sprite.SpriteTransformation;
import ch.logixisland.anuto.engine.render.sprite.SpriteTransformer;
import ch.logixisland.anuto.engine.render.sprite.StaticSprite;

public class Flyer extends Enemy implements SpriteTransformation {

    public final static String ENTITY_NAME = "flyer";
    private final static float ANIMATION_SPEED = 1.0f;

    private final static EnemyProperties ENEMY_PROPERTIES = new EnemyProperties.Builder()
            .setHealth(400)
            .setSpeed(1.3f)
            .setReward(30)
            .setWeakAgainst(WeaponType.Laser, WeaponType.Bullet)
            .setStrongAgainst(WeaponType.Glue)
            .build();

    public static class Factory extends EntityFactory {
        @Override
        public Entity create(GameEngine gameEngine) {
            return new Flyer(gameEngine);
        }
    }

    public static class Persister extends EnemyPersister {

    }

    private static class StaticData implements TickListener {
        SpriteTemplate mSpriteTemplate;
        AnimatedSprite mReferenceSprite;

        @Override
        public void tick() {
            mReferenceSprite.tick();
        }
    }

    private float mAngle;

    private final ReplicatedSprite mSprite;

    private Flyer(GameEngine gameEngine) {
        super(gameEngine, ENEMY_PROPERTIES);
        StaticData s = (StaticData) getStaticData();

        mSprite = getSpriteFactory().createReplication(s.mReferenceSprite);
        mSprite.setListener(this);
    }

    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    @Override
    public int getTextId() {
        return R.string.enemy_flyer;
    }

    @Override
    public void drawPreview(Canvas canvas) {
        StaticData s = (StaticData) getStaticData();

        StaticSprite sprite = getSpriteFactory().createStatic(Layers.ENEMY, s.mSpriteTemplate);
        sprite.setIndex(3);
        sprite.draw(canvas);
    }

    @Override
    public Object initStatic() {
        StaticData s = new StaticData();

        s.mSpriteTemplate = getSpriteFactory().createTemplate(R.attr.flyer, 6);
        s.mSpriteTemplate.setMatrix(0.9f, 0.9f, null, -90f);

        s.mReferenceSprite = getSpriteFactory().createAnimated(Layers.ENEMY, s.mSpriteTemplate);
        s.mReferenceSprite.setSequenceForwardBackward();
        s.mReferenceSprite.setFrequency(ANIMATION_SPEED);

        getGameEngine().add(s);

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

        if (hasWayPoint()) {
            mAngle = getDirection().angle();
        }
    }

    @Override
    public void draw(SpriteInstance sprite, Canvas canvas) {
        SpriteTransformer.translate(canvas, getPosition());
        canvas.rotate(mAngle);
    }
}
