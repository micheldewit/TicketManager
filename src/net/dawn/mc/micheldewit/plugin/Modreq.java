// Package Declaration
package net.dawn.mc.micheldewit.plugin;

// Basic Java Utility Import
import java.util.ArrayList;
import java.util.logging.Logger;

// Bukkit Implementation

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.dawn.mc.micheldewit.plugin.Sqlite;
import net.dawn.mc.micheldewit.plugin.Mysql;
import net.dawn.mc.micheldewit.plugin.UpdateChecker;
import net.dawn.mc.johnfg10.*;

public class Modreq extends JavaPlugin implements Listener {

			Logger log = Logger.getLogger("Minecraft.ticketman");
			
			private static Plugin plugin;
			
			public void onDisable(){
				log.info("[Ticket Manager] has been disabled");
			}
			private UpdateChecker updatechecker;
			public void onEnable() {
				plugin = this;
				updatechecker = new UpdateChecker();
				updatechecker.startUpdateCheck();
				getServer().getPluginManager().registerEvents(this, this);
				getCommand("ticket").setExecutor(this);
				// Enable Notification
				this.getConfig().options().copyDefaults(true);
				this.getConfig().addDefault("Slack.webhook", "https://changeme");
				this.getConfig().addDefault("database.type", "mysql");
				this.getConfig().addDefault("mysql.address", "127.0.0.1");
				this.getConfig().addDefault("mysql.port", 3306);
				this.getConfig().addDefault("mysql.databasename", "databasename");
				this.getConfig().addDefault("mysql.user", "username");
				this.getConfig().addDefault("mysql.password", "password");
				this.getConfig().addDefault("server.name", "ChangeMe");
				this.getConfig().addDefault("version.number", "1.2");
				this.saveConfig();
				if(!Sqlite.setupdatabase()){
					this.getServer().getPluginManager().disablePlugin(this);
				}
				else
				{
					log.info("[Ticket Manager] has been enabled");
				}
			}
			
			public static Plugin getPlugin(){
				return plugin;
			}
			
			@EventHandler
			public void join(PlayerJoinEvent ev){
				
				Player p = ev.getPlayer();
				if(p.hasPermission("TicketManager.Staff") || p.isOp()){
					ArrayList<String> s = Sqlite.getallopen();
					if(!(s.isEmpty())){
					p.sendMessage(ChatColor.YELLOW+"OpenTickets (The ID): "+ChatColor.GREEN+s);
					}
				}
				ArrayList<String> s = Sqlite.getplayeransweredtickets(p.getName());
				if(!(s.isEmpty())){
				p.sendMessage(ChatColor.YELLOW+"The following tickets have been answered:"+ChatColor.GREEN+s);
				}
				
			}
			
			public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args){
				String webhookUrl =  plugin.getConfig().getString("Slack.webhook");
				if(sender instanceof Player){
					Player p = (Player)sender;
				if(command.getName().equalsIgnoreCase("ticket")){
					if(args.length == 0){
						//show help
						sender.sendMessage(ChatColor.AQUA+"Player Commands:");
						sender.sendMessage(ChatColor.YELLOW+"/ticket open (Message) - "+ChatColor.WHITE+"Opens a ticket and notifies all staff");
						sender.sendMessage(ChatColor.YELLOW+"/ticket view - "+ChatColor.WHITE+"Shows you the ids of all your open and closed tickets");
						sender.sendMessage(ChatColor.YELLOW+"/ticket view (ticketid) - "+ChatColor.WHITE+"Shows you the information on the ticket id.");
						if(sender.hasPermission("TicketManager.Staff")){
						sender.sendMessage(ChatColor.AQUA+"Staff Commands:");
						sender.sendMessage(ChatColor.YELLOW+"/ticket show - "+ChatColor.WHITE+"Shows you all the open tickets.");
						sender.sendMessage(ChatColor.YELLOW+"/ticket player (playername) - "+ChatColor.WHITE+"Shows you the ids of all the open and closed tickets of (playername).");
						sender.sendMessage(ChatColor.YELLOW+"/ticket tp (id) - "+ChatColor.WHITE+"Teleports you to the location the ticket was made.");
						sender.sendMessage(ChatColor.YELLOW+"/ticket answer (id) (reply) - "+ChatColor.WHITE+"Replies to the ticket (id) and automatically closes it.");
						sender.sendMessage(ChatColor.YELLOW+"/ticket register (password) - "+ChatColor.WHITE+" Registers you for webui");
						sender.sendMessage(ChatColor.YELLOW+"/ticket SlackAPI - "+ChatColor.WHITE+" Shows SLACKAPI key. Requires OP");
						sender.sendMessage(ChatColor.YELLOW+"/ticket reload - "+ChatColor.WHITE+" Reloads the plugin");
						
						return true;
						}
					}
					if(args.length >= 1){
						
						if(args[0].equalsIgnoreCase("Open")){
							if(args.length == 1){
								sender.sendMessage(ChatColor.RED+ "/ticket open (description)");
								return true;
							}
							StringBuilder sb = new StringBuilder();
							for (int i = 1; i < args.length; i++){
							sb.append(args[i]).append(" ");
							}
							String allArgs = sb.toString().trim();
							String world = p.getWorld().getName(); 
							String LocalServer = plugin.getConfig().getString("server.name");
							Boolean success = Sqlite.createticket(p.getName(), allArgs, world, p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());
							if(!success){
								p.sendMessage(ChatColor.RED+"Ticket could not be created!");
								p.sendMessage(ChatColor.RED+"You have too many tickets!");
								return true;
							}
							p.sendMessage(ChatColor.AQUA+"Ticket successfully opened");
							p.sendMessage(ChatColor.AQUA+"To view your tickets use /ticket view");
							if(!webhookUrl.equalsIgnoreCase("disable"))
							{
								new SlackFormatter(p.getName(),LocalServer,allArgs);
							}
							
							
							for(Player player : Bukkit.getOnlinePlayers()){
								if(player.hasPermission("TicketManager.Staff") || player.isOp()){
								
									player.sendMessage(ChatColor.RED+"Ticket opened! "+ChatColor.AQUA+" Type /ticket show to see all the open tickets!");
								}
							}
							
							
							return true;
						}
						if(args[0].equalsIgnoreCase("Show")){
							if(p.isOp() || p.hasPermission("TicketManager.Staff") ){
								ArrayList<String>  s = Sqlite.getallopen();
								p.sendMessage(ChatColor.AQUA+"The open tickets are: "+ChatColor.YELLOW+s.toString());
								return true;
						}else{
							p.sendMessage(ChatColor.RED+"You don't have permission to use that command");
							return true;
						}
						}
						
						if(args[0].equalsIgnoreCase("player")){
							if(args.length == 2){
								if(p.isOp() || p.hasPermission("TicketManager.Staff") ){
								String playername = args[1];
									if(!Sqlite.getplayeransweredtickets(playername).isEmpty()){
										p.sendMessage(ChatColor.AQUA+playername+": answered tickets are: "+ChatColor.YELLOW+Sqlite.getplayeransweredtickets(playername));
									}
									p.sendMessage(ChatColor.AQUA+playername+": open tickets are: "+ChatColor.YELLOW+Sqlite.getplayersopentickets(playername));
									if(!Sqlite.getplayerclosedtickets(playername).isEmpty()){
										p.sendMessage(ChatColor.AQUA+playername+": closed tickets are: "+ChatColor.GRAY+Sqlite.getplayerclosedtickets(playername));
									}
									return true;
								}else{
									p.sendMessage(ChatColor.RED+"You don't have permission to use that command");
									return true;
								}
								
							}else{
								return false;
							}
						}
						
						if(args[0].equalsIgnoreCase("View")){
								if(sender instanceof Player){
									if(args.length == 1){
										p.sendMessage(ChatColor.AQUA+"Use /ticket view (id) to view your ticket");
											if(!Sqlite.getplayeransweredtickets(p.getName()).isEmpty()){
												p.sendMessage(ChatColor.AQUA+"Staff have answered: "+ChatColor.YELLOW+Sqlite.getplayeransweredtickets(p.getName()));
											}
											p.sendMessage(ChatColor.AQUA+"Your open tickets are: "+ChatColor.YELLOW+Sqlite.getplayersopentickets(p.getName()));
											if(!Sqlite.getplayerclosedtickets(p.getName()).isEmpty()){
												p.sendMessage(ChatColor.AQUA+"Your closed tickets are: "+ChatColor.GRAY+Sqlite.getplayerclosedtickets(p.getName()));
											}
											return true;
								}
									if(args.length == 2){				
										try{
											int i = Integer.parseInt(args[1]);
												if(sender.isOp() || sender.hasPermission("TicketManager.Staff")){
													if(Sqlite.getplayer(i).equalsIgnoreCase(p.getName())){
														if(!Sqlite.viewticket(p.getName(), i,true)){
															p.sendMessage(ChatColor.RED+"That ticket does not exist!");
														}
													}else{
														if(!Sqlite.viewticket(p.getName(), i,false)){
															p.sendMessage(ChatColor.RED+"That ticket does not exist!");
														}
													}
													return true;
												}
												if(Sqlite.getplayer(i).equalsIgnoreCase(p.getName())){
													if(!Sqlite.viewticket(p.getName(), i,true)){
														p.sendMessage(ChatColor.RED+"That ticket does not exist!");
													}
													return true;
												}else{
													p.sendMessage(ChatColor.RED+"You don't own this ticket!");
													return true;
												}
										}catch(Exception e){
											
										}
											
								}
						}
					}
						if(args[0].equalsIgnoreCase("tp")){
							if(sender.hasPermission("TicketManager.Staff") || sender.isOp()){
								if(args.length == 2){
									int i =0;
									try{
										i = Integer.parseInt(args[1]);
									}catch(Exception e){
										log.info(e.getMessage());
										return true;
									}
									Location ticketlocation = Sqlite.getlocation(i);
									p.teleport(ticketlocation);
									p.sendMessage(ChatColor.YELLOW+"Teleported to ticket's "+ChatColor.GREEN+args[1]+ChatColor.YELLOW+" location");
									Sqlite.viewticket(p.getName(), i,false);
										return true;
								}else{
									sender.sendMessage(ChatColor.RED+"Use the arguments /ticket tp [Ticket ID]");
									return true;
								}
							}else{
								sender.sendMessage(ChatColor.RED+"You do not have permission for this command");
								return true;
							}
							
						}
						if(args[0].equalsIgnoreCase("SlackAPI"))
						{
							if(p.isOp())
							{
								String TestLine = plugin.getConfig().getString("Slack.webhook");
								p.sendMessage(TestLine);
								
								return true;
								
							}
						}
						if(args[0].equalsIgnoreCase("reload")){
							if(p.isOp() || p.hasPermission("TicketManager.Staff") ){
								getConfig();
								saveConfig();
								getServer().getPluginManager().disablePlugin(plugin);
								getServer().getPluginManager().enablePlugin(plugin);
								sender.sendMessage(ChatColor.GREEN+"Plugin has been reloaded");
								return true;
							}
							else{
								sender.sendMessage(ChatColor.RED+"You do not have permission for this command");
								return true;
							}
						}
						
						if(args[0].equalsIgnoreCase("register")){
							if(args.length == 2){
									String password = args[1];
								if(sender.hasPermission("TicketManager.Staff") || sender.isOp()){
									if(Sqlite.registeruser(p.getName(), password)){
										p.sendMessage(ChatColor.YELLOW+"Successfully registered!");
										p.sendMessage(ChatColor.YELLOW+"Username:"+ChatColor.AQUA+p.getName()+ChatColor.YELLOW+" Password:"+ChatColor.AQUA+password);
										return true;
									}
									return true;
								}else{
									sender.sendMessage(ChatColor.RED+"You do have permission to answer tickets");
									return true;
								}
							}
						}
						if(args[0].equalsIgnoreCase("answer")){
							if(args.length > 2){
								StringBuilder anwsersb = new StringBuilder();
								for (int i = 2; i < args.length; i++){
								anwsersb.append(args[i]).append(" ");
								}
								String answer = anwsersb.toString().trim();
								int i =0;
								try{
									i = Integer.parseInt(args[1]);
								}catch(Exception e){
									log.info(e.getMessage());
									return true;
								}
								if(sender.hasPermission("TicketManager.Staff") || sender.isOp()){
									Sqlite.answer(answer, i);
									sender.sendMessage(ChatColor.YELLOW+"You answered ticket "+ChatColor.GREEN+args[1]);
									for(Player player : Bukkit.getOnlinePlayers()){
										if(player.hasPermission("TicketManager.Staff") ||player.isOp()){
											player.sendMessage(ChatColor.AQUA+"Ticket "+i+" has been answered.");
										}
										if(player.getName().equals(Sqlite.getplayer(i))){
											player.sendMessage(ChatColor.YELLOW+"Ticket: "+ChatColor.AQUA+i+ChatColor.YELLOW+" has been answered");
											player.sendMessage(ChatColor.YELLOW+"Type /ticket view "+ChatColor.AQUA+i+ChatColor.YELLOW+" to see your answer!");
										}
									}
									return true;
								}else{
									sender.sendMessage(ChatColor.RED+"You do have permission to answer tickets");
									return true;
								}
								}
							}else{
								sender.sendMessage(ChatColor.YELLOW+"You have not entered a ticket ID or This is not your ticket!");
								return true;
							}
						
						}
					
					sender.sendMessage(ChatColor.RED+"type /ticket (open) [reason] to notify staff.");
					return true;
				}
				}
				
				return false;
		}

			static void ticketView(String p,String open,String player,String reason, String reply, String world,double x, double y,double z,double ticketid) {
				Player sender = Bukkit.getPlayerExact(p);
				sender.sendMessage(ChatColor.GRAY+"=======================["+ChatColor.YELLOW+"Ticket "+ticketid+ChatColor.GRAY+"]====================");
				sender.sendMessage(ChatColor.YELLOW+"Submitted by: "+ChatColor.GRAY+player);
				sender.sendMessage(ChatColor.YELLOW+"Status: "+ChatColor.GRAY+open);
				sender.sendMessage(ChatColor.YELLOW+"Reason: "+ChatColor.GRAY+reason);
				if(reply.equalsIgnoreCase("")){
					sender.sendMessage(ChatColor.YELLOW+"Answer from Staff: "+ChatColor.GRAY+"Awaiting reply...");
				}else{
				sender.sendMessage(ChatColor.YELLOW+"Answer from Staff: "+ChatColor.GRAY+reply);
				}
				sender.sendMessage(ChatColor.GRAY+"=====================================================");
				
		}
			
}
