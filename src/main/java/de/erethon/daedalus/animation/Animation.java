package de.erethon.daedalus.animation;

import de.erethon.daedalus.Daedalus;
import de.erethon.daedalus.customentity.ModeledEntity;
import de.erethon.daedalus.customentity.core.Bone;
import de.erethon.daedalus.dataconverter.AnimationBlueprint;
import de.erethon.daedalus.dataconverter.AnimationFrame;

import java.util.HashMap;

public class Animation {
    private final AnimationBlueprint animationBlueprint;
    private final HashMap<Bone, AnimationFrame[]> animationFrames = new HashMap<>();
    private int counter = 0;

    public void incrementCounter() {
        counter++;
    }

    public Animation(AnimationBlueprint animationBlueprint, ModeledEntity modeledEntity) {
        this.animationBlueprint = animationBlueprint;
        animationBlueprint.getAnimationFrames().forEach((key, value) -> {
            for (Bone bone : modeledEntity.getSkeleton().getBones())
                if (bone.getBoneBlueprint().equals(key)) {
                    animationFrames.put(bone, value);
                    break;
                }
        });
        modeledEntity.getSkeleton().getBones().forEach(bone -> {
            if (!animationFrames.containsKey(bone)) {
                animationFrames.put(bone, null);
            }
        });
        Daedalus.log("Loaded animation: " + animationBlueprint.getAnimationName() + " with " + animationFrames.size() + " frames.");
    }

    public void resetCounter() {
        counter = 0;
    }

    public AnimationBlueprint getAnimationBlueprint() {
        return animationBlueprint;
    }

    public HashMap<Bone, AnimationFrame[]> getAnimationFrames() {
        return animationFrames;
    }

    public AnimationFrame[] getAnimationFramesForBone(Bone bone) {
        return animationFrames.get(bone);
    }

    public int getCounter() {
        return counter;
    }
}
