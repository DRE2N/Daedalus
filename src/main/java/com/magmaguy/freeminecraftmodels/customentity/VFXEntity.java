package com.magmaguy.freeminecraftmodels.customentity;

import com.magmaguy.freeminecraftmodels.Daedalus;
import com.magmaguy.freeminecraftmodels.dataconverter.FileModelConverter;
import com.magmaguy.freeminecraftmodels.utils.DataMappings;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class VFXEntity extends ModeledEntity {
    private static final HashMap<UUID, VFXEntity> vfxEntities = new HashMap<>();
    private LivingEntity mountedEntity = null;
    private boolean isMounted = false;

    protected VFXEntity(String entityID, Location targetLocation) {
        super(entityID, targetLocation);
        // No callbacks - skill entities are purely visual and cannot be interacted with
    }


    @Nullable
    public static VFXEntity create(String entityID, Location targetLocation) {
        FileModelConverter fileModelConverter = FileModelConverter.getConvertedFileModels().get(entityID);
        if (fileModelConverter == null) return null;

        VFXEntity VFXEntity = new VFXEntity(entityID, targetLocation);
        VFXEntity.spawn();
        vfxEntities.put(UUID.randomUUID(), VFXEntity);
        return VFXEntity;
    }

    @Nullable
    public static VFXEntity createMounted(String entityID, LivingEntity mountedEntity) {
        FileModelConverter fileModelConverter = FileModelConverter.getConvertedFileModels().get(entityID);
        if (fileModelConverter == null) return null;

        VFXEntity VFXEntity = new VFXEntity(entityID, mountedEntity.getLocation());
        VFXEntity.mountedEntity = mountedEntity;
        VFXEntity.isMounted = true;
        VFXEntity.spawn();
        VFXEntity.getSkeleton().setMounted(true); // Tell skeleton bones are mounted (use relative positions)
        VFXEntity.mountAllBones();
        vfxEntities.put(UUID.randomUUID(), VFXEntity);
        return VFXEntity;
    }

    public void mountOn(LivingEntity entity) {
        this.mountedEntity = entity;
        this.isMounted = true;
        getSkeleton().setMounted(true); // Tell skeleton bones are mounted (use relative positions)
        mountAllBones();
    }

    private void mountAllBones() {
        if (mountedEntity == null || !isMounted) return;

        int vehicleId = ((CraftEntity) mountedEntity).getHandle().getId();

        List<Integer> passengerIds = new ArrayList<>();

        for (org.bukkit.entity.Entity existingPassenger : mountedEntity.getPassengers()) {
            passengerIds.add(((CraftEntity) existingPassenger).getHandle().getId());
        }

        getSkeleton().getBones().forEach(bone -> {
            if (bone.getBoneTransforms().getPacketDisplayEntity() != null) {
                int entityId = bone.getBoneTransforms().getPacketDisplayEntity().getEntityId();
                passengerIds.add(entityId);
            }
            if (bone.getBoneTransforms().getPacketTextDisplayEntity() != null) {
                int entityId = bone.getBoneTransforms().getPacketTextDisplayEntity().getEntityId();
                passengerIds.add(entityId);
            }
            if (bone.getBoneTransforms().getPacketArmorStandEntity() != null) {
                int entityId = bone.getBoneTransforms().getPacketArmorStandEntity().getEntityId();
                passengerIds.add(entityId);
            }
        });

        List<Player> nearbyPlayers = new ArrayList<>();
        for (Player player : mountedEntity.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(mountedEntity.getLocation()) < 4096) { // 64 block radius
                getSkeleton().getBones().forEach(bone -> bone.displayTo(player));
                nearbyPlayers.add(player);
            }
        }
        org.bukkit.Bukkit.getScheduler().runTaskLater(Daedalus.getPlugin(), () -> {
            sendSetPassengersPacket(vehicleId, passengerIds, nearbyPlayers);
        }, 5L);
    }

    private void sendSetPassengersPacket(int vehicleId, List<Integer> passengerIds, List<Player> players) {
        try {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            buffer.writeVarInt(vehicleId);
            int[] passengerArray = passengerIds.stream().mapToInt(Integer::intValue).toArray();
            buffer.writeVarIntArray(passengerArray);

            buffer.resetReaderIndex();

            Packet<?> packet = (Packet<?>) DataMappings.SET_PASSENGERS_PACKET.newInstance(buffer);

            for (Player player : players) {
                ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
                serverPlayer.connection.send(packet);
            }

            buffer.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unmount() {
        if (mountedEntity == null || !isMounted) return;

        int vehicleId = ((CraftEntity) mountedEntity).getHandle().getId();

        List<Integer> originalPassengerIds = new ArrayList<>();
        for (org.bukkit.entity.Entity existingPassenger : mountedEntity.getPassengers()) {
            originalPassengerIds.add(((CraftEntity) existingPassenger).getHandle().getId());
        }

        List<Player> nearbyPlayers = new ArrayList<>();
        for (Player player : mountedEntity.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(mountedEntity.getLocation()) < 4096) {
                nearbyPlayers.add(player);
            }
        }

        sendSetPassengersPacket(vehicleId, originalPassengerIds, nearbyPlayers);

        this.mountedEntity = null;
        this.isMounted = false;
        getSkeleton().setMounted(false); // Tell skeleton bones are no longer mounted (use absolute positions)
    }

    @Override
    public void tick() {
        if (isMounted && mountedEntity != null) {
            if (!mountedEntity.isValid() || mountedEntity.isDead()) {
                remove();
                return;
            }
        }

        super.tick();
    }

    @Override
    public void remove() {
        if (isMounted) {
            unmount();
        }

        super.remove();
        vfxEntities.values().removeIf(entity -> entity == this);
    }

    @Nullable
    public LivingEntity getMountedEntity() {
        return mountedEntity;
    }

    public boolean isMounted() {
        return isMounted && mountedEntity != null;
    }

    @Override
    public Location getLocation() {
        // When mounted, return the current vehicle location, not the stale spawn location
        if (isMounted && mountedEntity != null) {
            return mountedEntity.getLocation();
        }
        return super.getLocation();
    }

    /**
     * Gets all active skill entities.
     *
     * @return HashMap of all skill entities
     */
    public static HashMap<UUID, VFXEntity> getVfxEntities() {
        return vfxEntities;
    }
}
