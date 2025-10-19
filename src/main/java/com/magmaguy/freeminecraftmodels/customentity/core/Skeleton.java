package com.magmaguy.freeminecraftmodels.customentity.core;

import com.magmaguy.freeminecraftmodels.customentity.ModeledEntity;
import com.magmaguy.freeminecraftmodels.dataconverter.BoneBlueprint;
import com.magmaguy.freeminecraftmodels.dataconverter.SkeletonBlueprint;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class Skeleton {

    private final List<BoneBlueprint> mainModel = new ArrayList<>();
    //In BlockBench models are referred to by name for animations, and names are unique
    private final HashMap<String, Bone> boneMap = new HashMap<>();
    private final SkeletonBlueprint skeletonBlueprint;
    private final SkeletonWatchers skeletonWatchers;
    private final List<Bone> nametags = new ArrayList<>();
    private float currentHeadPitch = 0;
    private float currentHeadYaw = 0;
//    private BukkitTask damageTintTask = null;
//    @Getter
//    @Setter
//    private DynamicEntity dynamicEntity = null; //todo: this wasn't in use?
    private ModeledEntity modeledEntity = null;
    private Bone rootBone = null;
    private boolean isMounted = false; // Track if bones are mounted as passengers

    public Skeleton(SkeletonBlueprint skeletonBlueprint, ModeledEntity modeledEntity) {
        this.skeletonBlueprint = skeletonBlueprint;
        this.modeledEntity = modeledEntity;
        skeletonBlueprint.getBoneMap().forEach((key, value) -> {
            if (value.getParent() == null) {
                Bone bone = new Bone(value, null, this);
                boneMap.put(key, bone);
                bone.getAllChildren(boneMap);
                rootBone = bone;
            }
        });
        skeletonWatchers = new SkeletonWatchers(this);
    }

    @Nullable
    public Location getCurrentLocation() {
       return modeledEntity.getLocation();
    }

    public void generateDisplays() {
        rootBone.generateDisplay();
        boneMap.values().forEach(bone -> {
            if (bone.getBoneBlueprint().isNameTag()) nametags.add(bone);
        });
    }

    public void remove() {
        boneMap.values().forEach(Bone::remove);
    }

    public List<Bone> getNametags() {
        List<Bone> nametags = new ArrayList<>();
        for (Bone value : boneMap.values()) {
            if (value.getBoneBlueprint().isNameTag())
                nametags.add(value);
        }
        return nametags;
    }

    /**
     * Returns the map of bones the Skeleton has
     *
     * @return
     */
    public Collection<Bone> getBones() {
        return boneMap.values();
    }

    private boolean tinting = false;
    private int tintCounter = 0;

    /**
     * This updates animations. The plugin runs this automatically, don't use it unless you know what you're doing!
     */
    public void tick() {
        skeletonWatchers.tick();

        // handle tint animation
        if (tinting) {
            tintCounter++;

            if (tintCounter <= 10) {
                // ramp from red (255,0,0) toward white (255,255,255)
                // At tintCounter=1: gAndB=0 -> Color(255,0,0) = red
                // At tintCounter=10: gAndB=255 -> Color(255,255,255) = white
                int gAndB = (int) ((tintCounter - 1) * 255.0 / 9.0);
                Color tint = Color.fromRGB(255, gAndB, gAndB);
                boneMap.values().forEach(b -> b.setHorseLeatherArmorColor(tint));
            } else {
                // after frame 10, either keep poofing (if dying) or finish
                if (!modeledEntity.isDying()) {
                    // done
                    tinting = false;
                    boneMap.values().forEach(b -> b.setHorseLeatherArmorColor(Color.WHITE));
                } else if (modeledEntity.isRemoved()) {
                    // entity gone, cancel
                    tinting = false;
                } else {
                    // still dying: emit poofs every 5 ticks
                    if (tintCounter % 5 == 0) {
                        boneMap.values().forEach(b -> b.spawnParticles(Particle.POOF, .1));
                    }
                }
            }
        }

        if (getSkeletonWatchers().hasObservers()) {
            rootBone.transform();
        }
    }

    public void tint() {
        // start (or restart) the tint animation
        tinting = true;
        tintCounter = 0;
    }

    public void teleport() {
        rootBone.teleport();
    }

    // Getters and Setters

    public SkeletonWatchers getSkeletonWatchers() {
        return skeletonWatchers;
    }

    public ModeledEntity getModeledEntity() {
        return modeledEntity;
    }

    public void setModeledEntity(ModeledEntity modeledEntity) {
        this.modeledEntity = modeledEntity;
    }

    public Bone getRootBone() {
        return rootBone;
    }

    public void setRootBone(Bone rootBone) {
        this.rootBone = rootBone;
    }

    public List<BoneBlueprint> getMainModel() {
        return mainModel;
    }

    public HashMap<String, Bone> getBoneMap() {
        return boneMap;
    }

    public SkeletonBlueprint getSkeletonBlueprint() {
        return skeletonBlueprint;
    }

    public float getCurrentHeadPitch() {
        return currentHeadPitch;
    }

    public void setCurrentHeadPitch(float currentHeadPitch) {
        this.currentHeadPitch = currentHeadPitch;
    }

    public float getCurrentHeadYaw() {
        return currentHeadYaw;
    }

    public void setCurrentHeadYaw(float currentHeadYaw) {
        this.currentHeadYaw = currentHeadYaw;
    }

    public boolean isMounted() {
        return isMounted;
    }

    public void setMounted(boolean mounted) {
        isMounted = mounted;
    }

}
