package me.lara.bungeeskywarsffa.listeners;

import me.lara.bungeeskywarsffa.BungeeSkywarsFFA;
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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;
import java.util.UUID;

public class PlayerListeners implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final BungeeSkywarsFFA instance = BungeeSkywarsFFA.getInstance();
        final Database database = instance.getDatabase();

        event.setJoinMessage("");

        if (LocationUtils.spawnLocation() == null) {
            player.sendMessage("§cBungeeSkyWarsFFA Setup is not completed! Spawn is not set.\n§cPlease make sure to complete the Setup with /setup!");
        }

        player.teleport(Objects.requireNonNull(LocationUtils.spawnLocation()));

        final UUID uuid = player.getUniqueId();
        if(!database.doesPlayerExistByUUID(uuid)) {
            database.createNewUser(uuid);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            final Player player = (Player) event.getEntity();

            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setCancelled(true);
            }

            if (player.getLocation().getY() > Objects.requireNonNull(LocationUtils.spawnLocation()).getY() - 10) {
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

        if(killer instanceof Player && killer != player) {
            Bukkit.broadcastMessage(BungeeSkywarsFFA.getPREFIX() + "§4" + player.getName() + "§c got killed by §4" + killer.getName());
            killer.sendMessage(BungeeSkywarsFFA.getPREFIX() + "§aYou killed §2" + player.getName() + "§a!");
            killer.setHealth(killer.getMaxHealth());
            database.addKill(killer.getUniqueId());
        } else {
            Bukkit.broadcastMessage(BungeeSkywarsFFA.getPREFIX() + "§4" + player.getName() + "§c died.");
        }


        for(Block block :  WorldListeners.blockExistTimeList.keySet()) {
            WorldListeners.blockExistTimePlayerList.remove(block);
        }

        player.sendMessage(BungeeSkywarsFFA.getPREFIX() + "§4You died.");
        database.addDeath(player.getUniqueId());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        final Player player = event.getPlayer();

        if (LocationUtils.spawnLocation() == null) {
            player.sendMessage("§cBungeeSkyWarsFFA Setup is not completed! Spawn is not set.\n§cPlease make sure to complete the Setup with /setup!");
        }

        event.setRespawnLocation(Objects.requireNonNull(LocationUtils.spawnLocation()));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final BungeeSkywarsFFA bungeeSkywarsFFA = BungeeSkywarsFFA.getInstance();


        if(bungeeSkywarsFFA.getConfig().isSet("Dead-height.Y") && player.getLocation().getY() < bungeeSkywarsFFA.getConfig().getDouble("Dead-height.Y")) {

            if(player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
            final Database database = BungeeSkywarsFFA.getInstance().getDatabase();

            player.getInventory().clear();
            player.teleport(Objects.requireNonNull(LocationUtils.spawnLocation()));

            if(!WorldListeners.blockExistTimeList.isEmpty()) {
                for (Block block : WorldListeners.blockExistTimeList.keySet()) {
                    WorldListeners.blockExistTimePlayerList.remove(block);
                }
            }

            player.sendMessage(BungeeSkywarsFFA.getPREFIX() + "§4You died.");
            database.addDeath(player.getUniqueId());
        }

        if (LocationUtils.spawnLocation() == null || !player.getInventory().isEmpty()) return;

        if (player.getLocation().getY() < Objects.requireNonNull(LocationUtils.spawnLocation()).getY() - 4) {
            KitUtils.giveBasicKit(player);
        }

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();

            assert block != null;
            if (block.getType().toString().toLowerCase().contains("door") || block.getType() == Material.BEACON) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {

        if(!(event.getEntity() instanceof Player)) return;

        event.getEntity().setFoodLevel(20);
        event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage("");
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }
}
