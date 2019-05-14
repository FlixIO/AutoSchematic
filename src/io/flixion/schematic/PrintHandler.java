package io.flixion.schematic;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import com.earth2me.essentials.Worth;
import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.Relation;

import io.flixion.main.AutoMain;
import io.flixion.main.MaterialEnumUtil;
import net.milkbowl.vault.economy.EconomyResponse;

public class PrintHandler implements Listener, CommandExecutor {
	private HashMap<UUID, BukkitTask> usingPrinter = new HashMap<>();
	private HashMap<UUID, ArrayList<ItemStack []>> savedInventories = new HashMap<>();
	private ArrayList<Relation> allowedRelations = new ArrayList<>();
	private ArrayList<String> allowedCommands = new ArrayList<>();
	private HashMap<Material, Double> itemPrices = new HashMap<>();
	private Worth w;
	private int searchRadius;
	private boolean usingEssentialsWorth;

	public PrintHandler(ArrayList<String> allowedCommands, HashMap<Material, Double> itemPrices,
			boolean usingEssentialsWorth, int searchRadius, ArrayList<Relation> allowedRelations) {
		this.allowedCommands = allowedCommands;
		this.itemPrices = itemPrices;
		this.usingEssentialsWorth = usingEssentialsWorth;
		this.searchRadius = searchRadius;
		this.allowedRelations = allowedRelations;
	}

	public PrintHandler(ArrayList<String> allowedCommands, boolean usingEssentialsWorth, int searchRadius, ArrayList<Relation> allowedRelations) {
		this.usingEssentialsWorth = usingEssentialsWorth;
		this.allowedCommands = allowedCommands;
		this.searchRadius = searchRadius;
		this.allowedRelations = allowedRelations;
		if (usingEssentialsWorth) {
			File f = new File(Bukkit.getPluginManager().getPlugin("Essentials").getDataFolder().getPath());
			if (f.exists()) {
				w = new Worth(f);
			}
		}
	}
	
	public boolean onCommand (CommandSender sender, Command cmd, String label, String [] args) {
		if (cmd.getName().equalsIgnoreCase("printer")) {
			if (sender instanceof Player) {
				if (sender.hasPermission("aschem.printer")) {
					Player p = (Player) sender;
					if (!isUsingPrinter(p.getUniqueId())) {
						boolean canUsePrinter = true;
						if (!Board.getInstance().getFactionAt(new FLocation(p)).getId().equals(FPlayers.getInstance().getByPlayer(p).getFactionId()) && !Board.getInstance().getFactionAt(new FLocation(p)).isWilderness() && !allowedRelations.contains(Board.getInstance().getFactionAt(new FLocation(p)).getRelationTo(FPlayers.getInstance().getByPlayer(p).getFaction()))) {
							canUsePrinter = false;
							p.sendMessage(AutoMain.langManager.getLangHash().get("PRINTER_STOP_TERRITORY"));
						}
						else {
							for (Entity entity : p.getNearbyEntities(searchRadius, searchRadius, searchRadius)) {
								if (entity instanceof Player) {
									if (!allowedRelations.contains(FPlayers.getInstance().getByPlayer((Player) entity).getRelationTo(FPlayers.getInstance().getByPlayer(p)))) {
										canUsePrinter = false;
										p.sendMessage(AutoMain.langManager.getLangHash().get("PRINTER_STOP_NEARBY_PLAYER"));
										break;
									}
								}
							}
						}
						if (canUsePrinter) {
							BukkitTask task = Bukkit.getScheduler().runTaskTimer(AutoMain.getInstance(), new Runnable() {
								
								@Override
								public void run() {
									if (!Board.getInstance().getFactionAt(new FLocation(p)).getId().equals(FPlayers.getInstance().getByPlayer(p).getFactionId()) && !Board.getInstance().getFactionAt(new FLocation(p)).isWilderness() && !allowedRelations.contains(Board.getInstance().getFactionAt(new FLocation(p)).getRelationTo(FPlayers.getInstance().getByPlayer(p).getFaction()))) {
										p.sendMessage(AutoMain.langManager.getLangHash().get("PRINTER_STOP_TERRITORY"));
										usingPrinter.get(p.getUniqueId()).cancel();
										p.setGameMode(GameMode.SURVIVAL);
										p.sendMessage(AutoMain.langManager.getLangHash().get("PRINTER_DISABLED"));
									}
									else {
										for (Entity entity : p.getNearbyEntities(searchRadius, searchRadius, searchRadius)) {
											if (entity instanceof Player) {
												if (!allowedRelations.contains(FPlayers.getInstance().getByPlayer((Player) entity).getRelationTo(FPlayers.getInstance().getByPlayer(p)))) {
													p.sendMessage(AutoMain.langManager.getLangHash().get("PRINTER_STOP_NEARBY_PLAYER"));
													usingPrinter.get(p.getUniqueId()).cancel();
													p.setGameMode(GameMode.SURVIVAL);
													p.sendMessage(AutoMain.langManager.getLangHash().get("PRINTER_DISABLED"));
													break;
												}
											}
										}
									}
								}
							}, 0, 20);
							usingPrinter.put(p.getUniqueId(), task);
							ArrayList<ItemStack []> inv = new ArrayList<>();
							inv.add(p.getInventory().getContents());
							inv.add(p.getInventory().getArmorContents());
							savedInventories.put(p.getUniqueId(), inv);
							p.getInventory().clear();
							p.getInventory().setArmorContents(null);
							p.setGameMode(GameMode.CREATIVE);
							p.sendMessage(AutoMain.langManager.getLangHash().get("PRINTER_ENABLED"));
						}
					}
					else {
						usingPrinter.get(p.getUniqueId()).cancel();
						usingPrinter.remove(p.getUniqueId());
						p.setGameMode(GameMode.SURVIVAL);
						p.getInventory().clear();
						p.getInventory().setContents(savedInventories.get(p.getUniqueId()).get(0));
						p.getInventory().setArmorContents(savedInventories.get(p.getUniqueId()).get(1));
						p.sendMessage(AutoMain.langManager.getLangHash().get("PRINTER_DISABLED"));
					}
				}
			}
			return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	@EventHandler
	public void placeUsingPrinter (BlockPlaceEvent e) {
		if (!e.isCancelled()) {
			if (isUsingPrinter(e.getPlayer().getUniqueId())) {
				if (usingEssentialsWorth) {
					if (w.getPrice(e.getPlayer().getItemInHand()) != null) {
						EconomyResponse r = AutoMain.econ.withdrawPlayer(e.getPlayer().getName(), w.getPrice(e.getPlayer().getItemInHand()).doubleValue());
						if (!r.transactionSuccess()) {
							e.setCancelled(true);
						}
						else {
							e.getPlayer().sendMessage(AutoMain.langManager.getLangHash().get("PRINTER_PURCHASE_TEXT").replaceAll("%amount%", r.amount + "").replaceAll("%item%", MaterialEnumUtil.valueOf(e.getBlock().getType().toString()).firstAllUpperCased()));
						}
					}
					else if (!e.getPlayer().hasPermission("aschem.printer.bypass")) {
						e.setCancelled(true);
					}
				}
				else {
					if (itemPrices.containsKey(e.getPlayer().getItemInHand().getType())) {
						EconomyResponse r = AutoMain.econ.withdrawPlayer(e.getPlayer().getName(), itemPrices.get(e.getPlayer().getItemInHand().getType()));
						if (!r.transactionSuccess()) {
							e.setCancelled(true);
						}
						else {
							e.getPlayer().sendMessage(AutoMain.langManager.getLangHash().get("PRINTER_PURCHASE_TEXT").replaceAll("{amount}", r.amount + "").replaceAll("{item}", MaterialEnumUtil.valueOf(e.getBlock().getType().toString()).firstAllUpperCased()));
						}
					}
					else if (!e.getPlayer().hasPermission("aschem.printer.bypass")) {
						e.setCancelled(true);
					}
				}
			}
		}
	}

	protected boolean isUsingPrinter(UUID u) {
		if (usingPrinter.containsKey(u)) {
			return true;
		} else
			return false;
	}

	public HashMap<UUID, BukkitTask> getUsingPrinter() {
		return usingPrinter;
	}

	public ArrayList<String> getAllowedCommands() {
		return allowedCommands;
	}

	public HashMap<UUID, ArrayList<ItemStack[]>> getSavedInventories() {
		return savedInventories;
	}
}
