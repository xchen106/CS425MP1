package node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

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
		

	
		
	/*	try {
			clientThread.join();
			serverThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
		
	}
	
	
	public void handleMessage(Message m) throws ParseException {
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
				sent = false;
			}else{	// if receiving a message
				String displayContent = "Received \"" + m.Content + "\" from " + m.Origin + ", Max delay is " + m.MaxDelay + " s, system time is " + System.currentTimeMillis();
				System.out.println(displayContent);
			}
			break;
			
		// Delete key ( == write)
		case 1:
			if(index == 'O'){
				sentToAllOthers(m);
			}else if(m.Origin == '\u0000'){	// If read the command from a text file, send it to the coordinator
				m.Origin = index;
				m.Index = ++messageIndex;
				sendToAnother(m, 'O');
			}else{	// If receive the delete command from a coordinator, delete immediately
				if(values.containsKey(m.Key)){
					values.remove(m.Key);
				}
				timeStamps.put(m.Key, m.RealDeliverTime);
				
				String displayContent = "Delete " + m.Key;
				System.out.println(displayContent);
				if(m.Origin == index){
					sent = false;
				}
			}
			break;
		
		// Get a value
		case 2:
			if(index == 'O'){
				sentToAllOthers(m);
			}else if(m.Origin == '\u0000'){	// If read the command from a text file
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
					sent = false;
				}else if(m.Model == 0){	// For linearizability, send it to the coordinator
					sendToAnother(m, 'O');
				}else{	// For eventual consistency, send the message request to all other nodes
					sentToAllOthers(m);
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
		// eventual consistent : directly send to other
		case 3:
			if(index == 'O'){
				sentToAllOthers(m);
			}else if(m.Origin == '\u0000'){	// If read the command from a text file
				m.Origin = index;
				m.Index = ++messageIndex;
				
				if(m.Model == 0 || m.Model == 1){	// If it's linearnizability or sequential consistent
					sendToAnother(m, 'O');
				}else{	// If it's eventual consistent
					sentToAllOthers(m);
				}		
				
			}else if(m.Model == 1 || m.Model == 0){	// If received a message and it's linearizability or sequential consistent
				values.put(m.Key, m.Value);
				
				String displayContent = "inserted key " + m.Key;
				System.out.println(displayContent);
				
				if(m.Origin == index && m.Index == messageIndex){
					sent = false;
				}
			}else{ // If received as eventual consistent
				
				if(m.Origin == index && m.Index == messageIndex){	// If it's a current write request from myself
					messagesReceived.add(m);
					if(messagesReceived.size() == m.Model - 1){
						values.put(m.Key, m.Value);
						timeStamps.put(m.Key, m.RealDeliverTime);
						messagesReceived.clear();
						
						String displayContent = "inserted key " + m.Key;
						System.out.println(displayContent);
					}
					
				}else if(m.Origin != index){	// If it's a write request from other nodes
					values.put(m.Key, m.Value);
					timeStamps.put(m.Key, m.RealDeliverTime);
					
					String displayContent = "inserted key " + m.Key;
					System.out.println(displayContent);
				}
			}
			break;
			
		// Update a key value pair
		case 4:
			if(index == 'O'){	// For the coordinator, send out the message to every node
				sentToAllOthers(m);
			}else if(m.Origin == '\u0000'){	// If read the command from a text file
				m.Origin = index;
				m.Index = ++messageIndex;
				
				if(m.Model == 0 || m.Model == 1){	// If it's linearnizability or sequential consistent
					sendToAnother(m, 'O');
				}else{	// If it's eventual consistent
					sentToAllOthers(m);
				}
				
			}else if(m.Model == 1 || m.Model == 0){	// If received a message and it's linearizability or sequential consistent
				if(m.Origin == index){
					String displayContent = "Key " + values.get(m.Key) + " updated to " + m.Value;
					System.out.println(displayContent);
					
					values.put(m.Key, m.Value);
					
					if(m.Index == messageIndex)
						sent = false;
				}else{
					String displayContent = "Key " + m.Key + " changed from " + values.get(m.Key) + " to " + m.Value;
					System.out.println(displayContent);
					
					values.put(m.Key, m.Value);				
				}
			}else{	 // If received as eventual consistent
				if(m.Origin == index && m.Index == messageIndex){	// If it's a current write request from myself
					messagesReceived.add(m);
					if(messagesReceived.size() == m.Model - 1){
						String displayContent = "Key " + values.get(m.Key) + " updated to " + m.Value;
						System.out.println(displayContent);
						
						values.put(m.Key, m.Value);
						timeStamps.put(m.Key, m.RealDeliverTime);
						messagesReceived.clear();
						
						sent = false;
					}
					
				}else if(m.Origin != index){	// If it's a write request from other nodes
					String displayContent = "Key " + m.Key + " changed from " + values.get(m.Key) + " to " + m.Value;
					System.out.println(displayContent);
					
					values.put(m.Key, m.Value);
					timeStamps.put(m.Key, m.RealDeliverTime);
				}
				
				
			}
			break;
			
		// Search
		case 5:
			if(m.Origin == '\u0000'){	// If read the command from a text file
				m.Origin = index;
				m.Index = ++messageIndex;
				
				sentToAllOthers(m);
			}else{
				if(m.Origin != index){	// If it's a request from another node, send back whether it contains the Key
					if(values.containsKey(m.Key) == true){
						m.Value = "True";
					}else{
						m.Value = "False";
					}
					sendToAnother(m, m.Origin);
				}else if(m.Index == messageIndex){	// If it's a request from myself, count the number of keys
					messagesReceived.add(m);
					if(messagesReceived.size() == 3){
						String displayContent = "";
						if(values.containsKey(m.Key) == true){
							displayContent += index + " ";
						}
						
						for(Message currentMessage : messagesReceived){
							if(m.Value == "True"){
								displayContent += currentMessage.From + " ";
							}
						}
						System.out.println(displayContent);

						messagesReceived.clear();
						sent = false;
					}
				}
			}
			break;
			
		// Delay
		case 6:
			int sleepTime = Integer.valueOf(m.Value);
			try {
				Thread.sleep((long)sleepTime*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			sent = false;
			
			break;
			
		// Show all
		case 7:
			for(Entry<String, String> entry : values.entrySet()){
				System.out.println(entry.getKey() + "," + entry.getValue());
			}
			
			sent = false;
			break;
			
		}
	}
	
	
	public void sentToAllOthers(Message message) throws ParseException{
		message.From = index;
		if(index!='A'){
			message.To = 'A';
			this.clientThread.channelA.putMessageString(message.messageToString());
		}
		if(index!='B'){
			message.To = 'B';
			this.clientThread.channelB.putMessageString(message.messageToString());
		}
		if(index!='C'){
			message.To = 'C';
			this.clientThread.channelC.putMessageString(message.messageToString());
		}
		if(index!='D'){
			message.To = 'D';
			this.clientThread.channelD.putMessageString(message.messageToString());
		}
	}
	
	public List<Message> sentToKOthers(Message message, int K) throws ParseException{
		int count = 0;
		List<Message> messagesToBeSent = new ArrayList<Message>();


		message.From = index;
		if(index!='A'){
			if(count < K){
				message.To = 'A';
				this.clientThread.channelA.putMessageString(message.messageToString());
				K++;
			}else{
				messagesToBeSent.add(new Message().stringToMessage(message.messageToString()));
			}
		}
		if(index!='B'){
			if(count < K){
				message.To = 'B';
				this.clientThread.channelB.putMessageString(message.messageToString());
				K++;
			}else{
				messagesToBeSent.add(new Message().stringToMessage(message.messageToString()));
			}		
		}
		if(index!='C'){
			if(count < K){
				message.To = 'C';
				this.clientThread.channelC.putMessageString(message.messageToString());
				K++;
			}else{
				messagesToBeSent.add(new Message().stringToMessage(message.messageToString()));
			}	
		}
		if(index!='D'){
			if(count < K){
				message.To = 'D';
				this.clientThread.channelD.putMessageString(message.messageToString());
				K++;
			}else{
				messagesToBeSent.add(new Message().stringToMessage(message.messageToString()));
			}		
		}
		
		return messagesToBeSent;
	}
	
	
	public void sendToAnother(Message message, char receiver) throws ParseException{
		if(receiver=='A'){
			message.To = 'A';
			this.clientThread.channelA.putMessageString(message.messageToString());		
		}
		if(receiver=='B'){
			message.To = 'B';
			this.clientThread.channelB.putMessageString(message.messageToString());		
		}
		if(receiver=='C'){
			message.To = 'C';
			this.clientThread.channelC.putMessageString(message.messageToString());		
		}
		if(receiver=='D'){
			message.To = 'D';
			this.clientThread.channelD.putMessageString(message.messageToString());		
		}
		if(receiver=='O'){
			message.To = 'O';
			this.clientThread.channelO.putMessageString(message.messageToString());	
		}
	}
	
	
		
}
