package me.lara.bungeeskywarsffa;

import fr.mrmicky.fastboard.FastBoard;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import me.lara.bungeeskywarsffa.commands.CommandStats;
import me.lara.bungeeskywarsffa.commands.KillStreakCommand;
import me.lara.bungeeskywarsffa.commands.SetupCommandExecutor;
import me.lara.bungeeskywarsffa.listeners.PlayerListeners;
import me.lara.bungeeskywarsffa.listeners.WorldListeners;
import me.lara.bungeeskywarsffa.utils.Database;
import me.lara.bungeeskywarsffa.utils.ItemBuilder;
import me.lara.bungeeskywarsffa.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;


public final class BungeeSkywarsFFA extends JavaPlugin {


  private static final String PREFIX = "§7[§3Sky§bWars§3FFA§7] ";

  private static BungeeSkywarsFFA instance;

  @SuppressWarnings("checkstyle:Indentation")
  public Database database;
  public HashMap<UUID, FastBoard> boards;

  public static String getPrefix() {
    return PREFIX;
  }

  public static BungeeSkywarsFFA getInstance() {
    return instance;
  }

  @SuppressWarnings("checkstyle:Indentation")
  @Override
  public void onEnable() {
    final PluginManager pluginManager = getServer().getPluginManager();
    instance = this;
    boards = new HashMap<>();

    pluginManager.registerEvents(new PlayerListeners(), this);
    pluginManager.registerEvents(new WorldListeners(), this);

    Objects.requireNonNull(getCommand("setup")).setExecutor(new SetupCommandExecutor());
    Objects.requireNonNull(getCommand("stats")).setExecutor(new CommandStats());
    Objects.requireNonNull(getCommand("killstreak")).setExecutor(new KillStreakCommand());

    getConfig().addDefault("MYSQL.HOSTNAME", "localhost");
    getConfig().addDefault("MYSQL.USERNAME", "root");
    getConfig().addDefault("MYSQL.PASSWORD", "");
    getConfig().addDefault("MYSQL.DATABASE", "SkyWarsFFA");
    getConfig().addDefault("MYSQL.PORT", 3306);

    getConfig().addDefault("Gameplay.limitCobweb", true);
    getConfig().addDefault("Gameplay.cobWebPlaceDelayTimeSeconds", 3);

    getConfig().addDefault("Messages.stats",
        Arrays.asList("&7&m--------&r&6Stats of %player%&7§m--------", "&eKills&8: &3%kills%",
            "&eDeaths&8: &3%deaths%", "&eKD&8: &3%kd%",
            "&7&m--------&r&6Stats of %player%&7§m--------"));
    getConfig().addDefault("Messages.cobWebLimit", "&cPlease wait before using Cobwebs again!");

    getConfig().addDefault("Scoreboard.title", "&b&lCubeSlide.net");
    getConfig().addDefault("Scoreboard.lines",
        Arrays.asList("", "&3Kills", "&b%kills%", "", "&3Deaths", "&b%deaths%", "", "&eKillStreak",
            "&6%killstreak%"));

    getConfig().options().copyDefaults(true);
    saveConfig();

    database = new Database();
    database.createTable();

    new BukkitRunnable() {
      @Override
      public void run() {
        Optional<Block> firstKey = WorldListeners.blockExistTimeList.keySet().stream().findFirst();
        if (firstKey.isPresent()) {
          Block block = firstKey.get();
          long breakTime =
              System.currentTimeMillis() - WorldListeners.blockExistTimeList.get(block);
          if (breakTime > 1000 * 5) {

            if (WorldListeners.blockExistTimePlayerList.containsKey(block)) {
              final Player player = WorldListeners.blockExistTimePlayerList.get(block).getPlayer();
              assert player != null;

              if (LocationUtils.spawnLocation() != null
                  && player.getLocation().getY() < Objects.requireNonNull(
                  LocationUtils.spawnLocation()).getY()) {
                if (block.getType() == Material.COBBLESTONE) {
                  player.getInventory().addItem(
                      ItemBuilder.buildItem(Material.COBBLESTONE, 1, "§eCobblestone",
                          Arrays.asList("", "§eCobblestone"), false));
                } else if (block.getType() == Material.COBWEB) {
                  player.getInventory().addItem(ItemBuilder.buildItem(Material.COBWEB, 1, "§3Web",
                      Arrays.asList("", "§3§lWorld wide Web."), false));
                }

                player.updateInventory();
              }
              WorldListeners.blockExistTimePlayerList.remove(block);
            }
            WorldListeners.blockExistTimeList.remove(block);
            Objects.requireNonNull(Bukkit.getWorld(block.getWorld().getName()))
                .getBlockAt(block.getLocation()).setType(Material.AIR);
          }

        }
      }
    }.runTaskTimer(getInstance(), 5, 5);
  }

  @SuppressWarnings("checkstyle:Indentation")
  @Override
  public void onDisable() {
    try {
      getDatabase().close();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @SuppressWarnings("checkstyle:Indentation")
  public void sendScoreboard(Player player) {

    final List<String> tempList = new ArrayList<>();
    final Database database = BungeeSkywarsFFA.getInstance().getDatabase();
    final UUID uuid = player.getUniqueId();
    final int kills = database.getKills(uuid);
    final int deaths = database.getDeaths(uuid);

    for (String lines : getConfig().getStringList("Scoreboard.lines")) {
      tempList.add(lines.replace("%kills%", String.valueOf(kills))
          .replace("%deaths%", String.valueOf(deaths)).replace("%killstreak%",
              String.valueOf(PlayerListeners.killStreakCount.getOrDefault(uuid, 0)))
          .replace("&", "§"));
    }

    FastBoard board;
    if (!boards.containsKey(uuid)) {
      board = new FastBoard(player);
      board.updateTitle(getStringFromPath("Scoreboard.title"));
    } else {
      board = boards.get(uuid);
    }
    board.updateLines(tempList);
  }

  public HashMap<UUID, FastBoard> getBoards() {
    return boards;
  }

  public String getStringFromPath(String path) {
    return getConfig().getString(path).replace("&", "§");
  }

  public Database getDatabase() {
    return database;
  }
}
