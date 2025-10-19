package com.magmaguy.freeminecraftmodels.packets;

import com.magmaguy.freeminecraftmodels.utils.DataAccessors;
import com.mojang.math.Transformation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.util.EulerAngle;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PacketBoneEntity extends PacketEntity {

    private net.minecraft.world.item.ItemStack item;

    public PacketBoneEntity(Location location) {
        super(EntityType.ITEM_DISPLAY, location);
    }

    public void initializeModel(String modelID) {
        synchedEntityData.set(DataAccessors.display_transformationInterpolationData, 0);
        synchedEntityData.set(DataAccessors.display_interpolationDelta, 1);
        synchedEntityData.set(DataAccessors.display_posRotInterpolationData, 1);
        synchedEntityData.set(DataAccessors.display_viewRangeData, 32f); // TODO: Configurable view range
        synchedEntityData.set(DataAccessors.display_widthData, 1f); // TODO: Configurable width
        synchedEntityData.set(DataAccessors.display_heightData, 1f); // TODO: Configurable height

        item = new ItemStack(Items.LEATHER_HORSE_ARMOR);
        item.set(DataComponents.ITEM_MODEL, ResourceLocation.parse(modelID));
        item.set(DataComponents.DYED_COLOR,new DyedItemColor(Color.WHITE.asRGB()));

        synchedEntityData.set(DataAccessors.itemDisplay_itemStackData, item);
        resendEntityDataForAll();
    }

    public void setHorseLeatherArmorColor(Color color) {
        item.set(DataComponents.DYED_COLOR,new DyedItemColor(color.asRGB()));
        synchedEntityData.set(DataAccessors.itemDisplay_itemStackData, item);
        resendEntityDataForAll();
    }

    public void sendLocationAndRotationPacket(Location location, EulerAngle eulerAngle) {
        teleport(location);
        Quaternionf quaternionf = eulerToQuaternion(
                Math.toDegrees(eulerAngle.getX()),
                Math.toDegrees(eulerAngle.getY()),
                Math.toDegrees(eulerAngle.getZ()));

        Transformation currentTransform = getTransformation();
        Transformation newTransformation = new Transformation(
                new Vector3f(0, 0, 0),
                quaternionf,
                currentTransform.getScale(),
                new Quaternionf(0, 0, 0, 1)
        );
        setTransformation(newTransformation);
    }

    public void sendLocationAndRotationAndScalePacket(Location location, EulerAngle eulerAngle, float scale) {
        teleport(location);
        Quaternionf quaternionf = eulerToQuaternion(
                Math.toDegrees(eulerAngle.getX()),
                Math.toDegrees(eulerAngle.getY()),
                Math.toDegrees(eulerAngle.getZ()));
        Transformation currentTrans = getTransformation();
        Transformation transformation = new Transformation(
                currentTrans.getTranslation(),
                quaternionf,
                new Vector3f(scale, scale, scale),
                currentTrans.getRightRotation()
        );
        setTransformation(transformation);
    }


    public void sendRotationAndScalePacket(EulerAngle eulerAngle, float scale) {
        Quaternionf quaternionf = eulerToQuaternion(
                Math.toDegrees(eulerAngle.getX()),
                Math.toDegrees(eulerAngle.getY()),
                Math.toDegrees(eulerAngle.getZ()));
        Transformation currentTrans = getTransformation();
        Transformation transformation = new Transformation(
                currentTrans.getTranslation(),
                quaternionf,
                new Vector3f(scale, scale, scale),
                currentTrans.getRightRotation()
        );
        setTransformation(transformation);
    }

    public void sendTranslationRotationAndScalePacket(Vector3f translation, EulerAngle eulerAngle, float scale) {
        Quaternionf quaternionf = eulerToQuaternion(
                Math.toDegrees(eulerAngle.getX()),
                Math.toDegrees(eulerAngle.getY()),
                Math.toDegrees(eulerAngle.getZ()));
        Transformation currentTrans = getTransformation();
        Transformation transformation = new Transformation(
                translation,
                quaternionf,
                new Vector3f(scale, scale, scale),
                currentTrans.getRightRotation()
        );
        setTransformation(transformation);
    }

}
