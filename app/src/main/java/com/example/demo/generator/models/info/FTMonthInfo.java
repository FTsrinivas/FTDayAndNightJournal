package com.example.demo.generator.models.info;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class FTMonthInfo {
    public List<FTDayInfo> dayInfos = new ArrayList<>();
    public String monthTitle = "";
    public String monthShortTitle = "";
    public int year = 2020;
    public int month = 0;

    public Locale locale;
    private FTYearFormatInfo yearFormatInfo;
    private String weekFormat;

    public FTMonthInfo(Locale locale, FTYearFormatInfo formatInfo, String weekFormat) {
        this.locale = locale;
        this.yearFormatInfo = formatInfo;
        this.weekFormat = weekFormat;
    }

    public void generate(int month, int year) {
        //this.generateDaysInfo(month, year);
        this.month = month;
        this.year = year;

        Calendar startDate = new GregorianCalendar(yearFormatInfo.locale);
        startDate.set(year, month, startDate.getActualMinimum(Calendar.DAY_OF_MONTH));

        Locale monthLocale = yearFormatInfo.locale;
        if (locale.toLanguageTag().contains("ja")
                || locale.toLanguageTag().contains("zh_hans")
                || locale.toLanguageTag().contains("zh_hant")
                || locale.toLanguageTag().contains("ko")
                || locale.toLanguageTag().contains("zh")) {
            monthLocale = new Locale("en", "US");
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM", monthLocale);
        this.monthTitle = simpleDateFormat.format(startDate.getTime());
        simpleDateFormat = new SimpleDateFormat("MMM", yearFormatInfo.locale);
        this.monthShortTitle = simpleDateFormat.format(startDate.getTime());
        int numberOfDaysInMonth = startDate.getActualMaximum(Calendar.DAY_OF_MONTH);

        int weekDay = startDate.get(Calendar.DAY_OF_WEEK);
        int startOffset = 1 - weekDay;
        if (weekFormat.equals("2")) {
            if (weekDay == 1) {
                startOffset = -6;
            } else {
                startOffset = 2 - weekDay;
            }
        }

        Calendar startDayOfMonthCal = new GregorianCalendar(yearFormatInfo.locale);
        startDayOfMonthCal.setTime(startDate.getTime());
        startDayOfMonthCal.add(Calendar.DAY_OF_MONTH, startOffset);
        Calendar lastDayOfMonth = new GregorianCalendar(yearFormatInfo.locale);
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

        int numberOfDays = numberOfDaysInMonth + endOffset + Math.abs(startOffset);

        Calendar nextDate = startDayOfMonthCal;
        if (numberOfDays < 42) {
            numberOfDays = 42;
        }
        for (int i = 0; i < numberOfDays; i++) {
            FTDayInfo dayInfo = new FTDayInfo(yearFormatInfo.locale, yearFormatInfo.dayFormat);
            dayInfo.belongsToSameMonth = !nextDate.getTime().before(startDayOfMonthCal.getTime())
                    && !nextDate.getTime().after(lastDayOfMonth.getTime())
                    && !nextDate.getTime().before(yearFormatInfo.startMonth)
                    && !nextDate.getTime().after(yearFormatInfo.endMonth)
                    && isDateInSameMonth(startDate, nextDate);
            dayInfo.populateDateInfo(nextDate.getTime());
            this.dayInfos.add(dayInfo);
            nextDate.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private boolean isDateInSameMonth(Calendar startDate, Calendar nextDate) {
        return (startDate.get(Calendar.YEAR) == nextDate.get(Calendar.YEAR) &&
                startDate.get(Calendar.MONTH) == nextDate.get(Calendar.MONTH)
                );
    }

    public void generateDaysInfo(int month, int year) {
        Calendar startDate = new GregorianCalendar(yearFormatInfo.locale);
        startDate.set(Calendar.YEAR, year);
        startDate.set(Calendar.MONTH, month);
        int numberOfDays = startDate.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int nextDay = 0; nextDay < numberOfDays; nextDay++) {
            FTDayInfo dayInfo = new FTDayInfo(yearFormatInfo.locale, yearFormatInfo.dayFormat);
            Calendar calendar = new GregorianCalendar(yearFormatInfo.locale);
            calendar.set(this.year, this.month, nextDay);
            dayInfo.populateDateInfo(calendar.getTime());
            this.dayInfos.add(dayInfo);
        }
    }
}