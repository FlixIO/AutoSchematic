package io.flixion.main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.massivecraft.factions.struct.Relation;

import io.flixion.schematic.ExploitProtection;
import io.flixion.schematic.PrintHandler;
import net.milkbowl.vault.economy.Economy;

public class AutoMain extends JavaPlugin {
	public static Economy econ;
	private static AutoMain instance;
	private YamlConfiguration settings;
	public static LangManager langManager = new LangManager();
	private PrintHandler ph;
	private ExploitProtection ep;
	
	public void onEnable() {
		instance = this;
		saveResource("config.yml", false);
		settings = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
		initLangHash();
		
		if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
			setupEconomy();
			Utils.logConsoleMessage("Plugin Hook: Vault - Successful");
		}
		else {
			Utils.logConsoleError("Vault Dependancy not found! Disabling plugin");
			Bukkit.getPluginManager().disablePlugin(this);
		}
		
		ArrayList<Relation> allowedRelations = new ArrayList<>();
		for (String s : settings.getStringList("settings.allowedRelations")) {
			try {
				allowedRelations.add(Relation.valueOf(s));
			} catch (IllegalArgumentException ex2) {
				Utils.logConsoleError("Configuration File Import Enum Error! - " + ex2.getMessage());
				Bukkit.getPluginManager().disablePlugin(this);
				return;
			}
		}
		
		if (settings.getBoolean("settings.useEssentialsWorth")) {
			if (Bukkit.getPluginManager().getPlugin("Essentials") != null) {
				setupEconomy();
				Utils.logConsoleMessage("Plugin Hook: Essentials - Successful");
				ph = new PrintHandler((ArrayList<String>) settings.getStringList("settings.allowedCommands"), true, settings.getInt("settings.nearbyPlayerSearchRadius"), allowedRelations);
				ep = new ExploitProtection(ph);
			}
			else {
				Utils.logConsoleError("Essentials Dependancy not found! Disabling plugin");
				Bukkit.getPluginManager().disablePlugin(this);
				return;
			}
		}
		else {
			HashMap<Material, Double> itemPrices = new HashMap<>();
			for (String s : settings.getStringList("settings.itemPrices")) {
				try {
					itemPrices.put(Material.valueOf(s.split("#")[0]), Double.parseDouble(s.split("#")[1]));
				} catch (IllegalArgumentException ex2) {
					Utils.logConsoleError("Configuration File Import Enum Error! - " + ex2.getMessage());
					Bukkit.getPluginManager().disablePlugin(this);
					return;
				}
			}
			ph = new PrintHandler((ArrayList<String>) settings.getStringList("settings.allowedCommands"), itemPrices, false, settings.getInt("settings.nearbyPlayerSearchRadius"), allowedRelations);
			ep = new ExploitProtection(ph);
		}
		Bukkit.getPluginManager().registerEvents(ph, this);
		Bukkit.getPluginManager().registerEvents(ep, this);
		getCommand("printer").setExecutor(ph);
	}
	
	private void initLangHash() {
		langManager.getLangHash().put("PRINTER_ENABLED", Utils.cc(settings.getString("lang.PRINTER_ENABLED")));
		langManager.getLangHash().put("PRINTER_DISABLED", Utils.cc(settings.getString("lang.PRINTER_DISABLED")));
		langManager.getLangHash().put("PRINTER_BAD_ACTION", Utils.cc(settings.getString("lang.PRINTER_BAD_ACTION")));
		langManager.getLangHash().put("PRINTER_STOP_TERRITORY", Utils.cc(settings.getString("lang.PRINTER_STOP_TERRITORY")));
		langManager.getLangHash().put("PRINTER_STOP_NEARBY_PLAYER", Utils.cc(settings.getString("lang.PRINTER_STOP_NEARBY_PLAYER")));
		langManager.getLangHash().put("PRINTER_PURCHASE_TEXT", Utils.cc(settings.getString("lang.PRINTER_PURCHASE_TEXT")));
		langManager.getLangHash().put("PRINTER_COMMAND_NOT_ALLOWED", Utils.cc(settings.getString("lang.PRINTER_COMMAND_NOT_ALLOWED")));
	}
	
	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			econ = economyProvider.getProvider();
		}
		return (econ != null);
	}

	public static AutoMain getInstance() {
		return instance;
	}
}
