package channel;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Utils {

	
	// parse the configuration file and store them into a hashmap
	HashMap<String, String> parseConfigure(String filePath){
		// parse results for the whole configuration file
		HashMap<String, String> parseResults = new HashMap<String, String>();
		
		try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
	        String line = br.readLine();
	        
	        // read line by line
	        while (line != null) {
	        	// split by whitespace characters
	            String[] parseResult = line.split("\\s+");
	        	parseResults.put(parseResult[0], parseResult[1]);
	        }
	    } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return parseResults;
	}
	
}
