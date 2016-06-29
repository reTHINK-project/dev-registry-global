package eu.rethink.globalregistry.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class XSDDateTime
{
	private static final DateTimeFormatter XML_DATE_TIME_FORMAT = ISODateTimeFormat.dateTimeNoMillis();
	
	private static final DateTimeFormatter DATE_TIME_FORMAT = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);
	
	public static DateTime parseXSDDateTime(String xsdDateTime)
	{
		return XML_DATE_TIME_FORMAT.parseDateTime(xsdDateTime);
	}
	
	public static String exportXSDDateTime(Date date)
	{
		DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		DateFormat tzFormatter = new SimpleDateFormat("Z");
		String timezone = tzFormatter.format(date);
		
		return formatter.format(date) + timezone.substring(0, 3) + ":" + timezone.substring(3);
	}
	
	/*public static String exportXSDDateTime(DateTime dateTime)
	{
		return DATE_TIME_FORMAT.print(dateTime);
	}*/
}