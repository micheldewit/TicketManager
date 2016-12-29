package net.dawn.mc.micheldewit.plugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import net.dawn.mc.micheldewit.SQL.Database;
import net.dawn.mc.micheldewit.SQL.MySQL;
import net.dawn.mc.micheldewit.SQL.SQLite;

public class Mysql {
	
	static Plugin plugin = Modreq.getPlugin();
	
	private static Database sql;
	
	public static void opensql(){
		if (!sql.open()) {
		    sql.open();
		}
	}
	
	public static boolean tableExists() {
		boolean tExists = false;	
		try(ResultSet rs = sql.query("SELECT * FROM tickets.tickets_config where config = 'Version'"))
		{
			while (rs.next())
			{
				String version = rs.getString("data");
				String configVersion = plugin.getConfig().getString("version.number");
				if(version.equals(configVersion))
				{
					tExists = true;

				}
			}
		}
		catch (SQLException e) {
		}
		return tExists;
	}
	  public static boolean setupdatabase(){ 

			  sql = new MySQL(plugin.getLogger(), 
		            "Modreq", 
		            plugin.getConfig().getString("mysql.address"), 
		            plugin.getConfig().getInt("mysql.port"), 
		            plugin.getConfig().getString("mysql.databasename"), 
		            plugin.getConfig().getString("mysql.user"), 
		            plugin.getConfig().getString("mysql.password"));


		  sql.open();
		  if(!tableExists())
		  {
		  try {
				System.out.println("Setup Started!");
				sql.query("CREATE TABLE IF NOT EXISTS tickets_config(id int NOT NULL AUTO_INCREMENT, config varchar(255), data varchar(255),PRIMARY KEY (id))");
				sql.query("INSERT INTO `tickets`.`tickets_config` (`config`, `data`) VALUES ('Version', '1.1')");
				sql.query("CREATE TABLE IF NOT EXISTS tickets (id int NOT NULL AUTO_INCREMENT,status varchar(255), name varchar(255), reason varchar(255), reply varchar(255), server varchar(255), world varchar(255),x DOUBLE, y DOUBLE, z DOUBLE,PRIMARY KEY (id))");
				sql.query("CREATE TABLE IF NOT EXISTS ticket_login (id int NOT NULL AUTO_INCREMENT, UserLevel int(5), username varchar(255), password varchar(255),PRIMARY KEY (id))");
		}   catch (SQLException e) {
		}
		  }
		  return true;
	  }
	  
	  public static boolean createticket(String player, String reason,String world,Double x,Double y, Double z){
		  opensql();
		  try {
			  int ticketnum = 0;
			 ResultSet rs = sql.query("select * from tickets where name='"+player+"'and status= 'open'");
		      while(rs.next())
		      {
		    	  ticketnum++;
		      }
		      if(ticketnum >=3){
		    	  return false;
		      }
		      String LocalServer = plugin.getConfig().getString("server.name");
		      sql.query("insert into tickets values(NULL,'open', '"+player+"', '"+reason+"', '"+" "+"', '"+LocalServer+"', '"+world+"', '"+x+"', '"+y+"', '"+z+"')");
		      return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	  }
	  
	  public static boolean registeruser(String username, String password){
		  opensql();
		  try {
			 ResultSet rs = sql.query("select * from ticket_login where username='"+username+"'");
		      while(rs.next())
		      {
		    	 return false;
		      }
		      sql.query("insert into ticket_login values(NULL,'"+username+"', '"+password+"')");
		      return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	  }
	  
	  public static String getplayer(int ticketid){
		  opensql();
		  try {
			  ResultSet rs = sql.query("select * from tickets where id='"+ticketid+"'");
		         while(rs.next())
		          {
		        	 return rs.getString("name");
		        	 
		          }
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	  }
	  
	  public static boolean viewticket(String p, int ticketid, Boolean close){
		  opensql();
		  try {
			  ResultSet rs = sql.query("select * from tickets where id='"+ticketid+"'");
	        	 if(close){
	        		 sql.query("update tickets set status= 'closed' where id= '"+ticketid+"' and status= 'answered'");
	        	 }
			  while(rs.next())
		          {
		        	 String name = rs.getString("name");
		        	 String open = rs.getString("status");
		        	 String reason = rs.getString("reason");
		        	 String reply = rs.getString("reply");
		        	 String world = rs.getString("world");
		        	 double x = rs.getDouble("x");
		        	 double y = rs.getDouble("y");
		        	 double z = rs.getDouble("z");
		        	 Modreq.ticketView(p, open, name, reason, reply, world, x, y, z, ticketid);
		            // read the result set
		        	 return true;
		          }
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	  }  
	  public static ArrayList<String> getallopen(){
		  opensql();
		  ArrayList<String> s = new ArrayList<String>();
		  try {
			  ResultSet rs = sql.query("select * from tickets where status= 'open'");
			  while(rs.next())
	          {
	        	 s.add(rs.getInt("id")+"");
	            // read the result set
	          }
			
			return s;
		} catch (SQLException e) {
			e.printStackTrace();
			return s;
		}
	  }
	  
	  
	  
	  public static ArrayList<String> getplayersopentickets(String player){
		  opensql();
		  ArrayList<String> s = new ArrayList<String>();
		  try {
			  ResultSet rs = sql.query("select * from tickets where name = '"+player+"' and status= 'open'");
			  while(rs.next())
	          {
				  s.add(rs.getInt("id")+"");
	            // read the result set
	          }
			
			return s;
		} catch (SQLException e) {
			e.printStackTrace();
			return s;
		}
	  }
	  public static ArrayList<String> getplayeransweredtickets(String player){
		  opensql();
		  ArrayList<String> s = new ArrayList<String>();
		  try {
			  ResultSet rs = sql.query("select * from tickets where name = '"+player+"' and status= 'answered'");
			  while(rs.next())
	          {
				  s.add(rs.getInt("id")+"");
	            // read the result set
	          }
			return s;
		} catch (SQLException e) {
			e.printStackTrace();
			return s;
		}
	  }
	  public static ArrayList<String> getplayerclosedtickets(String player){
		  opensql();
		  ArrayList<String> s = new ArrayList<String>();
		  try {
			  ResultSet rs = sql.query("select * from tickets where name = '"+player+"' and status= 'closed'");
			  while(rs.next())
	          {
				  s.add(rs.getInt("id")+"");
	            // read the result set
	          }
			
			return s;
		} catch (SQLException e) {
			e.printStackTrace();
			return s;
		}
	  }
	  
	  public static int firstticknum(String player){
		  opensql();
		  try {
			  ResultSet rs = sql.query("select * from tickets where name = '"+player+"' and status= 'open'");
			  while(rs.next())
	          {
	        	return rs.getInt("id");
	            // read the result set
	          }
			
			return 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	  }
	  
	  public static void answer(String reply, int ticketid){
		  opensql();
		  try {
			  ResultSet rs = sql.query("select * from tickets where id='"+ticketid+"'");
			  while(rs.next())
	          {
		        	 sql.query("update tickets set reply= '"+reply+"', status='answered' where id= '"+ticketid+"'");
	          }
			
			return;
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
	  }
	  
	  public static void closeticket(int ticketid){
		  opensql();
		  try {
			  ResultSet rs = sql.query("select * from tickets where id= '"+ticketid+"'");
			  while(rs.next())
	          {
		        	 sql.query("update tickets set status= 'closed' where id= '"+ticketid+"'");
	          }
			
			return;
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
	  }
	  
	  public static Location getlocation(int ticketid){
		  opensql();
		  try {
			  ResultSet rs = sql.query("select * from tickets where id= '"+ticketid+"'");
			  while(rs.next())
	          {
		        	 String world = rs.getString("world");
		        	 double x = rs.getDouble("x");
		        	 double y = rs.getDouble("y");
		        	 double z = rs.getDouble("z");
		        	 Location l = new Location(Bukkit.getWorld(world), x, y, z);
		        	 return l;
	          }
			
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	  }
	  
	  public static int numberoftickets(String player){
		  opensql();
		  try {
			  int ticketnum = 0;
				 ResultSet rs = sql.query("select * from tickets where name= '"+player+"'and status= 'open'");
		      while(rs.next())
		      {
		    	  ticketnum++;
		      }
		      return ticketnum;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	  }

}
