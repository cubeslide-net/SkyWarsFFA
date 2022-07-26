package me.lara.bungeeskywarsffa;

import me.lara.bungeeskywarsffa.commands.SetupCommandExecutor;
import me.lara.bungeeskywarsffa.listeners.PlayerListeners;
import me.lara.bungeeskywarsffa.listeners.WorldListeners;
import me.lara.bungeeskywarsffa.utils.ItemBuilder;
import me.lara.bungeeskywarsffa.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public final class BungeeSkywarsFFA extends JavaPlugin {

    private static BungeeSkywarsFFA instance;
    private static String PREFIX;

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

        getConfig().options().copyDefaults(true);
        saveConfig();
        new BukkitRunnable() {
            @Override
            public void run() {
                Optional<Block> firstKey = WorldListeners.blockExistTimeList.keySet().stream().findFirst();
                if (firstKey.isPresent()) {
                    Block block = firstKey.get();
                    long breakTime = System.currentTimeMillis() -  WorldListeners.blockExistTimeList.get(block);
                    if(breakTime > 1000 * 5) {
                        final Player player = WorldListeners.blockExistTimePlayerList.get(block).getPlayer();
                        assert player != null;


                        //Super quick but dirty check to not give dead Players their Items back after death. (Duplication Fix)
                        if(LocationUtils.spawnLocation() != null && player.getLocation().getY() < Objects.requireNonNull(LocationUtils.spawnLocation()).getY()) {
                            if(block.getType() == Material.COBBLESTONE) {
                                player.getInventory().addItem(ItemBuilder.buildItem(Material.COBBLESTONE, 1, "§eCobblestone", Arrays.asList("", "§eCobblestone"), false));
                            } else if(block.getType() == Material.COBWEB) {
                                player.getInventory().addItem(ItemBuilder.buildItem(Material.COBWEB, 1, "§3Web", Arrays.asList("", "§3§lWorld wide Web."), false));
                            }

                            player.updateInventory();
                        }



                        WorldListeners.blockExistTimePlayerList.remove(block);
                        WorldListeners.blockExistTimeList.remove(block);
                        Objects.requireNonNull(Bukkit.getWorld(block.getWorld().getName())).getBlockAt(block.getLocation()).setType(Material.AIR);
                    }

                }
            }
        }.runTaskTimer(getInstance(), 5, 5);

        PREFIX = "§7[§3Sky§bWars§3FFA§7] ";
    }
}
