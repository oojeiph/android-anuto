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
import ch.logixisland.anuto.entity.shot.MortarShot;
import ch.logixisland.anuto.util.RandomUtils;
import ch.logixisland.anuto.util.math.Vector2;

public class Mortar extends Tower implements SpriteTransformation {

    public final static String ENTITY_NAME = "mortar";
    private final static float SHOT_SPAWN_OFFSET = 0.6f;
    private final static float REBOUND_DURATION = 0.5f;

    private final static float INACCURACY = 1.0f;
    private final static float EXPLOSION_RADIUS = 1.5f;
    private final static float ENHANCE_EXPLOSION_RADIUS = 0.05f;

    private final static TowerProperties TOWER_PROPERTIES = new TowerProperties.Builder()
            .setValue(250)
            .setDamage(100)
            .setRange(2.5f)
            .setReload(2.0f)
            .setMaxLevel(10)
            .setWeaponType(WeaponType.Explosive)
            .setEnhanceBase(1.2f)
            .setEnhanceCost(125)
            .setEnhanceDamage(60)
            .setEnhanceRange(0.05f)
            .setEnhanceReload(0.05f)
            .setUpgradeTowerName(MineLayer.ENTITY_NAME)
            .setUpgradeCost(10000)
            .setUpgradeLevel(1)
            .build();

    public static class Factory extends EntityFactory {
        @Override
        public Entity create(GameEngine gameEngine) {
            return new Mortar(gameEngine);
        }
    }

    public static class Persister extends TowerPersister {

    }

    private static class StaticData {
        SpriteTemplate mSpriteTemplateBase;
        SpriteTemplate mSpriteTemplateCanon;
    }

    private float mExplosionRadius;
    private float mAngle = 90f;
    private boolean mRebounding = false;
    private final Aimer mAimer = new Aimer(this);

    private final StaticSprite mSpriteBase;
    private final AnimatedSprite mSpriteCanon;
    private final Sound mSound;

    private Mortar(GameEngine gameEngine) {
        super(gameEngine, TOWER_PROPERTIES);
        StaticData s = (StaticData) getStaticData();

        mExplosionRadius = EXPLOSION_RADIUS;

        mSpriteBase = getSpriteFactory().createStatic(Layers.TOWER_BASE, s.mSpriteTemplateBase);
        mSpriteBase.setIndex(RandomUtils.next(4));
        mSpriteBase.setListener(this);

        mSpriteCanon = getSpriteFactory().createAnimated(Layers.TOWER, s.mSpriteTemplateCanon);
        mSpriteCanon.setListener(this);
        mSpriteCanon.setSequenceForwardBackward();
        mSpriteCanon.setInterval(REBOUND_DURATION);

        mSound = getSoundFactory().createSound(R.raw.gas2_thomp);
    }

    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    @Override
    public Object initStatic() {
        StaticData s = new StaticData();

        s.mSpriteTemplateBase = getSpriteFactory().createTemplate(R.attr.base2, 4);
        s.mSpriteTemplateBase.setMatrix(1f, 1f, null, null);

        s.mSpriteTemplateCanon = getSpriteFactory().createTemplate(R.attr.mortar, 8);
        s.mSpriteTemplateCanon.setMatrix(0.8f, null, new Vector2(0.4f, 0.2f), -90f);

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
        mExplosionRadius += ENHANCE_EXPLOSION_RADIUS;
    }

    @Override
    public void tick() {
        super.tick();
        mAimer.tick();

        if (mAimer.getTarget() != null && isReloaded()) {
            Vector2 targetPos = mAimer.getTarget().getPositionAfter(MortarShot.TIME_TO_TARGET);
            targetPos = Vector2.polar(RandomUtils.next(INACCURACY), RandomUtils.next(360f)).add(targetPos);
            mAngle = getAngleTo(targetPos);
            Vector2 shotPos = Vector2.polar(SHOT_SPAWN_OFFSET, mAngle).add(getPosition());

            getGameEngine().add(new MortarShot(this, shotPos, targetPos, getDamage(), mExplosionRadius));
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

        if (sprite == mSpriteCanon) {
            canvas.rotate(mAngle);
        }
    }

    @Override
    public void preview(Canvas canvas) {
        mSpriteBase.draw(canvas);
        mSpriteCanon.draw(canvas);
    }

    @Override
    public List<TowerInfoValue> getTowerInfoValues() {
        List<TowerInfoValue> properties = new ArrayList<>();
        properties.add(new TowerInfoValue(R.string.damage, getDamage()));
        properties.add(new TowerInfoValue(R.string.splash, mExplosionRadius));
        properties.add(new TowerInfoValue(R.string.reload, getReloadTime()));
        properties.add(new TowerInfoValue(R.string.dps, getDamage() / getReloadTime()));
        properties.add(new TowerInfoValue(R.string.range, getRange()));
        properties.add(new TowerInfoValue(R.string.inflicted, getDamageInflicted()));
        return properties;
    }
}
