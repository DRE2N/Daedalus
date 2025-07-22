package de.erethon.daedalus.customentity.core.components;

import de.erethon.daedalus.api.ModeledEntityHitboxContactEvent;
import de.erethon.daedalus.api.ModeledEntityLeftClickEvent;
import de.erethon.daedalus.api.ModeledEntityRightClickEvent;
import de.erethon.daedalus.customentity.ModeledEntity;
import de.erethon.daedalus.customentity.ModeledEntityHitboxContactCallback;
import de.erethon.daedalus.customentity.ModeledEntityLeftClickCallback;
import de.erethon.daedalus.customentity.ModeledEntityRightClickCallback;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * This class handles left click, right click, and hitbox contact events for the entity.
 * Certain types of entities may have default behaviors for these events, which can be overriden by setting custom callbacks.
 */
public class InteractionComponent {
    private final ModeledEntity modeledEntity;
    private ModeledEntityLeftClickCallback leftClickCallback;
    private ModeledEntityRightClickCallback rightClickCallback;
    private ModeledEntityHitboxContactCallback hitboxContactCallback;

    public InteractionComponent(ModeledEntity modeledEntity) {
        this.modeledEntity = modeledEntity;
    }

    public void callLeftClickEvent(Player player) {
        ModeledEntityLeftClickEvent event = new ModeledEntityLeftClickEvent(player, modeledEntity);
        Bukkit.getPluginManager().callEvent(event);
    }

    public void callRightClickEvent(Player player) {
        ModeledEntityRightClickEvent event = new ModeledEntityRightClickEvent(player, modeledEntity);
        Bukkit.getPluginManager().callEvent(event);
    }

    /**
     * Triggers the appropriate hitbox contact event based on entity type
     * This method should be overridden by subclasses to fire their specific event types
     */
    protected void callHitboxContactEvent(Player player) {
        ModeledEntityHitboxContactEvent event = new ModeledEntityHitboxContactEvent(player, modeledEntity);
        Bukkit.getPluginManager().callEvent(event);
    }

    public void handleLeftClickEvent(Player player) {
        if (leftClickCallback == null) return;
        leftClickCallback.onLeftClick(player, modeledEntity);
    }

    public void handleRightClickEvent(Player player) {
        if (rightClickCallback == null) return;
        rightClickCallback.onRightClick(player, modeledEntity);
    }

    public void handleHitboxContactEvent(Player player) {
        if (hitboxContactCallback == null) return;
        hitboxContactCallback.onHitboxContact(player, modeledEntity);

    }

    // Clear all callbacks
    public void clearCallbacks() {
        this.leftClickCallback = null;
        this.rightClickCallback = null;
        this.hitboxContactCallback = null;
    }

    public static class InteractionComponentEvents implements Listener {
        @EventHandler
        public void onLeftClick(ModeledEntityLeftClickEvent event) {
            if (event.isCancelled()) return;
            event.getEntity().getInteractionComponent().handleLeftClickEvent(event.getPlayer());
        }

        @EventHandler
        public void onRightClick(ModeledEntityRightClickEvent event) {
            if (event.isCancelled()) return;
            event.getEntity().getInteractionComponent().handleRightClickEvent(event.getPlayer());
        }

        @EventHandler
        public void onHitboxContact(ModeledEntityHitboxContactEvent event) {
            if (event.isCancelled()) return;
            event.getEntity().getInteractionComponent().handleHitboxContactEvent(event.getPlayer());
        }
    }

    public ModeledEntity getModeledEntity() {
        return modeledEntity;
    }

    public void setLeftClickCallback(ModeledEntityLeftClickCallback leftClickCallback) {
        this.leftClickCallback = leftClickCallback;
    }

    public void setRightClickCallback(ModeledEntityRightClickCallback rightClickCallback) {
        this.rightClickCallback = rightClickCallback;
    }

    public ModeledEntityHitboxContactCallback getHitboxContactCallback() {
        return hitboxContactCallback;
    }

    public void setHitboxContactCallback(ModeledEntityHitboxContactCallback hitboxContactCallback) {
        this.hitboxContactCallback = hitboxContactCallback;
    }
}
