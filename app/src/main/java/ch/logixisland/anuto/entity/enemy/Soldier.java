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

public class Soldier extends Enemy implements SpriteTransformation {

    public final static String ENTITY_NAME = "soldier";
    private final static float ANIMATION_SPEED = 1f;

    private final static EnemyProperties ENEMY_PROPERTIES = new EnemyProperties.Builder()
            .setHealth(300)
            .setSpeed(1.0f)
            .setReward(10)
            .build();

    public static class Factory extends EntityFactory {
        @Override
        public Entity create(GameEngine gameEngine) {
            return new Soldier(gameEngine);
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

    private Soldier(GameEngine gameEngine) {
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
        return R.string.enemy_soldier;
    }

    @Override
    public void drawPreview(Canvas canvas) {
        StaticData s = (StaticData) getStaticData();
        getSpriteFactory().createStatic(Layers.ENEMY, s.mSpriteTemplate).draw(canvas);
    }

    @Override
    public Object initStatic() {
        StaticData s = new StaticData();

        s.mSpriteTemplate = getSpriteFactory().createTemplate(R.attr.soldier, 12);
        s.mSpriteTemplate.setMatrix(0.9f, 0.9f, null, null);

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
    public void draw(SpriteInstance sprite, Canvas canvas) {
        SpriteTransformer.translate(canvas, getPosition());
    }
}
