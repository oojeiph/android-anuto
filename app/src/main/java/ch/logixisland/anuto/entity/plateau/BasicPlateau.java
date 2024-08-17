package ch.logixisland.anuto.entity.plateau;

import android.graphics.Canvas;

import ch.logixisland.anuto.R;
import ch.logixisland.anuto.engine.logic.GameEngine;
import ch.logixisland.anuto.engine.logic.entity.Entity;
import ch.logixisland.anuto.engine.logic.entity.EntityFactory;
import ch.logixisland.anuto.engine.logic.entity.EntityPersister;
import ch.logixisland.anuto.engine.render.Layers;
import ch.logixisland.anuto.engine.render.sprite.SpriteInstance;
import ch.logixisland.anuto.engine.render.sprite.SpriteTemplate;
import ch.logixisland.anuto.engine.render.sprite.SpriteTransformation;
import ch.logixisland.anuto.engine.render.sprite.SpriteTransformer;
import ch.logixisland.anuto.engine.render.sprite.StaticSprite;
import ch.logixisland.anuto.util.RandomUtils;

public class BasicPlateau extends Plateau implements SpriteTransformation {

    public final static String ENTITY_NAME = "basic";

    public static class Factory extends EntityFactory {
        @Override
        public Entity create(GameEngine gameEngine) {
            return new BasicPlateau(gameEngine);
        }
    }

    public static class Persister extends EntityPersister {

    }

    private static class StaticData {
        SpriteTemplate mSpriteTemplate;
    }

    private final StaticSprite mSprite;

    private BasicPlateau(GameEngine gameEngine) {
        super(gameEngine);
        StaticData s = (StaticData) getStaticData();

        mSprite = getSpriteFactory().createStatic(Layers.PLATEAU, s.mSpriteTemplate);
        mSprite.setIndex(RandomUtils.next(4));
        mSprite.setListener(this);
    }

    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    @Override
    public Object initStatic() {
        StaticData s = new StaticData();

        s.mSpriteTemplate = getSpriteFactory().createTemplate(R.attr.plateau1, 4);
        s.mSpriteTemplate.setMatrix(1f, 1f, null, null);

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
