package com.magmaguy.freeminecraftmodels.packets;

import com.magmaguy.freeminecraftmodels.utils.DataAccessors;
import com.magmaguy.freeminecraftmodels.utils.DataMappings;
import com.mojang.math.Transformation;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PacketEntity {

    protected EntityType entityType;
    protected int entityId;
    protected UUID uuid;
    protected Set<ServerPlayer> viewers = new HashSet<>();
    protected SynchedEntityData synchedEntityData;
    protected boolean isRemoved = false;
    protected EntityDataAccessor<Byte> sharedFlags;

    double x;
    double y;
    double z;
    float yaw;
    float pitch;

    public PacketEntity(EntityType entityType, Location location) {
        synchedEntityData = DataMappings.getSynchedEntityData(entityType);
        this.entityType = entityType;
        this.entityId = Bukkit.getUnsafe().nextEntityId();
        this.uuid = UUID.randomUUID();
        this.sharedFlags = DataMappings.getAccessor(Entity.class, "DATA_SHARED_FLAGS_ID");
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    public void displayTo(Player player) {
        addNewViewer(((CraftPlayer) player).getHandle());
    }

    public void addNewViewer(ServerPlayer serverPlayer) {
        viewers.add(serverPlayer);
        ClientboundAddEntityPacket addEntityPacket = new ClientboundAddEntityPacket(entityId, uuid, x, y, z, pitch, yaw, entityType, 0, Vec3.ZERO, 0);
        ClientboundSetEntityDataPacket entityDataPacket = null;
        if (synchedEntityData != null) {
            entityDataPacket = new ClientboundSetEntityDataPacket(entityId, synchedEntityData.packAll());
        }
        if (entityDataPacket != null) {
            ClientboundBundlePacket bundlePacket = new ClientboundBundlePacket(List.of(addEntityPacket, entityDataPacket));
            serverPlayer.connection.send(bundlePacket);
            return;
        }
        ClientboundBundlePacket bundlePacket = new ClientboundBundlePacket(List.of(addEntityPacket));
        serverPlayer.connection.send(bundlePacket);
    }

    public void hideFrom(Player player) {
        removeViewer(((CraftPlayer) player).getHandle());
    }

    public void removeViewer(ServerPlayer serverPlayer) {
        ClientboundRemoveEntitiesPacket removeEntityPacket = new ClientboundRemoveEntitiesPacket(entityId);
        serverPlayer.connection.send(removeEntityPacket);
        viewers.remove(serverPlayer);
    }

    public void remove() {
        isRemoved = true;
        ClientboundRemoveEntitiesPacket removeEntityPacket = new ClientboundRemoveEntitiesPacket(entityId);
        sendPacketToAllViewers(removeEntityPacket);
    }

    public void resendEntityData(ServerPlayer serverPlayer) {
        if (synchedEntityData != null) {
            ClientboundSetEntityDataPacket entityDataPacket = new ClientboundSetEntityDataPacket(entityId, synchedEntityData.packAll());
            serverPlayer.connection.send(entityDataPacket);
        }
    }

    public void resendEntityDataForAll() {
        for (ServerPlayer viewer : viewers) {
            resendEntityData(viewer);
        }
    }

    public boolean hasViewers() {
        return !viewers.isEmpty();
    }

    protected void setSharedFlag(int flag, boolean set) {
        byte b = synchedEntityData.get(sharedFlags);
        if (set) {
            synchedEntityData.set(sharedFlags, (byte)(b | 1 << flag));
        } else {
            synchedEntityData.set(sharedFlags, (byte)(b & ~(1 << flag)));
        }
    }

    public void teleport(Location location) {
        teleport(location.getX(), location.getY(), location.getZ());
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    public void teleport(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        PositionMoveRotation positionMoveRotation = new PositionMoveRotation(new Vec3(x, y, z), new Vec3(0, 0, 0), yaw, pitch);
        ClientboundTeleportEntityPacket teleportEntityPacket = new ClientboundTeleportEntityPacket(entityId, positionMoveRotation, new HashSet<>(), false);
        sendPacketToAllViewers(teleportEntityPacket);
    }

    public void setView(double yaw, double pitch) {
        ClientboundMoveEntityPacket moveEntityPacket = new ClientboundMoveEntityPacket.Rot(entityId, (byte) (yaw * 256 / 360), (byte) (pitch * 256 / 360), true);
        sendPacketToAllViewers(moveEntityPacket);
    }

    private void sendPacketToAllViewers(Packet<?> packet) {
        for (ServerPlayer viewer : viewers) {
            viewer.connection.send(packet);
        }
    }

    public int getEntityId() {
        return entityId;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public SynchedEntityData getSynchedEntityData() {
        return synchedEntityData;
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public void setGlowing(boolean glowing) {
        setSharedFlag(6, glowing);
        resendEntityDataForAll();
    }

    public void setInvisible(boolean b) {
        setSharedFlag(5, b);
        resendEntityDataForAll();
    }

    public void setSilent(boolean b) {
        setSharedFlag(4, b);
        resendEntityDataForAll();
    }

    // Util method for client flags
    public byte setBit(byte oldBit, int offset, boolean value) {
        if (value) {
            oldBit = (byte)(oldBit | offset);
        } else {
            oldBit = (byte)(oldBit & ~offset);
        }

        return oldBit;
    }

    public Transformation getTransformation() {
        Transformation nms = Display.createTransformation(synchedEntityData);
        return new Transformation(nms.getTranslation(), nms.getLeftRotation(), nms.getScale(), nms.getRightRotation());
    }

    protected void setTransformation(Transformation transformation) {
        synchedEntityData.set(DataAccessors.display_translationData, transformation.getTranslation());
        synchedEntityData.set(DataAccessors.display_leftRotationData, transformation.getLeftRotation());
        synchedEntityData.set(DataAccessors.display_rightRotationData, transformation.getRightRotation());
        synchedEntityData.set(DataAccessors.display_scaleData, transformation.getScale());
        resendEntityDataForAll();
    }

    public void setScale(float scale) {
        setScale(new Vector3f(scale, scale, scale));
    }

    public void setScale(Vector3f scale) {
        Transformation transformation = getTransformation();
        Transformation newTransformation = new Transformation(transformation.getTranslation(), transformation.getLeftRotation(), scale, transformation.getRightRotation());
        setTransformation(newTransformation);
    }

    public Vector3f getScale() {
        return getTransformation().getScale();
    }

    public Vector3f getTranslation() {
        return getTransformation().getTranslation();
    }

    public void setTranslation(Vector3f translation) {
        Transformation transformation = getTransformation();
        Transformation newTransformation = new Transformation(translation, transformation.getLeftRotation(), transformation.getScale(), transformation.getRightRotation());
        setTransformation(newTransformation);
    }

    public Quaternionf getLeftRotation() {
        return getTransformation().getLeftRotation();
    }

    public void setLeftRotation(Quaternionf rotation) {
        Transformation transformation = getTransformation();
        Transformation newTransformation = new Transformation(transformation.getTranslation(), rotation, transformation.getScale(), transformation.getRightRotation());
        setTransformation(newTransformation);
    }

    public Quaternionf getRightRotation() {
        return getTransformation().getRightRotation();
    }

    public void setRightRotation(Quaternionf rotation) {
        Transformation transformation = getTransformation();
        Transformation newTransformation = new Transformation(transformation.getTranslation(), transformation.getLeftRotation(), transformation.getScale(), rotation);
        setTransformation(newTransformation);
    }

    protected void rotate(Quaternionf rotation) {
        if (rotation == null) return;
        setLeftRotation(rotation);
    }

    protected static Quaternionf eulerToQuaternion(double originalX, double originalY, double originalZ) {
        double yaw = Math.toRadians(originalZ);
        double pitch = Math.toRadians(originalY);
        double roll = Math.toRadians(originalX);

        double cy = Math.cos(yaw * 0.5);
        double sy = Math.sin(yaw * 0.5);
        double cp = Math.cos(pitch * 0.5);
        double sp = Math.sin(pitch * 0.5);
        double cr = Math.cos(roll * 0.5);
        double sr = Math.sin(roll * 0.5);

        double w = cr * cp * cy + sr * sp * sy;
        double x = sr * cp * cy - cr * sp * sy;
        double y = cr * sp * cy + sr * cp * sy;
        double z = cr * cp * sy - sr * sp * cy;

        return new Quaternionf(x, y, z, w);
    }
}
