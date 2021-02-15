package me.freakingchicken.manhunt;

import lombok.Getter;
import lombok.Setter;
import me.freakingchicken.manhunt.task.Tasks;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.List;

public class Game {

    @Getter private Player speedRunner;

    @Getter private List<Player> hunters;

    @Getter private final World startingWorld;

    @Getter @Setter private GameState gameState;

    public Game(Player speedRunner, List<Player> hunters) {

        this.gameState = GameState.INIT;

        this.speedRunner = speedRunner;

        this.hunters = hunters;

        this.startingWorld = speedRunner.getWorld();

        init();
    }

    public void init() {

        Location spawn = speedRunner.getWorld().getSpawnLocation();

        spawn.setX(spawn.getX()+0.5D);
        spawn.setY(speedRunner.getWorld().getHighestBlockYAt(spawn.getBlockX(), spawn.getBlockZ()) + 1);
        spawn.setZ(spawn.getZ()+0.5D);

        double radius = 2 * Math.PI;

        int midX = spawn.getBlockX();

        int midZ = spawn.getBlockZ();

        int dist = 2;

        speedRunner.teleport(spawn);

        // We have to freeze the speedrunner before we teleport them or else they might be moved by other players getting
        // teleported if collisions are on.

        speedRunner.setMetadata("mhfrozen", new FixedMetadataValue(ManHunt.getManHunt(), "mhfrozen"));

        for (Player player : hunters) {

            // We will do a bit of math to teleport the hunters in a circle around the speedrunner.

            double angle = hunters.indexOf(player) * radius / hunters.size();

            // Players are farther from the speedrunner based on the amount of them.

            int x = (int) (midX + (dist + (hunters.size()/8F)) * Math.cos(angle));
            int z = (int) (midZ + (dist + (hunters.size()/8F)) * Math.sin(angle));

            Location location = new Location(speedRunner.getWorld(), x + 0.5D, spawn.getBlockY(), z + 0.5D);

            location.setY(speedRunner.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ()) + 1);

            location.setDirection(location.clone().subtract(spawn.toVector()).toVector().multiply(-1));

            player.teleport(location);

            player.setMetadata("mhfrozen", new FixedMetadataValue(ManHunt.getManHunt(), "mhfrozen"));

            ItemStack compass = new ItemStack(Material.COMPASS);
            CompassMeta meta = (CompassMeta) compass.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + "Tracker");
            meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
            compass.setItemMeta(meta);

            player.getInventory().setItem(8, compass);
        }

        this.gameState = GameState.WAITING;

        speedRunner.removeMetadata("mhfrozen", ManHunt.getManHunt());

        Tasks.loopTracers();

    }

    public void endGame(boolean result) {
        gameState = GameState.ENDED;

        if (result) {
            if (speedRunner != null) {
                speedRunner.sendTitle(ChatColor.GREEN + speedRunner.getDisplayName(), ChatColor.GREEN + "Has won the ManHunt!", 10, 200, 10);
                speedRunner.playSound(speedRunner.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1F, 1F);
            }
            for (Player player : hunters) {
                if (speedRunner != null) {
                    player.sendTitle(ChatColor.RED + speedRunner.getDisplayName(), ChatColor.RED + "Has won the ManHunt!", 10, 200, 10);
                }
            }
        } else {
            if (speedRunner != null) {
                speedRunner.sendTitle(ChatColor.RED + "The Hunters", ChatColor.RED + "Have won the ManHunt!", 10, 200, 10);
            }
            for (Player player : hunters) {
                player.sendTitle(ChatColor.GREEN + "The Hunters", ChatColor.GREEN + "Have won the ManHunt!", 10, 200, 10);
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1F, 1F);
            }
        }

        Tasks.stopLoopingTracers();

        speedRunner = null;

        hunters.clear();

        ManHunt.setCurrentGame(null);
    }

    public enum GameState {
        INIT,
        WAITING,
        STARTED,
        ENDED
    }
}
