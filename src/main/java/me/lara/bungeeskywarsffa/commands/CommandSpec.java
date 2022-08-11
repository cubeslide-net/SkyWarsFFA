package me.lara.bungeeskywarsffa.commands;

import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;
import me.lara.bungeeskywarsffa.BungeeSkywarsFFA;
import me.lara.bungeeskywarsffa.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandSpec implements CommandExecutor {


  public static ArrayList<UUID> vanished = new ArrayList<>();

  @Override
  public boolean onCommand(CommandSender commandSender, Command command, String s,
      String[] args) {

    if(!(commandSender instanceof Player)) return true;

    Player player = (Player) commandSender;

    if(player.hasPermission("SkyWarsFFA.spec")) {

      if(args.length == 0) {
        player.sendMessage(BungeeSkywarsFFA.getPrefix() + "§c/spec [Player]");
        return true;
      }

      final Player target = Bukkit.getPlayer(args[0]);

      if(target == null) {
        player.sendMessage(BungeeSkywarsFFA.getPrefix() + "§cPlayer is not Online!");
        return true;
      }

      final UUID uuid = player.getUniqueId();

      if(!vanished.contains(uuid)) {
        for(Player all : Bukkit.getOnlinePlayers()) {
          all.hidePlayer(player);
        }

        player.setAllowFlight(true);
        player.setFlying(true);
        player.sendMessage(BungeeSkywarsFFA.getPrefix() + "§cYou are now vanished and spectating!");

        player.teleport(target.getLocation());
        vanished.add(player.getUniqueId());
      } else {
        for(Player all : Bukkit.getOnlinePlayers()) {
          all.showPlayer(player);
        }

        player.teleport(LocationUtils.spawnLocation());
        vanished.remove(player.getUniqueId());
        player.setAllowFlight(false);
        player.setFlying(false);
        player.sendMessage(BungeeSkywarsFFA.getPrefix() + "§aYou are no longer vanished!");
      }

    }

    return true;
  }
}
