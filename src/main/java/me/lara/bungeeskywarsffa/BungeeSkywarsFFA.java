package me.lara.bungeeskywarsffa;

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

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public final class BungeeSkywarsFFA extends JavaPlugin {

	
    private static final String PREFIX = "§7[§3Sky§bWars§3FFA§7] ";

    private static BungeeSkywarsFFA instance;
	
    public Database database;

    public static String getPREFIX() {
        return PREFIX;
    }

    public static BungeeSkywarsFFA getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        final PluginManager pluginManager = getServer().getPluginManager();
        instance = this;

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

        getConfig().addDefault("Messages.stats", Arrays.asList("&7&m--------&r&6Stats of %player%&7§m--------", "&eKills&8: &3%kills%", "&eDeaths&8: &3%deaths%", "&eKD&8: &3%kd%", "&7&m--------&r&6Stats of %player%&7§m--------"));
        getConfig().addDefault("Messages.cobWebLimit", "&cPlease wait before using Cobwebs again!");

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
                    long breakTime = System.currentTimeMillis() - WorldListeners.blockExistTimeList.get(block);
                    if (breakTime > 1000 * 5) {

                        if (WorldListeners.blockExistTimePlayerList.containsKey(block)) {
                            final Player player = WorldListeners.blockExistTimePlayerList.get(block).getPlayer();
                            assert player != null;


                            //Super quick but dirty check to not give dead Players their Items back after death. (Duplication Fix)
                            if (LocationUtils.spawnLocation() != null && player.getLocation().getY() < Objects.requireNonNull(LocationUtils.spawnLocation()).getY()) {
                                if (block.getType() == Material.COBBLESTONE) {
                                    player.getInventory().addItem(ItemBuilder.buildItem(Material.COBBLESTONE, 1, "§eCobblestone", Arrays.asList("", "§eCobblestone"), false));
                                } else if (block.getType() == Material.COBWEB) {
                                    player.getInventory().addItem(ItemBuilder.buildItem(Material.COBWEB, 1, "§3Web", Arrays.asList("", "§3§lWorld wide Web."), false));
                                }

                                player.updateInventory();
                            }
                            WorldListeners.blockExistTimePlayerList.remove(block);
                        }
                        WorldListeners.blockExistTimeList.remove(block);
                        Objects.requireNonNull(Bukkit.getWorld(block.getWorld().getName())).getBlockAt(block.getLocation()).setType(Material.AIR);
                    }

                }
            }
        }.runTaskTimer(getInstance(), 5, 5);
    }

    @Override
    public void onDisable() {
        try {
            getDatabase().close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getStringFromPath(String path) {
        return getConfig().getString(path).replace("&", "§");
    }

    public Database getDatabase() {
        return database;
    }
}
