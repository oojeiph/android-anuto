package ch.logixisland.anuto.entity.shot;

import ch.logixisland.anuto.engine.logic.GameEngine;
import ch.logixisland.anuto.engine.logic.entity.Entity;
import ch.logixisland.anuto.entity.EntityTypes;
import ch.logixisland.anuto.util.math.Vector2;

public abstract class Shot extends Entity {

    private final Entity mOrigin;
    private float mSpeed;
    private Vector2 mDirection;
    private boolean mEnabled = true;

    Shot(Entity origin) {
        super(origin.getGameEngine());
        mOrigin = origin;
    }

    @Override
    public final int getEntityType() {
        return EntityTypes.SHOT;
    }

    @Override
    public void tick() {
        super.tick();

        if (mEnabled) {
            move(Vector2.mul(mDirection, mSpeed / GameEngine.TARGET_FRAME_RATE));
        }
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public Entity getOrigin() {
        return mOrigin;
    }

    public float getSpeed() {
        return mSpeed;
    }

    protected void setSpeed(float speed) {
        this.mSpeed = speed;
    }

    protected void setDirection(Vector2 direction) {
        this.mDirection = direction;
    }

}
