package net.hollowbit.archipeloserver.tools;

import java.io.File;
import java.util.Scanner;

public class FileReader {
	
	public static String readFileIntoString(String path) {
		File file = new File(path);
		if (file.exists()) {
			try {
				Scanner scanner = new Scanner(file);
				String fileData = "";
				
				while (scanner.hasNext()) {
					fileData += scanner.next();
				}
					
				scanner.close();
				return fileData;
			} catch (Exception e) {}
		}

		return null;
	}
	
}
