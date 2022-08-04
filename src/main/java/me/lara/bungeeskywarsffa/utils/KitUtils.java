package me.lara.bungeeskywarsffa.utils;

import java.util.Arrays;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class KitUtils {

  public static void giveBasicKit(Player player) {

    player.getInventory().clear();

    player.getInventory().setHelmet(
        ItemBuilder.buildItem(Material.NETHERITE_HELMET, 1, "§c§lBasic Helmet",
            Arrays.asList("", "§cHelmet of the Basic-Kit."), true));
    player.getInventory().setChestplate(
        ItemBuilder.buildItem(Material.DIAMOND_CHESTPLATE, 1, "§c§lBasic T-Shirt",
            Arrays.asList("", "§cT-Shirt of the Basic-Kit."), true));
    player.getInventory().setLeggings(
        ItemBuilder.buildItem(Material.DIAMOND_LEGGINGS, 1, "§c§lBasic Hot-pants",
            Arrays.asList("", "§cHot-pants of the Basic-Kit."), true));
    player.getInventory().setBoots(
        ItemBuilder.buildItem(Material.DIAMOND_BOOTS, 1, "§c§lBasic Boots",
            Arrays.asList("", "§cBoots of the Basic-Kit."), true));

    player.getInventory().setItem(0,
        ItemBuilder.buildItem(Material.DIAMOND_SWORD, 1, "§3§lDiamond Sword", Arrays.asList(""),
            true));
    player.getInventory().setItem(1, ItemBuilder.buildItem(Material.COBWEB, 16, "§3Web",
        Arrays.asList("", "§3§lWorld wide Web."), false));
    player.getInventory().setItem(2,
        ItemBuilder.buildItem(Material.GOLDEN_APPLE, 3, "§6§lGolden Apple", Arrays.asList(""),
            false));
    player.getInventory().setItem(8,
        ItemBuilder.buildItem(Material.ENDER_PEARL, 2, "§5§lEnderpearl", Arrays.asList(""), false));
    player.getInventory().setItem(3,
        ItemBuilder.buildItem(Material.COBBLESTONE, 32, "§eCobblestone",
            Arrays.asList("", "§eCobblestone"), false));

    player.setFoodLevel(20);
    player.setHealth(20);
    player.getActivePotionEffects().clear();
    player.setFireTicks(0);
  }

}
