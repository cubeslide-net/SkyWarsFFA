package me.lara.bungeeskywarsffa.commands;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import me.lara.bungeeskywarsffa.BungeeSkywarsFFA;
import me.lara.bungeeskywarsffa.utils.Database;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandStats implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

    final Database database = BungeeSkywarsFFA.getInstance().getDatabase();

    final Player player = (Player) sender;

    final DecimalFormat decimalFormat = new DecimalFormat("#.##");
    final List<String> messages = BungeeSkywarsFFA.getInstance().getConfig()
        .getStringList("Messages.stats");

    if (args.length == 0) {
      final UUID uuid = player.getUniqueId();
      final double kills = database.getKills(uuid);
      final double deaths = database.getDeaths(uuid);

      double kdr = kills / deaths;

      for (String line : messages) {
        String message = line.replace("&", "§").replace("%player%", player.getName())
            .replace("%kills%", String.valueOf((int) kills)).replace("%deaths%", String.valueOf((int) deaths))
            .replace("%kd%", decimalFormat.format(kdr)).replace("%rank%", String.valueOf(database.getRank(uuid)));

        player.sendMessage(message);
      }
    } else {

      OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

      if (!database.doesPlayerExistByUUID(target.getUniqueId())) {
        player.sendMessage(BungeeSkywarsFFA.getPrefix() + "§cThis Player has not Played yet!");
        return true;
      }

      final UUID targetUUID = target.getUniqueId();
      final double kills = database.getKills(targetUUID);
      final double deaths = database.getDeaths(targetUUID);

      double kdr = kills / deaths;

      for (String line : messages) {
        String message = line.replace("&", "§")
            .replace("%player%", Objects.requireNonNull(target.getName()))
            .replace("%kills%", String.valueOf(kills)).replace("%deaths%", String.valueOf(deaths))
            .replace("%kd%", decimalFormat.format(kdr)).replace("%rank%", String.valueOf(database.getRank(targetUUID)));
        player.sendMessage(message);
      }

    }

    return true;
  }
}
