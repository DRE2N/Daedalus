package com.magmaguy.freeminecraftmodels.config;

import com.magmaguy.freeminecraftmodels.Daedalus;
import com.magmaguy.freeminecraftmodels.MetadataHandler;
import com.magmaguy.freeminecraftmodels.utils.ZipFile;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class OutputFolder {
    private OutputFolder() {
    }

    public static void initializeConfig() {
        String path = MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath();
        String baseDirectory = path + File.separatorChar + "output";
        File mainFolder = new File(baseDirectory);
        try {
            if (mainFolder.exists()) FileUtils.deleteDirectory(mainFolder);
        } catch (Exception e) {
            Daedalus.log("Failed to delete folder " + mainFolder.getAbsolutePath());
        }
        mainFolder.mkdir();
        generateDirectory(baseDirectory + File.separatorChar + "Daedalus" + File.separatorChar + "assets" + File.separatorChar + "erethon" + File.separatorChar + "textures");
        generateDirectory(baseDirectory + File.separatorChar + "Daedalus" + File.separatorChar + "assets" + File.separatorChar + "erethon" + File.separatorChar + "models");
        generateDirectory(baseDirectory + File.separatorChar + "Daedalus" + File.separatorChar + "assets" + File.separatorChar + "minecraft" + File.separatorChar + "atlases");
        generateFileFromResources("pack.mcmeta", baseDirectory + File.separatorChar + "Daedalus" + File.separatorChar + "pack.mcmeta");
        generateFileFromResources("blocks.json", baseDirectory + File.separatorChar + "Daedalus" + File.separatorChar + "assets" + File.separatorChar + "minecraft" + File.separatorChar + "atlases" + File.separatorChar + "blocks.json");
    }

    public static void zipResourcePack() {
        String outputPath = MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath() + File.separatorChar + "output";
        ZipFile.zip(
                new File(outputPath + File.separatorChar + "Daedalus"),
                outputPath + File.separatorChar + "Daedalus.zip");

        // Copy to additional directory if configured
        if (!DefaultConfig.additionalOutputDirectory.isEmpty()) {
            try {
                File additionalDir = new File(DefaultConfig.additionalOutputDirectory);
                if (!additionalDir.exists()) {
                    additionalDir.mkdirs();
                }
                File sourceFolder = new File(outputPath + File.separatorChar + "Daedalus");
                File destinationFolder = new File(additionalDir, "Daedalus");

                // Delete existing folder if it exists
                if (destinationFolder.exists()) {
                    FileUtils.deleteDirectory(destinationFolder);
                }

                // Copy the entire folder
                FileUtils.copyDirectory(sourceFolder, destinationFolder);
                Daedalus.log("Resource pack folder copied to: " + destinationFolder.getAbsolutePath());
            } catch (Exception e) {
                Daedalus.log("Failed to copy resource pack folder to additional directory: " + DefaultConfig.additionalOutputDirectory);
                e.printStackTrace();
            }
        }
    }

    private static void generateFileFromResources(String filename, String destination) {
        try {
            InputStream inputStream = MetadataHandler.PLUGIN.getResource(filename);
            File newFile = new File(destination);
            newFile.mkdirs();
            if (!newFile.exists()) newFile.createNewFile();
            // Copy the InputStream to the file
            Files.copy(inputStream, newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            Daedalus.log("Failed to generate default resource pack elements");
            e.printStackTrace();
        }
    }

    private static void generateDirectory(String path) {
        File file = new File(path);
        file.mkdirs();
        file.mkdir();
    }
}
