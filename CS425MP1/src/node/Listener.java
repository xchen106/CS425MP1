package node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.ParseException;

import channel.Message;

public class Listener implements Runnable{
	
	protected Socket clientSocket = null;
	Node node;

	public Listener(Socket clientSocket, Node node){
		this.clientSocket = clientSocket;
		this.node = node;
	}
	
	// handle received message
	public void run(){
		try {
        	InputStream input = clientSocket.getInputStream();
			InputStreamReader inputReader = new InputStreamReader(input);
            BufferedReader bufferReader = new BufferedReader(inputReader);
            while(true){
	            String string = bufferReader.readLine();
				Message m = new Message().stringToMessage(string);
				m.setRealDeliverTime();
				
				this.node.handleMessage(m);
            }
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
