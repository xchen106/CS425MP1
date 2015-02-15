package node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import channel.Message;

public class Node {
	
	Client clientThread;
	Server serverThread;
	char index;
	
	
/*	public static void main(String[] args){
		new Node().initialization("accessories/configuration", 'A');
	}*/
	
	public void handleMessage(Message m)
	{
		
	}
	public void initialization(String configurationFile, char index){
		
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
	
}
