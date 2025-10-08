package com.magmaguy.freeminecraftmodels.utils;

import com.mojang.authlib.GameProfile;
import com.magmaguy.freeminecraftmodels.Daedalus;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataMappings {

    public static Map<EntityType<?>, Entity> ENTITY_DATA_MAPPINGS = new HashMap<>();
    public static Map<Class<? extends Entity>, Map<String, EntityDataAccessor<?>>> DATA_ACCESSOR_MAPPINGS = new HashMap<>();
    public static Constructor<?> SET_PASSENGERS_PACKET;

    private static Field ITEMS_BY_ID_FIELD;

    static {
        try {
            ITEMS_BY_ID_FIELD = SynchedEntityData.class.getDeclaredField("itemsById");
            ITEMS_BY_ID_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Failed to initialize DataMappings: Could not find 'itemsById' field in SynchedEntityData.", e);
        }
    }

    public static void generateMappings(Level level) {
        BuiltInRegistries.ENTITY_TYPE.forEach(type -> {
            Entity entity = type.create(level, EntitySpawnReason.LOAD);
            if (entity == null) {
                return;
            }
            ENTITY_DATA_MAPPINGS.put(type, entity);
            Map<String, EntityDataAccessor<?>> dataAccessors = new HashMap<>();
            for (Field declaredField : entity.getClass().getDeclaredFields()) {
                if (!declaredField.getName().startsWith("DATA_")) {
                    continue;
                }
                try {
                    declaredField.setAccessible(true);
                    if (declaredField.getType() != EntityDataAccessor.class) {
                        continue;
                    }
                    EntityDataAccessor<?> accessor = (EntityDataAccessor<?>) declaredField.get(entity);
                    dataAccessors.put(declaredField.getName(), accessor);
                    if (accessor != null) {
                        Daedalus.log("[DataMappings] Found data accessor " + declaredField.getName() + " for " + type.getDescriptionId());
                    } else {
                        Daedalus.log("[DataMappings] Found null data accessor " + declaredField.getName() + " for " + type.getDescriptionId());
                    }
                }
                catch (IllegalAccessException e) {
                    Daedalus.log("[DataMappings] Failed to access data accessor field " + declaredField.getName() + " for entity " + type.getDescriptionId());
                    e.printStackTrace();
                }
            }
            DATA_ACCESSOR_MAPPINGS.put(entity.getClass(), dataAccessors);
        });
        // Special case for players
        try {
            Player player = new ServerPlayer(MinecraftServer.getServer(), (ServerLevel) level, new GameProfile(UUID.randomUUID(), "MappingsGenerator"), ClientInformation.createDefault());
            ENTITY_DATA_MAPPINGS.put(EntityType.PLAYER, player);
            Map<String, EntityDataAccessor<?>> playerDataAccessors = new HashMap<>();
            // its Player class, not the ServerPlayer class
            for (Field declaredField : player.getClass().getSuperclass().getDeclaredFields()) {
                if (!declaredField.getName().startsWith("DATA_")) {
                    continue;
                }
                try {
                    declaredField.setAccessible(true);
                    if (declaredField.getType() != EntityDataAccessor.class) {
                        continue;
                    }
                    EntityDataAccessor<?> accessor = (EntityDataAccessor<?>) declaredField.get(player);
                    playerDataAccessors.put(declaredField.getName(), accessor);
                    if (accessor != null) {
                        Daedalus.log("[DataMappings] Found data accessor " + declaredField.getName() + " for Player");
                    } else {
                        Daedalus.log("[DataMappings] Found null data accessor " + declaredField.getName() + " for Player");
                    }
                } catch (IllegalAccessException e) {
                    Daedalus.log("[DataMappings] Failed to access data accessor field " + declaredField.getName() + " for entity Player");
                    e.printStackTrace();
                }
            }
            DATA_ACCESSOR_MAPPINGS.put(player.getClass(), playerDataAccessors);
            Daedalus.log("[Models] Found data accessor for Player base: " + playerDataAccessors.size());
            // ServerPlayer -> Player -> Avatar -> LivingEntity
            Map<String, EntityDataAccessor<?>> avatarDataAccessors = new HashMap<>();
            for (Field declaredField : player.getClass().getSuperclass().getSuperclass().getDeclaredFields()) {
                if (!declaredField.getName().startsWith("DATA_")) {
                    continue;
                }
                try {
                    declaredField.setAccessible(true);
                    if (declaredField.getType() != EntityDataAccessor.class) {
                        continue;
                    }
                    EntityDataAccessor<?> accessor = (EntityDataAccessor<?>) declaredField.get(player);
                    avatarDataAccessors.put(declaredField.getName(), accessor);
                    if (accessor != null) {
                        Daedalus.log("[DataMappings] Found data accessor " + declaredField.getName() + " for Avatar");
                    } else {
                        Daedalus.log("[DataMappings] Found null data accessor " + declaredField.getName() + " for Avatar");
                    }
                } catch (IllegalAccessException e) {
                    Daedalus.log("[DataMappings] Failed to access data accessor field " + declaredField.getName() + " for entity Avatar");
                    e.printStackTrace();
                }
            }
            DATA_ACCESSOR_MAPPINGS.put(Avatar.class, avatarDataAccessors);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // Add parent class data accessors
        Map<String, EntityDataAccessor<?>> entityDataAccessors = new HashMap<>();
        for (Field declaredField : Entity.class.getDeclaredFields()) {
            if (!declaredField.getName().startsWith("DATA_") || declaredField.getType() != EntityDataAccessor.class) {
                continue;
            }
            try {
                declaredField.setAccessible(true);
                EntityDataAccessor<?> accessor = (EntityDataAccessor<?>) declaredField.get(Entity.class);
                entityDataAccessors.put(declaredField.getName(), accessor);
                if (accessor != null) {
                    Daedalus.log("[DataMappings] Found data accessor " + declaredField.getName() + " for Entity base");
                } else {
                    Daedalus.log("[DataMappings] Found null data accessor " + declaredField.getName() + " for Entity base");
                }
            }
            catch (IllegalAccessException e) {
                Daedalus.log("[DataMappings] Failed to access data accessor field " + declaredField.getName() + " for Entity base");
                e.printStackTrace();
            }
        }
        DATA_ACCESSOR_MAPPINGS.put(Entity.class, entityDataAccessors);
        // Living too, who knows
        Map<String, EntityDataAccessor<?>> livingDataAccessors = new HashMap<>();
        for (Field declaredField : LivingEntity.class.getDeclaredFields()) {
            if (!declaredField.getName().startsWith("DATA_")) {
                continue;
            }
            try {
                declaredField.setAccessible(true);
                EntityDataAccessor<?> accessor = (EntityDataAccessor<?>) declaredField.get(LivingEntity.class);
                livingDataAccessors.put(declaredField.getName(), accessor);
                if (accessor != null) {
                    Daedalus.log("[DataMappings] Found data accessor " + declaredField.getName() + " for LivingEntity base");
                } else {
                    Daedalus.log("[DataMappings] Found null data accessor " + declaredField.getName() + " for LivingEntity base");
                }
            }
            catch (IllegalAccessException e) {
                Daedalus.log("[DataMappings] Failed to access data accessor field " + declaredField.getName() + " for LivingEntity base");
                e.printStackTrace();
            }
        }
        DATA_ACCESSOR_MAPPINGS.put(LivingEntity.class, livingDataAccessors);
        // Special case for the display entity base class
        Map<String, EntityDataAccessor<?>> displayDataAccessors = new HashMap<>();
        for (Field declaredField : Display.class.getDeclaredFields()) {
            if (!declaredField.getName().startsWith("DATA_")) {
                continue;
            }
            try {
                declaredField.setAccessible(true);
                EntityDataAccessor<?> accessor = (EntityDataAccessor<?>) declaredField.get(Display.class);
                displayDataAccessors.put(declaredField.getName(), accessor);
                if (accessor != null) {
                    Daedalus.log("[DataMappings] Found data accessor " + declaredField.getName() + " for Display base");
                } else {
                    Daedalus.log("[DataMappings] Found null data accessor " + declaredField.getName() + " for Display base");
                }
            }
            catch (IllegalAccessException e) {
                Daedalus.log("[DataMappings] Failed to access data accessor field " + declaredField.getName() + " for Display base");
                e.printStackTrace();
            }
        }
        DATA_ACCESSOR_MAPPINGS.put(Display.class, displayDataAccessors);
        // Special case for the set passengers packet, as the only public constructor wants an entity
        try {
            Class<?> clazz = Class.forName("net.minecraft.network.protocol.game.ClientboundSetPassengersPacket");
            SET_PASSENGERS_PACKET = clazz.getDeclaredConstructor(FriendlyByteBuf.class);
            SET_PASSENGERS_PACKET.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> void defineFromTemplate(SynchedEntityData.Builder builder, SynchedEntityData.DataItem<T> templateItem) {
        builder.define(templateItem.getAccessor(), templateItem.getValue());
    }

    public static SynchedEntityData getSynchedEntityData(EntityType<?> type) {
        Entity templateEntity = ENTITY_DATA_MAPPINGS.get(type);
        if (templateEntity == null) {
            throw new IllegalStateException("[DataMappings] DataMappings not initialized for " + type.getDescriptionId());
        }
        return getSynchedEntityData(templateEntity, templateEntity);
    }

    /**
     * Creates a new SynchedEntityData object for a specific entity instance
     * by copying the initial default values from a pre-generated template entity.
     *
     * @param targetEntity The entity instance for which to create the data.
     * @return A new SynchedEntityData object populated with default values.
     */
    public static SynchedEntityData getSynchedEntityData(Entity targetEntity) {
        Entity templateEntity = ENTITY_DATA_MAPPINGS.get(targetEntity.getType());
        if (templateEntity == null) {
            throw new IllegalStateException("[DataMappings] DataMappings not initialized for " + targetEntity.getType().getDescriptionId());
        }
        return getSynchedEntityData(templateEntity, targetEntity);
    }

    /**
     * Core logic to create SynchedEntityData for a target by copying from a template.
     *
     * @param templateEntity The pre-generated entity with the correct default data.
     * @param targetEntity   The entity that will "own" the new SynchedEntityData object.
     * @return A new SynchedEntityData object.
     */
    private static SynchedEntityData getSynchedEntityData(Entity templateEntity, Entity targetEntity) {
        SynchedEntityData templateData = templateEntity.getEntityData();

        SynchedEntityData.Builder dataBuilder = new SynchedEntityData.Builder(targetEntity);

        try {
            SynchedEntityData.DataItem<?>[] templateItems = (SynchedEntityData.DataItem<?>[]) ITEMS_BY_ID_FIELD.get(templateData);

            for (SynchedEntityData.DataItem<?> templateItem : templateItems) {
                if (templateItem != null) {
                    defineFromTemplate(dataBuilder, templateItem);
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to copy SynchedEntityData via reflection", e);
        }

        return dataBuilder.build();
    }

    public static EntityDataAccessor getAccessor(Class<? extends Entity> clazz, String dataAccessor) {
        if (!DATA_ACCESSOR_MAPPINGS.containsKey(clazz)) {
            Daedalus.log("[DataMappings] No data accessors found for class " + clazz.getName());
            return null;
        }
        if (!DATA_ACCESSOR_MAPPINGS.get(clazz).containsKey(dataAccessor)) {
            Daedalus.log("[DataMappings] No data accessor found for " + dataAccessor + " in class " + clazz.getName());
            // Print all we have for now
            Daedalus.log("[DataMappings] Data accessors for " + clazz.getName() + ":");
            DATA_ACCESSOR_MAPPINGS.get(clazz).forEach((key, value) -> Daedalus.log("- " + key));
            return null;
        }
        return DATA_ACCESSOR_MAPPINGS.get(clazz).get(dataAccessor);
    }


}
