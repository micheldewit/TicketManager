package net.dawn.mc.micheldewit.plugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

import net.dawn.mc.micheldewit.SQL.Database;
import net.dawn.mc.micheldewit.SQL.MySQL;
import net.dawn.mc.micheldewit.SQL.SQLite;

public class Sqlite {
	
	static Plugin plugin = Modreq.getPlugin();
	
	private static Database sql;
	public static String sqltype =  plugin.getConfig().getString("database.type");

	public static boolean tableExists(){
		sql.open();
		boolean tExists = false;	
		try(ResultSet rs = sql.query("SELECT * FROM tickets_config where config = 'Version'"))
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
		finally
        {
          if(sql != null)
			{
			  sql.close();
			}
        }
		return tExists;
	}
	  public static boolean setupdatabase(){ 

		  if(sqltype.equals("mysql")){
			  sql = new MySQL(plugin.getLogger(), 
		            "Modreq", 
		            plugin.getConfig().getString("mysql.address"), 
		            plugin.getConfig().getInt("mysql.port"), 
		            plugin.getConfig().getString("mysql.databasename"), 
		            plugin.getConfig().getString("mysql.user"), 
		            plugin.getConfig().getString("mysql.password"));
		  }
		  else
		  {
			  sql = new SQLite(plugin.getLogger(),
					  "Modreq",
					  plugin.getDataFolder().getAbsolutePath(),
					  "TicketManager");
		  }
		  
		  if(!tableExists()){
			  try {
				  
					System.out.println("Setup Started!");
					if(sqltype.equals("mysql")){
						sql.open();
						sql.query("CREATE TABLE IF NOT EXISTS tickets_config(id int NOT NULL AUTO_INCREMENT, config varchar(255), data varchar(255),PRIMARY KEY (id))");
						sql.query("INSERT INTO `tickets_config` (`config`, `data`) VALUES ('Version', '1.1')");
						sql.query("CREATE TABLE IF NOT EXISTS tickets (id int NOT NULL AUTO_INCREMENT,status varchar(255), name varchar(255), reason varchar(255), reply varchar(255), server varchar(255), world varchar(255),x DOUBLE, y DOUBLE, z DOUBLE,PRIMARY KEY (id))");
						sql.query("CREATE TABLE IF NOT EXISTS ticket_login (id int NOT NULL AUTO_INCREMENT, UserLevel int(5), username varchar(255), password varchar(255),PRIMARY KEY (id))");
						sql.close();
					}
					else{
						sql.open();
						sql.query("CREATE TABLE `ticket_login` (`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, `UserLevel` int(5), `username`varchar(255),`password`	varchar(255));");
						sql.query("CREATE TABLE tickets (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, status varchar(255), name varchar(255), reason varchar(255), reply varchar(255), server varchar(255), world varchar(255),x DOUBLE, y DOUBLE, z DOUBLE);");
						sql.query("CREATE TABLE `tickets_config` (`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, `config` varchar(255), `data` varchar(255));");
						sql.query("INSERT INTO `tickets_config` (`config`, `data`) VALUES ('Version', '1.2');");
						sql.close();
					}
			}   
			  catch (SQLException e) {
				  sql.close();
			  }
			  finally
		        {
		          if(sql != null)
					{
					  sql.close();
					}
		        }
		  }
		  return true;
	  }
	  
	  public static boolean stageInsert(String Stage)
	  {
		  try {
			  sql.open();
			  sql.query(Stage);
			  sql.close();
			  return true;
		  } catch (SQLException e)
		  {
			  return false;
		  }
		  finally
	        {
	          if(sql != null)
				{
				  sql.close();
				}
	        }
	  }
	  
	  public static boolean createticket(String player, String reason,String world,Double x,Double y, Double z){
		  String LocalServer = plugin.getConfig().getString("server.name");
		  try {
		      
		      sql.open();
		      String escaped = escapeHtml4(reason);
		      escaped = escaped.replaceAll("'", "\'");
		      //sql.prepare("insert into tickets value(NULL, 'open', ?, ?, ' ', ?, ?, ?, ?, ?)");
		      
		      sql.query("insert into tickets values(NULL, 'open', '"+player+"', '"+escaped+"', '"+" "+"', '"+LocalServer+"', '"+world+"', '"+x+"', '"+y+"', '"+z+"');");
		      sql.close();
		      return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		  finally
	        {
	          if(sql != null)
				{
				  sql.close();
				}
	        }
		 
	  }
	  
	  public static boolean registeruser(String username, String password){
		  try {
			  sql.open();
			 ResultSet rs = sql.query("select * from ticket_login where username='"+username+"';");
		      while(rs.next())
		      {
		    	 return false;
		      }
		      sql.close();
		      sql.open();
		      sql.query("insert into ticket_login values(NULL,'"+username+"', '"+password+"');");
		      sql.close();
		      return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		  finally
	        {
	          if(sql != null)
				{
				  sql.close();
				}
	        }
	  }
	  
	  public static String getplayer(int ticketid){
		  try {
			  sql.open();
			  ResultSet rs = sql.query("select * from tickets where id='"+ticketid+"';");
		         while(rs.next())
		          {
		        	 return rs.getString("name");
		        	 
		          }
		    sql.close();
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		  finally
	        {
	          if(sql != null)
				{
				  sql.close();
				}
	        }
	  }
	  
	  public static boolean viewticket(String p, int ticketid, Boolean close){
		  try {
			  sql.open();
			  ResultSet rs = sql.query("select * from tickets where id='"+ticketid+"';");
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
			  sql.close();
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		  finally
	        {
	          if(sql != null)
				{
				  sql.close();
				}
	        }
	  }  
	  public static ArrayList<String> getallopen(){
		  ArrayList<String> s = new ArrayList<String>();
		  try {
			  sql.open();
			  ResultSet rs = sql.query("select * from tickets where status= 'open';");
			  while(rs.next())
	          {
	        	 s.add(rs.getInt("id")+"");
	            // read the result set
	          }
			  sql.close();
			return s;
		} catch (SQLException e) {
			e.printStackTrace();
			return s;
		}
		  finally
	        {
	          if(sql != null)
				{
				  sql.close();
				}
	        }
	  }
	  
	  
	  
	  public static ArrayList<String> getplayersopentickets(String player){
		  ArrayList<String> s = new ArrayList<String>();
		  try {
			  sql.open();
			  ResultSet rs = sql.query("select * from tickets where name = '"+player+"' and status= 'open';");
			  while(rs.next())
	          {
				  s.add(rs.getInt("id")+"");
	            // read the result set
	          }
			  sql.close();
			return s;
		} catch (SQLException e) {
			e.printStackTrace();
			return s;
		}
		  finally
	        {
	          if(sql != null)
				{
				  sql.close();
				}
	        }
	  }
	  public static ArrayList<String> getplayeransweredtickets(String player){
		  ArrayList<String> s = new ArrayList<String>();
		  try {
			  sql.open();
			  ResultSet rs = sql.query("select * from tickets where name = '"+player+"' and status= 'answered';");
			  while(rs.next())
	          {
				  s.add(rs.getInt("id")+"");
	            // read the result set
	          }
			  sql.close();
			return s;
		} catch (SQLException e) {
			e.printStackTrace();
			return s;
		}
		  finally
	        {
	          if(sql != null)
				{
				  sql.close();
				}
	        }
	  }
	  public static ArrayList<String> getplayerclosedtickets(String player){
		  ArrayList<String> s = new ArrayList<String>();
		  try {
			  sql.open();
			  ResultSet rs = sql.query("select * from tickets where name = '"+player+"' and status= 'closed';");
			  while(rs.next())
	          {
				  s.add(rs.getInt("id")+"");
	            // read the result set
	          }
			  sql.close();
			return s;
		} catch (SQLException e) {
			e.printStackTrace();
			return s;
		}
		  finally
	        {
	          if(sql != null)
				{
				  sql.close();
				}
	        }
	  }
	  
	  public static int firstticknum(String player){
		  try {
			  sql.open();
			  ResultSet rs = sql.query("select * from tickets where name = '"+player+"' and status= 'open';");
			  while(rs.next())
	          {
	        	return rs.getInt("id");
	            // read the result set
	          }
			  sql.close();
			return 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	  }
	  
	  public static void answer(String reply, int ticketid){
		  try {
			  sql.open();
			  ResultSet rs = sql.query("select * from tickets where id='"+ticketid+"';");
			  while(rs.next())
	          {
				  	String escaped = escapeHtml4(reply);
				  	escaped = reply.replaceAll("'", "\'");
		        	 sql.query("update tickets set reply= '"+escaped+"', status='answered' where id= '"+ticketid+"';");
	          }
			  sql.close();
			return;
		} catch (SQLException e) {
			e.printStackTrace();
			
			return;
		}
		  finally
	        {
	          if(sql != null)
				{
				  sql.close();
				}
	        }
	  }
	  
	  public static void closeticket(int ticketid){
		  try {
			  sql.open();
			  ResultSet rs = sql.query("select * from tickets where id= '"+ticketid+"'");
			  sql.close();
			  while(rs.next())
	          {
				  	sql.open();
		        	 sql.query("update tickets set status= 'closed' where id= '"+ticketid+"'");
		        	 sql.close();
	          }
			  
			return;
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		  finally
	        {
	          if(sql != null)
				{
				  sql.close();
				}
	        }
	  }
	  
	  public static Location getlocation(int ticketid){
		  try {
			  sql.open();
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
			  sql.close();
			return null;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		  finally
	        {
	          if(sql != null)
				{
				  sql.close();
				}
	        }
	  }
	  
	  public static int numberoftickets(String player){
		  try {
			  int ticketnum = 0;
			  sql.open();
				 ResultSet rs = sql.query("select * from tickets where name= '"+player+"'and status= 'open'");
		      while(rs.next())
		      {
		    	  ticketnum++;
		      }
		      sql.close();
		      return ticketnum;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
		  finally
	        {
	          if(sql != null)
				{
				  sql.close();
				}
	        }
	  }
}