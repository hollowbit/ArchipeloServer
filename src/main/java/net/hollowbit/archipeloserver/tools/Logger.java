package net.hollowbit.archipeloserver.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;

import net.hollowbit.archipeloserver.ArchipeloServer;
import net.hollowbit.archipeloserver.entity.living.Player;
import net.hollowbit.archipeloserver.network.packets.ChatMessagePacket;

public class Logger {
	
	private ArrayList<String> logs;
	
	public Logger () {
		logs = new ArrayList<String>();
	}
	
	public void broadcast (String message, String sender) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd/HH:mm:ss");
		Date date = new Date();
		String log = "[CHAT " + dateFormat.format(date) + " by " + sender + "]" + message;
		logs.add(log);
		System.out.println(log);
		
		ChatMessagePacket packet = new ChatMessagePacket(message, sender);
		for (Player player : ArchipeloServer.getServer().getWorld().getOnlinePlayers()) {
			player.sendPacket(packet);
		}
	}
	
	public void log (LogType type, String message) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd/HH:mm:ss");
		Date date = new Date();
		String log = "[" + type.toString() + " " + dateFormat.format(date) + "] " + message;
		logs.add(log);
		System.out.println(log);
	}
	
	public void info (String message) {
		log(LogType.INFO, message);
	}
	
	public void caution (String message) {
		log(LogType.CAUTION, message);
	}
	
	public void error (String message) {
		log(LogType.ERROR, message);
	}
	
	public void save () {
		DateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd/HH:mm:ss");
		Date date = new Date();
		
		try {
			Formatter formatter = new Formatter(new File ("logs/" + dateFormat.format(date) + ".log"));
			for (String log : logs) {
				formatter.format("%s\n", log);
			}
			formatter.flush();
			formatter.close();
		} catch (FileNotFoundException e) {
			System.out.println("Couldn't save log for some reason.");
		}
	}
	
}
