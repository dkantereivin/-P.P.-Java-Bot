package com.programmerspalace;

import javax.security.auth.login.LoginException;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.PingCommand;
import com.programmerspalace.snippets.CommandSnippet;
import com.programmerspalace.snippets.CommandSnippetAdd;
import com.programmerspalace.snippets.CommandSnippetList;
import com.programmerspalace.snippets.SnippetsMorphiaConnection;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class App {
	public static void main(String[] args) {
		//read all settings
		Settings.init();
		//open morphia connection
		SnippetsMorphiaConnection.init();
		
		//get the discord token
		String token = Settings.getSetting("token");
		//get the prefix
		String prefix = Settings.getSetting("prefix");
		//get the owner id
		String ownerid = Settings.getSetting("owner");
		
		//setting up JDAUtilities
		EventWaiter waiter = new EventWaiter();
		CommandClientBuilder client = new CommandClientBuilder();
		
		client.useDefaultGame();
		client.setPrefix(prefix);
		client.setOwnerId(ownerid);
				
		//add commands here
		//a simple ping command, built into JDAUtilities
		client.addCommand(new PingCommand());
		client.addCommand(new CommandSnippet());
		client.addCommand(new CommandSnippetList(waiter));
		client.addCommand(new CommandSnippetAdd(waiter));
		
		//build the JDA
		try {
			JDA jda = JDABuilder.createDefault(token)
					.addEventListeners(waiter, client.build())
					.build();
			System.out.println("App.main JDA success");
		} catch (LoginException e) {
			//cannot log in
			//print stack then exit program
			e.printStackTrace();
			return;
		}
    }
}
