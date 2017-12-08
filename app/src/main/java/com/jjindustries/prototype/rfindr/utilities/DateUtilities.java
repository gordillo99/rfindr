package com.jjindustries.prototype.rfindr.utilities;

import android.support.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtilities {

    private static final String dbDateFormat = "yyyy-MM-dd HH:mm:ss.SSS";

    public static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat(dbDateFormat);
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public static String formatDate(Date date) {
        SimpleDateFormat sdfDate = new SimpleDateFormat(dbDateFormat);
        String strDate = sdfDate.format(date);
        return strDate;
    }

    @Nullable
    public static Date convertStringToDate(String dateString) {
        SimpleDateFormat df = new SimpleDateFormat(dbDateFormat);
        Date date;
        try {
            date = df.parse(dateString);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date addTimeToDate(Date date, int type, int numUnits) throws UnsupportedOperationException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        switch(type) {
            case Calendar.SECOND: // add seconds
                cal.add(Calendar.SECOND, numUnits);
                break;
            case Calendar.MINUTE: // add minutes
                cal.add(Calendar.MINUTE, numUnits);
                break;
            case Calendar.HOUR_OF_DAY: // add hours
                cal.add(Calendar.HOUR_OF_DAY, numUnits);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported type of time.");
        }

        return cal.getTime();
    }
}
