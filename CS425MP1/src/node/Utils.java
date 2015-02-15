package node;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Utils {

	
	// parse the configuration file and store them into a hashmap
	public HashMap<String, String> parseConfigure(String filePath){
		// parse results for the whole configuration file
		HashMap<String, String> parseResults = new HashMap<String, String>();
		
		try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
	        String line = br.readLine();
	        while (line != null) {
	        	// split by whitespace characters
	            String[] parseResult = line.split("\\s+");
	        	parseResults.put(parseResult[0], parseResult[1]);
	        	line = br.readLine();
	        }
	    } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return parseResults;
	}
	
	// pass the input message
	public HashMap<String, String> parseInputCommand(String command){
		// store the parsed results
		HashMap<String, String> parseResultMap = new HashMap<String, String>();
		
		String[] parseResult = command.split("\\s+");
		String operation = parseResult[0].toLowerCase();
		parseResultMap.put("operation", operation);
		
		switch(operation){
		case "send":
			parseResultMap.put("message", parseResult[1]);
			parseResultMap.put("destination", parseResult[2].toUpperCase());
			break;
		}
		
		
		return parseResultMap;
	}
	
	
	
}
