package me.lara.bungeeskywarsffa.commands;

import me.lara.bungeeskywarsffa.BungeeSkywarsFFA;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;

import java.util.Objects;

public class SetupCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("This Command only works for Players.");
            return true;
        }

        final Player player = (Player) sender;
        final BungeeSkywarsFFA bungeeSkywarsFFA = BungeeSkywarsFFA.getInstance();
        final Configuration config = bungeeSkywarsFFA.getConfig();
        final Location location = player.getLocation();

        if (!player.hasPermission("BungeeSykWarsFFA.setup")) {
            player.sendMessage("§cYou don't have Permissions to run this Command.");
            return true;
        }

        if (args.length == 0) {
            sendhelp(player);
        } else {

            if (args[0].equalsIgnoreCase("setspawn")) {
                config.set("Spawn.X", location.getX());
                config.set("Spawn.Y", location.getY());
                config.set("Spawn.Z", location.getZ());

                config.set("Spawn.YAW", location.getYaw());
                config.set("Spawn.PITCH", location.getPitch());
                config.set("Spawn.world", Objects.requireNonNull(location.getWorld()).getName());
                bungeeSkywarsFFA.saveConfig();
                bungeeSkywarsFFA.reloadConfig();
                player.sendMessage("§aSpawn point set.");
            } else if (args[0].equalsIgnoreCase("buildHeight")) {
                config.set("Build-height.Y", location.getY());
                bungeeSkywarsFFA.saveConfig();
                bungeeSkywarsFFA.reloadConfig();
                player.sendMessage("§aBuild-height set.");
            } else if(args[0].equalsIgnoreCase("deadHeight")) {
                config.set("Dead-height.Y", location.getY());
                bungeeSkywarsFFA.saveConfig();
                bungeeSkywarsFFA.reloadConfig();
                player.sendMessage("§aDead-height set.");
            } else {
                sendhelp(player);
            }

        }

        return false;
    }

    private void sendhelp(Player player) {
        player.sendMessage("§cSetup Commands:");
        player.sendMessage("§c/setup setSpawn §7- §cSet the Re/spawn-point.");
        player.sendMessage("§c/setup buildHeight §7 - §cBelow this Point it is allowed to build.");
        player.sendMessage("§c/setup deadHeight §7 - §cBelow this Point Players will die.");
    }
}
