package de.donutshop;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AdminShopGUI implements Listener {

    private final DonutShop plugin;
    private static final Map<UUID, String> editingCategory = new HashMap<>();
    private static final Map<UUID, PendingItem> pendingItems = new HashMap<>();

    public AdminShopGUI(DonutShop plugin) {
        this.plugin = plugin;
    }

    private static class PendingItem {
        final String category;
        final ItemStack item;

        PendingItem(String category, ItemStack item) {
            this.category = category;
            this.item = item;
        }
    }

    public void openSelector(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "§cӘкімші: Категорияны таңдаңыз");
        ConfigurationSection categories = plugin.getConfig().getConfigurationSection("main-menu.items");

        if (categories != null) {
            for (String key : categories.getKeys(false)) {
                String catKey = categories.getString(key + ".category");
                String name = categories.getString(key + ".name");
                String matName = categories.getString(key + ".material");

                if (catKey != null && matName != null) {
                    try {
                        ItemStack item = new ItemStack(Material.valueOf(matName));
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName(name);
                            item.setItemMeta(meta);
                        }
                        inv.setItem(Integer.parseInt(key), item);
                    } catch (IllegalArgumentException e) {
                    }
                }
            }
        }
        player.openInventory(inv);
    }

    public void openEditor(Player player, String category) {
        String title = "§cТүзету: " + category;
        Inventory inv = Bukkit.createInventory(null, 27, title);

        ConfigurationSection items = plugin.getConfig().getConfigurationSection("categories." + category + ".items");
        if (items != null) {
            for (String key : items.getKeys(false)) {
                try {
                    int slot = Integer.parseInt(key);
                    String matName = items.getString(key + ".material");
                    double price = items.getDouble(key + ".price");

                    if (matName != null) {
                        ItemStack item = new ItemStack(Material.valueOf(matName));
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName("§e" + matName);
                            meta.setLore(java.util.List.of("§7Бағасы: §a$" + price, "§cАлып тастау үшін оң жақ батырманы басыңыз"));
                            item.setItemMeta(meta);
                        }
                        inv.setItem(slot, item);
                    }
                } catch (Exception e) {
                }
            }
        }

        editingCategory.put(player.getUniqueId(), category);
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        if (title.equals("§cӘкімші: Категорияны таңдаңыз")) {
            event.setCancelled(true);
            if (event.getCurrentItem() == null) return;

            int slot = event.getSlot();
            String category = plugin.getConfig().getString("main-menu.items." + slot + ".category");
            if (category != null) {
                openEditor(player, category);
            }
            return;
        }

        if (editingCategory.containsKey(player.getUniqueId()) && title.startsWith("§cТүзету: ")) {
            String category = editingCategory.get(player.getUniqueId());

            if (event.getClickedInventory() == event.getView().getTopInventory()) {
                event.setCancelled(true);
                if (event.isRightClick() && event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
                    int slot = event.getSlot();
                    plugin.getConfig().set("categories." + category + ".items." + slot, null);
                    plugin.saveConfig();
                    player.sendMessage("§cЗат алынып тасталды!");
                    openEditor(player, category);
                }
            }
            else if (event.getClickedInventory() == event.getView().getBottomInventory()) {
                ItemStack clicked = event.getCurrentItem();
                if (clicked != null && clicked.getType() != Material.AIR) {
                    event.setCancelled(true);
                    player.closeInventory();

                    pendingItems.put(player.getUniqueId(), new PendingItem(category, clicked));
                    player.sendMessage("§aБұл заттың бағасын чатқа енгізіңіз:");
                }
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (pendingItems.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            PendingItem pending = pendingItems.remove(player.getUniqueId());

            try {
                double price = Double.parseDouble(event.getMessage());
                addItemToConfig(pending.category, pending.item, price);
                player.sendMessage("§aЗат сәтті қосылды!");

                Bukkit.getScheduler().runTask(plugin, () -> openEditor(player, pending.category));

            } catch (NumberFormatException e) {
                player.sendMessage("§cЖарамсыз сан! Зат қосу тоқтатылды.");
            }
        }
    }

    private void addItemToConfig(String category, ItemStack item, double price) {
        ConfigurationSection items = plugin.getConfig().getConfigurationSection("categories." + category + ".items");
        int slot = 0;

        for (int i = 0; i < 27; i++) {
            if (i == 18) continue;
            if (items == null || !items.contains(String.valueOf(i))) {
                slot = i;
                break;
            }
        }

        String path = "categories." + category + ".items." + slot;
        plugin.getConfig().set(path + ".material", item.getType().name());
        plugin.getConfig().set(path + ".name", item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : "&e" + item.getType().name());
        plugin.getConfig().set(path + ".price", price);
        plugin.saveConfig();
    }
}