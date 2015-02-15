package node;

import java.io.IOException;
import java.util.HashMap;

import main.Utils;
import channel.DelayedChannel;

public class Client extends Thread{
	
	// delayed channels
	static DelayedChannel channelA;
	static DelayedChannel channelB;
	static DelayedChannel channelC;
	static DelayedChannel channelD;
	
	// the addresses and ports for the channels
	String serverAddressA;
    String serverAddressB;
    String serverAddressC;
    String serverAddressD;
    String serverPortA;
    String serverPortB;
    String serverPortC;
    String serverPortD;
    
    // maximum delay
    int maxDelay;
    
    public Client(HashMap<String, String> configuration){
    	serverAddressA = configuration.get("serverAddressA");
        serverAddressB = configuration.get("serverAddressB");
        serverAddressC = configuration.get("serverAddressC");
        serverAddressD = configuration.get("serverAddressD");
        serverPortA = configuration.get("serverPortA");
        serverPortB = configuration.get("serverPortB");
        serverPortC = configuration.get("serverPortC");
        serverPortD = configuration.get("serverPortD");
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
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
        
        channelA.start();
        channelB.start();
        channelC.start();
        channelD.start();
	}
	
	public void runInput(String input){
		HashMap<String, String> parsedOperation = new Utils().parseInputCommand(input);
		
		switch(parsedOperation.get("operation")){
		case "send":
			sendMessages(parsedOperation);
		
		}
		
	}
	
	
	public void sendMessages(HashMap<String, String> parsedOperation){
		String message = parsedOperation.get("message");
		switch(parsedOperation.get("destination")){
		case "A":
			channelA.putContents(message);break;
		case "B":
			channelB.putContents(message);break;
		case "C":
			channelC.putContents(message);break;
		case "D":
			channelD.putContents(message);break;
		}
	}
	
}
