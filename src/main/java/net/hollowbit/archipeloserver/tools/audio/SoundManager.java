package net.hollowbit.archipeloserver.tools.audio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;

import net.hollowbit.archipeloserver.tools.StaticTools;

public class SoundManager {
	
	private HashSet<String> sounds;
	
	public SoundManager () {
		sounds = new HashSet<String>();
		
		//Load all sounds form the sound list file
		InputStream in = getClass().getResourceAsStream("/sounds.json");
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String fileString = "";
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				fileString += line;
			}
			reader.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String[] soundPaths = StaticTools.getJson().fromJson(String[].class, fileString);
		for (String path : soundPaths) {
			sounds.add(path);
		}
	}
	
	public boolean doesSoundExist (String path) {
		return sounds.contains(path);
	}
}
