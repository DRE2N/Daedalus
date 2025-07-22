package de.erethon.daedalus.dataconverter;


import java.util.HashMap;
import java.util.List;

public class AnimationsBlueprint {
    private final HashMap<String, AnimationBlueprint> animations = new HashMap<>();

    public AnimationsBlueprint(List<Object> rawAnimationData, String modelName, SkeletonBlueprint skeletonBlueprint) {
        for (Object animation : rawAnimationData) {
            AnimationBlueprint animationBlueprintObject =new AnimationBlueprint(animation, modelName, skeletonBlueprint);
            animations.put(animationBlueprintObject.getAnimationName(), animationBlueprintObject);
        }
    }

    public HashMap<String, AnimationBlueprint> getAnimations() {
        return animations;
    }
}
