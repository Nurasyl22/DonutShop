package de.donutshop;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AdminShopCommand implements CommandExecutor {

    private final DonutShop plugin;

    public AdminShopCommand(DonutShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cБұл команданы тек ойыншылар ғана қолдана алады!");
            return true;
        }

        Player player = (Player) sender;
        if (!player.isOp()) {
            player.sendMessage("§cСізде бұл команданы қолдануға рұқсат жоқ!");
            return true;
        }

        new AdminShopGUI(plugin).openSelector(player);
        return true;
    }
}