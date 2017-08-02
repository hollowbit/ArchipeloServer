package net.hollowbit.archipeloserver.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Formatter;
import java.util.Scanner;

import net.hollowbit.archipeloserver.ArchipeloServer;

public class Configuration {
	
	public String dbAddress = "localhost:3306";
	public String dbUsername = "root";
	public String dbPassword = "password";
	public String name = "ServerName";
	public String hbServerAddress = "localhost:22123";
	public String hbServerPassword = "astrongpassword";
	public String motd = "Welcome to Archipelo!";
	public int region = 0;
	public String spawnMap = "island";
	public float spawnX = 0;
	public float spawnY = 0;
	
	public Configuration () {
		File configFile = new File("config.yml");
		
		if (!configFile.exists()) {
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				ArchipeloServer.getServer().getLogger().error("Could not create config.yml");
			}
		}
		
		Scanner scanner = null;
		try {
			scanner = new Scanner(configFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		while(scanner.hasNext()) {
			switch(scanner.next().replace(":", "")) {
			case "dbAddress":
				this.dbAddress = scanner.nextLine().substring(1);
				break;
			case "dbUsername":
				this.dbUsername = scanner.nextLine().substring(1);
				break;
			case "dbPassword":
				this.dbPassword = scanner.nextLine().substring(1);
				break;
			case "name":
				this.name = scanner.nextLine().substring(1);
				break;
			case "hbServerAddress":
				this.hbServerAddress = scanner.nextLine().substring(1);
				break;
			case "hbServerPassword":
				this.hbServerPassword = scanner.nextLine().substring(1);
				break;
			case "motd":
				this.motd = scanner.nextLine().substring(1);
				break;
			case "region":
				this.region = Integer.parseInt(scanner.nextLine().substring(1));
				break;
			case "spawnMap":
				this.spawnMap = scanner.nextLine().substring(1);
				break;
			case "spawnX":
				this.spawnX = Float.parseFloat(scanner.nextLine().substring(1));
				break;
			case "spawnY":
				this.spawnY = Float.parseFloat(scanner.nextLine().substring(1));
				break;
			}
		}
		scanner.close();
		
		ArchipeloServer.getServer().getLogger().info("Configuration loaded!");
		
		save();
	}
	
	public void save () {
		File configFile = new File("config.yml");
		try {
			Formatter formatter = new Formatter(configFile);
			formatter.format("%s: %s\n", "dbAddress", dbAddress);
			formatter.format("%s: %s\n", "dbUsername", dbUsername);
			formatter.format("%s: %s\n", "dbPassword", dbPassword);
			formatter.format("%s: %s\n", "name", name);
			formatter.format("%s: %s\n", "hbServerAddress", hbServerAddress);
			formatter.format("%s: %s\n", "hbServerPassword", hbServerPassword);
			formatter.format("%s: %s\n", "motd", motd);
			formatter.format("%s: %s\n", "region", region);
			formatter.format("%s: %s\n", "spawnMap", spawnMap);
			formatter.format("%s: %s\n", "spawnX", spawnX);
			formatter.format("%s: %s", "spawnY", spawnY);
			formatter.flush();
			formatter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
}
