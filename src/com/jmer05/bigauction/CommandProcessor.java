package com.jmer05.bigauction;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandProcessor {
	@SuppressWarnings("deprecation")
	public boolean process(Main main, CommandSender sender, Command cmd, String label, String[] args) {
		
		if(cmd.getName().equalsIgnoreCase("auc") || cmd.getName().equalsIgnoreCase("auction")){
			
			// If no args
			if(args.length == 0) {
				
				// if no running auctions
				if(main.RunningAuction == null) {
					sender.sendMessage("§3There are currently no running auctions. Use §b/auc help §3for a list of commands.");
					return true;
				}
				
				String topbidder = "Nobody";
				if(main.RunningAuction.topbid.bidder != null) topbidder = main.RunningAuction.topbid.bidder.getDisplayName();
				
				sender.sendMessage("§bCurrent Auction:");
				sender.sendMessage("§3Item: §b" + main.fstack(main.RunningAuction.item.item));
				sender.sendMessage("§3Winner: §b" + topbidder);
				sender.sendMessage("§3Time remaining: §b" + main.ftime(main.RunningAuction.timeleft));
				sender.sendMessage("§3Queue: §b" + main.AuctionQueue.size() + " items §3in queue: do §b/auc queue§3.");
				
				return true;
	
			}
			
			// If help
			else if(args[0].equalsIgnoreCase("help")) {
				
				sender.sendMessage("§3Showing Auction Help");
				sender.sendMessage("§b/auc §3shows information on the running auction");
				sender.sendMessage("§b/auc cancel §3Removes your auctions from the queue" + (sender instanceof Player ? "":"§c Not available in the console"));
				sender.sendMessage("§b/auc hand §a[Starting Bid] §3Auctions off whats in your hand" + (sender instanceof Player ? "":"§c Not available in the console"));
				sender.sendMessage("§b/auc shorten §3Sets remaining auction time to §b" + main.ftime(main.getConfig().getInt("shorten")));
				sender.sendMessage("§b/auc preview §3Shows you what the item looks like" + (sender instanceof Player ? "":"§c Not available in the console"));
				sender.sendMessage("§b/auc queue §3Shows you the list of queued auction owners");
				if(sender.hasPermission("bigauction.stop") || sender.hasPermission("bigauction.admin")) sender.sendMessage("§b/auc stop §3Stops the running auction");
				sender.sendMessage("§b/bid §a[Amount] §3bid on the running auction"  + (sender instanceof Player ? "":"§c Not available in the console"));
				
				return true;
			}
			
			// If cancel
			else if(args[0].equalsIgnoreCase("cancel")) {
				
				if(!(sender instanceof Player)) {
					sender.sendMessage("§cOnly in-game players un-queue their auctions.");
					return true;
				}
				
				Player player = (Player) sender;
				
				int index = 0;
				try {
					for(AuctionItem item : main.AuctionQueue) {
						if(item.owner == player) {
							player.getInventory().addItem(item.item);
							main.AuctionQueue.remove(index);
						}
						index++;
					}
					
					if(index > 0) {
						player.sendMessage("§3Canceled all queued auctions.");
					} else {
						player.sendMessage("§cNo queued auctions to cancel.");
					}
					
				} catch (Exception e) {
					// TODO:
				}
				
				return true;
			}
			
			// If hand
			else if(args[0].equalsIgnoreCase("hand")) {
				
				if(!(sender instanceof Player)) {
					sender.sendMessage("§cOnly in-game players can start auctions.");
					return true;
				}
				
				AuctionItem sell = new AuctionItem();
				try {
					sell.price = Double.parseDouble(args[1]);
				} catch(Exception e) {
					sell.price = main.getConfig().getDouble("price");
				}
				
				Player player = (Player) sender;
				
				int index = 0;
				for(AuctionItem auction : main.AuctionQueue) {
					if(auction.owner == player) {
						index++;
					}
				}
				
				if(index >= main.getConfig().getInt("max-queue")) {
					sender.sendMessage("§cYou already have " + main.getConfig().getInt("max-queue") + " item(s) queued.");
					return true;
				}
				
				if(player.getInventory().getItemInMainHand().getType() == Material.AIR){
					sender.sendMessage("§cYou need to hold an item in your hand.");
					return true;
				}
				
				sell.item = player.getInventory().getItemInMainHand();
				sell.owner = player;
				
				main.AuctionQueue.add(sell);
				sender.sendMessage("§3Your auction of §b" + main.fstack(sell.item) + "§3 for $§b" + main.fprice(sell.price) + "§3 will begin soon." + (sender.hasPermission("bigauction.cancel") ? " You can cancel at anytime with §b/auc cancel§3.":""));
				
				player.getInventory().removeItem(player.getInventory().getItemInHand());
				
				return true;
			}
			
			// If shorten
			else if(args[0].equalsIgnoreCase("shorten")) {
				
				Player player = (Player) sender;
				
				if(!(sender.hasPermission("bigauction.admin") || main.RunningAuction.owner == player)) {
					sender.sendMessage("§cYou do not have permission to shorten this auction.");
					return true;
				}
				
				String name = sender.getName();
				if(sender instanceof Player) {
					name = player.getDisplayName();
				}
				
				int duration = main.getConfig().getInt("shorten");
				if(main.RunningAuction.timeleft < duration) {
					sender.sendMessage("§cAuction is shorter than §e" + main.ftime(duration) + "§c.");
					return true;
				}
				main.RunningAuction.timeleft = duration;
				Bukkit.broadcastMessage("§b" + name + "§3 shortened this auction to §b" + main.ftime(duration) + "§3.");
				return true;
			}
			
			// If queue
			else if(args[0].equalsIgnoreCase("queue")) {
				
				if(sender instanceof Player) {
					Player player = (Player) sender;
					try {
						
						sender.sendMessage("§3Showing auction queue");
						
						if(main.AuctionIsRunning) {
							sender.sendMessage("§b[Running] §3" + main.RunningAuction.owner.getDisplayName());
						}
						int index = 0;
						for(AuctionItem item : main.AuctionQueue) {
							if(item.owner == player) {
								sender.sendMessage("§e[Queued] §a" + main.AuctionQueue.get(index).owner.getDisplayName());
							} else {
								sender.sendMessage("§e[Queued] §2" + main.AuctionQueue.get(index).owner.getDisplayName());
							}
							
							index ++;
						}
					} catch (Exception e) {
						// TODO:
					}
				} else {
					try {
						
						sender.sendMessage("§3Showing auction queue");
						
						if(main.AuctionIsRunning) {
							sender.sendMessage("§b[Running] §3" + main.RunningAuction.owner.getDisplayName());
						}
						int index = 0;
						for(@SuppressWarnings("unused") AuctionItem item : main.AuctionQueue) {
							sender.sendMessage("§e[Queued] §2" + main.AuctionQueue.get(index).owner.getDisplayName());
			
							index ++;
						}
					} catch (Exception e) {
						// TODO:
					}
				}
				
				return true;
				
			}
			
			// If stop
			else if(args[0].equalsIgnoreCase("stop")) {
				
				try {
					if(!sender.hasPermission("bigauction.stop")) {
						sender.sendMessage("§cYou do not have permission to stop auctions.");
						return true;
					}
					
					String name = sender.getName();
					if(sender instanceof Player) {
						name = ((Player) sender).getDisplayName();
					}
					
					main.stopAuction();
					Bukkit.broadcastMessage("§b" + name + "§3 stopped this auction.");
				} catch(Exception e) {
					sender.sendMessage("§cNo running auctions to stop.");
				}
				
				return true;
				
			}
			
			// If preview
			else if(args[0].equalsIgnoreCase("preview")) {
				
				if(!(sender instanceof Player)) {
					sender.sendMessage("§cOnly in-game players can preview auctions.");
					return true;
				}
				
				Player player = (Player) sender;
				main.viewer = Bukkit.createInventory(null, 27, "Auction Preview");
				main.viewer.setItem(13, main.RunningAuction.item.item);
				
				player.openInventory(main.viewer);
				
				return true;
			}
			
			else {
				sender.sendMessage("§3Unknown command: use §c/auc help §afor a list of commands.");
				return true;
			}
			
			

		} else if(cmd.getName().equalsIgnoreCase("bid")){
			if(!(sender instanceof Player)) {
				sender.sendMessage("§cOnly in-game players can bid on auctions.");
				return true;
			}
			
			Player player = (Player) sender;
			
			double bid;
			try {
				bid = Double.parseDouble(args[0]);
			} catch(Exception e) {
				bid = main.RunningAuction.topbid.amount + main.getConfig().getDouble("increment");
			}
			
			if(bid < main.RunningAuction.topbid.amount + main.getConfig().getDouble("increment")) {
				sender.sendMessage("§cThe minimum has been raised to §e$" + main.fprice(main.RunningAuction.topbid.amount + main.getConfig().getDouble("increment")) + "§c.");
				return true;
			}
			
			if(main.eco.has(player, bid)) {
				
				try {
					main.RunningAuction.topbid.bidder.sendMessage("§e" + player.getDisplayName() + "§c just bid higher than you by §e$" + main.fprice(bid - main.RunningAuction.topbid.amount) + "§c.");
				} catch (Exception e) {
					// TODO:
				}
				
				if(main.RunningAuction.owner == player) {
					sender.sendMessage("§cYou can not bid on your own auction.");
					return true;
				}
				
				if(main.RunningAuction.topbid.bidder == player) {
					sender.sendMessage("§cYou are already winning this auction.");
					return true;
				}
				
				Bukkit.broadcastMessage("§b" + player.getDisplayName() + "§3 bid on §b" + main.fstack(main.RunningAuction.item.item) + " §3for §b$" + main.fprice(bid) + "§3.");
				
				AuctionBid instance = new AuctionBid();
				instance.amount = bid;
				instance.bidder = player;
				main.RunningAuction.topbid = instance;
				
			} else {
				sender.sendMessage("§cYou can not afford to bid on this auction.");
				return true;
			}
			
			return true;
			
		}

		return false;
	}
}
