package node;

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
	HashMap<String, Long> timeStamps;
	List<Message> messagesReceived;

	List<Message> messagesToBeSent;
	boolean sent = false;
	
	
	public static void main(String[] args) throws InterruptedException{
		new Node().initialization("./accessories/configuration", args[0].charAt(0));
	}
	
	
	public void initialization(String configurationFile, char index) throws InterruptedException{
		this.index = index;
		this.messageIndex = -1;
		this.values = new HashMap<String, String>();
		this.timeStamps = new HashMap<String, Long>();
		this.messagesReceived = new ArrayList<Message>();
		
		// read the addresses and port numbers from the configuration file
		HashMap<String, String> configuration = new Utils().parseConfigure(configurationFile);
		
		System.out.println("IP address = " + configuration.get("serverAddress" + index));
		System.out.println("port number  = " + configuration.get("serverPort" + index));
		if(index == 'O'){
			System.out.println("machine index = coordinator");
		}else{
			System.out.println("machine index = " + index);
		}
		
		serverThread = new Server(configuration, index, this);
		clientThread = new Client(configuration, index, this);
		
		serverThread.start();
		Thread.sleep(2000);
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
			if(m.Key == null || m.Key.length()==0){	// if sending a message
				String displayContent = "Sent \"" + m.Content +"\" to " + m.To + ", system time is " + new Date().getTime();
				m.Key = "receive";
				m.From = index;
				m.Origin = index;
				m.Index = messageIndex;
				sendToAnother(m, m.To);
				System.out.println("\n"+displayContent);
				messageIndex++;
				sent = false;
			}else{	// if receiving a message
				String displayContent = "Received \"" + m.Content + "\" from " + m.Origin + ", Max delay is " + m.MaxDelay + " s, system time is " + new Date().getTime();
				System.out.println("\n"+displayContent);
			}
			break;
			
		// Delete key ( == write)
		case 1:
			if(index == 'O'){
				sentToAllOthers(m);
			}else if(m.Origin == '\u0000'){	// If read the command from a text file, send it to the coordinator
				m.Origin = index;
				m.Index = messageIndex;
				m.Content = String.valueOf(new Date().getTime());	// store the sent time
				sendToAnother(m, 'O');
			}else{	// If receive the delete command from a coordinator, delete immediately
				if(values.containsKey(m.Key)){
					values.remove(m.Key);
					timeStamps.put(m.Key, Long.valueOf(m.Content));
					String displayContent = "Delete " + m.Key;
					System.out.println("\n"+displayContent);
				}else{
					String displayContent = "Key " + m.Key + " doesn't exist";
					timeStamps.put(m.Key, Long.valueOf(m.Content));
					System.out.println("\n"+displayContent);
				}
				
				if(m.Origin == index && m.Index == messageIndex){	
					messageIndex++;
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
				m.Index = messageIndex;
				if(m.Model == 2){	// For sequential consistency, don't need to broadcast, directly read
					String displayContent = "Get " + m.Key + " ";
					if(values.containsKey(m.Key)){
						displayContent += values.get(m.Key);
					}else{
						displayContent += "doesn't exist";
					}
					System.out.println("\n"+displayContent);
					
					messageIndex++;
					sent = false;
				}else if(m.Model == 1){	// For linearizability, send it to the coordinator
					sendToAnother(m, 'O');
				}else if(m.Model == 3){	// directly read from myself
					Long timeStamp = null;
					String value = null;
					// compare current node's time
					if(timeStamps.containsKey(m.Key)){
						timeStamp = timeStamps.get(m.Key);
						value = values.get(m.Key);
					}
					String displayContent = "<";
					displayContent += ("(" + value + "," + timeStamp + ")" ); 
					displayContent += ">";
					displayContent  = "get(" + m.Key + ") = (" + value + "," + timeStamp + ")," + "examed = " + displayContent;
					System.out.println("\n"+displayContent);
					
					// start to repair
					System.out.println("\nstart to repair...");
					Message newMessage = new Message();
					newMessage.Origin = index;
					newMessage.Index = messageIndex;
					newMessage.Operation = 8;
					newMessage.Key = "request";
					sentToAllOthers(newMessage);
					
				}else if(m.Model == 4){	// For eventual consistency, send the message request to all other nodes
					sentToAllOthers(m);
				}
			}else if(m.Model == 1 && m.Origin == index && m.Index == messageIndex){	// If it's a just sent linearinizability read request
				String displayContent = "Get " + m.Key + " ";
				if(values.containsKey(m.Key)){
					displayContent += values.get(m.Key);
				}else{
					displayContent += " deosn't exist";
				}
				System.out.println("\n"+displayContent);

				messageIndex++;
				sent = false;
			}else if(m.Model == 4){	// If it's an eventually consistent request
				if(m.Origin != index){	// If it's a request from another node, send back the value and timestamp
					m.Value = values.get(m.Key);
					m.Content = String.valueOf(timeStamps.get(m.Key));
					
					sendToAnother(m, m.Origin);
				}else if(m.Index == messageIndex){	// If it's a request from myself, and it's about the current read request, save it into the temporary messages query and check whether the condition is satisfied
					messagesReceived.add(m);
					if(messagesReceived.size() == m.Model - 3){
						String displayContent = "<";
						
						Long latestTimeStamp  = null;
						String latestValue = null;
							
						Long timeStamp = null;
						String value = null;
						// compare current node's time
						if(timeStamps.containsKey(m.Key)){
							timeStamp = timeStamps.get(m.Key);
							value = values.get(m.Key);
							if(latestTimeStamp == null || timeStamp > latestTimeStamp){
								latestTimeStamp = timeStamp;
								latestValue = value;
							}
						}
						displayContent += ("(" + value + "," + timeStamp + ")" ); 
							
						// for another messages received
						for(Message currentMessage : messagesReceived){
							if(m.Content !=null && m.Content.equals("null") == false){
								timeStamp = Long.valueOf(currentMessage.Content);
								value = currentMessage.Value;
								if(latestTimeStamp == null || timeStamp > latestTimeStamp){
									latestTimeStamp = timeStamp;
									latestValue = value;
								}
							}
							displayContent += ("(" + value + "," + currentMessage.Content + ")" ); 
						}
						displayContent += ">";
						displayContent  = "get(" + m.Key + ") = (" + latestValue + "," + latestTimeStamp + ")," + "examed = " + displayContent;
						System.out.println("\n"+displayContent);
						
						messageIndex++;
						messagesReceived.clear();
						
						// start to repair
						System.out.println("\nstart to repair...");
						Message newMessage = new Message();
						newMessage.Origin = index;
						newMessage.Index = messageIndex;
						newMessage.Operation = 8;
						newMessage.Key = "request";
						sentToAllOthers(newMessage);						
					}
				}
			}
			break;
		
		// Insert a key value pair
		// eventual consistent : directly send to other
		case 3:
			if(index == 'O'){
				System.out.println("received a request");
				sentToAllOthers(m);
			}else if(m.Origin == '\u0000'){	// If read the command from a text file
				m.Origin = index;
				m.Index = messageIndex;
								
				if(m.Model == 1 || m.Model == 2){	// If it's linearnizability or sequential consistent
					sendToAnother(m, 'O');
				}else{	// If it's eventual consistent
					sentToAllOthers(m);
					if(m.Model == 3){	// if W = 1, directly write to myself
						values.put(m.Key, m.Value);
						timeStamps.put(m.Key, new Date().getTime());
						
						String displayContent = "inserted key " + m.Key;
						System.out.println("\n"+displayContent);
						messageIndex++;
						sent = false;
					}
				}		
				
			}else if(m.Model == 1 || m.Model == 2){	// If received a message and it's linearizability or sequential consistent
				values.put(m.Key, m.Value);
				
				String displayContent = "inserted key " + m.Key;
				System.out.println("\n"+displayContent);
				
				if(m.Origin == index && m.Index == messageIndex){
					messageIndex++;
					sent = false;
				}
			}else{ // If received as eventual consistent				
				if(m.Origin == index && m.Index == messageIndex && m.Model == 4){	// If it's a current write request from myself (only handle when W == 4)
					messagesReceived.add(m);
					if(messagesReceived.size() == m.Model - 3){
						values.put(m.Key, m.Value);
						timeStamps.put(m.Key, Long.valueOf(m.Content));
						messagesReceived.clear();
						
						String displayContent = "inserted key " + m.Key;
						System.out.println("\n"+displayContent);
						messageIndex++;
						sent = false;
					}
					
				}else if(m.Origin != index){	// If it's a write request from other nodes
					values.put(m.Key, m.Value);
					timeStamps.put(m.Key, m.SentTime);
					m.Content = String.valueOf(m.SentTime);
					
					String displayContent = "inserted key " + m.Key;
					System.out.println("\n"+displayContent);
					sendToAnother(m, m.Origin);
				}
			}
			break;
			
		// Update a key value pair
		case 4:
			if(index == 'O'){	// For the coordinator, send out the message to every node
				sentToAllOthers(m);
			}else if(m.Origin == '\u0000'){	// If read the command from a text file
				m.Origin = index;
				m.Index = messageIndex;
				
				if(m.Model == 1 || m.Model == 2){	// If it's linearnizability or sequential consistent
					sendToAnother(m, 'O');
				}else{	// If it's eventual consistent
					sentToAllOthers(m);
					if(m.Model == 3){	// if W = 1, directly write to myself
						values.put(m.Key, m.Value);
						timeStamps.put(m.Key, new Date().getTime());
						
						String displayContent = "Key " + m.Key + " updated to " + m.Value;
						System.out.println("\n"+displayContent);
						messageIndex++;
						sent = false;
					}
				}
				
			}else if(m.Model == 1 || m.Model == 2){	// If received a message and it's linearizability or sequential consistent
				if(m.Origin == index){
					String displayContent = "Key " + values.get(m.Key) + " updated to " + m.Value;
					System.out.println("\n"+displayContent);
					
					values.put(m.Key, m.Value);
					
					if(m.Index == messageIndex){
						messageIndex++;
						sent = false;
					}
				}else{
					String displayContent = "Key " + m.Key + " changed from " + values.get(m.Key) + " to " + m.Value;
					System.out.println("\n"+displayContent);
					
					values.put(m.Key, m.Value);				
				}
			}else{	 // If received as eventual consistent
				if(m.Origin == index && m.Index == messageIndex && m.Model == 4){	// If it's a current write request from myself, only need to handle when model == 4
					messagesReceived.add(m);
					if(messagesReceived.size() == m.Model - 3){
						String displayContent = "Key " + values.get(m.Key) + " updated to " + m.Value;
						System.out.println("\n"+displayContent);
						
						values.put(m.Key, m.Value);
						timeStamps.put(m.Key, Long.valueOf(m.Content));
						messagesReceived.clear();
						
						messageIndex++;
						sent = false;
					}
					
				}else if(m.Origin != index){	// If it's a write request from other nodes
					String displayContent = "Key " + m.Key + " changed from " + values.get(m.Key) + " to " + m.Value;
					System.out.println("\n"+displayContent);
					
					values.put(m.Key, m.Value);
					timeStamps.put(m.Key, m.SentTime);
					
					m.Content = m.SentTime.toString();
					sendToAnother(m, m.Origin);
				}
				
				
			}
			break;
			
		// Search
		case 5:
			if(m.Origin == '\u0000'){	// If read the command from a text file
				m.Origin = index;
				m.Index = messageIndex;
				
				sentToAllOthers(m);
			}else{
				if(m.Origin != index){	// If it's a request from another node, send back whether it contains the Key
					if(values.get(m.Key) != null){
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
							if(currentMessage.Value.equals("True")){
								displayContent += currentMessage.From + " ";
							}
						}
						System.out.println("\n"+displayContent);

						messagesReceived.clear();
						messageIndex++;
						sent = false;
					}
				}
			}
			break;
			
		// Delay
		case 6:
			double sleepTime = Double.valueOf(m.Value);
			try {
				Thread.sleep((long)sleepTime*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			messageIndex++;
			sent = false;
			
			break;
			
		// Show all
		case 7:
			for(Entry<String, String> entry : values.entrySet()){
				System.out.println(entry.getKey() + "," + entry.getValue());
			}
	//		for(Entry<String, Long> entry : timeStamps.entrySet()){
	//			System.out.println(entry.getKey() + "," + entry.getValue());
	//		}
			System.out.println();
			messageIndex++;
			sent = false;
			break;
			
		// Repair
		case 8:
			if(m.Origin != index){	// received repair request from another node
				if(m.Key.equals("request")){	// node requesting all key value timestamp pairs
					m.Content = getAllKeyValueWithTime();
					sendToAnother(m, m.Origin);
				}else if(m.Key.equals("repair")){	// update key value timestamp pairs from the existing latest values
					List<String> contentList = new ArrayList<String>();
					contentList.add(m.Content);
					
					// combine and repair
					repairKeyValueToLatest(contentList);
					
					System.out.println("repaired");
					for(Entry<String, String> entry : values.entrySet()){
						System.out.println(entry.getKey() + "," + entry.getValue());
					}
					System.out.println();
					
				}
			}else if(m.Origin == index && m.Index == messageIndex){	// my own repair request returned from other nodes
				// count whether have received messages from all other nodes
				messagesReceived.add(m);
				if(messagesReceived.size() == 3){
					List<String> contentList = new ArrayList<String>();
					for(Message message : messagesReceived){
						contentList.add(message.Content);
					}
					
					// combine and repair
					repairKeyValueToLatest(contentList);
					
					// send to others
					m.Content = getAllKeyValueWithTime();
					m.Key = "repair";
					sentToAllOthers(m);
					
					System.out.println("repaired");
					for(Entry<String, String> entry : values.entrySet()){
						System.out.println(entry.getKey() + "," + entry.getValue());
					}
					System.out.println();

					messagesReceived.clear();
					messageIndex++;
					sent = false;
				}
			}
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
	
	public void sendToAnother(Message message, char receiver) throws ParseException{
		message.From = index;
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
	
	
	/**
	 * Method for getting the string representation of the whole key value hashmap with time stamp
	 * @return
	 */
	public String getAllKeyValueWithTime()
	{
		StringBuilder sb=new StringBuilder();
		for(String key: timeStamps.keySet())
		{
			String v=values.get(key);
			Long t=timeStamps.get(key);
			sb.append(key).append(",");
			if(v != null){
				sb.append(v);
			}
			sb.append(",").append(t).append("|");
		}
		return sb.toString();
	}
	
	
	/**
	 * Method for repairing the current node's key value pairs, after receiving all the key value 
	 * from the other nodes.
	 * 
	 * @param kvarray
	 * @throws ParseException
	 */
	public void repairKeyValueToLatest(List<String> kvarray) throws ParseException
	{
		for(String kvhashmap: kvarray)
		{
			String[] kvs=kvhashmap.split("\\|");
			if(kvs[0].length()<=0)
				continue;
			for(int i=0;i<kvs.length;i++)
			{
				String kv=kvs[i];
				String[] elements=kv.split(",",-1);
				if(elements[0].length()<=0)
					continue;
				String key=elements[0];
				String v=elements[1];
				Long ts= Long.valueOf(elements[2]);
				if(!timeStamps.containsKey(key) || timeStamps.get(key) < ts)
				{
					timeStamps.put(key, ts);
					if(values.containsKey(key) && v.length()==0){
						values.remove(key);
					}else{
						values.put(key, v);
					}
					
				}
			}
		}
	}
	
		
}
