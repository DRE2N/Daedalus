package de.erethon.daedalus.animation;

import java.util.Optional;

public class AttackState implements IAnimState {
    private final AnimationStateConfig cfg;
    private boolean finished;

    public AttackState(AnimationStateConfig cfg) {
        this.cfg = cfg;
    }

    @Override
    public void enter() {
        cfg.getAnimation().resetCounter();
        finished = false;
    }

    @Override
    public void update() {
        if (cfg.getAnimation().getCounter() >= cfg.getAnimation().getAnimationBlueprint().getDuration()) {
            finished = true;
        }
    }

    @Override
    public void exit() {
    }

    @Override
    public AnimationStateType getType() {
        return AnimationStateType.ATTACK;
    }

    @Override
    public Optional<AnimationStateType> nextState() {
        return finished ? Optional.of(AnimationStateType.IDLE) : Optional.empty();
    }

    @Override
    public Animation getAnimation() {
        return cfg.getAnimation();
    }

    @Override
    public boolean isLoop() {
        return cfg.isLoop();
    }
}