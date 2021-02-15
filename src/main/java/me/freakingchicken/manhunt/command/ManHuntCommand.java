package me.freakingchicken.manhunt.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import me.freakingchicken.manhunt.Game;
import me.freakingchicken.manhunt.ManHunt;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class ManHuntCommand {

    public static CommandAPICommand command() {
        return new CommandAPICommand("manhunt")
                .withArguments(new PlayerArgument("speedrunner"))
                .executes((sender, args) -> {
                    if (!sender.hasPermission("manhunt.start")) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to start a manhunt! You must have manhunt.start to start a game.");
                        return;
                    }
                    if (ManHunt.getCurrentGame() != null) {
                        sender.sendMessage(ChatColor.RED + "There is currently an active Manhunt game. Please wait for it to finish.");
                    } else {
                        Player speedRunner = (Player) args[0];
                        List<Player> list = speedRunner.getWorld().getPlayers();
                        list.remove(speedRunner);
                        ManHunt.setCurrentGame(new Game(speedRunner, list));
                    }
                });
    }
}
