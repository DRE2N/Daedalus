package de.erethon.daedalus.customentity.core;

import de.erethon.bedrock.chat.MessageUtil;
import de.erethon.daedalus.dataconverter.BoneBlueprint;
import de.erethon.daedalus.packets.PacketArmorStand;
import de.erethon.daedalus.packets.PacketBoneEntity;
import de.erethon.daedalus.packets.PacketTextDisplayEntity;
import de.erethon.daedalus.utils.TransformationMatrix;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

public class BoneTransforms {

    private final Bone parent;
    private final Bone bone;
    private final TransformationMatrix localMatrix = new TransformationMatrix();
    private TransformationMatrix globalMatrix = new TransformationMatrix();
    private PacketArmorStand packetArmorStandEntity = null;
    private PacketBoneEntity packetDisplayEntity = null;
    private PacketTextDisplayEntity packetTextDisplayEntity = null;

    public BoneTransforms(Bone bone, Bone parent) {
        this.bone = bone;
        this.parent = parent;
    }

    public void setTextDisplayText(Component text) {
        if (packetTextDisplayEntity == null) return;
        packetTextDisplayEntity.setText(text);
    }

    public void setTextDisplayVisible(boolean visible) {
        if (packetTextDisplayEntity == null) return;
    }

    public void transform() {
        updateLocalTransform();
        updateGlobalTransform();
    }

    public void updateGlobalTransform() {
        if (parent != null) {
            TransformationMatrix.multiplyMatrices(parent.getBoneTransforms().globalMatrix, localMatrix, globalMatrix);
            if (bone.getBoneBlueprint().isHead()) {
                // Store the inherited scale before resetting
                double[] inheritedScale = globalMatrix.getScale(); // or however you access scale in your matrix

                globalMatrix.resetRotation();
                float yaw = -bone.getSkeleton().getCurrentHeadYaw() + 180;
                globalMatrix.rotateY((float) Math.toRadians(yaw));
                globalMatrix.rotateX(-(float) Math.toRadians(bone.getSkeleton().getCurrentHeadPitch()));

                // Reapply the inherited scale
                globalMatrix.scale(inheritedScale[0], inheritedScale[1], inheritedScale[2]);
            }
        } else {
            globalMatrix = localMatrix;
        }
    }

    public void updateLocalTransform() {
        localMatrix.resetToIdentityMatrix();
        shiftPivotPoint();
        translateModelCenter();
        translateAnimation();
        rotateAnimation();
        rotateDefaultBoneRotation();
        scaleAnimation();
        shiftPivotPointBack();
        rotateByEntityYaw();
    }

    private void scaleAnimation() {
        localMatrix.scale(getDisplayEntityScale() / 2.5f, getDisplayEntityScale() / 2.5f, getDisplayEntityScale() / 2.5f);
    }

    //Shift to model center
    private void translateModelCenter() {
        localMatrix.translateLocal(bone.getBoneBlueprint().getModelCenter());

        //The bone is relative to its parent, so remove the offset of the parent
        if (parent != null) {
            Vector3f modelCenter = parent.getBoneBlueprint().getModelCenter();
            modelCenter.mul(-1);
            localMatrix.translateLocal(modelCenter);
        }
    }

    private void shiftPivotPoint() {
        localMatrix.translateLocal(bone.getBoneBlueprint().getBlueprintModelPivot().mul(-1));
    }

    private void translateAnimation() {
        localMatrix.translateLocal(
                -bone.getAnimationTranslation().get(0),
                bone.getAnimationTranslation().get(1),
                bone.getAnimationTranslation().get(2));
    }

    private void rotateAnimation() {
        Vector test = new Vector(bone.getAnimationRotation().get(0), -bone.getAnimationRotation().get(1), -bone.getAnimationRotation().get(2));
        test.rotateAroundY(Math.PI);
        localMatrix.rotateAnimation(
                (float) test.getX(),
                (float) test.getY(),
                (float) test.getZ());
    }

    private void rotateDefaultBoneRotation() {
        localMatrix.rotateLocal(
                bone.getBoneBlueprint().getBlueprintOriginalBoneRotation().get(0),
                bone.getBoneBlueprint().getBlueprintOriginalBoneRotation().get(1),
                bone.getBoneBlueprint().getBlueprintOriginalBoneRotation().get(2));
    }

    private void shiftPivotPointBack() {
        //Remove the pivot point, go back to the model center
        localMatrix.translateLocal(bone.getBoneBlueprint().getBlueprintModelPivot());
    }

    public void generateDisplay() {
        transform();
        if (bone.getBoneBlueprint().isDisplayModel()) {
            if (bone.getBoneBlueprint().isNameTag()) {
                initializeTextDisplayBone();
                return;
            }
            initializeDisplayEntityBone();
            //initializeArmorStandBone(); - For bedrock support, this would be used instead
        }
    }

    private void initializeTextDisplayBone() {
        Location textDisplayLocation = getArmorStandTargetLocation();
        packetTextDisplayEntity = new PacketTextDisplayEntity(textDisplayLocation);
    }

    private void initializeDisplayEntityBone() {
        Location displayEntityLocation = getDisplayEntityTargetLocation();
        packetDisplayEntity = new PacketBoneEntity(displayEntityLocation);
        packetDisplayEntity.initializeModel(bone.getBoneBlueprint().getModelID());
        //packetDisplayEntity.sendLocationAndRotationPacket(displayEntityLocation, getDisplayEntityRotation());
        packetDisplayEntity.sendLocationAndRotationAndScalePacket(getDisplayEntityTargetLocation(), getDisplayEntityRotation(), (float) globalMatrix.getScale()[0] * 2.5f);
    }

    private void rotateByEntityYaw() {
        //rotate by yaw amount
        if (parent == null) {
            localMatrix.rotateLocal(0, (float) -Math.toRadians(bone.getSkeleton().getCurrentLocation().getYaw() + 180), 0);
        }
    }

    protected Location getArmorStandTargetLocation() {
        double[] translatedGlobalMatrix = globalMatrix.getTranslation();
        Location armorStandLocation = new Location(bone.getSkeleton().getCurrentLocation().getWorld(),
                translatedGlobalMatrix[0],
                translatedGlobalMatrix[1],
                translatedGlobalMatrix[2])
                .add(bone.getSkeleton().getCurrentLocation());
        armorStandLocation.setYaw(180);
        armorStandLocation.subtract(new Vector(0, BoneBlueprint.ARMOR_STAND_PIVOT_POINT_HEIGHT, 0));
        return armorStandLocation;
    }

    protected Location getDisplayEntityTargetLocation() {
        double[] translatedGlobalMatrix = globalMatrix.getTranslation();
        Location displayLocation = new Location(bone.getSkeleton().getCurrentLocation().getWorld(),
                translatedGlobalMatrix[0],
                translatedGlobalMatrix[1],
                translatedGlobalMatrix[2])
                .add(bone.getSkeleton().getCurrentLocation());
        displayLocation.setYaw(180);
        return displayLocation;
    }

    protected EulerAngle getDisplayEntityRotation() {
        double[] rotation = globalMatrix.getRotation();
        return new EulerAngle(-rotation[0], rotation[1], -rotation[2]);
    }

    protected EulerAngle getArmorStandEntityRotation() {
        double[] rotation = globalMatrix.getRotation();
        return new EulerAngle(-rotation[0], -rotation[1], rotation[2]);
    }

    public void sendUpdatePacket() {
        if (packetArmorStandEntity != null && packetArmorStandEntity.hasViewers()) {
            //sendArmorStandUpdatePacket(); Bedrock support would use this
        }
        if (packetDisplayEntity != null && packetDisplayEntity.hasViewers()) {
            sendDisplayEntityUpdatePacket();
        }
        if (packetTextDisplayEntity != null && packetTextDisplayEntity.hasViewers()) {
            sendTextDisplayUpdatePacket();
        }
    }

    private void sendTextDisplayUpdatePacket() {
        packetTextDisplayEntity.sendLocationAndRotationAndScalePacket(
                getArmorStandTargetLocation(),
                new EulerAngle(0, 0, 0),
                1f);
    }


    protected float getDisplayEntityScale() {
        float scale = bone.getAnimationScale() == -1 ? 2.5f : bone.getAnimationScale() * 2.5f;
        //Only the root bone/head should be scaling up globally like this, otherwise the scale will be inherited by each bone and then become progressively larger or smaller
        if (bone.getParent() == null) {
            double scaleModifier = bone.getSkeleton().getModeledEntity().getScaleModifier();
            if (bone.getSkeleton().getModeledEntity().getUnderlyingEntity() != null && bone.getSkeleton().getModeledEntity() instanceof LivingEntity livingEntity && livingEntity.getAttribute(Attribute.SCALE) != null)
                scaleModifier *= livingEntity.getAttribute(Attribute.SCALE).getValue();
            scale *= (float) scaleModifier;
        }
        return scale;
    }


    private void sendDisplayEntityUpdatePacket() {
        if (packetDisplayEntity != null) {
            packetDisplayEntity.sendLocationAndRotationAndScalePacket(getDisplayEntityTargetLocation(), getDisplayEntityRotation(), (float) globalMatrix.getScale()[0] * 2.5f);
        }
    }

    // Getters
    public Bone getParent() {
        return parent;
    }

    public Bone getBone() {
        return bone;
    }

    public TransformationMatrix getLocalMatrix() {
        return localMatrix;
    }

    public TransformationMatrix getGlobalMatrix() {
        return globalMatrix;
    }

    public PacketArmorStand getPacketArmorStandEntity() {
        return packetArmorStandEntity;
    }

    public PacketBoneEntity getPacketDisplayEntity() {
        return packetDisplayEntity;
    }

    public PacketTextDisplayEntity getPacketTextDisplayEntity() {
        return packetTextDisplayEntity;
    }

}