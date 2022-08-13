package me.lara.bungeeskywarsffa.listeners;

import fr.mrmicky.fastboard.FastBoard;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import me.lara.bungeeskywarsffa.BungeeSkywarsFFA;
import me.lara.bungeeskywarsffa.commands.CommandSpec;
import me.lara.bungeeskywarsffa.utils.Database;
import me.lara.bungeeskywarsffa.utils.KitUtils;
import me.lara.bungeeskywarsffa.utils.LocationUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerListeners implements Listener {

  public static final HashMap<UUID, Integer> killStreakCount = new HashMap<>();

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    final Player player = event.getPlayer();
    final BungeeSkywarsFFA instance = BungeeSkywarsFFA.getInstance();
    final Database database = instance.getDatabase();

    event.setJoinMessage("");

    for(Player all : Bukkit.getOnlinePlayers()) {
      if(CommandSpec.vanished.contains(all.getUniqueId())) {
        player.hidePlayer(all);
      }
    }

    if (LocationUtils.spawnLocation() == null) {
      player.sendMessage(
          "§cBungeeSkyWarsFFA Setup is not completed! Spawn is not set.\n§cPlease make sure to complete the Setup with /setup!");
    }

    player.teleport(Objects.requireNonNull(LocationUtils.spawnLocation()));

    final UUID uuid = player.getUniqueId();
    if (!database.doesPlayerExistByUUID(uuid)) {
      database.createNewUser(uuid);
    }
    for (Player all : Bukkit.getOnlinePlayers()) {
      BungeeSkywarsFFA.getInstance().sendScoreboard(all);
    }
  }

  @EventHandler
  public void onDamage(EntityDamageEvent event) {
    if (event.getEntity() instanceof Player) {
      final Player player = (Player) event.getEntity();

      if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
        event.setCancelled(true);
      }

      if(CommandSpec.vanished.contains(player.getUniqueId())) {
        event.setCancelled(true);
        player.sendMessage(BungeeSkywarsFFA.getPrefix() + "§cYou are not allowed to hit Players while being vanish.");
      }

      if (player.getLocation().getY()
          > Objects.requireNonNull(LocationUtils.spawnLocation()).getY() - 10) {
        event.setCancelled(true);
      }

    }
  }

  @EventHandler
  public void onDeath(PlayerDeathEvent event) {
    final Player player = event.getEntity();
    final Player killer = event.getEntity().getKiller();
    final Database database = BungeeSkywarsFFA.getInstance().getDatabase();

    event.setDroppedExp(0);
    event.getDrops().clear();
    event.setDeathMessage("");

    new BukkitRunnable() {
      @Override
      public void run() {
        player.spigot().respawn();
      }
    }.runTaskLater(BungeeSkywarsFFA.getInstance(), 20 * 5);

    if (killer instanceof Player && killer != player) {
      Bukkit.broadcastMessage(
          BungeeSkywarsFFA.getPrefix() + "§4" + player.getName() + "§c got killed by §4"
              + killer.getName());
      killer.sendMessage(
          BungeeSkywarsFFA.getPrefix() + "§aYou killed §2" + player.getName() + "§a!");
      killer.setHealth(killer.getMaxHealth());
      database.addKill(killer.getUniqueId());
      KitUtils.giveBasicKit(killer);

      final UUID killerUUID = killer.getUniqueId();
      if (killStreakCount.containsKey(killerUUID)) {
        killStreakCount.put(killerUUID, killStreakCount.get(killerUUID) + 1);
      } else {
        killStreakCount.put(killerUUID, 1);
      }

    } else {
      Bukkit.broadcastMessage(BungeeSkywarsFFA.getPrefix() + "§4" + player.getName() + "§c died.");
    }

    for (Block block : WorldListeners.blockExistTimeList.keySet()) {
      WorldListeners.blockExistTimePlayerList.remove(block);
    }

    player.sendMessage(BungeeSkywarsFFA.getPrefix() + "§4You died.");

    final UUID uuid = player.getUniqueId();
    if (killStreakCount.containsKey(uuid)) {
      killStreakCount.remove(uuid);
    }

    database.addDeath(player.getUniqueId());
    for (Player all : Bukkit.getOnlinePlayers()) {
      BungeeSkywarsFFA.getInstance().sendScoreboard(all);
    }
  }

  @EventHandler
  public void onRespawn(PlayerRespawnEvent event) {
    final Player player = event.getPlayer();

    if (LocationUtils.spawnLocation() == null) {
      player.sendMessage(
          "§cBungeeSkyWarsFFA Setup is not completed! Spawn is not set.\n§cPlease make sure to complete the Setup with /setup!");
    }

    event.setRespawnLocation(Objects.requireNonNull(LocationUtils.spawnLocation()));
  }

  @EventHandler
  public void onMove(PlayerMoveEvent event) {
    final Player player = event.getPlayer();
    final BungeeSkywarsFFA bungeeSkywarsFFA = BungeeSkywarsFFA.getInstance();

    if (player.getGameMode() == GameMode.CREATIVE
        || player.getGameMode() == GameMode.SPECTATOR) {
      return;
    }

    if (bungeeSkywarsFFA.getConfig().isSet("Dead-height.Y")
        && player.getLocation().getY() < bungeeSkywarsFFA.getConfig().getDouble("Dead-height.Y")) {

      final Database database = BungeeSkywarsFFA.getInstance().getDatabase();

      player.getInventory().clear();
      player.teleport(Objects.requireNonNull(LocationUtils.spawnLocation()));
      player.setHealth(20);

      if (!WorldListeners.blockExistTimeList.isEmpty()) {
        for (Block block : WorldListeners.blockExistTimeList.keySet()) {
          WorldListeners.blockExistTimePlayerList.remove(block);
        }
      }

      player.sendMessage(BungeeSkywarsFFA.getPrefix() + "§4You died.");

      if(lastHit.containsKey(player.getUniqueId())) {
        final Player killer = Bukkit.getPlayer(lastHit.get(player.getUniqueId()));

        if(killer == null) {
          lastHit.remove(player.getUniqueId());
        } else {
          Bukkit.broadcastMessage(
              BungeeSkywarsFFA.getPrefix() + "§4" + player.getName() + "§c got killed by §4"
                  + killer.getName());
          killer.sendMessage(
              BungeeSkywarsFFA.getPrefix() + "§aYou killed §2" + player.getName() + "§a!");
          killer.setHealth(killer.getMaxHealth());
          database.addKill(killer.getUniqueId());
          KitUtils.giveBasicKit(killer);
          final UUID killerUUID = killer.getUniqueId();
          if (killStreakCount.containsKey(killerUUID)) {
            killStreakCount.put(killerUUID, killStreakCount.get(killerUUID) + 1);
          } else {
            killStreakCount.put(killerUUID, 1);
          }

          lastHit.remove(player.getUniqueId());
        }


      }

      database.addDeath(player.getUniqueId());

      final UUID uuid = player.getUniqueId();
      if (killStreakCount.containsKey(uuid)) {
        killStreakCount.remove(uuid);
      }

      for (Player all : Bukkit.getOnlinePlayers()) {
        BungeeSkywarsFFA.getInstance().sendScoreboard(all);
      }

    }

    if (LocationUtils.spawnLocation() == null || !player.getInventory().isEmpty()) {
      return;
    }

    if (player.getLocation().getY()
        < Objects.requireNonNull(LocationUtils.spawnLocation()).getY() - 4) {
      KitUtils.giveBasicKit(player);
    }

  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {

    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
      Block block = event.getClickedBlock();

      assert block != null;
      if (block.getType().toString().toLowerCase().contains("door")
          || block.getType() == Material.BEACON) {
        event.setCancelled(true);
      }
    }
  }

  private static final HashMap<UUID, UUID> lastHit = new HashMap<>();

  @EventHandler
  public void onDamage(EntityDamageByEntityEvent event) {
    if(event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
      final Player damager = (Player) event.getDamager();
      final Player player = (Player) event.getEntity();
      final UUID damagerUUID = damager.getUniqueId();
      final UUID playerUUID = player.getUniqueId();

      lastHit.put(damagerUUID, playerUUID);
      lastHit.put(playerUUID, damagerUUID);
    }
  }

  @EventHandler
  public void onFoodLevelChange(FoodLevelChangeEvent event) {

    if (!(event.getEntity() instanceof Player)) {
      return;
    }

    event.getEntity().setFoodLevel(20);
    event.setCancelled(true);
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    final Player player = event.getPlayer();
    final BungeeSkywarsFFA bungeeSkywarsFFA = BungeeSkywarsFFA.getInstance();
    event.setQuitMessage("");

    final UUID uuid = player.getUniqueId();
    FastBoard fastBoard = bungeeSkywarsFFA.getBoards().get(uuid);

    if (fastBoard == null) {
      return;
    }

    fastBoard.delete();
    bungeeSkywarsFFA.getBoards().remove(uuid);

  }

  @EventHandler
  public void onDrop(PlayerDropItemEvent event) {
    event.setCancelled(true);
  }
}
