package me.lara.bungeeskywarsffa.listeners;

import me.lara.bungeeskywarsffa.BungeeSkywarsFFA;
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

import java.util.Objects;

public class PlayerListeners implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        event.setJoinMessage("");

        if (LocationUtils.spawnLocation() == null) {
            player.sendMessage("§cBungeeSkyWarsFFA Setup is not completed! Spawn is not set.\n§cPlease make sure to complete the Setup with /setup!");
        }

        player.teleport(Objects.requireNonNull(LocationUtils.spawnLocation()));
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

        event.setDroppedExp(0);
        event.getDrops().clear();
        event.setDeathMessage("");
        player.spigot().respawn();

        if(killer instanceof Player && killer != player) {
            Bukkit.broadcastMessage(BungeeSkywarsFFA.getPREFIX() + "§4" + player.getName() + "§c got killed by §4" + killer.getName());
            killer.sendMessage(BungeeSkywarsFFA.getPREFIX() + "§aYou killed §2" + player.getName() + "§a!");
            killer.setHealth(killer.getMaxHealth());
        } else {
            Bukkit.broadcastMessage(BungeeSkywarsFFA.getPREFIX() + "§4" + player.getName() + "§c died.");
        }


        player.sendMessage(BungeeSkywarsFFA.getPREFIX() + "§4You died.");
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

        if(bungeeSkywarsFFA.getConfig().isSet("Dead-height.Y") && player.getLocation().getY() <bungeeSkywarsFFA.getConfig().getDouble("Dead-height.Y")) {

            if(player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

            player.setHealth(0);
        }

        if (LocationUtils.spawnLocation() == null || !player.getInventory().isEmpty()) return;

        if (player.getLocation().getY() < Objects.requireNonNull(LocationUtils.spawnLocation()).getY() - 4) {
            KitUtils.giveBasicKit(player);
        }

    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();

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
