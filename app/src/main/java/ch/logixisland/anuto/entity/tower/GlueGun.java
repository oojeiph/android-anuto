package ch.logixisland.anuto.entity.tower;

import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.List;

import ch.logixisland.anuto.R;
import ch.logixisland.anuto.engine.logic.GameEngine;
import ch.logixisland.anuto.engine.logic.entity.Entity;
import ch.logixisland.anuto.engine.logic.entity.EntityFactory;
import ch.logixisland.anuto.engine.render.Layers;
import ch.logixisland.anuto.engine.render.sprite.AnimatedSprite;
import ch.logixisland.anuto.engine.render.sprite.SpriteInstance;
import ch.logixisland.anuto.engine.render.sprite.SpriteTemplate;
import ch.logixisland.anuto.engine.render.sprite.SpriteTransformation;
import ch.logixisland.anuto.engine.render.sprite.SpriteTransformer;
import ch.logixisland.anuto.engine.render.sprite.StaticSprite;
import ch.logixisland.anuto.engine.sound.Sound;
import ch.logixisland.anuto.entity.enemy.WeaponType;
import ch.logixisland.anuto.entity.shot.GlueShot;
import ch.logixisland.anuto.util.RandomUtils;
import ch.logixisland.anuto.util.math.Vector2;

public class GlueGun extends Tower implements SpriteTransformation {

    public final static String ENTITY_NAME = "glueGun";
    private final static float SHOT_SPAWN_OFFSET = 0.7f;
    private final static float REBOUND_DURATION = 0.5f;
    private final static float GLUE_INTENSITY = 1.2f;
    private final static float ENHANCE_GLUE_INTENSITY = 0.3f;
    private final static float GLUE_DURATION = 2.5f;

    private final static TowerProperties TOWER_PROPERTIES = new TowerProperties.Builder()
            .setValue(1300)
            .setDamage(0)
            .setRange(2.5f)
            .setReload(3.0f)
            .setMaxLevel(5)
            .setWeaponType(WeaponType.Glue)
            .setEnhanceBase(1.2f)
            .setEnhanceCost(200)
            .setEnhanceDamage(0)
            .setEnhanceRange(0.2f)
            .setEnhanceReload(0f)
            .setUpgradeTowerName(Teleporter.ENTITY_NAME)
            .setUpgradeCost(1700)
            .setUpgradeLevel(2)
            .build();

    public static class Factory extends EntityFactory {
        @Override
        public Entity create(GameEngine gameEngine) {
            return new GlueGun(gameEngine);
        }
    }

    public static class Persister extends TowerPersister {

    }

    private static class StaticData {
        SpriteTemplate mSpriteTemplateBase;
        SpriteTemplate mSpriteTemplateCanon;
    }

    private float mAngle = 90f;
    private float mGlueIntensity;
    private boolean mRebounding = false;
    private final Aimer mAimer = new Aimer(this);

    private final StaticSprite mSpriteBase;
    private final AnimatedSprite mSpriteCanon;
    private final Sound mSound;

    private GlueGun(GameEngine gameEngine) {
        super(gameEngine, TOWER_PROPERTIES);
        StaticData s = (StaticData) getStaticData();

        mGlueIntensity = GLUE_INTENSITY;

        mSpriteBase = getSpriteFactory().createStatic(Layers.TOWER_BASE, s.mSpriteTemplateBase);
        mSpriteBase.setListener(this);
        mSpriteBase.setIndex(RandomUtils.next(4));

        mSpriteCanon = getSpriteFactory().createAnimated(Layers.TOWER, s.mSpriteTemplateCanon);
        mSpriteCanon.setListener(this);
        mSpriteCanon.setSequenceForwardBackward();
        mSpriteCanon.setInterval(REBOUND_DURATION);

        mSound = getSoundFactory().createSound(R.raw.explosive1_chk);
    }

    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    @Override
    public Object initStatic() {
        StaticData s = new StaticData();

        s.mSpriteTemplateBase = getSpriteFactory().createTemplate(R.attr.base1, 4);
        s.mSpriteTemplateBase.setMatrix(1f, 1f, null, null);

        s.mSpriteTemplateCanon = getSpriteFactory().createTemplate(R.attr.glueGun, 6);
        s.mSpriteTemplateCanon.setMatrix(0.8f, 1.0f, new Vector2(0.4f, 0.4f), -90f);

        return s;
    }

    @Override
    public void init() {
        super.init();

        getGameEngine().add(mSpriteBase);
        getGameEngine().add(mSpriteCanon);
    }

    @Override
    public void clean() {
        super.clean();

        getGameEngine().remove(mSpriteBase);
        getGameEngine().remove(mSpriteCanon);
    }

    @Override
    public void enhance() {
        super.enhance();
        mGlueIntensity += ENHANCE_GLUE_INTENSITY;
    }

    @Override
    public void tick() {
        super.tick();
        mAimer.tick();

        if (isReloaded() && mAimer.getTarget() != null) {
            float dist = getDistanceTo(mAimer.getTarget());
            float time = dist / GlueShot.MOVEMENT_SPEED;

            Vector2 target = mAimer.getTarget().getPositionAfter(time);

            mAngle = getAngleTo(target);

            Vector2 position = Vector2.polar(SHOT_SPAWN_OFFSET, getAngleTo(target)).add(getPosition());
            getGameEngine().add(new GlueShot(this, position, target, mGlueIntensity, GLUE_DURATION));
            mSound.play();

            setReloaded(false);
            mRebounding = true;
        }

        if (mRebounding && mSpriteCanon.tick()) {
            mRebounding = false;
        }
    }

    @Override
    public Aimer getAimer() {
        return mAimer;
    }

    @Override
    public void draw(SpriteInstance sprite, Canvas canvas) {
        SpriteTransformer.translate(canvas, getPosition());
        canvas.rotate(mAngle);
    }

    @Override
    public void preview(Canvas canvas) {
        mSpriteBase.draw(canvas);
        mSpriteCanon.draw(canvas);
    }

    @Override
    public List<TowerInfoValue> getTowerInfoValues() {
        List<TowerInfoValue> properties = new ArrayList<>();
        properties.add(new TowerInfoValue(R.string.intensity, mGlueIntensity));
        properties.add(new TowerInfoValue(R.string.duration, GLUE_DURATION));
        properties.add(new TowerInfoValue(R.string.reload, getReloadTime()));
        properties.add(new TowerInfoValue(R.string.range, getRange()));
        return properties;
    }
}
