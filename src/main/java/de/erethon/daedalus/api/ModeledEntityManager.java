package de.erethon.daedalus.api;

import de.erethon.daedalus.customentity.DynamicEntity;
import de.erethon.daedalus.customentity.ModeledEntity;
import de.erethon.daedalus.dataconverter.FileModelConverter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class ModeledEntityManager {
    private ModeledEntityManager() {
    }

    /**
     * Returns combined lists of all ModeledEntities (dynamic and static).
     *
     * @return Returns all ModeledEntities currently instanced by the plugin
     */
    public static HashSet<ModeledEntity> getAllEntities() {
        return (HashSet<ModeledEntity>) ModeledEntity.getLoadedModeledEntities().clone();
    }

    /**
     * Returns whether a model exists by a given name
     *
     * @param modelName Name to check
     * @return Whether the model exists
     */
    public static boolean modelExists(String modelName) {
        return FileModelConverter.getConvertedFileModels().containsKey(modelName);
    }

    /**
     * Returns the list of dynamic entities currently instanced by the plugin
     *
     * @return The list of currently instanced dynamic entities
     */
    public static HashMap<UUID, DynamicEntity> getDynamicEntities() {
        return DynamicEntity.getDynamicEntities();
    }

}
