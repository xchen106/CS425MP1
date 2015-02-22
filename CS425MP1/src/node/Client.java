package node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import channel.DelayedChannel;
import channel.Message;

public class Client extends Thread{
	
	// machine index
	char index;
	
	// delayed channels
	DelayedChannel channelA;
	DelayedChannel channelB;
	DelayedChannel channelC;
	DelayedChannel channelD;
	DelayedChannel channelO;
	
	// the addresses and ports for the channels
	String serverAddressA;
    String serverAddressB;
    String serverAddressC;
    String serverAddressD;
    String serverAddressO;
    String serverPortA;
    String serverPortB;
    String serverPortC;
    String serverPortD;
    String serverPortO;
    
    // the node
    Node node;
    
    // maximum delay
    int maxDelay;
    
    public Client(HashMap<String, String> configuration, char index, Node node){
    	this.index = index;
    	this.serverAddressA = configuration.get("serverAddressA");
        serverAddressB = configuration.get("serverAddressB");
        serverAddressC = configuration.get("serverAddressC");
        serverAddressD = configuration.get("serverAddressD");
        serverAddressO = configuration.get("serverAddressO");
        serverPortA = configuration.get("serverPortA");
        serverPortB = configuration.get("serverPortB");
        serverPortC = configuration.get("serverPortC");
        serverPortD = configuration.get("serverPortD");
        serverPortO = configuration.get("serverPortO");
        maxDelay = Integer.valueOf(configuration.get("delay"));
    }
	
	// initialization
	public void run(){		
        
        // start the channels
        try {
        	channelA = new DelayedChannel(serverAddressA, serverPortA, maxDelay);
			channelB = new DelayedChannel(serverAddressB, serverPortB, maxDelay);
	        channelC = new DelayedChannel(serverAddressC, serverPortC, maxDelay);
	        channelD = new DelayedChannel(serverAddressD, serverPortD, maxDelay);
	        channelO = new DelayedChannel(serverAddressO, serverPortO, maxDelay);
        } catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
        
        channelA.start();
        channelB.start();
        channelC.start();
        channelD.start();        
        channelO.start();
        
        
        // Print a menu
        while(true){
        	System.out.println("Select your input type: \n(1) Input from keyboard\n(2) Input from file\n");
        	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        	
        	int option = 0;
        	try {
				option = Integer.valueOf(br.readLine());
			
	        	if(option == 1){	// read from screen input
	        		String messageLine = br.readLine();
	        		handleInputMessage(messageLine);
	        		// Wait until the message 
	        		while(this.node.sent == true);
	        	}else{				// read from file input
	        		String fileLocation = br.readLine();
	        		Path path = Paths.get(fileLocation);
	        		Charset charset = Charset.forName("US-ASCII");
	        		BufferedReader reader = Files.newBufferedReader(path, charset);
	        		
	        		String messageLine = null;
	        		while ((messageLine = reader.readLine()) != null) {
		        		handleInputMessage(messageLine);
		        		// Wait until the message
		    	        while(this.node.sent == true);
	        		}   

	        	}
        	
        	} catch (NumberFormatException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		}
        
        
	}
	
	
	// handle the input message from either keyboard or file
	public void handleInputMessage(String messageLine){
		Message m = new Message(messageLine);
		this.node.handleMessage(m);
		this.node.sent = true;
    }
	
}
