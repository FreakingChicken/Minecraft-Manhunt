package me.freakingchicken.manhunt.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.PlayerArgument;
import me.freakingchicken.manhunt.Game;
import me.freakingchicken.manhunt.ManHunt;
import me.freakingchicken.manhunt.util.Config;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class ReloadCommand {

    public static CommandAPICommand command() {
        return new CommandAPICommand("manhuntreload")
                .executes((sender, args) -> {
                    if (!sender.hasPermission("manhunt.reload")) {
                        sender.sendMessage(ChatColor.RED + "You do not have permission to reload manhunt! You must have manhunt.reload to reload.");
                        return;
                    }
                    Config.reload();
                    sender.sendMessage(ChatColor.GREEN + "ManHunt config has been reloaded.");
                });
    }
}
