package de.erethon.daedalus.customentity;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface ModeledEntityHitboxContactCallback {
    void onHitboxContact(Player player, ModeledEntity entity);
}
