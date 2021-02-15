package me.freakingchicken.manhunt.task;

import me.freakingchicken.manhunt.ManHunt;
import me.freakingchicken.manhunt.event.PlayerEvents;
import me.freakingchicken.manhunt.util.Config;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class Tasks {

    public static int loopTracersID;

    public static void loopTracers() {

        loopTracersID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(ManHunt.getManHunt(), () -> {
            if (Config.getCustomFile().getBoolean("nether-end-tracking")) {
                for (Map.Entry<Player, Location> entry : PlayerEvents.getLastLocation().entrySet()) {
                    if (entry.getKey().getWorld().getEnvironment() != World.Environment.NORMAL) {
                        PlayerEvents.drawTracer(entry.getKey().getLocation(), entry.getValue(), entry.getKey());
                    }
                }
            }

            // If the config value gets changed we will just stop the loop.

            if (Config.getCustomFile().getBoolean("compass-always-track")) {
                stopLoopingTracers();
            }
        }, 20L, 1L);

    }

    public static void stopLoopingTracers() {
        if (loopTracersID != 0) {
            Bukkit.getServer().getScheduler().cancelTask(loopTracersID);
            loopTracersID = 0;
        }
    }

}
