package de.erethon.daedalus.config;

import de.erethon.daedalus.MetadataHandler;
import de.erethon.daedalus.utils.ZipFile;
import de.erethon.bedrock.chat.MessageUtil;
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
            MessageUtil.log("Failed to delete folder " + mainFolder.getAbsolutePath());
        }
        mainFolder.mkdir();
        generateDirectory(baseDirectory + File.separatorChar + "Daedalus" + File.separatorChar + "assets" + File.separatorChar + "erethon" + File.separatorChar + "textures");
        generateDirectory(baseDirectory + File.separatorChar + "Daedalus" + File.separatorChar + "assets" + File.separatorChar + "erethon" + File.separatorChar + "models");
        generateDirectory(baseDirectory + File.separatorChar + "Daedalus" + File.separatorChar + "assets" + File.separatorChar + "minecraft" + File.separatorChar + "atlases");
        generateFileFromResources("pack.mcmeta", baseDirectory + File.separatorChar + "Daedalus" + File.separatorChar + "pack.mcmeta");
        generateFileFromResources("blocks.json", baseDirectory + File.separatorChar + "Daedalus" + File.separatorChar + "assets" + File.separatorChar + "minecraft" + File.separatorChar + "atlases" + File.separatorChar + "blocks.json");
    }

    public static void zipResourcePack() {
        ZipFile.zip(
                new File(MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath() + File.separatorChar + "output" + File.separatorChar + "Daedalus"),
                MetadataHandler.PLUGIN.getDataFolder().getAbsolutePath() + File.separatorChar + "output" + File.separatorChar + "Daedalus.zip");
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
            MessageUtil.log("Failed to generate default resource pack elements");
            e.printStackTrace();
        }
    }

    private static void generateDirectory(String path) {
        File file = new File(path);
        file.mkdirs();
        file.mkdir();
    }
}
