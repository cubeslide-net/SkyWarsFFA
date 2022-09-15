package me.lara.bungeeskywarsffa.commands;

import java.util.UUID;

import me.lara.bungeeskywarsffa.BungeeSkywarsFFA;
import me.lara.bungeeskywarsffa.listeners.PlayerListeners;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KillStreakCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s,
                             String[] strings) {

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("This Command only works for Players.");
            return true;
        }

        final Player player = (Player) commandSender;
        final UUID uuid = player.getUniqueId();

        if (!PlayerListeners.killStreakCount.containsKey(uuid)) {
            player.sendMessage(BungeeSkywarsFFA.getPrefix() + "§7Your current KillStreak§8: §e0");
            return true;
        }

        player.sendMessage(BungeeSkywarsFFA.getPrefix() + "§7Your current KillStreak§8: §e"
                + PlayerListeners.killStreakCount.get(uuid));

        return true;
    }
}
