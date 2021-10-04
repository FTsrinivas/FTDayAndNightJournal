package com.example.demo.generator.models.info;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class FTMonthlyCalendarInfo {
    public List<FTDayInfo> dayInfos = new ArrayList<>();
    public String year = "2020";
    public String shortMonth = "Jan";
    public String fullMonth = "Jan";
    public Locale locale = Locale.getDefault();

    private FTDayFormatInfo format;
    private String weekFormat;
    private int numberOfdaysInMonth = 1;

    public FTMonthlyCalendarInfo(Locale locale, FTDayFormatInfo formatInfo, String weekFormat) {
        this.locale = locale;
        this.format = formatInfo;
        this.weekFormat = weekFormat;
    }

    void generate(int month, int year) {
        Calendar startDate = new GregorianCalendar(locale);
        startDate.set(year, month, startDate.getActualMinimum(Calendar.DAY_OF_MONTH));

        numberOfdaysInMonth = startDate.getActualMaximum(Calendar.DAY_OF_MONTH) - 1;

        FTDayInfo dateInfo = new FTDayInfo(locale, format);
        dateInfo.populateDateInfo(startDate.getTime());
        this.shortMonth = dateInfo.monthString;
        this.fullMonth = dateInfo.fullMonthString;
        this.year = dateInfo.yearString;

        int weekDay = startDate.get(Calendar.DAY_OF_WEEK);
        int startOffset = 1 - weekDay;
        if (weekFormat.equals("2")) {
            if (weekDay == 1) {
                startOffset = -6;
            } else {
                startOffset = 2 - weekDay;
            }
        }

        Calendar startDayOfMonthCal = new GregorianCalendar(locale);
        startDayOfMonthCal.setTime(startDate.getTime());
        startDayOfMonthCal.add(Calendar.DAY_OF_MONTH, startOffset);
        Calendar lastDayOfMonth = new GregorianCalendar(locale);
        lastDayOfMonth.setTime(startDate.getTime());
        lastDayOfMonth.set(Calendar.DAY_OF_MONTH, lastDayOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH));

        int endOffset = 7 - lastDayOfMonth.get(Calendar.DAY_OF_WEEK);
        if (weekFormat.equals("2")) {
            if (lastDayOfMonth.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                endOffset = 0;
            } else {
                endOffset = 8 - lastDayOfMonth.get(Calendar.DAY_OF_WEEK);
            }
        }

        int numberOfDays = numberOfdaysInMonth + endOffset + Math.abs(startOffset);

        Calendar nextDate = startDayOfMonthCal;
        if (numberOfDays < 42) {
            numberOfDays = 42;
        }

        for (int i = 0; i < numberOfDays; i++) {
            FTDayInfo dayInfo = new FTDayInfo(locale, format);
            dayInfo.belongsToSameMonth = nextDate.get(Calendar.MONTH) == month;
            dayInfo.populateDateInfo(nextDate.getTime());
            this.dayInfos.add(dayInfo);
            nextDate.add(Calendar.DATE, 1);
        }
    }
}
