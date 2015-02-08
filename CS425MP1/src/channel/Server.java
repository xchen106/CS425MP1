package channel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


// reference http://tutorials.jenkov.com/java-multithreaded-servers/multithreaded-server.html


public class Server {
	protected int port = 8080;
    protected ServerSocket listener = null;
    protected Thread runningThread = null;
    
    // initialize the port number
    public Server(int port){
    	this.port = port;
    }
	
	public void run(){
		// synchronize
		synchronized(this){
            this.runningThread = Thread.currentThread();
        }
		
		// open server socket
		try {
            this.listener = new ServerSocket(this.port);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port "+this.port, e);
        }
        
		// wait for request
        while(true){
            Socket clientSocket = null;
            try {
                clientSocket = this.listener.accept();
            } catch (IOException e) {
            	throw new RuntimeException("Error connecting", e);
            }
            // start a new thread to handle the request
            new Thread(new ServerThread(clientSocket)).start();
        }
        
	}
	
	
	public static void main(String[] args){
		new Server(9090).run();
	}
	
}
