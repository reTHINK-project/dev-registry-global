package eu.rethink.globalregistry.util;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Half-Blood on 1/4/2017.
 */
public class XSDDateTime {
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

    //A substring of year, month, day,hours,minute,second is computed. The String "value" is then parsed to date.
    //If the component of parsed date is same as computed substring, then the date is valid as well as values inside date are also valid.
    //For timezone, compute the substring of Timezone and pass it over the function isValidTimeZone.
    //If it returns true, that means the timezone attached to the date is valid.

    public static boolean validateXSDDateTime(String value)  {
        Date date = null;

        try{

            String value_year = value.substring(0,4);
            String value_month = value.substring(5,7);
            String value_day = value.substring(8,10);
            String value_hour = value.substring(11,13);
            String value_minute = value.substring(14,16);
            String value_second = value.substring(17,19);
            String value_timeZone = value.substring(19);
            if(value_timeZone.equals("Z")){
                value_timeZone = "+00:00";
            }
            String isValid_timeZone = "GMT" + value_timeZone;
            boolean isValid_tz = isValidTimeZone(isValid_timeZone);
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            date = formatter.parse(value);
            SimpleDateFormat year_format = new SimpleDateFormat("yyyy");
            String date_year = year_format.format(date);
            SimpleDateFormat month_format = new SimpleDateFormat("MM");
            String date_month = month_format.format(date);
            SimpleDateFormat day_format = new SimpleDateFormat("dd");
            String date_day = day_format.format(date);
            SimpleDateFormat hour_format = new SimpleDateFormat("HH");
            String date_hour = hour_format.format(date);
            SimpleDateFormat minute_format = new SimpleDateFormat("mm");
            String date_minute = minute_format.format(date);
            SimpleDateFormat second_format = new SimpleDateFormat("ss");
            String date_second = second_format.format(date);

            if(value_year.equals(date_year) && value_month.equals(date_month) && value_day.equals(date_day)
            && value_hour.equals(date_hour) && value_minute.equals(date_minute) && value_second.equals(date_second)
                    && isValid_tz){
                return true;
            }
            /*
            DateFormat tzFormatter = new SimpleDateFormat("Z");
            String timezone = tzFormatter.format(date);
            if ((!value.equals(formatter.format(date) + timezone.substring(0, 3) + ":" + timezone.substring(3))) &&
                    (!value.equals(formatter.format(date)))) {
                date = null;
            }*/
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isValidTimeZone(final String timeZone) {
        final String DEFAULT_GMT_TIMEZONE = "GMT";
        if (timeZone.equals(DEFAULT_GMT_TIMEZONE)) {
            return true;
        } else {
            // if custom time zone is invalid,
            // time zone id returned is always "GMT" by default
            String id = TimeZone.getTimeZone(timeZone).getID();
            if (!id.equals(DEFAULT_GMT_TIMEZONE)) {
                return true;
            }
        }
        return false;
    }
}
