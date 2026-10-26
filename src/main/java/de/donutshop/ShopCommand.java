package de.donutshop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {

    private final DonutShop plugin;

    public ShopCommand(DonutShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.isOp()) {
                sender.sendMessage("§cСізде бұл команданы қолдануға рұқсат жоқ!");
                return true;
            }
            plugin.reloadConfig();
            sender.sendMessage("§aКонфигурация қайта жүктелді!");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cБұл команданы тек ойыншылар ғана қолдана алады!");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("donutshop.use")) {
            player.sendMessage("§cСізде бұл команданы қолдануға рұқсат жоқ!");
            return true;
        }

        ShopGUI.openShop(player, plugin);
        return true;
    }
}