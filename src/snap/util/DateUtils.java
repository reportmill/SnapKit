package snap.util;
import java.util.*;
import static java.util.Calendar.*;

/**
 * Some helper methods for Date.
 */
public class DateUtils {

    /**
     * Returns the number of years between two dates.
     */
    public static int getYearsBetween(Date aDate1, Date aDate2)
    {
        Calendar a = getCalendar(aDate1);
        Calendar b = getCalendar(aDate2);
        int diff = b.get(YEAR) - a.get(YEAR);
        if (a.get(MONTH)>b.get(MONTH) || (a.get(MONTH)==b.get(MONTH) && a.get(DATE)>b.get(DATE)))
            diff--;
        return diff;
    }

    /**
     * Returns the number of months between two dates.
     */
    public static int getMonthsBetween(Date aDate1, Date aDate2)
    {
        Calendar a = getCalendar(aDate1);
        Calendar b = getCalendar(aDate2);
        int diff = b.get(MONTH) - a.get(MONTH);
        if (a.get(DATE)>b.get(DATE))
            diff--;
        return diff;
    }

    /**
     * Returns an age string between two dates.
     */
    public static String getAgeString(Date aDate1, Date aDate2)
    {
        int years = getYearsBetween(aDate1, aDate2);
        int months = getMonthsBetween(aDate1, aDate2); if (months<0) months += 12;
        String str = years + "yr"; if (months!=0) str += " " + months + 'm';
        return str;
    }

    /**
     * Returns a calendar for given date.
     */
    public static Calendar getCalendar(Date aDate)
    {
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(aDate);
        return cal;
    }
}