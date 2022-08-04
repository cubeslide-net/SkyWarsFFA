package me.lara.bungeeskywarsffa.utils;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemBuilder {

  public static ItemStack buildItem(Material material, int count, String displayName,
      List<String> lore, boolean unbreakable) {
    final ItemStack itemStack = new ItemStack(material, count);
    final ItemMeta itemMeta = itemStack.getItemMeta();
    assert itemMeta != null;
    itemMeta.setDisplayName(displayName);
    itemMeta.setLore(lore);
    itemMeta.setUnbreakable(unbreakable);

    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }


}
