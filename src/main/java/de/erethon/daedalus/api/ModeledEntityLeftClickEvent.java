package de.erethon.daedalus.api;

import de.erethon.daedalus.customentity.ModeledEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ModeledEntityLeftClickEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private final ModeledEntity entity;
    private final Player player;
    private boolean cancelled = false;

    public ModeledEntityLeftClickEvent(Player player, ModeledEntity entity) {
        this.entity = entity;
        this.player = player;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public ModeledEntity getEntity() {
        return entity;
    }

    public Player getPlayer() {
        return player;
    }
}