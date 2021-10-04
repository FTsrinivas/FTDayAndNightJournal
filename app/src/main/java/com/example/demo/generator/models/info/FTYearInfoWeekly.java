package com.example.demo.generator.models.info;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FTYearInfoWeekly {

    public List<FTWeekInfo> weeklyInfos = new ArrayList<>();
    String weekFormat = "1";
    private FTYearFormatInfo format;

    public FTYearInfoWeekly(FTYearFormatInfo formatInfo) {
        format = formatInfo;
        this.weekFormat = format.weekFormat;
    }

    public void generate() {
        Calendar startDate = new GregorianCalendar(format.locale);
        startDate.setTime(format.startMonth);
        startDate.set(startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH), startDate.getActualMinimum(Calendar.DAY_OF_MONTH));
        int startWeekDay = startDate.get(Calendar.DAY_OF_WEEK);
        int weekStartOff = Integer.parseInt(weekFormat);
        int startOffset = 1 - startWeekDay;
        if (weekStartOff == 2) {
            if (startWeekDay == 1) {
                startOffset = -6;
            } else {
                startOffset = 2 - startWeekDay;
            }
        }
        startDate.add(Calendar.DAY_OF_MONTH, startOffset);

        Calendar endDateFirst = new GregorianCalendar(format.locale);
        endDateFirst.setTime(format.endMonth);

        int daysInMonth = endDateFirst.getActualMaximum(Calendar.DAY_OF_MONTH);
        Calendar endDate = new GregorianCalendar(format.locale);
        endDate.set(endDateFirst.get(Calendar.YEAR), endDateFirst.get(Calendar.MONTH), daysInMonth);
        int endWeekDay = endDate.get(Calendar.DAY_OF_WEEK);
        if (weekStartOff == 1) {
            endDate.add(Calendar.DAY_OF_WEEK, 7 - endWeekDay);
        } else {
            startOffset = 6 - endWeekDay;
            if (endWeekDay == 1) {
                startOffset = 0;
            }
            endDate.add(Calendar.DAY_OF_WEEK, startOffset);
        }
        int numberOfWeeks = weeksBetween(startDate, endDate);
        Calendar weekDay = startDate;
        for (int i = 0; i < numberOfWeeks; i++) {
            FTWeekInfo weekInfo = new FTWeekInfo(format.locale, format.dayFormat);
            weekInfo.generate(weekDay.getTime());
            weekDay.add(Calendar.DAY_OF_YEAR, 7);
            this.weeklyInfos.add(weekInfo);
        }
    }

    private int weeksBetween(Calendar startDate, Calendar endDate) {
        setToZero(startDate);
        setToZero(endDate);
        int start = (int) TimeUnit.MILLISECONDS.toDays(startDate.getTimeInMillis());
        int end = (int) TimeUnit.MILLISECONDS.toDays(endDate.getTimeInMillis()) + 1;
        return (end - start) % 7 == 0 ? (int) (end - start) / 7 : (((int) (end - start) / 7) + 1);
    }

    private void setToZero(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
    }
}
