package ch.logixisland.anuto.entity.tower;

import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.logixisland.anuto.R;
import ch.logixisland.anuto.engine.logic.GameEngine;
import ch.logixisland.anuto.engine.logic.entity.Entity;
import ch.logixisland.anuto.engine.logic.entity.EntityFactory;
import ch.logixisland.anuto.engine.logic.map.MapPath;
import ch.logixisland.anuto.engine.render.Layers;
import ch.logixisland.anuto.engine.render.sprite.AnimatedSprite;
import ch.logixisland.anuto.engine.render.sprite.SpriteInstance;
import ch.logixisland.anuto.engine.render.sprite.SpriteTemplate;
import ch.logixisland.anuto.engine.render.sprite.SpriteTransformation;
import ch.logixisland.anuto.engine.render.sprite.SpriteTransformer;
import ch.logixisland.anuto.engine.sound.Sound;
import ch.logixisland.anuto.entity.enemy.WeaponType;
import ch.logixisland.anuto.entity.shot.Mine;
import ch.logixisland.anuto.util.RandomUtils;
import ch.logixisland.anuto.util.container.KeyValueStore;
import ch.logixisland.anuto.util.math.Intersections;
import ch.logixisland.anuto.util.math.Line;
import ch.logixisland.anuto.util.math.Vector2;

public class MineLayer extends Tower implements SpriteTransformation {

    public final static String ENTITY_NAME = "mineLayer";
    private final static float ANIMATION_DURATION = 1f;
    private final static int MAX_MINE_COUNT = 3;
    private final static int ENHANCE_MAX_MINE_COUNT = 1;
    private final static float EXPLOSION_RADIUS = 2.0f;
    private final static float ENHANCE_EXPLOSION_RADIUS = 0.05f;

    private final static TowerProperties TOWER_PROPERTIES = new TowerProperties.Builder()
            .setValue(10250)
            .setDamage(3100)
            .setRange(2.5f)
            .setReload(2.5f)
            .setMaxLevel(10)
            .setWeaponType(WeaponType.Explosive)
            .setEnhanceBase(1.4f)
            .setEnhanceCost(750)
            .setEnhanceDamage(270)
            .setEnhanceRange(0.0f)
            .setEnhanceReload(0.05f)
            .setUpgradeTowerName(RocketLauncher.ENTITY_NAME)
            .setUpgradeCost(102850)
            .setUpgradeLevel(2)
            .build();

    public static class Factory extends EntityFactory {
        @Override
        public Entity create(GameEngine gameEngine) {
            return new MineLayer(gameEngine);
        }
    }

    public static class Persister extends TowerPersister {
        @Override
        public KeyValueStore writeEntityData(Entity entity) {
            KeyValueStore data = super.writeEntityData(entity);
            MineLayer mineLayer = (MineLayer) entity;

            List<Vector2> minePositions = new ArrayList<>();
            for (Mine mine : mineLayer.mMines) {
                if (!mine.isFlying()) {
                    minePositions.add(mine.getPosition());
                }
            }
            data.putVectorList("minePositions", minePositions);

            return data;
        }

        @Override
        public void readEntityData(Entity entity, KeyValueStore entityData) {
            super.readEntityData(entity, entityData);
            MineLayer mineLayer = (MineLayer) entity;

            for (Vector2 minePosition : entityData.getVectorList("minePositions")) {
                Mine mine = new Mine(mineLayer, minePosition, mineLayer.getDamage(), mineLayer.mExplosionRadius);
                mineLayer.mMines.add(mine);
                mine.addListener(mineLayer.mMineListener);
                mineLayer.getGameEngine().add(mine);
            }
        }
    }

    private static class StaticData {
        public SpriteTemplate mSpriteTemplate;
    }

    private final float mAngle;
    private int mMaxMineCount;
    private float mExplosionRadius;
    private boolean mShooting;
    private Collection<Line> mSections;
    private final Collection<Mine> mMines = new ArrayList<>();

    private final AnimatedSprite mSprite;
    private final Sound mSound;

    private final Entity.Listener mMineListener = new Entity.Listener() {
        @Override
        public void entityRemoved(Entity entity) {
            Mine mine = (Mine) entity;
            mine.removeListener(this);
            mMines.remove(mine);
        }
    };

    private MineLayer(GameEngine gameEngine) {
        super(gameEngine, TOWER_PROPERTIES);
        StaticData s = (StaticData) getStaticData();

        mSprite = getSpriteFactory().createAnimated(Layers.TOWER_BASE, s.mSpriteTemplate);
        mSprite.setListener(this);
        mSprite.setSequenceForwardBackward();
        mSprite.setInterval(ANIMATION_DURATION);

        mAngle = RandomUtils.next(360f);
        mMaxMineCount = MAX_MINE_COUNT;
        mExplosionRadius = EXPLOSION_RADIUS;

        mSound = getSoundFactory().createSound(R.raw.gun2_donk);
    }

    @Override
    public String getEntityName() {
        return ENTITY_NAME;
    }

    @Override
    public Object initStatic() {
        StaticData s = new StaticData();

        s.mSpriteTemplate = getSpriteFactory().createTemplate(R.attr.mineLayer, 6);
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

        for (Mine m : mMines) {
            m.removeListener(mMineListener);
            m.remove();
        }

        mMines.clear();
    }

    @Override
    public void setPosition(Vector2 position) {
        super.setPosition(position);
        List<MapPath> paths = getGameEngine().getGameMap().getPaths();
        mSections = getPathSectionsInRange(paths);
    }

    @Override
    public void move(Vector2 offset) {
        super.move(offset);
        List<MapPath> paths = getGameEngine().getGameMap().getPaths();
        mSections = getPathSectionsInRange(paths);
    }

    @Override
    public void enhance() {
        super.enhance();
        mMaxMineCount += ENHANCE_MAX_MINE_COUNT;
        mExplosionRadius += ENHANCE_EXPLOSION_RADIUS;
    }

    @Override
    public void tick() {
        super.tick();

        if (isReloaded() && mMines.size() < mMaxMineCount && !mSections.isEmpty()) {
            mShooting = true;
            setReloaded(false);
        }

        if (mShooting) {
            mSprite.tick();

            if (mSprite.getSequenceIndex() == 5) {
                Mine m = new Mine(this, getPosition(), getTarget(), getDamage(), mExplosionRadius);
                m.addListener(mMineListener);
                mMines.add(m);
                getGameEngine().add(m);
                mSound.play();

                mShooting = false;
            }
        }

        if (mSprite.getSequenceIndex() != 0) {
            mSprite.tick();
        }
    }

    @Override
    public void draw(SpriteInstance sprite, Canvas canvas) {
        SpriteTransformer.translate(canvas, getPosition());
        canvas.rotate(mAngle);
    }

    @Override
    public void preview(Canvas canvas) {
        mSprite.draw(canvas);
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

    private Vector2 getTarget() {
        float totalLen = 0f;

        for (Line section : mSections) {
            totalLen += section.length();
        }

        float dist = RandomUtils.next(totalLen);

        for (Line section : mSections) {
            float length = section.length();

            if (dist > length) {
                dist -= length;
            } else {
                return section
                        .direction()
                        .mul(dist)
                        .add(section.getPoint1());
            }
        }

        return null;
    }

    private Collection<Line> getPathSectionsInRange(Collection<MapPath> paths) {
        Collection<Line> sections = new ArrayList<>();

        for (MapPath path : paths) {
            sections.addAll(Intersections.getPathSectionsInRange(path.getWayPoints(), getPosition(), getRange()));
        }

        return sections;
    }
}
