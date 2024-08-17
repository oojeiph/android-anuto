package ch.logixisland.anuto.entity.tower;

import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.List;

import ch.logixisland.anuto.R;
import ch.logixisland.anuto.engine.logic.GameEngine;
import ch.logixisland.anuto.engine.logic.entity.Entity;
import ch.logixisland.anuto.engine.logic.entity.EntityFactory;
import ch.logixisland.anuto.engine.render.Layers;
import ch.logixisland.anuto.engine.render.sprite.SpriteInstance;
import ch.logixisland.anuto.engine.render.sprite.SpriteTemplate;
import ch.logixisland.anuto.engine.render.sprite.SpriteTransformation;
import ch.logixisland.anuto.engine.render.sprite.SpriteTransformer;
import ch.logixisland.anuto.engine.render.sprite.StaticSprite;
import ch.logixisland.anuto.engine.sound.Sound;
import ch.logixisland.anuto.entity.effect.TeleportEffect;
import ch.logixisland.anuto.entity.enemy.Enemy;
import ch.logixisland.anuto.entity.enemy.WeaponType;
import ch.logixisland.anuto.util.RandomUtils;
import ch.logixisland.anuto.util.iterator.StreamIterator;

public class Teleporter extends Tower implements SpriteTransformation {

    public final static String ENTITY_NAME = "teleporter";
    private final static float TELEPORT_DISTANCE = 15f;
    private final static float ENHANCE_TELEPORT_DISTANCE = 5f;

    private final static TowerProperties TOWER_PROPERTIES = new TowerProperties.Builder()
            .setValue(3000)
            .setDamage(0)
            .setRange(3.5f)
            .setReload(5.0f)
            .setMaxLevel(5)
            .setWeaponType(WeaponType.None)
            .setEnhanceBase(1.2f)
            .setEnhanceCost(2000)
            .setEnhanceDamage(0)
            .setEnhanceRange(0f)
            .setEnhanceReload(0.5f)
            .setUpgradeLevel(3)
            .build();

    public static class Factory extends EntityFactory {
        @Override
        public Entity create(GameEngine gameEngine) {
            return new Teleporter(gameEngine);
        }
    }

    public static class Persister extends TowerPersister {

    }

    private static class StaticData {
        SpriteTemplate mSpriteTemplateBase;
        SpriteTemplate mSpriteTemplateTower;
    }

    private float mTeleportDistance;
    private final Aimer mAimer = new Aimer(this);

    private final StaticSprite mSpriteBase;
    private final StaticSprite mSpriteTower;
    private final Sound mSound;

    private Teleporter(GameEngine gameEngine) {
        super(gameEngine, TOWER_PROPERTIES);
        StaticData s = (StaticData) getStaticData();

        mTeleportDistance = TELEPORT_DISTANCE;

        mSpriteBase = getSpriteFactory().createStatic(Layers.TOWER_BASE, s.mSpriteTemplateBase);
        mSpriteBase.setListener(this);
        mSpriteBase.setIndex(RandomUtils.next(4));

        mSpriteTower = getSpriteFactory().createStatic(Layers.TOWER, s.mSpriteTemplateTower);
        mSpriteTower.setListener(this);
        mSpriteTower.setIndex(RandomUtils.next(4));

        mSound = getSoundFactory().createSound(R.raw.gas3_hht);
    }

    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    @Override
    public Object initStatic() {
        StaticData s = new StaticData();

        s.mSpriteTemplateBase = getSpriteFactory().createTemplate(R.attr.base4, 4);
        s.mSpriteTemplateBase.setMatrix(1f, 1f, null, null);

        s.mSpriteTemplateTower = getSpriteFactory().createTemplate(R.attr.teleportTower, 4);
        s.mSpriteTemplateTower.setMatrix(0.8f, 0.8f, null, null);

        return s;
    }

    @Override
    public void init() {
        super.init();

        getGameEngine().add(mSpriteBase);
        getGameEngine().add(mSpriteTower);
    }

    @Override
    public void clean() {
        super.clean();

        getGameEngine().remove(mSpriteBase);
        getGameEngine().remove(mSpriteTower);
    }

    @Override
    public void enhance() {
        super.enhance();
        mTeleportDistance += ENHANCE_TELEPORT_DISTANCE;
    }

    @Override
    public void tick() {
        super.tick();

        mAimer.tick();
        Enemy target = mAimer.getTarget();

        if (isReloaded() && target != null) {
            // double check because two TeleportTowers might shoot simultaneously
            if (!target.isBeingTeleported() && getDistanceTo(target) <= getRange()) {
                getGameEngine().add(new TeleportEffect(this, getPosition(), target, mTeleportDistance));
                mSound.play();
                setReloaded(false);
            } else {
                mAimer.setTarget(null);
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
    }

    @Override
    public void preview(Canvas canvas) {
        mSpriteBase.draw(canvas);
        mSpriteTower.draw(canvas);
    }

    @Override
    public List<TowerInfoValue> getTowerInfoValues() {
        List<TowerInfoValue> properties = new ArrayList<>();
        properties.add(new TowerInfoValue(R.string.distance, mTeleportDistance));
        properties.add(new TowerInfoValue(R.string.reload, getReloadTime()));
        properties.add(new TowerInfoValue(R.string.range, getRange()));
        return properties;
    }

    @Override
    public StreamIterator<Enemy> getPossibleTargets() {
        return super.getPossibleTargets()
                .filter(enemy -> !enemy.isBeingTeleported() && !enemy.wasTeleported());
    }
}
