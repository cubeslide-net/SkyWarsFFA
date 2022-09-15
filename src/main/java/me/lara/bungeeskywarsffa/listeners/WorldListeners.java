package me.lara.bungeeskywarsffa.listeners;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.lara.bungeeskywarsffa.BungeeSkywarsFFA;
import me.lara.bungeeskywarsffa.utils.LocationUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WorldListeners implements Listener {

  public static final ConcurrentHashMap<Block, Long> blockExistTimeList = new ConcurrentHashMap<>();
  private final HashMap<UUID, Long> lastCobwebPlace = new HashMap<>();

  @EventHandler
  public void onBreak(BlockBreakEvent event) {
    event.setCancelled(true);
  }

  @EventHandler
  public void onBlockForm(BlockFormEvent event) {
    event.setCancelled(true);
  }

  @EventHandler
  public void onPlace(BlockPlaceEvent event) {
    final Player player = event.getPlayer();

    if (event.isCancelled()) {
      return;
    }

    if (event.getBlockPlaced().getLocation().getY() >= LocationUtils.buildHeight()) {
      event.setCancelled(true);
      return;
    }

    final BungeeSkywarsFFA bungeeSkywarsFFA = BungeeSkywarsFFA.getInstance();
    final UUID uuid = player.getUniqueId();
    final Block block = event.getBlockPlaced();

    if (block.getType() == Material.COBWEB && bungeeSkywarsFFA.getConfig()
        .getBoolean("Gameplay.limitCobweb")) {

      if (!lastCobwebPlace.containsKey(uuid)) {
        lastCobwebPlace.put(uuid, System.currentTimeMillis());
      } else if (System.currentTimeMillis() - lastCobwebPlace.get(uuid)
          > bungeeSkywarsFFA.getConfig().getLong("Gameplay.cobWebPlaceDelayTimeSeconds") * 1000) {
        lastCobwebPlace.remove(uuid);
      } else {
        event.setCancelled(true);
        player.sendMessage(bungeeSkywarsFFA.getStringFromPath("Messages.cobWebLimit"));
      }


    }

    blockExistTimeList.put(block, System.currentTimeMillis());
  }

  @EventHandler
  public void onWeatherChange(WeatherChangeEvent event) {
    if (event.toWeatherState()) {
      event.setCancelled(true);
    }
  }

}
