package com.programmerspalace.snippets;

import java.util.Arrays;

import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.programmerspalace.Settings;

import dev.morphia.Datastore;
import dev.morphia.Morphia;
/**
 * A morphia connection for the snippets database
 * 
 * @author Sam
 *
 */

public class SnippetsMorphiaConnection {
	private static Datastore datastore;

	/**
	 * initialises the datastore, must be run before accessing the database
	 */
	public static void init() {
		System.out.println("MorphiaConnection.init Attempting to connect to mongoDB");

		//set the time out options
		//TODO move to settings
		MongoClientOptions mongoOptions = MongoClientOptions.builder()
				.socketTimeout(60000) // Wait 1m for a query to finish, https://jira.mongodb.org/browse/JAVA-1076
				.connectTimeout(15000) // Try the initial connection for 15s, http://blog.mongolab.com/2013/10/do-you-want-a-timeout/
				.maxConnectionIdleTime(600000) // Keep idle connections for 10m, so we discard failed connections quickly
				.readPreference(ReadPreference.primaryPreferred()) // Read from the primary, if not available use a secondary
				.build();
		
		//set the credentials
		MongoCredential credential = MongoCredential.createCredential(
				Settings.getSetting("mongo-snippets-username"),
				Settings.getSetting("mongo-snippets-name"),
				Settings.getSetting("mongo-snippets-password").toCharArray());
		
		try {
			//create the server address
			Integer port = Integer.parseInt(Settings.getSetting("mongo-port"));
			ServerAddress address = new ServerAddress(Settings.getSetting("mongo-host"), port);
			
			//build the settings
			MongoClientSettings mongoSettings = MongoClientSettings.builder()
					.credential(credential)
					.applyToClusterSettings(builder -> builder.hosts(Arrays.asList(address)))
					.build();
			
			MongoClient mongoClient = MongoClients.create(mongoSettings);
			
			//create the datastore
			datastore = Morphia.createDatastore(mongoClient, Settings.getSetting("mongo-snippets-name"));
			datastore.ensureIndexes();
			datastore.ensureCaps();
			
			System.out.println("MorphiaConnection.init Connected to mongoDB");
			
		} catch (NumberFormatException e) {
			System.out.println("MorphiaConnection.init \"mongo-port\" must be an integer");
			e.printStackTrace();
		}
	}
	
	public static Datastore getDatastore() {
		return datastore;
	}
}
