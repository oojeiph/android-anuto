package ch.logixisland.anuto.entity.tower;

import android.graphics.Canvas;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.logixisland.anuto.R;
import ch.logixisland.anuto.engine.logic.GameEngine;
import ch.logixisland.anuto.engine.logic.entity.Entity;
import ch.logixisland.anuto.engine.logic.entity.EntityFactory;
import ch.logixisland.anuto.engine.logic.loop.TickTimer;
import ch.logixisland.anuto.engine.logic.map.MapPath;
import ch.logixisland.anuto.engine.render.Layers;
import ch.logixisland.anuto.engine.render.sprite.SpriteInstance;
import ch.logixisland.anuto.engine.render.sprite.SpriteTemplate;
import ch.logixisland.anuto.engine.render.sprite.SpriteTransformation;
import ch.logixisland.anuto.engine.render.sprite.SpriteTransformer;
import ch.logixisland.anuto.engine.render.sprite.StaticSprite;
import ch.logixisland.anuto.entity.enemy.WeaponType;
import ch.logixisland.anuto.entity.shot.GlueShot;
import ch.logixisland.anuto.util.RandomUtils;
import ch.logixisland.anuto.util.iterator.StreamIterator;
import ch.logixisland.anuto.util.math.Intersections;
import ch.logixisland.anuto.util.math.Line;
import ch.logixisland.anuto.util.math.Vector2;

public class GlueTower extends Tower implements SpriteTransformation {

    public final static String ENTITY_NAME = "glueTower";
    private final static float SHOT_SPAWN_OFFSET = 0.8f;
    private final static float CANON_OFFSET_MAX = 0.5f;
    private final static float CANON_OFFSET_STEP = CANON_OFFSET_MAX / GameEngine.TARGET_FRAME_RATE / 0.8f;
    private final static float GLUE_INTENSITY = 1.2f;
    private final static float ENHANCE_GLUE_INTENSITY = 0.2f;
    private final static float GLUE_DURATION = 1.5f;

    private final static TowerProperties TOWER_PROPERTIES = new TowerProperties.Builder()
            .setValue(500)
            .setDamage(0)
            .setRange(1.5f)
            .setReload(2.0f)
            .setMaxLevel(5)
            .setWeaponType(WeaponType.Glue)
            .setEnhanceBase(1.2f)
            .setEnhanceCost(100)
            .setEnhanceDamage(0)
            .setEnhanceRange(0.1f)
            .setEnhanceReload(0f)
            .setUpgradeTowerName(GlueGun.ENTITY_NAME)
            .setUpgradeCost(800)
            .setUpgradeLevel(1)
            .build();

    public static class Factory extends EntityFactory {
        @Override
        public Entity create(GameEngine gameEngine) {
            return new GlueTower(gameEngine);
        }
    }

    public static class Persister extends TowerPersister {

    }

    private static class StaticData {
        SpriteTemplate mSpriteTemplateBase;
        SpriteTemplate mSpriteTemplateTower;
        SpriteTemplate mSpriteTemplateCanon;
    }

    private class SubCanon implements SpriteTransformation {
        float mAngle;
        StaticSprite mSprite;

        @Override
        public void draw(SpriteInstance sprite, Canvas canvas) {
            SpriteTransformer.translate(canvas, getPosition());
            canvas.rotate(mAngle);
            canvas.translate(mCanonOffset, 0);
        }
    }

    private float mGlueIntensity;
    private boolean mShooting;
    private float mCanonOffset;
    private final SubCanon[] mCanons = new SubCanon[8];
    private final Collection<Vector2> mTargets = new ArrayList<>();
    private final StaticSprite mSpriteBase;

    private final StaticSprite mSpriteTower;
    private final TickTimer mUpdateTimer = TickTimer.createInterval(0.1f);

    private GlueTower(GameEngine gameEngine) {
        super(gameEngine, TOWER_PROPERTIES);
        StaticData s = (StaticData) getStaticData();

        mGlueIntensity = GLUE_INTENSITY;

        mSpriteBase = getSpriteFactory().createStatic(Layers.TOWER, s.mSpriteTemplateBase);
        mSpriteBase.setListener(this);
        mSpriteBase.setIndex(RandomUtils.next(4));

        mSpriteTower = getSpriteFactory().createStatic(Layers.TOWER_UPPER, s.mSpriteTemplateTower);
        mSpriteTower.setListener(this);
        mSpriteTower.setIndex(RandomUtils.next(6));

        for (int i = 0; i < mCanons.length; i++) {
            SubCanon c = new SubCanon();
            c.mAngle = 360f / mCanons.length * i;
            c.mSprite = getSpriteFactory().createStatic(Layers.TOWER_LOWER, s.mSpriteTemplateCanon);
            c.mSprite.setListener(c);
            mCanons[i] = c;
        }
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

        s.mSpriteTemplateTower = getSpriteFactory().createTemplate(R.attr.glueShot, 6);
        s.mSpriteTemplateTower.setMatrix(0.3f, 0.3f, null, null);

        s.mSpriteTemplateCanon = getSpriteFactory().createTemplate(R.attr.glueTowerGun, 4);
        s.mSpriteTemplateCanon.setMatrix(0.3f, 0.4f, null, -90f);

        return s;
    }

    @Override
    public void init() {
        super.init();

        getGameEngine().add(mSpriteBase);
        getGameEngine().add(mSpriteTower);

        for (SubCanon c : mCanons) {
            getGameEngine().add(c.mSprite);
        }
    }

    @Override
    public void clean() {
        super.clean();

        getGameEngine().remove(mSpriteBase);
        getGameEngine().remove(mSpriteTower);

        for (SubCanon c : mCanons) {
            getGameEngine().remove(c.mSprite);
        }
    }

    @Override
    public void setBuilt() {
        super.setBuilt();
        determineTargets();
    }

    @Override
    public void enhance() {
        super.enhance();
        mGlueIntensity += ENHANCE_GLUE_INTENSITY;
    }

    @Override
    public void tick() {
        super.tick();

        if (isReloaded() && mUpdateTimer.tick() && !getPossibleTargets().isEmpty()) {
            mShooting = true;
            setReloaded(false);
        }

        if (mShooting) {
            mCanonOffset += CANON_OFFSET_STEP;

            if (mCanonOffset >= CANON_OFFSET_MAX) {
                mShooting = false;

                for (Vector2 target : mTargets) {
                    Vector2 position = Vector2.polar(SHOT_SPAWN_OFFSET, getAngleTo(target)).add(getPosition());
                    getGameEngine().add(new GlueShot(this, position, target, mGlueIntensity, GLUE_DURATION));
                }
            }
        } else if (mCanonOffset > 0f) {
            mCanonOffset -= CANON_OFFSET_STEP;
        }
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
        properties.add(new TowerInfoValue(R.string.intensity, mGlueIntensity));
        properties.add(new TowerInfoValue(R.string.duration, GLUE_DURATION));
        properties.add(new TowerInfoValue(R.string.reload, getReloadTime()));
        properties.add(new TowerInfoValue(R.string.range, getRange()));
        return properties;
    }

    private void determineTargets() {
        List<MapPath> paths = getGameEngine().getGameMap().getPaths();
        Collection<Line> sections = getPathSectionsInRange(paths);
        float dist = 0f;

        mTargets.clear();

        for (Line sect : sections) {
            float angle = sect.angle();
            float length = sect.length();

            while (dist < length) {
                final Vector2 target = Vector2.polar(dist, angle).add(sect.getPoint1());

                boolean free = StreamIterator.fromIterable(mTargets)
                        .filter(value -> value.distanceTo(target) < 0.5f)
                        .isEmpty();

                if (free) {
                    mTargets.add(target);
                }

                dist += 1f;
            }

            dist -= length;
        }
    }

    private Collection<Line> getPathSectionsInRange(Collection<MapPath> paths) {
        Collection<Line> sections = new ArrayList<>();

        for (MapPath path : paths) {
            sections.addAll(Intersections.getPathSectionsInRange(path.getWayPoints(), getPosition(), getRange()));
        }

        return sections;
    }
}
