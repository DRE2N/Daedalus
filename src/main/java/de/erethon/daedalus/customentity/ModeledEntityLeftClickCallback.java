package de.erethon.daedalus.customentity;

import org.bukkit.entity.Player;

// Functional interfaces for callbacks
@FunctionalInterface
public interface ModeledEntityLeftClickCallback {
    void onLeftClick(Player player, ModeledEntity entity);
}
