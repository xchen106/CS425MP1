package channel;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
	
	public void transformForCoordinator()
	{
		if(this.Operation>=5&&this.Operation<=8)
		{
			this.To=this.From;
			this.From='O';
			this.Operation-=4;
		}
	}
	public Message()
	{
		
	}
	public Message(String c)
	{
		String[] parseResult = c.split("\\s+");
		String op = parseResult[0];
		op=op.toLowerCase();
		switch(op)
		{
		case "send": this.Operation=0;this.Content=parseResult[1];this.To=parseResult[2].charAt(0);break;
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
	public String messageToString()
	{
		StringBuilder sb=new StringBuilder();
		sb.append(Content).append(";");
		sb.append(Key).append(";");
		sb.append(Value).append(";");
		sb.append(From).append(";");
		sb.append(To).append(";");
		sb.append(MaxDelay).append(";");
		sb.append(Operation).append(";");
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sb.append(formatter.format(SentTime)).append(";");
		sb.append(formatter.format(EstimatedDeliverTime)).append(";");
		sb.append(formatter.format(RealDeliverTime));
		return sb.toString();
	}
	public Message stringToMessage(String s) throws ParseException
	{
		String[] ss=s.split(";");
		Message m=new Message();
		m.Content=ss[0];
		m.Key=ss[1];
		m.Value=ss[2];
		m.From=ss[3].charAt(0);
		m.To=ss[4].charAt(0);
		m.MaxDelay=Integer.parseInt(ss[5]);
		m.Operation=Integer.parseInt(ss[6]);
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		m.SentTime=formatter.parse(ss[7]);
		m.EstimatedDeliverTime=formatter.parse(ss[8]);
		m.RealDeliverTime=formatter.parse(ss[9]);
		return m;
		
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
	public static void main(String[] args) throws ParseException
	{
		Message m=new Message("Send hello A");
		m.setSentTime();
		m.setEstimatedDeliverTime(10);
		m.setRealDeliverTime();
		System.out.println(m.messageToString());
		System.out.println(m.stringToMessage( m.messageToString()).messageToString());
	}
}
