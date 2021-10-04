package com.example.demo.generator.models.info;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class FTYearInfoMonthly {
    public List<FTMonthInfo> monthInfos = new ArrayList<>();
    public List<FTMonthlyCalendarInfo> monthlyCalendarInfos = new ArrayList<>();

    public FTYearFormatInfo yearFormatInfo;
    private String weekFormat = "";

    public FTYearInfoMonthly(FTYearFormatInfo yearFormatInfo) {
        this.yearFormatInfo = yearFormatInfo;
        this.weekFormat = yearFormatInfo.weekFormat;
    }

    public void generate() {
        int totalMonths = Math.abs(monthsBetween(yearFormatInfo.startMonth, yearFormatInfo.endMonth));
        Calendar startDate = new GregorianCalendar(yearFormatInfo.locale);
        startDate.setTime(yearFormatInfo.startMonth);
        int curMonth = startDate.get(Calendar.MONTH);
        int curYear = startDate.get(Calendar.YEAR);
        for (int i = 0; i < totalMonths; i++) {
            FTMonthInfo monthInfo = new FTMonthInfo(yearFormatInfo.locale, yearFormatInfo, weekFormat);
            if (curMonth >= 12) {
                curMonth = Calendar.JANUARY;
                curYear += 1;
            }
            monthInfo.generate(curMonth, curYear);
            monthInfos.add(monthInfo);

            FTMonthlyCalendarInfo monthlyCalendar = new FTMonthlyCalendarInfo(yearFormatInfo.locale, yearFormatInfo.dayFormat, this.weekFormat);
            monthlyCalendar.generate(curMonth, curYear);
            monthlyCalendarInfos.add(monthlyCalendar);

            curMonth += 1;
        }
    }

    private int monthsBetween(Date startDate, Date endDate) {
        Calendar start = new GregorianCalendar(yearFormatInfo.locale);
        start.setTime(startDate);
        Calendar end = new GregorianCalendar(yearFormatInfo.locale);
        end.setTime(endDate);

        int year = end.get(Calendar.YEAR) - start.get(Calendar.YEAR);
        int months = (end.get(Calendar.MONTH) + 1) - ((start.get(Calendar.MONTH) + 1));
        if (end.get(Calendar.DAY_OF_MONTH) < (start.get(Calendar.DAY_OF_MONTH))) {
            months--;
        }
        return Math.abs(months + (year * 12) + 1);
    }
}
