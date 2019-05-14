package io.flixion.main;
import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;

public class Utils {
	public static boolean notNull(Object o) {
		if (o != null) {
			return true;
		}
		return false;
	}
	
	public static void logConsoleError(String message) {
		Bukkit.getServer().getConsoleSender().sendMessage(
				Utils.cc("&4-------------------------------------------------------------------------------------"));
		Bukkit.getServer().getConsoleSender().sendMessage(Utils.cc("&4[ASchem ERROR] " + message));
		Bukkit.getServer().getConsoleSender().sendMessage(
				Utils.cc("&4-------------------------------------------------------------------------------------"));
	}

	public static void logConsoleMessage(String message) {
		Bukkit.getServer().getLogger().log(Level.INFO,
				"-------------------------------------------------------------------------------------");
		Bukkit.getServer().getLogger().log(Level.INFO, "[ASchem] " + message);
		Bukkit.getServer().getLogger().log(Level.INFO,
				"-------------------------------------------------------------------------------------");
	}
	
	public static String cc (String m) {
		return ChatColor.translateAlternateColorCodes('&', m);
	}
	
	public static ItemStack createItem(Material m, String displayName, ArrayList<String> lore, short durability, int amount) {
		ItemStack i = new ItemStack(m, amount);
		ItemMeta meta = i.getItemMeta();
		meta.setDisplayName(cc(displayName));
		meta.setLore(lore);
		i.setDurability(durability);
		i.setItemMeta(meta);
		return i;
	}
}
