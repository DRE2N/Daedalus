package de.erethon.daedalus.animation;

import de.erethon.bedrock.chat.MessageUtil;
import de.erethon.daedalus.customentity.ModeledEntity;
import de.erethon.daedalus.dataconverter.AnimationsBlueprint;

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
