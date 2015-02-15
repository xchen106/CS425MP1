package node;

import java.io.IOException;
import java.util.HashMap;

import channel.DelayedChannel;

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
	}
	
	// run the input command
	public void runInput(String input){
		HashMap<String, String> parsedOperation = new Utils().parseInputCommand(input);
		
		switch(parsedOperation.get("operation")){
		case "send":
			sendMessages(parsedOperation);			
			break;
		
		}
		
	}
	
	// send a message to another machine
	public void sendMessages(HashMap<String, String> parsedOperation){
		String messageContent = parsedOperation.get("message");
		String desitation = parsedOperation.get("destination");
		// the message to be shown on the local machine
		String clientMessage = "Sent \"" + messageContent + "\" to " + desitation + ", system time is " + System.currentTimeMillis();
		System.out.println(clientMessage);
		// the message to be sent
		String serverMessage = "Received \"" + messageContent + "\" from " + index + " Max delay is " + String.valueOf(maxDelay) + " s, system time is " + System.currentTimeMillis();
		
		switch(desitation){
		case "A":
			channelA.putContents(serverMessage);break;
		case "B":
			channelB.putContents(serverMessage);break;
		case "C":
			channelC.putContents(serverMessage);break;
		case "D":
			channelD.putContents(serverMessage);break;
		case "O":
			channelO.putContents(serverMessage);break;
		}
	}
	
}
