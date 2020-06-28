package com.programmerspalace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Settings {
	private static Map<String, String> settings = new HashMap<String, String>();
	
	/**
	 * add a single setting to the map
	 * @param setting setting name and value, surrounded in quotes and split by a colon
	 */
	private static void addSetting(String setting) {
		//find first colon
		int colon = setting.indexOf(':');
		
		//if there is no colon
		if (colon == -1) {
			return;
		}
		
		//get the info from the setting
		String settingName = setting.substring(0, colon).trim();
		settingName = settingName.substring(1, settingName.length()-1);
		
		String settingContent = setting.substring(colon+1, setting.length()).trim();
		settingContent = settingContent.substring(1, settingContent.length()-1);
		
		//add setting to the map
		settings.put(settingName, settingContent);
	}
	
	/**
	 * read all settings from file
	 * ToDo connect to the database and read all settings from the database
	 */
	public static void init() {
		System.out.println("Settings.init Loading settings from config file");
		//LOAD CONFIG FILE
		
		//open the config file and add all lines to the settings list
		try(Stream<String> lines = Files.lines(Paths.get("config/config.conf"))) {
			lines.forEach(setting -> addSetting(setting));
		} catch (IOException e) {
			//file not found
			e.printStackTrace();
			return;
		}
		
		System.out.println("Settings.init Loading settings from MongoDB");
		
		
	}
	
	/**
	 * returns a setting by name
	 * @param name name of the setting as stored in file or in database
	 * @return the value of the setting
	 */
	public static String getSetting(String name) {
		return settings.get(name);
	}
}
