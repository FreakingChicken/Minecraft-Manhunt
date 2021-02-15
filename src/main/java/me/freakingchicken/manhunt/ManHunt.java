package me.freakingchicken.manhunt;

import lombok.Getter;
import lombok.Setter;
import me.freakingchicken.manhunt.command.ManHuntCommand;
import me.freakingchicken.manhunt.command.ReloadCommand;
import me.freakingchicken.manhunt.event.PlayerEvents;
import me.freakingchicken.manhunt.util.Config;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class ManHunt extends JavaPlugin {

    @Getter private static ManHunt manHunt;
    @Getter @Setter private static Game currentGame;

    @Override
    public void onEnable() {
        // Plugin startup logic
        manHunt = this;

        Bukkit.getServer().getPluginManager().registerEvents(new PlayerEvents(), this);

        Config.setup();

        Config.getCustomFile().addDefault("compass-always-track", true);

        Config.getCustomFile().addDefault("nether-end-tracking", true);

        Config.getCustomFile().options().copyDefaults(true);

        Config.save();

        ManHuntCommand.command().register();

        ReloadCommand.command().register();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
