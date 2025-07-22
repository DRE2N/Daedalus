package de.erethon.daedalus.customentity.core.components;

import de.erethon.daedalus.animation.AnimationManager;
import de.erethon.daedalus.customentity.ModeledEntity;
import de.erethon.daedalus.dataconverter.FileModelConverter;
import de.erethon.daedalus.utils.MathToolkit;
import de.erethon.bedrock.chat.MessageUtil;

/**
 * The AnimationComponent class provides functionalities to manage and animate
 * a ModeledEntity instance. It enables playing, stopping, and blending animations
 * as well as scaling down and removing the entity with transitions.
 */
public class AnimationComponent {

    private final ModeledEntity modeledEntity;
    private final int scaleDurationTicks = 20;
    private AnimationManager animationManager = null;
    private boolean isScalingDown = false;
    private int scaleTicksElapsed = 0;
    private double scaleStart = 1.0;
    private double scaleEnd = 0.0;

    public AnimationComponent(ModeledEntity modeledEntity) {
        this.modeledEntity = modeledEntity;
    }

    /**
     * Stops all currently playing animations
     */
    public void stopCurrentAnimations() {
        if (animationManager != null) animationManager.stop();
    }

    /**
     * Plays an animation as set by the string name.
     *
     * @param animationName  Name of the animation - case-sensitive
     * @param blendAnimation If the animation should blend. If set to false, the animation passed will stop other animations.
     *                       If set to true, the animation will be mixed with any currently ongoing animations
     * @return Whether the animation successfully started playing.
     */
    public boolean playAnimation(String animationName, boolean blendAnimation, boolean loop) {
        return animationManager.play(animationName, blendAnimation, loop);
    }

    /**
     * Checks if the animation with the specified name exists for the current animation manager.
     *
     * @param animationName The name of the animation to check for. Case-sensitive.
     * @return true if the animation exists in the animation manager, false otherwise.
     */
    public boolean hasAnimation(String animationName) {
        if (animationManager == null) return false;
        return animationManager.hasAnimation(animationName);
    }

    /**
     * Plays the "death" animation for the entity using the animation manager.
     * The animation is non-blending and non-looping.
     *
     * @return true if the "death" animation was successfully played; false if the animation manager is null or the animation could not be played.
     */
    public boolean playDeathAnimation() {
        if (animationManager == null) return false;
        return animationManager.play("death", false, false);
    }

    /**
     * Internal use only - do not call manually
     */
    public void tick() {
        if (isScalingDown) {
            scaleTicksElapsed++;

            double t = Math.min(scaleTicksElapsed / (double) scaleDurationTicks, 1.0);
            modeledEntity.setScaleModifier(MathToolkit.lerp((float) scaleStart, (float) scaleEnd, (float) t));

            if (scaleTicksElapsed >= scaleDurationTicks) {
                modeledEntity.setScaleModifier(0.0);
                isScalingDown = false;
                modeledEntity.remove(); // triggers isRemoved = true
            }
        }
        if (animationManager == null) return;
        animationManager.tick();
    }

    /**
     * Internal use only - do not call manually
     */
    public void initializeAnimationManager(FileModelConverter fileModelConverter) {
        if (fileModelConverter.getAnimationsBlueprint() != null) {
            try {
                animationManager = new AnimationManager(modeledEntity, fileModelConverter.getAnimationsBlueprint());
                MessageUtil.log("Initialized AnimationManager for entityID: " + modeledEntity.getEntityID() + " with " + fileModelConverter.getAnimationsBlueprint().getAnimations().size() + " animations.");
            } catch (Exception e) {
                MessageUtil.log("Failed to initialize AnimationManager for entityID: " + modeledEntity.getEntityID() + ". Error: " + e.getMessage());
            }
        } else {
            MessageUtil.log("No AnimationsBlueprint found for entityID: " + modeledEntity.getEntityID() + ". AnimationManager not initialized.");
        }
    }

    /**
     * Internal use only - do not call manually
     */
    public void removeWithMinimizedAnimation() {
        isScalingDown = true;
        scaleTicksElapsed = 0;
        scaleStart = modeledEntity.getScaleModifier();
        scaleEnd = 0.0;
    }

    public ModeledEntity getModeledEntity() {
        return modeledEntity;
    }

    public AnimationManager getAnimationManager() {
        return animationManager;
    }

    public boolean isScalingDown() {
        return isScalingDown;
    }

    public void setAnimationManager(AnimationManager animationManager) {
        this.animationManager = animationManager;
    }

    public void setScalingDown(boolean scalingDown) {
        isScalingDown = scalingDown;
    }

    public int getScaleDurationTicks() {
        return scaleDurationTicks;
    }

    public int getScaleTicksElapsed() {
        return scaleTicksElapsed;
    }
}
