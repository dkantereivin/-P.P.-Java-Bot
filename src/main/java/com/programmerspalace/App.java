package com.programmerspalace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.security.auth.login.LoginException;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.PingCommand;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class App {
    public static void main(String[] args) {
    	//create a list to store the contents of the config file
		List<String> configFile;
		
		//open the config file and add all lines to the configFile list
		try(Stream<String> lines = Files.lines(Paths.get("config.conf"))) {
			configFile = lines.collect(Collectors.toList());
		} catch (IOException e) {
			//if token cant be found, bot cant be run
			//print stack then exit program
			e.printStackTrace();
			return;
		}
		
		//the first line should contain a discord token
		String token = configFile.get(0);
		//the second line should contain the default command prefix
		String prefix = configFile.get(1);
		
		
		//setting up JDAUtilities
		EventWaiter waiter = new EventWaiter();
		CommandClientBuilder client = new CommandClientBuilder();
		
		client.useDefaultGame();
		client.setPrefix(prefix);
		
		//add commands here
		//a simple ping command, built into JDAUtilities
		client.addCommand(new PingCommand());

		//build the JDA
		try {
			JDA jda = JDABuilder.createDefault(token)
					.addEventListeners(waiter, client.build())
					.build();
		} catch (LoginException e) {
			//cannot log in
			//print stack then exit program
			e.printStackTrace();
			return;
		}
    }
}
