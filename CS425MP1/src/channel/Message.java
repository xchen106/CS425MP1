package channel;
import java.util.Calendar;
import java.util.Date;


public class Message {
	public String Content;
	Date SentTime;
	Date EstimatedDeliverTime;
	Date RealDeliverTime;
	public String Key;
	public String Value;
	public char From;
	public char To;
	public int MaxDelay;

	/**
	 * 0 -- linearizable
	 * 1 -- sequential
	 * 2 -- event W1 R1
	 * 3 -- event W2 R2
	 */
	public int Model;
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
	public int Operation;
	
	
	public Message(String c)
	{
		String[] parseResult = c.split("\\s+");
		String op = parseResult[0];
		op=op.toLowerCase();
		switch(op)
		{
		case "send": this.Operation=0;break;
		case "delete": this.Operation=5; this.Key=parseResult[1];this.Model = Integer.parseInt(parseResult[2]); break;
		case "get": this.Operation=6; this.Key=parseResult[1];this.Value=parseResult[2]; this.Model = Integer.parseInt(parseResult[3]); break;
		case "insert": this.Operation=7; this.Key=parseResult[1];this.Value=parseResult[2];this.Model = Integer.parseInt(parseResult[3]); break;
		case "update": this.Operation=8; this.Key=parseResult[1];this.Value=parseResult[2];this.Model = Integer.parseInt(parseResult[3]); break;
		case "search": this.Operation=9; this.Key=parseResult[1]; break;
		case "delay": this.Operation=11; this.Value=parseResult[1];break;
		case "show-all": this.Operation=12;break;
		default: System.out.println("WRONG FORMAT OF INPUT!");
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
