package me.lara.bungeeskywarsffa.commands;

import me.lara.bungeeskywarsffa.BungeeSkywarsFFA;
import me.lara.bungeeskywarsffa.utils.Database;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class CommandStats implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        final Database database = BungeeSkywarsFFA.getInstance().getDatabase();

        final Player player = (Player) sender;
        if (args.length == 0) {
            final UUID uuid = player.getUniqueId();
            player.sendMessage("§7§m--------§r§6Stats of " + player.getName() + "§7§m--------");

            final int kills = database.getKills(uuid);
            final int deaths = database.getDeaths(uuid);

            player.sendMessage("§9Kills§8: §3" + kills);
            player.sendMessage("§9Deaths§8: §3" + deaths);

            try {
                player.sendMessage("§9KD§8: §3" + (double) (kills / deaths));
            } catch (Exception exception) {
                player.sendMessage("§9KD§8: §30.0");
            }

            player.sendMessage("§7§m--------§r§6Stats of " + player.getName() + "§7§m--------");
        } else {

            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

            assert target != null;
            if (!database.doesPlayerExistByUUID(target.getUniqueId())) {
                player.sendMessage(BungeeSkywarsFFA.getPREFIX() + "§cThis Player has not Played yet!");
                return true;
            }

            player.sendMessage("§7§m--------§r§6Stats of " + target.getName() + "§7§m--------");

            final int kills = database.getKills(target.getUniqueId());
            final int deaths = database.getDeaths(target.getUniqueId());

            player.sendMessage("§9Kills§8: §3" + kills);
            player.sendMessage("§9Deaths§8: §3" + deaths);

            try {
                player.sendMessage("§9KD§8: §3" + (double) (kills / deaths));
            } catch (Exception exception) {
                player.sendMessage("§9KD§8: §30.0");
            }
            player.sendMessage("§7§m--------§r§6Stats of " + target.getName() + "§7§m--------");

        }


        return true;
    }
}
