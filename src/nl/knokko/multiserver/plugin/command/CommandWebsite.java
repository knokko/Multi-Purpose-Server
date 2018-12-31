package nl.knokko.multiserver.plugin.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import nl.knokko.multiserver.plugin.ServerPlugin;
import nl.knokko.multiserver.rayzr.JSONMessage;
import nl.knokko.multiserver.rayzr.JSONMessage.ClickEvent;
import nl.knokko.multiserver.rayzr.JSONMessage.HoverEvent;
import nl.knokko.multiserver.rayzr.JSONMessage.MessagePart;

public class CommandWebsite implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			if (ServerPlugin.getHTTPHandler() != null) {
				String address = "http://" + Bukkit.getIp() + ":" + Bukkit.getPort();
				if (sender instanceof Player) {
					JSONMessage message = JSONMessage.create("The website address is ");
					MessagePart link = message.new MessagePart(address);
					link.setColor(ChatColor.BLUE);
					JSONMessage hoverMessage = JSONMessage.create("Visit the website");
					hoverMessage.color(ChatColor.YELLOW);
					link.setOnHover(HoverEvent.showText(hoverMessage));
					link.setOnClick(ClickEvent.openURL(address));
					message.then(link);
					message.send((Player) sender);
				} else {
					sender.sendMessage("The website address is " + address);
				}
			} else {
				sender.sendMessage(ChatColor.YELLOW + "This server doesn't have a website?");
			}
		} else {
			if (args[0].equals("reload")) {
				if (sender.hasPermission("multipurposeserver.website.reload")) {
					ServerPlugin plugin = ServerPlugin.getInstance();
					plugin.reloadConfig();
					plugin.readWebsiteConfig(plugin.getConfig());
					sender.sendMessage(ChatColor.GREEN + "The website has been refreshed.");
				} else {
					sender.sendMessage(ChatColor.DARK_RED + "You do not have access to this command.");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "You should use /website [reload]");
			}
		}
		return true;
	}
}