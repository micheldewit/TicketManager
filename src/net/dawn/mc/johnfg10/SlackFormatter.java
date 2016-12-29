package net.dawn.mc.johnfg10;

import in.ashwanthkumar.slack.webhook.Slack;
import in.ashwanthkumar.slack.webhook.SlackAttachment;
import in.ashwanthkumar.slack.webhook.SlackAttachment.Field;
import net.dawn.mc.micheldewit.plugin.Modreq;

import java.io.IOException;

import org.bukkit.plugin.Plugin;

/**
 * Created by john on 17/12/16.
 */
public class SlackFormatter extends Slack {
	static Plugin plugin = Modreq.getPlugin();
	public static String webhookUrl =  plugin.getConfig().getString("Slack.webhook");
    public SlackFormatter(String playerName, String serverName, String ticketInformation) {
        super(webhookUrl);
        displayName("Ticket Manager");
        String TicketString;
		TicketString = "*Ticket:* \n"
				+ ticketInformation
				+ "\n"
				+ "*Server:* "
				+ "_"
				+ serverName
				+ "_";
		String NewTicketSubmmit = "A new Ticket has been submitted by:"
				+ playerName;
        SlackAttachment slackAttachment = new SlackAttachment("\n")
        		
        		.author(playerName,"https://crafatar.com/avatars/"+playerName)
        		.preText(NewTicketSubmmit)
                .color("warning")
                .addMarkdownIn("text")
                .text(TicketString);
        try {
            push(slackAttachment);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
