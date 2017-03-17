package com.coremantra.tutorial.thenewyorktimes.utils;

/**
 * Created by radhikak on 3/17/17.
 */

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    static DateFormat mmDDYYYYDateFormat = new SimpleDateFormat("MM/dd/yyyy");

    public static String getCurrentDate() {

        Date date = new Date();
        return mmDDYYYYDateFormat.format(date);
    }

    /**
     * Function generates the required string the the format MM/dd/yyyy
     * @param selectedYear
     * @param selectedDay
     * @param selectedMonth
     * @return
     */
    public static String createBeginDateString (
            int selectedYear, int selectedDay, int selectedMonth) {

        String year = String.valueOf(selectedYear);
        String month = String.valueOf(selectedMonth + 1);
        String day = String.valueOf(selectedDay);

        StringBuilder builder = new StringBuilder();
        builder.append(month).append("/")
                .append(day).append("/")
                .append(year);

        return builder.toString();
    }
}