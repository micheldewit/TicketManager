package net.dawn.mc.micheldewit.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;
public class UpdateChecker {
	
	static Plugin plugin = Modreq.getPlugin();
	public String currentVersion = plugin.getDescription().getVersion();
		public UpdateChecker() {	
		}
		
		private String readurl = "[url]https://raw.githubusercontent.com/micheldewit/TicketMan/master/version.txt[/url]";
 
		public void startUpdateCheck() {
			if (plugin.getConfig().getBoolean("update-checker")) {
				Logger log = plugin.getLogger();
				try {
					log.info("Checking for a new version...");
					URL url = new URL(readurl);
					BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
					String str;
					while ((str = br.readLine()) != null) {
						String line = str;
						if (line.charAt(0) == '1' && line.charAt(2) == '3') {
								String updatemsg = line.substring(5);
								log.info(updatemsg);
						}
					}
					br.close();
				} catch (IOException e) {
					log.severe("The UpdateChecker URL is invalid! Please let me know!");
				}
			}
		}
}