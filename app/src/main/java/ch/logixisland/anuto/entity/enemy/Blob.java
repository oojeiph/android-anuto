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

public class Blob extends Enemy implements SpriteTransformation {

    public final static String ENTITY_NAME = "blob";
    private final static float ANIMATION_SPEED = 1.5f;

    private final static EnemyProperties ENEMY_PROPERTIES = new EnemyProperties.Builder()
            .setHealth(600)
            .setSpeed(0.5f)
            .setReward(20)
            .setWeakAgainst(WeaponType.Explosive)
            .setStrongAgainst(WeaponType.Bullet)
            .build();

    public static class Factory extends EntityFactory {
        @Override
        public Entity create(GameEngine gameEngine) {
            return new Blob(gameEngine);
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

    private final ReplicatedSprite mSprite;

    private Blob(GameEngine gameEngine) {
        super(gameEngine, ENEMY_PROPERTIES);
        StaticData s = (StaticData) getStaticData();

        mSprite = getSpriteFactory().createReplication(s.mReferenceSprite);
        mSprite.setListener(this);
    }

    @Override
    public int getTextId() {
        return R.string.enemy_blob;
    }

    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    @Override
    public Object initStatic() {
        StaticData s = new StaticData();

        s.mSpriteTemplate = getSpriteFactory().createTemplate(R.attr.blob, 9);
        s.mSpriteTemplate.setMatrix(0.9f, 0.9f, null, null);

        s.mReferenceSprite = getSpriteFactory().createAnimated(Layers.ENEMY, s.mSpriteTemplate);
        s.mReferenceSprite.setSequenceForward();
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
    public void drawPreview(Canvas canvas) {
        StaticData s = (StaticData) getStaticData();
        getSpriteFactory().createStatic(Layers.ENEMY, s.mSpriteTemplate).draw(canvas);
    }

    @Override
    public void draw(SpriteInstance sprite, Canvas canvas) {
        SpriteTransformer.translate(canvas, getPosition());
    }
}
