package channel;

import java.io.IOException;
import java.util.HashMap;

// reference http://www.careerbless.com/samplecodes/java/beginners/socket/SocketBasic1.php


public class Client {
	
	static DelayedChannel channel1;
	static DelayedChannel channel2;
	static DelayedChannel channel3;
	
	public void initialization(HashMap<String, String> configuration, int maxDelay){
		
		// read the configuration
		String serverAddress1 = configuration.get("serverAddress1");
        String serverAddress2 = configuration.get("serverAddress2");
        String serverAddress3 = configuration.get("serverAddress3");
        String serverPort1 = configuration.get("serverPort1");
        String serverPort2 = configuration.get("serverPort2");
        String serverPort3 = configuration.get("serverPort3");
        
        // start the channels
        try {
        	channel1 = new DelayedChannel(serverAddress1, serverPort1, maxDelay);
			channel2 = new DelayedChannel(serverAddress2, serverPort2, maxDelay);
	        channel3 = new DelayedChannel(serverAddress3, serverPort3, maxDelay);
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
        
        channel1.start();
        channel2.start();
        channel3.start();
	}
	
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		
        channel1.putContents("test1");
        channel2.putContents("test2");
        channel3.putContents("test3");
        
        channel1.join();
        channel2.join();
        channel3.join();
        System.exit(0);
    }
	
}
