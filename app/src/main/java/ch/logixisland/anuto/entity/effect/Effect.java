package ch.logixisland.anuto.entity.effect;

import ch.logixisland.anuto.engine.logic.entity.Entity;
import ch.logixisland.anuto.engine.logic.loop.TickTimer;
import ch.logixisland.anuto.entity.EntityTypes;

public abstract class Effect extends Entity {

    enum State {
        NotStarted,
        Active,
        Ended
    }

    private final Entity mOrigin;
    private TickTimer mTimer;
    private State mState;

    Effect(Entity origin) {
        super(origin.getGameEngine());
        mOrigin = origin;
        mState = State.NotStarted;
    }

    Effect(Entity origin, float duration) {
        this(origin);
        mTimer = TickTimer.createInterval(duration);
    }

    public Entity getOrigin() {
        return mOrigin;
    }

    @Override
    public final int getEntityType() {
        return EntityTypes.EFFECT;
    }

    @Override
    public void tick() {
        super.tick();

        if (mState == State.NotStarted) {
            mState = State.Active;
            effectBegin();
        }

        if (mTimer != null && mTimer.tick()) {
            mState = State.Ended;
            effectEnd();
            remove();
        }
    }

    protected void effectBegin() {

    }

    protected void effectEnd() {

    }
}
