package com.magmaguy.freeminecraftmodels.animation;

import com.magmaguy.freeminecraftmodels.customentity.ModeledEntity;
import com.magmaguy.freeminecraftmodels.dataconverter.AnimationsBlueprint;

import java.util.HashMap;

public class Animations {
    private final HashMap<String, Animation> animations = new HashMap<>();
    private final AnimationsBlueprint animationBlueprint;
    public Animations(AnimationsBlueprint animationsBlueprint, ModeledEntity modeledEntity){
        this.animationBlueprint = animationsBlueprint;
        animationsBlueprint.getAnimations().forEach((key, value) -> animations.put(key, new Animation(value, modeledEntity)));
    }

    public Animation getAnimation(String animationName) {
        return animations.get(animationName);
    }

    public HashMap<String, Animation> getAnimations() {
        return animations;
    }
}
