package channel;
import java.util.Calendar;
import java.util.Date;


public class Message {
	String Content;
	Date SentTime;
	Date EstimatedDeliverTime;
	Date RealDeliverTime;
	public Message(String c)
	{
		Content=c;
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
