package de.erethon.daedalus.customentity;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface ModeledEntityRightClickCallback {
    void onRightClick(Player player, ModeledEntity entity);
}
