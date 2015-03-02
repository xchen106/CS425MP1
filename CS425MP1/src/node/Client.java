package node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
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
    	this.node = node;
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
    	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    	while(true){
    		try{
	        	System.out.println("\nSelect your input type: \n(1) Input from keyboard\n(2) Input from file\n");
	        	
	        	int option = 0;
	        	try {
	        		option = Integer.valueOf(br.readLine());
	        	} catch (NumberFormatException e1) {
	    			e1.printStackTrace();
	    			continue;
	    		}
	        	
	        	
		       	if(option == 1){	// read from screen input
		        	String messageLine = br.readLine();
		        	handleInputMessage(messageLine);
		       		// Wait until the message 
		        	int count = 0;
		       		while(this.node.sent == true){
		       			if(count % 8 == 0){
        					System.out.print(".");
        				}
        				count++;
		       			Thread.sleep(100);
		       		};
		       		System.out.println();
		       		
		       	}else{				// read from file input
		       		String fileLocation = br.readLine();
		       		Path path = Paths.get(fileLocation);
		       		Charset charset = Charset.forName("US-ASCII");
		       		BufferedReader reader = Files.newBufferedReader(path, charset);
		        		
		       		String messageLine = null;
	        		while ((messageLine = reader.readLine()) != null) {
	        			
	        			System.out.println("Current commane = " + messageLine);
	        			
	        			handleInputMessage(messageLine);
			        	// Wait until the message
	        			int count = 0;
	        			while(this.node.sent == true){
	        				if(count % 20 == 0){
	        					System.out.print(".");
	        				}
	        				count++;
			       			Thread.sleep(10);
			       		};
			       		System.out.println();
		       		}   
		       	}
			}  catch (IOException e1) {
				e1.printStackTrace();
				continue;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        	
        }
	}
	
	
	// handle the input message from either keyboard or file
	public void handleInputMessage(String messageLine){
		Message m = new Message(messageLine);
		this.node.sent = true;
		try {
			this.node.handleMessage(m);
		} catch (ParseException e) {
			e.printStackTrace();
			handleInputMessage(messageLine);
		}
    }
	
}
