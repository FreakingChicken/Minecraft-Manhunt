package me.freakingchicken.manhunt.event;

import lombok.Getter;
import me.freakingchicken.manhunt.Game;
import me.freakingchicken.manhunt.ManHunt;
import me.freakingchicken.manhunt.util.Config;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerEvents implements Listener {

    @Getter private static Map<Player, Location> lastLocation = new HashMap<>();

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (ManHunt.getCurrentGame() != null) {
            Game game = ManHunt.getCurrentGame();
            if (Config.getCustomFile().getBoolean("compass-always-track")) {
                for (Player player : game.getHunters()) {
                    if (player.getWorld() == game.getSpeedRunner().getWorld()) {
                        player.setCompassTarget(game.getSpeedRunner().getLocation());
                        if (player.getWorld().getEnvironment() != World.Environment.NORMAL && Config.getCustomFile().getBoolean("nether-end-tracking")) {
                            drawTracer(player.getLocation(), game.getSpeedRunner().getLocation(), player);
                        }
                    }
                }
            }

            if (game.getGameState() == Game.GameState.WAITING) {
                if (event.getPlayer() == game.getSpeedRunner()) {
                    Location loc1 = new Location(event.getFrom().getWorld(), event.getFrom().getX(), event.getFrom().getY(), event.getFrom().getZ());
                    Location loc2 = new Location(event.getTo().getWorld(), event.getTo().getX(), event.getTo().getY(), event.getTo().getZ());
                    if (loc1.equals(loc2)) {
                        return;
                    }
                    startGame();
                }
            }

            if (event.getPlayer().hasMetadata("mhfrozen")) {
                float yaw = event.getTo().getYaw();
                float pitch = event.getTo().getPitch();
                Location location = new Location(event.getFrom().getWorld(), event.getFrom().getX(), event.getFrom().getY(), event.getFrom().getZ());
                location.setPitch(pitch);
                location.setYaw(yaw);
                event.getPlayer().teleport(location);
            }
        }
    }

    public static void drawTracer(Location from, Location to, Player player) {
        if (from.getWorld().equals(to.getWorld())) {
            Vector point = from.toVector();
            Vector direction = to.toVector().clone().subtract(point).setY(0).normalize().multiply(3);
            Vector p = point.add(direction);

            Particle.DustOptions particles = new Particle.DustOptions(Color.fromRGB(
                    ThreadLocalRandom.current().nextInt(0, 255+1),
                    ThreadLocalRandom.current().nextInt(0, 255+1),
                    ThreadLocalRandom.current().nextInt(0, 255+1)), 1);

            player.spawnParticle(Particle.REDSTONE, p.getX(), p.getY() + 1, p.getZ(), 1, 0, 0, 0, particles);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (ManHunt.getCurrentGame() != null) {
            if (event.getEntity() == ManHunt.getCurrentGame().getSpeedRunner()) {
                ManHunt.getCurrentGame().endGame(false);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (ManHunt.getCurrentGame() != null && !Config.getCustomFile().getBoolean("compass-always-track")) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
                if (event.getItem() != null && event.getHand() == EquipmentSlot.HAND) {
                    if (event.getItem().getType() == Material.COMPASS && event.getItem().getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Tracker")) {
                        if (player.getWorld() == ManHunt.getCurrentGame().getSpeedRunner().getWorld()) {
                            event.setCancelled(true);
                            player.setCompassTarget(ManHunt.getCurrentGame().getSpeedRunner().getLocation());
                            lastLocation.put(player, ManHunt.getCurrentGame().getSpeedRunner().getLocation());
                            player.sendMessage(ChatColor.GREEN + "Updated tracking location for " + ManHunt.getCurrentGame().getSpeedRunner().getDisplayName());
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (ManHunt.getCurrentGame() != null) {
            if (ManHunt.getCurrentGame().getGameState() == Game.GameState.WAITING) {
                if (player == ManHunt.getCurrentGame().getSpeedRunner()) {
                    event.setCancelled(true);
                }
                if (event.getDamager() == ManHunt.getCurrentGame().getSpeedRunner()) {
                    if (ManHunt.getCurrentGame().getHunters().contains(player)) {
                        startGame();
                    }
                }
            }
        }
    }

    public static void startGame() {
        ManHunt.getCurrentGame().getSpeedRunner().removeMetadata("mhfrozen", ManHunt.getManHunt());
        ManHunt.getCurrentGame().getSpeedRunner().playSound(ManHunt.getCurrentGame().getSpeedRunner().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 2F);
        for (Player hunter : ManHunt.getCurrentGame().getHunters()) {
            hunter.removeMetadata("mhfrozen", ManHunt.getManHunt());
            hunter.playSound(hunter.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 2F);
        }
        ManHunt.getCurrentGame().setGameState(Game.GameState.STARTED);
    }

    @EventHandler
    public void onDragonDeath(EntityDeathEvent event) {
        if (ManHunt.getCurrentGame() != null) {
            if (event.getEntity() instanceof EnderDragon) {
                ManHunt.getCurrentGame().endGame(true);
            }
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (ManHunt.getCurrentGame() != null) {
            if (ManHunt.getCurrentGame().getHunters().contains(event.getPlayer())) {
                ItemStack compass = new ItemStack(Material.COMPASS);
                ItemMeta meta = compass.getItemMeta();
                meta.setDisplayName(ChatColor.GREEN + "Tracker");
                meta.addEnchant(Enchantment.VANISHING_CURSE, 1, true);
                compass.setItemMeta(meta);
                event.getPlayer().getInventory().setItem(8, compass);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (ManHunt.getCurrentGame() != null) {
            if (ManHunt.getCurrentGame().getHunters().contains(event.getPlayer())) {
                ManHunt.getCurrentGame().getHunters().remove(event.getPlayer());
                if (ManHunt.getCurrentGame().getHunters().isEmpty()) {
                    ManHunt.getCurrentGame().endGame(true);
                }
            }
            if (ManHunt.getCurrentGame().getSpeedRunner() == event.getPlayer()) {
                ManHunt.getCurrentGame().endGame(false);
            }
        }
    }
}
