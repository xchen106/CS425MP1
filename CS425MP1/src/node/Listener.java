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
	
	public void run(){
		try {			
			// handle the request
			handle(clientSocket);
			
			// close the socket
            clientSocket.close();
		} catch (IOException e) {
            e.printStackTrace();
        }
		
	}
	
	// handle received message
	public void handle(Socket clientSocket){
        try {
        	InputStream input = clientSocket.getInputStream();
			InputStreamReader inputReader = new InputStreamReader(input);
            BufferedReader bufferReader = new BufferedReader(inputReader);
            // TODO: while(true)
            String string = bufferReader.readLine();
			Message m = new Message().stringToMessage(string);
			m.setRealDeliverTime();
			
			this.node.handleMessage(m);
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
}
