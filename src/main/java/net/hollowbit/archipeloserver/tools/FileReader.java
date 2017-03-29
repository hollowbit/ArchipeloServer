package net.hollowbit.archipeloserver.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileReader {
	
	public static String readFileIntoString(String path) {
		String fileData = "";
		
		InputStream in = FileReader.class.getResourceAsStream(path);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				fileData += line;
			}
			reader.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileData;
	}
	
}
