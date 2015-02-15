package channel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerThread implements Runnable{
	
	protected Socket clientSocket = null;

	public ServerThread(Socket clientSocket){
		this.clientSocket = clientSocket;
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
	
	// save key-value pair ??
	// output message ??
	public void handle(Socket clientSocket){
        try {
        	InputStream input = clientSocket.getInputStream();
			InputStreamReader inputReader = new InputStreamReader(input);
            BufferedReader bufferReader = new BufferedReader(inputReader);
            String line = bufferReader.readLine();
			
			System.out.println(line);
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
