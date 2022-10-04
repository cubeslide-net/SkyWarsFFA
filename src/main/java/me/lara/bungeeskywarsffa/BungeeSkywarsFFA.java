package me.lara.bungeeskywarsffa;

import de.hglabor.knockbackapi.api.KnockbackConfiguration;
import de.hglabor.knockbackapi.api.KnockbackSettings;
import de.hglabor.knockbackapi.registry.EntityKnockbackRegistry;
import fr.mrmicky.fastboard.FastBoard;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import me.lara.bungeeskywarsffa.commands.CommandSpec;
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
import org.bukkit.configuration.Configuration;
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
    Objects.requireNonNull(getCommand("spec")).setExecutor(new CommandSpec());

    final Configuration config = getConfig();
    
    config.addDefault("MYSQL.HOSTNAME", "localhost");
    config.addDefault("MYSQL.USERNAME", "root");
    config.addDefault("MYSQL.PASSWORD", "");
    config.addDefault("MYSQL.DATABASE", "SkyWarsFFA");
    config.addDefault("MYSQL.PORT", 3306);

    config.addDefault("Gameplay.limitCobweb", true);
    config.addDefault("Gameplay.cobWebPlaceDelayTimeSeconds", 3);

    config.addDefault("Gameplay.1_8_Knockback", true);

    config.addDefault("Messages.stats",
        Arrays.asList("&7&m--------&r&6Stats of %player%&7§m--------",
            "&eKills&8: &3%kills%",
            "&eDeaths&8: &3%deaths%", "&eK/D&8: &3%kd%",
            "&eRank&8: §7#&3%rank%",
            "&7&m--------&r&6Stats of %player%&7§m--------"));
    config.addDefault("Messages.cobWebLimit", "&cPlease wait before using Cobwebs again!");

    config.addDefault("Scoreboard.title", "&b&lCubeSlide.net");
    config.addDefault("Scoreboard.lines",
        Arrays.asList("", "&3Kills", "&b%kills%", "", "&3Deaths", "&b%deaths%", "", "&eKillStreak",
            "&6%killstreak%"));

    config.options().copyDefaults(true);
    saveConfig();

    database = new Database();
    database.createTable();

    new BukkitRunnable() {
      @Override
      public void run() {
        if(WorldListeners.blockExistTimeList.isEmpty()) return;

        for(Block block : WorldListeners.blockExistTimeList.keySet()) {
          long breakTime =
              System.currentTimeMillis() - WorldListeners.blockExistTimeList.get(block);
          if (breakTime > 1000 * 5) {
            WorldListeners.blockExistTimeList.remove(block);
            Objects.requireNonNull(Bukkit.getWorld(block.getWorld().getName()))
                .getBlockAt(block.getLocation()).setType(Material.AIR);
          }
        }
      }
    }.runTaskTimer(getInstance(), 5, 5);

    new BukkitRunnable() {
      @Override
      public void run() {
        if(Bukkit.getOnlinePlayers().size() == 0) {
          database.sendKeelAlivePing();
          getLogger().info("Database-KeepAlive-Ping send.");
        }
      }
    }.runTaskTimer(getInstance(), 20 * 300, 20 * 300);


    if(getConfig().getBoolean("Gameplay.1_8_Knockback")){
      EntityKnockbackRegistry.INSTANCE.register(
              this,
              new KnockbackConfiguration(
                      Map.of(),
                      new KnockbackSettings()
                              .modifyKnockback(true)
                              .knockbackFriction(3.0)
                              .knockbackHorizontal(0.65)
              ));
    }


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
      boards.put(uuid, board);
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
