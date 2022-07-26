package me.lara.bungeeskywarsffa.listeners;

import me.lara.bungeeskywarsffa.utils.LocationUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class WorldListeners implements Listener {

    public static final ConcurrentHashMap<Block, Long> blockExistTimeList = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<Block, Player> blockExistTimePlayerList = new ConcurrentHashMap<>();

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();

        if (event.getBlockPlaced().getLocation().getY() >= LocationUtils.buildHeight()) {
            event.setCancelled(true);
            return;
        }

        final Block block = event.getBlockPlaced();

        blockExistTimeList.put(block, System.currentTimeMillis());
        blockExistTimePlayerList.put(block, player);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) {
            event.setCancelled(true);
        }
    }

}
