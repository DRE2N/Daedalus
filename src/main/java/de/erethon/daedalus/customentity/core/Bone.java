package de.erethon.daedalus.customentity.core;

import de.erethon.daedalus.config.DefaultConfig;
import de.erethon.daedalus.dataconverter.BoneBlueprint;
import de.erethon.daedalus.thirdparty.BedrockChecker;
import de.erethon.bedrock.chat.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Bone {
    private final BoneBlueprint boneBlueprint;
    private final List<Bone> boneChildren = new ArrayList<>();
    private final Bone parent;
    private final Skeleton skeleton;
    private final BoneTransforms boneTransforms;
    private Vector3f animationTranslation = new Vector3f();
    private Vector3f animationRotation = new Vector3f();
    private float animationScale = -1;

    public Bone(BoneBlueprint boneBlueprint, Bone parent, Skeleton skeleton) {
        this.boneBlueprint = boneBlueprint;
        this.parent = parent;
        this.skeleton = skeleton;
        this.boneTransforms = new BoneTransforms(this, parent);
        for (BoneBlueprint child : boneBlueprint.getBoneBlueprintChildren())
            boneChildren.add(new Bone(child, this, skeleton));
    }

    public void updateAnimationTranslation(float x, float y, float z) {
        animationTranslation = new Vector3f(x, y, z);
    }

    public void updateAnimationRotation(double x, double y, double z) {
        animationRotation = new Vector3f((float) Math.toRadians(x), (float) Math.toRadians(y), (float) Math.toRadians(z));
    }

    public void updateAnimationScale(float animationScale) {
        this.animationScale = animationScale;
    }

    //Note that several optimizations might be possible here, but that syncing with a base entity is necessary.
    public void transform() {
        boneTransforms.transform();
        boneChildren.forEach(Bone::transform);
        skeleton.getSkeletonWatchers().sendPackets(this);
    }

    public void generateDisplay() {
        boneTransforms.generateDisplay();
        boneChildren.forEach(Bone::generateDisplay);
    }

    public void setNameVisible(boolean visible) {
        boneChildren.forEach(child -> child.setNameVisible(visible));
    }

    public void remove() {
        if (boneTransforms.getPacketTextDisplayEntity() != null)
            boneTransforms.getPacketTextDisplayEntity().remove();
        if (boneTransforms.getPacketArmorStandEntity() != null) boneTransforms.getPacketArmorStandEntity().remove();
        if (boneTransforms.getPacketDisplayEntity() != null) boneTransforms.getPacketDisplayEntity().remove();
        boneChildren.forEach(Bone::remove);
    }

    protected void getAllChildren(HashMap<String, Bone> children) {
        boneChildren.forEach(child -> {
            children.put(child.getBoneBlueprint().getBoneName(), child);
            child.getAllChildren(children);
        });
    }

    public void sendUpdatePacket() {
        boneTransforms.sendUpdatePacket();
    }

    boolean warned = false;

    public void displayTo(Player player) {
        boolean isBedrock = BedrockChecker.isBedrock(player);
        if (isBedrock && DefaultConfig.sendCustomModelsToBedrockClients) return;
        if (boneBlueprint.isNameTag()) {
            if (boneTransforms.getPacketTextDisplayEntity() == null) {
                if (!warned) {
                    MessageUtil.log("nametag bone did not spawn name tag");
                    warned = true;
                }
                return;
            }
            boneTransforms.getPacketTextDisplayEntity().displayTo(player);
        }
        else if (boneTransforms.getPacketDisplayEntity() != null)
            boneTransforms.getPacketDisplayEntity().displayTo(player);
    }

    public void hideFrom(UUID playerUUID) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (boneTransforms.getPacketTextDisplayEntity() != null)
            boneTransforms.getPacketTextDisplayEntity().hideFrom(player);
        if (boneTransforms.getPacketArmorStandEntity() != null)
            boneTransforms.getPacketArmorStandEntity().hideFrom(player);
        if (boneTransforms.getPacketDisplayEntity() != null)
            boneTransforms.getPacketDisplayEntity().hideFrom(player);
    }

    public void setHorseLeatherArmorColor(Color color) {
        if (boneTransforms.getPacketArmorStandEntity() != null)
            //boneTransforms.getPacketArmorStandEntity().setHorseLeatherArmorColor(color); - Bedrock support
        if (boneTransforms.getPacketDisplayEntity() != null)
            boneTransforms.getPacketDisplayEntity().setHorseLeatherArmorColor(color);
    }

    public void spawnParticles(Particle particle, double speed) {
        Location boneLocation;
        if (boneTransforms.getPacketDisplayEntity() != null) {
            boneLocation = boneTransforms.getDisplayEntityTargetLocation();
            if (boneLocation.getWorld() == null) return;
            boneLocation.getWorld().spawnParticle(particle, boneLocation, 1, 1, 1, 1, speed);
        } else if (boneTransforms.getPacketArmorStandEntity() != null) {
            boneLocation = boneTransforms.getArmorStandTargetLocation();
            if (boneLocation.getWorld() == null) return;
            boneLocation.getWorld().spawnParticle(particle, boneLocation, 1, 1, 1, 1, speed);
        }
    }

    public void teleport() {
        sendTeleportPacket();
        boneChildren.forEach(Bone::teleport);
    }

    private void sendTeleportPacket() {
        if (boneTransforms.getPacketArmorStandEntity() != null) {
            boneTransforms.getPacketArmorStandEntity().teleport(boneTransforms.getArmorStandTargetLocation());
        }
        if (boneTransforms.getPacketDisplayEntity() != null) {
            boneTransforms.getPacketDisplayEntity().teleport(boneTransforms.getDisplayEntityTargetLocation());
        }
        skeleton.getSkeletonWatchers().resync(true);
    }

    // Getters
    public String getBoneName() {
        return boneBlueprint.getBoneName();
    }

    public Vector3f getAnimationTranslation() {
        return animationTranslation;
    }

    public Vector3f getAnimationRotation() {
        return animationRotation;
    }

    public float getAnimationScale() {
        return animationScale;
    }

    public BoneTransforms getBoneTransforms() {
        return boneTransforms;
    }

    public List<Bone> getBoneChildren() {
        return boneChildren;
    }

    public Bone getParent() {
        return parent;
    }

    public Skeleton getSkeleton() {
        return skeleton;
    }

    public BoneBlueprint getBoneBlueprint() {
        return boneBlueprint;
    }

}
