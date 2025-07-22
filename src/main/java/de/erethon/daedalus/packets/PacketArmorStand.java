package de.erethon.daedalus.packets;


import net.minecraft.world.entity.EntityType;
import org.bukkit.Location;

public class PacketArmorStand extends PacketEntity {

    public PacketArmorStand(EntityType entityType, Location location) {
        super(entityType, location);
    }
}
