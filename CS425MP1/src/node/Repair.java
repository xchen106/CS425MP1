package node;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Repair extends Node
{
	/**
	 * Method for getting the string representation of the whole key value hashmap with time stamp
	 * @return
	 */
	public String getAllKeyValueWithTime()
	{
		StringBuilder sb=new StringBuilder();
		for(String key: values.keySet())
		{
			String v=values.get(key);
			Date t=timeStamps.get(key);
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String ts=formatter.format(t);
			sb.append(key).append(";").append(v).append(";").append(ts).append("|");
		}
		return sb.toString();
	}
	/**
	 * Method for repairing the current node's key value pairs, after receiving all the key value 
	 * from the other nodes.
	 * 
	 * @param kvarray
	 * @throws ParseException
	 */
	public void repairKeyValueToLatest(ArrayList<String> kvarray) throws ParseException
	{
		for(String kvhashmap: kvarray)
		{
			String[] kvs=kvhashmap.split("\\|");
			if(kvs[0].length()<=0)
				continue;
			for(int i=0;i<kvs.length;i++)
			{
				String kv=kvs[i];
				String[] elements=kv.split(";");
				if(elements[0].length()<=0)
					continue;
				String key=elements[0];
				String v=elements[1];
				DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date ts=formatter.parse(elements[2]);
				if(!timeStamps.containsKey(key)||timeStamps.get(key).before(ts))
				{
					timeStamps.put(key, ts);
					values.put(key, v);
				}
			}
		}
	}
	public static void main(String[] args)
	{
		Repair rp=new Repair();
		rp.values=new HashMap<String,String>();
		rp.timeStamps=new HashMap<String,Date>();
		rp.values.put("key1", "value1");
		rp.values.put("key2", "value2");
		rp.timeStamps.put("key1",new Date());
		rp.timeStamps.put("key2", new Date());
		System.out.println(rp.getAllKeyValueWithTime());
		String s=rp.getAllKeyValueWithTime();
		rp.values=new HashMap<String,String>();
		rp.timeStamps=new HashMap<String,Date>();
		rp.values.put("key2", "value3");
		rp.timeStamps.put("key2", new Date());
		ArrayList<String> kvarray=new ArrayList<String>();
		kvarray.add(s);
		try {
			rp.repairKeyValueToLatest(kvarray);
			System.out.println("Done");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
