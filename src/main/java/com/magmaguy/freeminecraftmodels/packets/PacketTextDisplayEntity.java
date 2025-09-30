package com.magmaguy.freeminecraftmodels.packets;

import com.magmaguy.freeminecraftmodels.utils.DataAccessors;
import com.mojang.math.Transformation;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.minecraft.world.entity.EntityType;
import org.bukkit.Location;
import org.bukkit.util.EulerAngle;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class PacketTextDisplayEntity extends PacketEntity {

    public PacketTextDisplayEntity(Location location) {
        super(EntityType.TEXT_DISPLAY, location);
    }

    public void setText(Component text) {
        net.minecraft.network.chat.Component vanilla = PaperAdventure.asVanilla(text);
        synchedEntityData.set(DataAccessors.textDisplay_text, vanilla);
    }

    public void sendLocationAndRotationPacket(Location location, EulerAngle eulerAngle) {
        teleport(location);
        Quaternionf quaternionf = eulerToQuaternion(
                Math.toDegrees(eulerAngle.getX()),
                Math.toDegrees(eulerAngle.getY()),
                Math.toDegrees(eulerAngle.getZ()));
        rotate(quaternionf);
    }

    public void sendLocationAndRotationAndScalePacket(Location location, EulerAngle eulerAngle, float scale) {
        teleport(location);
        Quaternionf quaternionf = eulerToQuaternion(
                Math.toDegrees(eulerAngle.getX()),
                Math.toDegrees(eulerAngle.getY()),
                Math.toDegrees(eulerAngle.getZ()));
        Transformation transformation = getTransformation();
        transformation = new Transformation(transformation.getTranslation(), quaternionf, new Vector3f(scale,scale,scale), transformation.getRightRotation());
        setTransformation(transformation);
    }
}
