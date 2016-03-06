package ir.mohandesplus.examnight.utils;

public class TimeUtils {

    public final static long SECOND = 1000;
    public final static long MINUTE = 60 * SECOND;
    public final static long HOUR = 60 * MINUTE;
    public final static long DAY = 24 * HOUR;

    public static long getMinuteDifference(long startTime, long endTime) {
        return (endTime - startTime) / (MINUTE);
    }

    public static String getTimeDifference(long startTime, long endTime) {

        long difference = endTime - startTime;
        int day=0, hour=0, minute=0;
        String and = " و ";

        while (difference > DAY) {
            day++;
            difference -= DAY;
        }
        while (difference > HOUR) {
            hour++;
            difference -= HOUR;
        }
        while (difference > MINUTE) {
            minute++;
            difference -= minute;
        }

        if (day<1 && hour<1 && minute<1) return "کمتر از یک‌دقیقه";

        String result = "";
        if (day > 0) result +=  LanguageUtils.getPersianNumbers(day + " روز" + and);
        if (hour > 0) result +=  LanguageUtils.getPersianNumbers(hour + " ساعت" + and);
        if (minute > 0) result +=  LanguageUtils.getPersianNumbers(minute + "دقیقه");

        return result;

    }

}
