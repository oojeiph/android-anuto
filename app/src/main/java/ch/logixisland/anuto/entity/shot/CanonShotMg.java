package ch.logixisland.anuto.entity.shot;

import android.graphics.Canvas;

import ch.logixisland.anuto.R;
import ch.logixisland.anuto.engine.logic.entity.Entity;
import ch.logixisland.anuto.engine.render.Layers;
import ch.logixisland.anuto.engine.render.sprite.SpriteInstance;
import ch.logixisland.anuto.engine.render.sprite.SpriteTemplate;
import ch.logixisland.anuto.engine.render.sprite.SpriteTransformation;
import ch.logixisland.anuto.engine.render.sprite.SpriteTransformer;
import ch.logixisland.anuto.engine.render.sprite.StaticSprite;
import ch.logixisland.anuto.entity.EntityTypes;
import ch.logixisland.anuto.entity.enemy.Enemy;
import ch.logixisland.anuto.util.RandomUtils;
import ch.logixisland.anuto.util.math.Vector2;

public class CanonShotMg extends Shot implements SpriteTransformation {

    private final static float HIT_RANGE = 0.5f;
    public final static float MOVEMENT_SPEED = 8.0f;

    private static class StaticData {
        public SpriteTemplate mSpriteTemplate;
    }

    private final float mAngle;
    private final float mDamage;

    private final StaticSprite mSprite;

    public CanonShotMg(Entity origin, Vector2 position, Vector2 direction, float damage) {
        super(origin);
        setPosition(position);
        setSpeed(MOVEMENT_SPEED);
        setDirection(direction);

        mAngle = direction.angle();
        mDamage = damage;

        StaticData s = (StaticData) getStaticData();

        mSprite = getSpriteFactory().createStatic(Layers.SHOT, s.mSpriteTemplate);
        mSprite.setListener(this);
        mSprite.setIndex(RandomUtils.next(4));
    }

    @Override
    public Object initStatic() {
        StaticData s = new StaticData();

        s.mSpriteTemplate = getSpriteFactory().createTemplate(R.attr.canonMgShot, 4);
        s.mSpriteTemplate.setMatrix(0.2f, null, null, -90f);

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

        Enemy enemy = (Enemy) getGameEngine().getEntitiesByType(EntityTypes.ENEMY)
                .filter(inRange(getPosition(), HIT_RANGE))
                .first();

        if (enemy != null) {
            enemy.damage(mDamage, getOrigin());
            this.remove();
        }

        if (!isPositionVisible()) {
            this.remove();
        }
    }

    @Override
    public void draw(SpriteInstance sprite, Canvas canvas) {
        SpriteTransformer.translate(canvas, getPosition());
        canvas.rotate(mAngle);
    }
}
