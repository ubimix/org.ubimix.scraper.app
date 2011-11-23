package org.webreformatter.resources.adapters.cache;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class contains utility methods used to transform dates
 * (serialization/deserialization and so on).
 * 
 * @author kotelnikov
 */
public class DateUtil {

    /**
     * One day in milliseconds.
     */
    public final static int DAY;

    /**
     * Date-time formats
     */
    private final static SimpleDateFormat[] FORMATS;

    /**
     * One hour in milliseconds.
     */
    public final static int HOUR;

    /**
     * One minute in milliseconds.
     */
    public final static int MIN;

    /**
     * One second == 1000 milliseconds
     */
    public final static int SEC;

    // Internal fields initialization
    static {
        SEC = 1000;
        MIN = SEC * 60;
        HOUR = MIN * 60;
        DAY = HOUR * 24;

        // Examples: // Wed, 30 Dec 2009 21:38:03 GMT
        String[] templates = {
            "EEE, d MMM yyyy HH:mm:ss z",
            "EEE, d MMM yyyy HH:mm:ss Z" };
        FORMATS = new SimpleDateFormat[templates.length];
        int i = 0;
        for (String template : templates) {
            SimpleDateFormat format = new SimpleDateFormat(template);
            FORMATS[i++] = format;
        }
    }

    /**
     * Returns the string representation of the specified date.
     * 
     * @param date the date to format
     * @return the string representation of the specified date.
     */
    public static String formatDate(long date) {
        return FORMATS[0].format(date);
    }

    /**
     * Returns the number representation of the date corresponding to the given
     * string
     * 
     * @param str the serialized form of the datetime
     * @return the long representation of the date corresponding to the given
     *         string
     */
    public static long parseDate(String str) {
        long result = -1;
        for (SimpleDateFormat format : FORMATS) {
            try {
                Date date = format.parse(str);
                result = date.getTime();
                break;
            } catch (Exception e) {
                // Just skip it and try with the next format
            }
        }
        return result;
    }

}
