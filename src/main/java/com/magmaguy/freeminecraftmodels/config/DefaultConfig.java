package com.magmaguy.freeminecraftmodels.config;

import com.magmaguy.freeminecraftmodels.Daedalus;
import com.magmaguy.freeminecraftmodels.MetadataHandler;
import org.bukkit.configuration.file.FileConfiguration;

public class DefaultConfig {

    public static int maxModelViewDistance = 512;
    public static int maxInteractionAndAttackDistanceForLivingEntities = 64;
    public static int maxInteractionAndAttackDistanceForProps = 64;
    public static boolean sendCustomModelsToBedrockClients = false;
    public static String additionalOutputDirectory = "";

    public static void initializeConfig() {
        MetadataHandler.PLUGIN.saveDefaultConfig();
        FileConfiguration config = MetadataHandler.PLUGIN.getConfig();

        maxModelViewDistance = config.getInt("maxModelViewDistance", 512);
        maxInteractionAndAttackDistanceForLivingEntities = config.getInt("maxInteractionAndAttackDistanceForLivingEntities", 64);
        maxInteractionAndAttackDistanceForProps = config.getInt("maxInteractionAndAttackDistanceForProps", 64);
        sendCustomModelsToBedrockClients = config.getBoolean("sendCustomModelsToBedrockClients", false);
        additionalOutputDirectory = config.getString("additionalOutputDirectory", "");

        Daedalus.log("Configuration loaded successfully.");
        if (!additionalOutputDirectory.isEmpty()) {
            Daedalus.log("Additional output directory: " + additionalOutputDirectory);
        }
    }
}
