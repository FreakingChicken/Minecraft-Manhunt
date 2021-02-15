package me.freakingchicken.manhunt.util;

import lombok.Getter;
import me.freakingchicken.manhunt.ManHunt;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class Config {

    @Getter private static File file;
    @Getter private static FileConfiguration customFile;

    public static void setup() {
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("ManHunt").getDataFolder(), "config.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        customFile = YamlConfiguration.loadConfiguration(file);
    }

    public static void save() {
        try {
            customFile.save(file);
        } catch (IOException e) {
            System.out.println("There was an error while trying to save the file.");
            e.printStackTrace();
        }
    }

    public static void reload() {
        customFile = YamlConfiguration.loadConfiguration(file);
    }
}
