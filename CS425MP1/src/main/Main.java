package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import node.Client;
import node.Server;

public class Main {
	
	public static void main(String[] args){
		new Main().initialization("accessories/configuration");
	}
	

	public void initialization(String configurationFile){
		
		// read the addresses and port numbers from the configuration file
		HashMap<String, String> configuration = new Utils().parseConfigure(configurationFile);
		
		Client clientThread = new Client(configuration);
		Server serverThread = new Server(configuration);
		
		clientThread.start();
		serverThread.start();
		
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
