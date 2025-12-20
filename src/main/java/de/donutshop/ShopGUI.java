package de.donutshop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopGUI implements Listener {

    private static final Map<UUID, BuySession> sessions = new HashMap<>();

    private static class BuySession {
        final String category;
        final int slot;
        final Material material;
        final double unitPrice;
        int amount = 1;

        BuySession(String category, int slot, Material material, double unitPrice) {
            this.category = category;
            this.slot = slot;
            this.material = material;
            this.unitPrice = unitPrice;
        }
    }

    public static void openShop(Player player, DonutShop plugin) {
        String title = plugin.getConfig().getString("main-menu.title", "&6Donut Shop");
        int size = plugin.getConfig().getInt("main-menu.size", 27);
        Inventory inv = Bukkit.createInventory(null, size, title);

        ConfigurationSection itemsSection = plugin.getConfig().getConfigurationSection("main-menu.items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                try {
                    int slot = Integer.parseInt(key);
                    String matName = itemsSection.getString(key + ".material");
                    String name = itemsSection.getString(key + ".name");
                    String lore = itemsSection.getString(key + ".lore");

                    if (matName != null) {
                        Material material = Material.valueOf(matName.toUpperCase());
                        inv.setItem(slot, createItem(material, name, lore));
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid shop config for slot: " + key);
                }
            }
        }

        player.openInventory(inv);
    }

    public static void openCategory(Player player, DonutShop plugin, String category) {
        String path = "categories." + category;
        String title = plugin.getConfig().getString(path + ".title");
        int size = plugin.getConfig().getInt(path + ".size", 27);

        if (title == null) return;

        Inventory inv = Bukkit.createInventory(null, size, title);
        ConfigurationSection itemsSection = plugin.getConfig().getConfigurationSection(path + ".items");

        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                int slot = Integer.parseInt(key);
                String matName = itemsSection.getString(key + ".material");
                String name = itemsSection.getString(key + ".name");
                double price = itemsSection.getDouble(key + ".price");

                if (matName != null) {
                    String lore = "§7Buy price: §a$" + price;
                    inv.setItem(slot, createItem(Material.valueOf(matName.toUpperCase()), name, lore));
                }
            }
        }

        inv.setItem(18, createItem(Material.RED_STAINED_GLASS_PANE, "§cBack", "§7Click to return"));

        player.openInventory(inv);
    }

    private static ItemStack createItem(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> loreList = new ArrayList<>();
            loreList.add(lore);
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        DonutShop plugin = (DonutShop) Bukkit.getPluginManager().getPlugin("DonutShop");
        if (plugin == null) return;

        String currentTitle = event.getView().getTitle();
        String mainTitle = plugin.getConfig().getString("main-menu.title", "&6Donut Shop");
        Player player = (Player) event.getWhoClicked();

        if (currentTitle.equals(mainTitle)) {
            event.setCancelled(true);
            int slot = event.getSlot();
            String category = plugin.getConfig().getString("main-menu.items." + slot + ".category");
            if (category != null) {
                openCategory(player, plugin, category);
            }
            return;
        }

        ConfigurationSection categories = plugin.getConfig().getConfigurationSection("categories");
        if (categories != null) {
            for (String catKey : categories.getKeys(false)) {
                if (currentTitle.equals(categories.getString(catKey + ".title"))) {
                    event.setCancelled(true);

                    if (event.getSlot() == 18) {
                        openShop(player, plugin);
                        return;
                    }

                    initiateBuy(player, plugin, catKey, event.getSlot(), event.getCurrentItem());
                    return;
                }
            }
        }

        if (currentTitle.startsWith("§8Buy: ")) {
            event.setCancelled(true);
            if (!sessions.containsKey(player.getUniqueId())) {
                player.closeInventory();
                return;
            }
            handleBuyMenuClick(event, plugin, sessions.get(player.getUniqueId()));
        }
    }

    private void initiateBuy(Player player, DonutShop plugin, String category, int slot, ItemStack clickedItem) {
        String path = "categories." + category + ".items." + slot;
        if (!plugin.getConfig().contains(path)) return;

        double buyPrice = plugin.getConfig().getDouble(path + ".price");
        Material material = clickedItem.getType();

        BuySession session = new BuySession(category, slot, material, buyPrice);
        sessions.put(player.getUniqueId(), session);
        openBuyMenu(player, session);
    }

    private void openBuyMenu(Player player, BuySession session) {
        Inventory inv = Bukkit.createInventory(null, 27, "§8Buy: " + session.material.name());
        updateBuyMenu(inv, session);
        player.openInventory(inv);
    }

    private void updateBuyMenu(Inventory inv, BuySession session) {
        inv.clear();

        ItemStack item = new ItemStack(session.material, Math.min(session.amount, session.material.getMaxStackSize()));
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e" + session.material.name());
            List<String> lore = new ArrayList<>();
            lore.add("§7Amount: §f" + session.amount);
            lore.add("§7Total Price: §a$" + (session.amount * session.unitPrice));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        inv.setItem(13, item);

        inv.setItem(21, createItem(Material.RED_STAINED_GLASS_PANE, "§cCancel", "§7Click to cancel"));
        inv.setItem(23, createItem(Material.LIME_STAINED_GLASS_PANE, "§aConfirm", "§7Click to buy"));

        if (session.material.getMaxStackSize() > 1) {
            inv.setItem(15, createItem(Material.LIME_STAINED_GLASS_PANE, "§aAdd 1", "§7Click to add 1"));
            inv.setItem(16, createItem(Material.LIME_STAINED_GLASS_PANE, "§aAdd 10", "§7Click to add 10", 10));
            inv.setItem(17, createItem(Material.LIME_STAINED_GLASS_PANE, "§aAdd 64", "§7Click to add 64", 64));

            if (session.amount > 1) inv.setItem(11, createItem(Material.RED_STAINED_GLASS_PANE, "§cRemove 1", "§7Click to remove 1"));
            if (session.amount > 10) inv.setItem(10, createItem(Material.RED_STAINED_GLASS_PANE, "§cRemove 10", "§7Click to remove 10", 10));
            if (session.amount > 64) inv.setItem(9, createItem(Material.RED_STAINED_GLASS_PANE, "§cRemove 64", "§7Click to remove 64", 64));
        }
    }

    private ItemStack createItem(Material mat, String name, String lore, int amount) {
        ItemStack item = createItem(mat, name, lore);
        item.setAmount(Math.min(amount, 64));
        return item;
    }

    private void handleBuyMenuClick(InventoryClickEvent event, DonutShop plugin, BuySession session) {
        int slot = event.getSlot();
        Player player = (Player) event.getWhoClicked();

        if (slot == 21) {
            openCategory(player, plugin, session.category);
            sessions.remove(player.getUniqueId());
            return;
        }

        if (slot == 23) {
            processPurchase(player, session);
            return;
        }

        if (session.material.getMaxStackSize() > 1) {
            if (slot == 15) session.amount += 1;
            else if (slot == 16) session.amount += 10;
            else if (slot == 17) session.amount += 64;
            else if (slot == 11 && session.amount > 1) session.amount -= 1;
            else if (slot == 10 && session.amount > 10) session.amount -= 10;
            else if (slot == 9 && session.amount > 64) session.amount -= 64;

            updateBuyMenu(event.getInventory(), session);
        }
    }

    private void processPurchase(Player player, BuySession session) {
        double totalPrice = session.amount * session.unitPrice;

        if (DonutShop.getEconomy() == null) {
            player.sendMessage("§cEconomy not available!");
            return;
        }
        if (DonutShop.getEconomy().getBalance(player) < totalPrice) {
            player.sendMessage("§cInsufficient funds! Need $" + totalPrice);
            return;
        }

        DonutShop.getEconomy().withdrawPlayer(player, totalPrice);
        ItemStack toGive = new ItemStack(session.material, session.amount);
        player.getInventory().addItem(toGive);
        player.sendMessage("§aBought " + session.amount + "x " + session.material.name().toLowerCase().replace("_", " ") + " for $" + totalPrice);
    }
}
