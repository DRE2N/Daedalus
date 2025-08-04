package de.erethon.daedalus;

import de.erethon.daedalus.config.ModelsFolder;
import de.erethon.daedalus.config.OutputFolder;
import de.erethon.daedalus.customentity.DynamicEntity;
import de.erethon.daedalus.customentity.ModeledEntitiesClock;
import de.erethon.daedalus.customentity.ModeledEntity;
import de.erethon.daedalus.customentity.ModeledEntityEvents;
import de.erethon.daedalus.customentity.PropEntity;
import de.erethon.daedalus.customentity.core.OBBHitDetection;
import de.erethon.daedalus.customentity.core.components.InteractionComponent;
import de.erethon.daedalus.dataconverter.FileModelConverter;
import de.erethon.daedalus.listeners.EntityTeleportEvent;
import de.erethon.daedalus.utils.DataMappings;
import de.erethon.bedrock.chat.MessageUtil;
import de.erethon.bedrock.plugin.EPlugin;
import de.erethon.bedrock.plugin.EPluginSettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.Random;

public final class Daedalus extends EPlugin implements Listener, CommandExecutor {

    public static Daedalus plugin;

    public Daedalus() {
        settings = EPluginSettings.builder().build();
    }

    @Override
    public void onEnable() {
        super.onEnable();
        plugin = this;
        MetadataHandler.PLUGIN = this;
        //Initialize plugin configuration files
        OutputFolder.initializeConfig();
        ModelsFolder.initializeConfig();
        Bukkit.getPluginManager().registerEvents(new ModeledEntityEvents(), this);
        Bukkit.getPluginManager().registerEvents(new OBBHitDetection(), this);
        Bukkit.getPluginManager().registerEvents(new PropEntity.PropEntityEvents(), this);
        Bukkit.getPluginManager().registerEvents(new EntityTeleportEvent(), this);
        Bukkit.getPluginManager().registerEvents(new DynamicEntity.ModeledEntityEvents(), this);
        Bukkit.getPluginManager().registerEvents(new InteractionComponent.InteractionComponentEvents(), this);
        Bukkit.getPluginManager().registerEvents(this, this);
        OutputFolder.zipResourcePack();

        ModeledEntitiesClock.start();

        PropEntity.onStartup();
        OBBHitDetection.startProjectileDetection();
        getCommand("daedalus").setExecutor(this);
        // Generate Data Mappings, needs a Level to work with
        CraftWorld craftWorld = (CraftWorld) Bukkit.getWorlds().get(0);
        DataMappings.generateMappings(craftWorld.getHandle());
    }

    @Override
    public void onDisable() {
        FileModelConverter.shutdown();
        ModeledEntity.shutdown();
        ModeledEntitiesClock.shutdown();
        OBBHitDetection.shutdown();
        Bukkit.getServer().getScheduler().cancelTasks(MetadataHandler.PLUGIN);
        HandlerList.unregisterAll(MetadataHandler.PLUGIN);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (args[0].equalsIgnoreCase("spawn")) {
            int amount = args.length > 2 ? Integer.parseInt(args[2]) : 1;
            Random rand = new Random();
            for (int i = 0; i < amount; i++) {
                Location spawnLocation = player.getTargetBlockExact(32).getLocation().add(0.5, 1, 0.5);
                spawnLocation.setPitch(0);
                spawnLocation.setYaw(player.getLocation().getYaw());
                if (amount > 1) {
                    spawnLocation.setX(spawnLocation.getX() + rand.nextDouble(-i, i + 1));
                    spawnLocation.setZ(spawnLocation.getZ() + rand.nextDouble(-i, i + 1));
                }
                String modelId = args.length > 1 ? args[1] : "default_model";
                String scaleInput = args.length > 3 ? args[3] : "1.0";
                double scale = Double.parseDouble(scaleInput);
                try {
                ModeledEntity modeledEntity = new ModeledEntity(modelId, spawnLocation);
                    modeledEntity.spawn();
                    modeledEntity.setScaleModifier(scale);
                    if (modelId.contains("catapult")) {
                        modeledEntity.playAnimation("action_catapult", false, true);
                    }
                } catch (Exception e) {
                    MessageUtil.sendMessage(player, "Error spawning model: " + e.getMessage());
                    e.printStackTrace();
                    return true;
                }
            }
            MessageUtil.sendMessage(player, "You have spawned a new model.");
            return true;
        }
        if (args[0].equalsIgnoreCase("status")) {
            MessageUtil.sendMessage(player, "Daedalus status");
            MessageUtil.sendMessage(player, " ");
            MessageUtil.sendMessage(player, "Loaded Models: " + FileModelConverter.getConvertedFileModels().size());
            MessageUtil.sendMessage(player, "Active Models: " + ModeledEntity.getLoadedModeledEntities().size());
            MessageUtil.sendMessage(player, "Tick threads: " + ModeledEntitiesClock.THREAD_COUNT);
            MessageUtil.sendMessage(player, "Pending models: " + ModeledEntitiesClock.ticker.getPendingCount());
            MessageUtil.sendMessage(player, "Average models per thread: " + ModeledEntitiesClock.ticker.getAveragePartitionSize());
            return true;
        }
        if (args[0].equalsIgnoreCase("list")) {
            MessageUtil.sendMessage(player, "Daedalus loaded models:");
            if (FileModelConverter.getConvertedFileModels().isEmpty()) {
                MessageUtil.sendMessage(player, "No models loaded.");
            }
            for (String modelId : FileModelConverter.getConvertedFileModels().keySet()) {
                FileModelConverter fileModelConverter = FileModelConverter.getConvertedFileModels().get(modelId);
                if (fileModelConverter != null) {
                    MessageUtil.sendMessage(player, " - " + modelId + " (" + fileModelConverter.getModelName() + ")");
                } else {
                    MessageUtil.sendMessage(player, " - " + modelId + " (unknown file)");
                }
            }
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            OutputFolder.initializeConfig();
            ModelsFolder.initializeConfig();
            OutputFolder.zipResourcePack();
            MessageUtil.sendMessage(player, "Configuration files reloaded.");
            return true;
        }
        return false;
    }

    public static  Daedalus getPlugin() {
        return plugin;
    }

    public static void log(String message) {
        plugin.getLogger().info(message);
    }

}
