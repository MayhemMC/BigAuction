package com.jmer05.bigauction;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin implements Listener {

	public ArrayList<AuctionItem> AuctionQueue = new ArrayList<AuctionItem>();
	public RunningAuction RunningAuction = null;
	public boolean AuctionIsRunning = false;
	public Inventory viewer = null;
	
	private ConsoleCommandSender console = getServer().getConsoleSender();
	private String brand(String msg) {
		return ChatColor.DARK_AQUA + "[" + ChatColor.AQUA + "BigAuction" + ChatColor.DARK_AQUA + "] " + ChatColor.RESET + msg;
	};

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		
		if (!setupEconomy()) {
            console.sendMessage(brand(ChatColor.RED + "Fatel: " + ChatColor.DARK_RED + "No Vault dependency found."));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
		
		this.saveDefaultConfig();
		console.sendMessage(brand("Enabled"));
		
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
		    public void run() {
				if(AuctionQueue.size() > 0) {
					if(AuctionIsRunning == false) {
					
						AuctionIsRunning = true;
						
						AuctionItem auction = AuctionQueue.get(0);
						AuctionQueue.remove(0);
						
						RunningAuction = new RunningAuction();
						RunningAuction.item = auction;
						RunningAuction.timeleft = getConfig().getInt("duration");
						RunningAuction.owner = auction.owner;
						RunningAuction.topbid.amount = auction.price;
						
						getServer().broadcastMessage("§b" + auction.owner.getDisplayName() + " §3is auctioning §b" + fstack(auction.item) + "§3 for $§b" + fprice(auction.price) + "§3.");
						
					}
				}
				
				if(AuctionIsRunning == true){
					
					RunningAuction.timeleft--;
					if(RunningAuction.timeleft == 0) {
						stopAuction();
					}
					
					try {
						List<Integer> reminders = getConfig().getIntegerList("reminders");
						for(double time : reminders) {
							if(time == RunningAuction.timeleft) {
								String name = "Nobody";
								if(RunningAuction.topbid.bidder != null) name = RunningAuction.topbid.bidder.getDisplayName();
								
								double price = getConfig().getDouble("price");
								if(RunningAuction.topbid.amount != 0) price = RunningAuction.topbid.amount;
								getServer().broadcastMessage("§b" + name + " §3wins §b" + fstack(RunningAuction.item.item) + "§3 for $§b" + fprice(price) + "§3 in §b" + ftime(RunningAuction.timeleft) + "§3!");
							}
						}
					} catch(Exception e) {
						// TODO:
					}
					
				}
		    }
		}, 0, 1000);
		
	}
	
	protected void stopAuction() {

		AuctionIsRunning = false;
		
		String name = "Nobody";
		Player winner = RunningAuction.owner;
		if(RunningAuction.topbid.bidder != null) name = RunningAuction.topbid.bidder.getDisplayName();
		if(name != "Nobody") winner = RunningAuction.topbid.bidder;
		
		double price = getConfig().getDouble("price");
		if(RunningAuction.topbid.amount != 0) price = RunningAuction.topbid.amount;
		
		winner.getInventory().addItem(RunningAuction.item.item);
		eco.withdrawPlayer(winner, price);
		eco.depositPlayer(RunningAuction.owner, price);
		
		getServer().broadcastMessage("§b" + name + " §3won §b" + fstack(RunningAuction.item.item) + "§3 for $§b" + fprice(price) + "§3!");
		
		RunningAuction = null;
		
	}

	public Economy eco = null;
	private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        
        if (rsp == null) return false;
        eco = rsp.getProvider();
        
        return eco != null;
    }

	@Override
	public void onDisable() {
		AuctionQueue.clear();
		RunningAuction = null;
		console.sendMessage(brand("Disabled"));
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		Inventory inventory = event.getInventory();
		try {
			if (inventory.getName().equals(viewer.getName())) {
				event.setCancelled(true);
			}
		} catch(Exception e) {
			// TODO:
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		return new CommandProcessor().process(this, sender, cmd, label, args);
	}

	public String ftime(int seconds) {
		int minutes = (int) Math.floor(seconds/60);
		String mins = String.format("%02d", minutes);
		String secs = String.format("%02d", seconds - minutes*60);
		return mins + ":" + secs;
	}
	
	public String fprice(double price) {
		return String.format("%.2f",price);
	}
	
	@SuppressWarnings("deprecation")
	public String fstack(ItemStack material) {
		return material.getAmount() + "§3x§b" + material.getData().toString();
	}
	
}
