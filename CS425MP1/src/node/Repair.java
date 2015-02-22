package node;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
			String[] kvs=kvhashmap.split("|");
			for(int i=0;i<kvs.length-1;i++)
			{
				String kv=kvs[i];
				String[] elements=kv.split(";");
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


}
