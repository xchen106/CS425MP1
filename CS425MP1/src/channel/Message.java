package channel;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class Message {
	public String Content;
	public Date SentTime;
	public Date EstimatedDeliverTime;
	public Date RealDeliverTime;
	public String Key;
	public String Value;
	public char From;
	public char To;
	public char Origin;
	public int MaxDelay;
	public int Index;

	/**
	 * 1 -- linearizable
	 * 2 -- sequential
	 * 3 -- event W1 R1
	 * 4 -- event W2 R2
	 */
	public int Model;
	/**
	 * 0 -- print (send)
	 * 1 -- delete
	 * 2 -- get
	 * 3 -- insert
	 * 4 -- update
	 * 5 -- search
	 * 6-- delay
	 * 7-- show-all
	 */
	
	// TODO: repair
	
	public int Operation;
	
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
		case "delete": this.Operation=1; this.Key=parseResult[1]; break;
		case "get": this.Operation=2; this.Key=parseResult[1]; this.Model = Integer.parseInt(parseResult[2]); break;
		case "insert": this.Operation=3; this.Key=parseResult[1];this.Value=parseResult[2];this.Model = Integer.parseInt(parseResult[3]); break;
		case "update": this.Operation=4; this.Key=parseResult[1];this.Value=parseResult[2];this.Model = Integer.parseInt(parseResult[3]); break;
		case "search": this.Operation=5; this.Key=parseResult[1]; break;
		case "delay": this.Operation=6; this.Value=parseResult[1];break;
		case "show-all": this.Operation=7;break;
		default: System.out.println("WRONG FORMAT OF INPUT!");
		}
		
	}
	
	public String messageToString()
	{
		StringBuilder sb=new StringBuilder();
		sb.append(Content).append(";");
		sb.append(Key).append(";");
		sb.append(Value).append(";");
		sb.append(Origin).append(";");
		sb.append(From).append(";");
		sb.append(To).append(";");
		sb.append(MaxDelay).append(";");
		sb.append(Operation).append(";");
		sb.append(Model).append(";");
		sb.append(Index).append(";");
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if(SentTime != null)
			sb.append(formatter.format(SentTime));
		sb.append(";");
		if(EstimatedDeliverTime != null)
			sb.append(formatter.format(EstimatedDeliverTime));
		sb.append(";");
		if(RealDeliverTime != null)
			sb.append(formatter.format(RealDeliverTime));
		return sb.toString();
	}
	public Message stringToMessage(String s) throws ParseException
	{
		String[] ss=s.split(";", -1);
		
		Message m=new Message();
		m.Content=ss[0];
		m.Key=ss[1];
		m.Value=ss[2];
		m.Origin=ss[3].charAt(0);
		m.From=ss[4].charAt(0);
		m.To=ss[5].charAt(0);
		m.MaxDelay=Integer.parseInt(ss[6]);
		m.Operation=Integer.parseInt(ss[7]);
		m.Model=Integer.parseInt(ss[8]);
		m.Index=Integer.parseInt(ss[9]);
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if(ss[10]!=null && ss[10].equals("")==false) m.SentTime=formatter.parse(ss[10]);
		else m.SentTime = null;
		if(ss[11]!=null && ss[11].equals("")==false) m.EstimatedDeliverTime=formatter.parse(ss[11]);
		else m.EstimatedDeliverTime = null;
		if(ss[12]!=null && ss[12].equals("")==false) m.RealDeliverTime=formatter.parse(ss[12]);
		else m.RealDeliverTime = null;
		
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
