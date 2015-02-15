package channel;

import java.util.HashMap;

public class Main {

	
	
	
	public void initialization(String configurationFile){
		
		// read the addresses and port numbers from the configuration file
		HashMap<String, String> configuration = new Utils().parseConfigure(configurationFile);
		
	}
	
}
