package com.magmaguy.freeminecraftmodels.customentity;

import com.magmaguy.freeminecraftmodels.animation.AnimationManager;
import com.magmaguy.freeminecraftmodels.customentity.core.OrientedBoundingBox;
import com.magmaguy.freeminecraftmodels.customentity.core.Skeleton;
import com.magmaguy.freeminecraftmodels.dataconverter.BoneBlueprint;
import com.magmaguy.freeminecraftmodels.dataconverter.FileModelConverter;
import com.magmaguy.freeminecraftmodels.dataconverter.SkeletonBlueprint;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class ModeledEntity {
    private final int scaleDurationTicks = 20;
    /**
     * Whether the entity is currently dying.
     * This is set to true when the entity is in the process of getting removed with a death animation.
     */
    @Getter
    private boolean isDying = false;
    @Getter
    private static final HashSet<ModeledEntity> loadedModeledEntities = new HashSet<>();
    @Getter
    private final String entityID;
    @Getter
    private final String name = "default";
    private final Location spawnLocation;
    @Getter
    private final List<TextDisplay> nametags = new ArrayList<>();
    @Getter
    private final Location lastSeenLocation;
    @Getter
    protected LivingEntity livingEntity = null;
    @Getter
    private SkeletonBlueprint skeletonBlueprint = null;
    @Getter
    private Skeleton skeleton;
    private AnimationManager animationManager = null;
    @Getter
    private OrientedBoundingBox obbHitbox = null;

    public ModeledEntity(String entityID, Location spawnLocation) {
        this.entityID = entityID;
        this.spawnLocation = spawnLocation;
        this.lastSeenLocation = spawnLocation;

        FileModelConverter fileModelConverter = FileModelConverter.getConvertedFileModels().get(entityID);
        if (fileModelConverter == null) {
            Logger.warn("Failed to initialize ModeledEntity: FileModelConverter not found for entityID: " + entityID);
            return;
        }

        skeletonBlueprint = fileModelConverter.getSkeletonBlueprint();
        if (skeletonBlueprint == null) {
            Logger.warn("Failed to initialize ModeledEntity: SkeletonBlueprint not found for entityID: " + entityID);
            return;
        }

        skeleton = new Skeleton(skeletonBlueprint, this);

        if (fileModelConverter.getAnimationsBlueprint() != null) {
            try {
                animationManager = new AnimationManager(this, fileModelConverter.getAnimationsBlueprint());
            } catch (Exception e) {
                Logger.warn("Failed to initialize AnimationManager for entityID: " + entityID + ". Error: " + e.getMessage());
            }
        } else {
            Logger.warn("No AnimationsBlueprint found for entityID: " + entityID + ". AnimationManager not initialized.");
        }

        loadedModeledEntities.add(this);
    }
    @Getter
    private boolean isRemoved = false;
    @Getter
    @Setter
    private double scaleModifier = 1.0;

    private static boolean isNameTag(ArmorStand armorStand) {
        return armorStand.getPersistentDataContainer().has(BoneBlueprint.nameTagKey, PersistentDataType.BYTE);
    }

    public OrientedBoundingBox getObbHitbox() {
        if (obbHitbox == null) {
            if (getSkeletonBlueprint().getHitbox() != null) {
                return obbHitbox = new OrientedBoundingBox(
                        getSkeleton().getCurrentLocation(),
                        getSkeletonBlueprint().getHitbox().getWidthX(),
                        getSkeletonBlueprint().getHitbox().getHeight(),
                        getSkeletonBlueprint().getHitbox().getWidthZ());
            } else {
                return obbHitbox = new OrientedBoundingBox(getSkeleton().getCurrentLocation(), 1, 2, 1);
            }
        } else return obbHitbox;
    }
    private boolean isScalingDown = false;
    private int scaleTicksElapsed = 0;
    private double scaleStart = 1.0;

    public Location getSpawnLocation() {
        return spawnLocation.clone();
    }

    protected void displayInitializer(Location targetLocation) {
        skeleton.generateDisplays(targetLocation);
    }

    public void spawn(Location location) {
        displayInitializer(location);
    }
    private double scaleEnd = 0.0;

    public void spawn() {
        spawn(lastSeenLocation);
    }

    public static void shutdown() {
        Iterator<ModeledEntity> iterator = loadedModeledEntities.iterator();
        while (iterator.hasNext()) {
            ModeledEntity entity = iterator.next();
            entity.shutdownRemove();
        }
        loadedModeledEntities.clear();
    }

    protected void shutdownRemove() {
        remove();
    }

    public void tick() {
        if (isRemoved) return; // ⬅ Stop ticking if the entity is already removed

        getSkeleton().transform();
        updateHitbox();

        if (isScalingDown) {
            scaleTicksElapsed++;

            double t = Math.min(scaleTicksElapsed / (double) scaleDurationTicks, 1.0);
            scaleModifier = lerp(scaleStart, scaleEnd, t);

            if (scaleTicksElapsed >= scaleDurationTicks) {
                scaleModifier = 0.0;
                isScalingDown = false;
                remove(); // triggers isRemoved = true
            }
        }

        if (animationManager != null) {
            animationManager.tick();
        }
    }

    private double lerp(double start, double end, double t) {
        return start + (end - start) * t;
    }

    protected void updateHitbox() {
        getObbHitbox().update(getLocation());
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

    public void removeWithDeathAnimation() {
        isDying = true;
        if (animationManager != null) {
            if (!animationManager.play("death", false, false)) {
                remove();
            }
        } else remove();
    }

    public void removeWithMinimizedAnimation() {
        if (isScalingDown) return;
        isDying = true;
        isScalingDown = true;
        scaleTicksElapsed = 0;
        scaleStart = scaleModifier;
        scaleEnd = 0.0;
    }

    public void remove() {
        if (isRemoved) {
            return;
        }

        skeleton.remove();
        loadedModeledEntities.remove(this);
        if (livingEntity != null) livingEntity.remove();
        isRemoved = true;
    }

    /**
     * Stops all currently playing animations
     */
    public void stopCurrentAnimations() {
        if (animationManager != null) animationManager.stop();
    }

    public boolean hasAnimation(String animationName) {
        if (animationManager == null) return false;
        return animationManager.hasAnimation(animationName);
    }

    /**
     * Sets the custom name that is visible in-game on the entity
     *
     * @param name Name to set
     */
    public void setName(String name) {
        skeleton.setName(name);
    }

    /**
     * Default is false
     *
     * @param visible Sets whether the name is visible
     */
    public void setNameVisible(boolean visible) {
        skeleton.setNameVisible(visible);
    }

    /**
     * Returns the name tag locations. Useful if you want to add more text above or below them.
     * Not currently guaranteed to be the exact location.
     *
     * @return
     */
    public List<ArmorStand> getNametagArmorstands() {
        return skeleton.getNametags();
    }

    public Location getLocation() {
        return spawnLocation.clone();
    }

    public boolean isChunkLoaded() {
        return getWorld().isChunkLoaded(getLocation().getBlockX() >> 4, getLocation().getBlockZ() >> 4);
    }

    public World getWorld() {
        //Overriden by extending classes
        return null;
    }

    public void damage(Player player, double damage) {
        //Overriden by extending classes
    }

    public void damage(Player player) {
        //Overriden by extending classes
    }

    public void teleport(Location location) {
        skeleton.teleport(location);
    }
}
