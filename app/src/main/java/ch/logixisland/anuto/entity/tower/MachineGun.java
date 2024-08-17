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
import ch.logixisland.anuto.entity.enemy.Enemy;
import ch.logixisland.anuto.entity.enemy.WeaponType;
import ch.logixisland.anuto.entity.shot.CanonShotMg;
import ch.logixisland.anuto.entity.shot.Shot;
import ch.logixisland.anuto.util.RandomUtils;
import ch.logixisland.anuto.util.math.MathUtils;
import ch.logixisland.anuto.util.math.Vector2;

public class MachineGun extends Tower implements SpriteTransformation {

    public final static String ENTITY_NAME = "machineGun";
    private final static float SHOT_SPAWN_OFFSET = 0.7f;
    private final static float MG_ROTATION_SPEED = 3f;

    private final static TowerProperties TOWER_PROPERTIES = new TowerProperties.Builder()
            .setValue(94200)
            .setDamage(20000)
            .setRange(3.5f)
            .setReload(0.15f)
            .setMaxLevel(15)
            .setWeaponType(WeaponType.Bullet)
            .setEnhanceBase(1.5f)
            .setEnhanceCost(750)
            .setEnhanceDamage(120)
            .setEnhanceRange(0.05f)
            .setEnhanceReload(0.005f)
            .setUpgradeLevel(3)
            .build();

    public static class Factory extends EntityFactory {
        @Override
        public Entity create(GameEngine gameEngine) {
            return new MachineGun(gameEngine);
        }
    }

    public static class Persister extends TowerPersister {

    }

    private static class StaticData {
        SpriteTemplate mSpriteTemplateBase;
        SpriteTemplate mSpriteTemplateCanon;
    }

    private final float mBaseReloadTime;
    private float mAngle = 90f;
    private final StaticSprite mSpriteBase;
    private final AnimatedSprite mSpriteCanon;
    private int mShotCount = 0;
    private final Sound mSound;
    private final Aimer mAimer = new Aimer(this);

    private MachineGun(GameEngine gameEngine) {
        super(gameEngine, TOWER_PROPERTIES);
        StaticData s = (StaticData) getStaticData();

        mSpriteBase = getSpriteFactory().createStatic(Layers.TOWER_BASE, s.mSpriteTemplateBase);
        mSpriteBase.setListener(this);
        mSpriteBase.setIndex(RandomUtils.next(4));

        mSpriteCanon = getSpriteFactory().createAnimated(Layers.TOWER, s.mSpriteTemplateCanon);
        mSpriteCanon.setListener(this);
        mSpriteCanon.setSequenceForward();

        mBaseReloadTime = getReloadTime();
        mSpriteCanon.setFrequency(MG_ROTATION_SPEED);

        mSound = getSoundFactory().createSound(R.raw.gun3_dit);
        mSound.setVolume(0.5f);
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

        s.mSpriteTemplateCanon = getSpriteFactory().createTemplate(R.attr.canonMg, 5);
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
        mSpriteCanon.setFrequency(MG_ROTATION_SPEED * mBaseReloadTime / getReloadTime());
    }

    @Override
    public void tick() {
        super.tick();
        mAimer.tick();

        if (mAimer.getTarget() != null) {
            Vector2 shootingDirection = calcShootingDirection(mAimer.getTarget());
            mAngle = shootingDirection.angle();
            mSpriteCanon.tick();

            if (isReloaded()) {
                Shot shot = new CanonShotMg(this, getPosition(), shootingDirection, getDamage());
                shot.move(Vector2.polar(SHOT_SPAWN_OFFSET, mAngle));
                getGameEngine().add(shot);
                mShotCount++;

                if (mShotCount % 2 == 0) {
                    mSound.play();
                }

                setReloaded(false);
            }
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
        properties.add(new TowerInfoValue(R.string.damage, getDamage()));
        properties.add(new TowerInfoValue(R.string.reload, getReloadTime()));
        properties.add(new TowerInfoValue(R.string.dps, getDamage() / getReloadTime()));
        properties.add(new TowerInfoValue(R.string.range, getRange()));
        properties.add(new TowerInfoValue(R.string.inflicted, getDamageInflicted()));
        return properties;
    }

    private Vector2 calcShootingDirection(Enemy target) {
        Vector2 ps = getDirectionTo(target).mul(SHOT_SPAWN_OFFSET).add(getPosition());
        Vector2 pt = target.getPosition();
        float ptToPsAngle = pt.angleTo(ps);

        Vector2 dt = target.getDirection();
        if (dt == null) {
            // target has no waypoint
            return getDirectionTo(target);
        }

        float vs = CanonShotMg.MOVEMENT_SPEED;
        float vt = target.getSpeed();

        float alpha = dt.angle() - ptToPsAngle;
        float beta = MathUtils.toDegrees((float) Math.asin(vt * Math.sin(MathUtils.toRadians(alpha)) / vs));

        float angle = 180f + ptToPsAngle - beta;
        return Vector2.polar(1f, angle);
    }
}
