package channel;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.concurrent.Semaphore;



public class DelayedChannel extends Thread{

	//The queue storing the contents to be sent
	ArrayDeque<Message> Contents; 
	//Maximum delay for the channel
	int MaxDelay;
	//The socket needed for sending messages
	Socket MySocket;
	//Semaphore for blocking when empty
	Semaphore CurrentCount;
	//Writer for output to channel
	BufferedWriter out;
	public DelayedChannel(String hostname, String portnum, int max) throws NumberFormatException, UnknownHostException, IOException
	{
		super("DelayedChannel");
		MySocket =new Socket(hostname, Integer.parseInt(portnum));
		out= new BufferedWriter(new OutputStreamWriter(MySocket.getOutputStream()));
		MaxDelay=max;
		Contents= new ArrayDeque<Message>();
		CurrentCount=new Semaphore(0);
	}
	/*
	 * The put method for all the contents
	 */
	public void putCommands(String c)
	{
		Message m=new Message(c);
				
		m.MaxDelay = MaxDelay;
		m.setSentTime();
		m.setEstimatedDeliverTime(MaxDelay);
		Contents.add(m);
		//notify the blocking thread
		CurrentCount.release();
		
	}
	public void putMessageString(String c) throws ParseException
	{
		Message m = new Message().stringToMessage(c);
		
		m.MaxDelay = MaxDelay;
		m.setSentTime();
		m.setEstimatedDeliverTime(MaxDelay);
		Contents.add(m);
		//notify the blocking thread
		CurrentCount.release();
	}
	
	public void putMessage(Message m){
		m.MaxDelay = MaxDelay;
		m.setSentTime();
		m.setEstimatedDeliverTime(MaxDelay);
		Contents.add(m);
		//notify the blocking thread
		CurrentCount.release();
	}
	
	
	/*
	 * The run method, executed for every thread
	 */
	public void run()
	{
		
		while(true)
		{
			try {
				//Block when empty
				CurrentCount.acquire();
				//get Message out from the queue
				Message m=Contents.poll();
				//get current time
				Date date= new Date();
				//if need to be delayed, then sleep for the difference
				if(date.before(m.EstimatedDeliverTime))
				{
					long milisecond=m.EstimatedDeliverTime.getTime() - date.getTime();
					Thread.sleep(milisecond);
				}
				//send the message
				out.write(m.messageToString()+"\n");
				out.flush();
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
}
