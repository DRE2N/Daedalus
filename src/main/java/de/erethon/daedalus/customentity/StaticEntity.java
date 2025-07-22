package de.erethon.daedalus.customentity;

import de.erethon.daedalus.customentity.core.ModeledEntityInterface;
import de.erethon.daedalus.dataconverter.FileModelConverter;
import org.bukkit.Location;
import org.checkerframework.checker.nullness.qual.Nullable;

public class StaticEntity extends ModeledEntity implements ModeledEntityInterface {
    protected StaticEntity(String entityID, Location targetLocation) {
        super(entityID, targetLocation);
        setLeftClickCallback((player, entity) -> entity.damage(player, 1));
    }

    @Nullable
    public static StaticEntity create(String entityID, Location targetLocation) {
        FileModelConverter fileModelConverter = FileModelConverter.getConvertedFileModels().get(entityID);
        if (fileModelConverter == null) return null;
        StaticEntity staticEntity = new StaticEntity(entityID, targetLocation);
        staticEntity.spawn();
        return staticEntity;
    }
}
