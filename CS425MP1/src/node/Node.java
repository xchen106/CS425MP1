package node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import channel.Message;

public class Node {
	
	Client clientThread;
	Server serverThread;
	char index;
	// The index for the message that it just sent
	int messageIndex;
	HashMap<String, String> values;
	HashMap<String, Date> timeStamps;
	List<Message> messagesReceived;
	List<Message> currentMessageQueue;
	List<Message> messagesToBeSent;
	boolean sent = false;
	
	
/*	public static void main(String[] args){
		new Node().initialization("accessories/configuration", 'A');
	}*/
	
	
	public void initialization(String configurationFile, char index){
		this.index = index;
		this.messageIndex = -1;
		this.values = new HashMap<String, String>();
		
		// read the addresses and port numbers from the configuration file
		HashMap<String, String> configuration = new Utils().parseConfigure(configurationFile);
		
		serverThread = new Server(configuration, index, this);
		clientThread = new Client(configuration, index, this);
		
		serverThread.start();
		try {
			System.in.read();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		clientThread.start();
		
		
		while(true){
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String input;
	
			try {
				while((input=br.readLine())!=null){
					clientThread.runInput(input);					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	
		
	/*	try {
			clientThread.join();
			serverThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
		
	}
	
	
	public void handleMessage(Message m) {
		int operation = m.Operation;
		
		switch(operation){
		// Sent text messages
		case 0:
			if(m.Key == null){	// if sending a message
				String displayContent = "Sent \"" + m.Content +"\" to " + m.To + ", system time is " + System.currentTimeMillis();
				m.Key = "receive";
				m.From = index;
				m.Origin = index;
				m.Index = ++messageIndex;
				sendToAnother(m, m.To);
				System.out.println(displayContent);
			}else{	// if receiving a message
				String displayContent = "Received \"" + m.Content + "\" from " + m.Origin + ", Max delay is " + m.MaxDelay + " s, system time is " + System.currentTimeMillis();
				System.out.println(displayContent);
			}
			break;
			
		// Delete key ( == write)
		case 1:
			if(m.Origin == '\u0000'){	// If read the command from a text file, send it to the coordinator
				m.Origin = index;
				m.Index = ++messageIndex;
				sendToAnother(m, 'O');
				sent = true;
			}else{	// If receive the delete command from a coordinator, delete immediately
				if(values.containsKey(m.Key)){
					values.remove(m.Key);
				}
				String displayContent = "Delete " + m.Key;
				System.out.println(displayContent);
				if(m.Origin == index){
					sent = false;
				}
			}
			break;
		
		// Read a value
		case 2:
			if(m.Origin == '\u0000'){	// If read the command from a text file
				m.Origin = index;
				m.Index = ++messageIndex;
				if(m.Model == 1){	// For sequential consistency, don't need to broadcast, directly read
					String displayContent = "Get " + m.Key + " ";
					if(values.containsKey(m.Key)){
						displayContent += values.get(m.Key);
					}else{
						displayContent += "null";
					}
					System.out.println(displayContent);
				}else if(m.Model == 0){	// For linearizability, send it to the coordinator
					sendToAnother(m, 'O');
					sent = true;
				}else{	// For eventual consistency, send the message request to all other nodes
					sentToAllOthers(m);
					sent = true;
				}
			}else if(m.Model == 0 && m.Origin == index && m.Index == messageIndex){	// If it's a just sent linearinizability read request
				String displayContent = "Get " + m.Key + " ";
				if(values.containsKey(m.Key)){
					displayContent += values.get(m.Key);
				}else{
					displayContent += "null";
				}
				System.out.println(displayContent);

				sent = false;
			}else if(m.Model == 2 || m.Model == 3){	// If it's an eventually consistent request
				if(m.Origin != index){	// If it's a request from another node, send back the value and timestamp
					m.Value = values.get(m.Key);
					if(m.Value != null){
						m.Content = timeStamps.get(m.Value).toString();
					}
					sendToAnother(m, m.Origin);
				}else if(m.Index == messageIndex){	// If it's a request from myself, and it's about the current read request, save it into the temporary messages query and check whether the condition is satisfied
					messagesReceived.add(m);
					if(messagesReceived.size() == m.Model - 1){
						String displayContent = "<";
						DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						try {
							Date latestTimeStamp  = formatter.parse("0000-00-00 00:00:00");
							String latestValue = "";
							for(Message currentMessage : messagesReceived){
								Date timeStamp = formatter.parse(currentMessage.Content);
								String value = currentMessage.Value;
								displayContent += ("(" + value + "," + currentMessage.Content + ")" ); 
								if(timeStamp.after(latestTimeStamp)){
									latestTimeStamp = timeStamp;
									latestValue = value;
								}
							}
							displayContent += ">";
							displayContent  = "get(" + m.Key + ") = (" + latestValue + "," + latestTimeStamp + ")," + "examed = " + displayContent;
							System.out.println(displayContent);
						}catch (ParseException e) {
							e.printStackTrace();
						}
						sent = false;
						messagesReceived.clear();
					}
				}
			}
			break;
		
		// Insert a key value pair
		case 3:
			if(m.Origin == '\u0000'){	// If read the command from a text file
				m.Origin = index;
				m.Index = ++messageIndex;
				sendToAnother(m, 'O');
			}else{
				
				
				
				if(m.Origin == index){
					sent = false;
				}
			}
			
			
		}
		
		
		
	}
	
	
	public void sentToAllOthers(Message message){
		if(index!='A'){
			this.clientThread.channelA.putMessage(message.Content, message.Key, message.Value, message.Origin, index, 'A', message.MaxDelay, message.Model, message.Operation, message.Index);
		}
		if(index!='B'){
			this.clientThread.channelB.putMessage(message.Content, message.Key, message.Value, message.Origin, index, 'B', message.MaxDelay, message.Model, message.Operation, message.Index);
		}
		if(index!='C'){
			this.clientThread.channelC.putMessage(message.Content, message.Key, message.Value, message.Origin, index, 'C', message.MaxDelay, message.Model, message.Operation, message.Index);
		}
		if(index!='D'){
			this.clientThread.channelD.putMessage(message.Content, message.Key, message.Value, message.Origin, index, 'D', message.MaxDelay, message.Model, message.Operation, message.Index);
		}
	}
	
	public void sendToAnother(Message message, char receiver){
		if(receiver=='A'){
			this.clientThread.channelA.putMessage(message.Content, message.Key, message.Value, message.Origin, index, 'A', message.MaxDelay, message.Model, message.Operation, message.Index);
		}
		if(receiver=='B'){
			this.clientThread.channelB.putMessage(message.Content, message.Key, message.Value, message.Origin, index, 'B', message.MaxDelay, message.Model, message.Operation, message.Index);
		}
		if(receiver=='C'){
			this.clientThread.channelC.putMessage(message.Content, message.Key, message.Value, message.Origin, index, 'C', message.MaxDelay, message.Model, message.Operation, message.Index);
		}
		if(receiver=='D'){
			this.clientThread.channelD.putMessage(message.Content, message.Key, message.Value, message.Origin, index, 'D', message.MaxDelay, message.Model, message.Operation, message.Index);
		}
		if(receiver=='O'){
			this.clientThread.channelO.putMessage(message.Content, message.Key, message.Value, message.Origin, index, 'O', message.MaxDelay, message.Model, message.Operation, message.Index);
		}
	}
	
	
		
}
