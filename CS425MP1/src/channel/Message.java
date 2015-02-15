package channel;
import java.util.Calendar;
import java.util.Date;


public class Message {
	String Content;
	Date SentTime;
	Date EstimatedDeliverTime;
	Date RealDeliverTime;
	String key;
	String value;
	char from;
	char to;
	/**
	 * 0 -- linearizable
	 * 1 -- sequential
	 * 2 -- event W1 R1
	 * 3 -- event W2 R2
	 */
	int model;
	/**
	 * 0 -- print (send)
	 * 1 -- delete
	 * 2 -- get
	 * 3 -- insert
	 * 4 -- update
	 * 5 -- delete (coordinator)
	 * 6 -- get (coordinator)
	 * 7 -- insert (coordinator)
	 * 8 -- update (coordinator)
	 * 9 -- search
	 * 10-- has key or not
	 * 11-- delay
	 * 12-- show-all
	 */
	int operation;
	
	
	public Message(String c)
	{
		String[] parseResult = c.split("\\s+");
		String op = parseResult[0];
		op=op.toLowerCase();
		switch(op)
		{
		case "send": this.operation=0;
		
		}
		
	}
	public void setSentTime()
	{
		SentTime=new Date();
	}
	public void setEstimatedDeliverTime(int MaxDelay)
	{
		int delay=(int)(Math.random()*MaxDelay);
		Calendar cal = Calendar.getInstance(); // creates calendar
		cal.setTime(new Date()); // sets calendar time/date
		cal.add(Calendar.SECOND, delay); // adds one hour
		EstimatedDeliverTime=cal.getTime(); // returns new date object, one hour in the future
		
	}
	public void setRealDeliverTime()
	{
		RealDeliverTime=new Date();
	}
	public static void main(String[] args)
	{
		Message m=new Message("Hello");
		m.setSentTime();
		System.out.println(m.SentTime.getSeconds());
		m.setEstimatedDeliverTime(10);
		System.out.println(m.EstimatedDeliverTime.getSeconds());
	}
}
