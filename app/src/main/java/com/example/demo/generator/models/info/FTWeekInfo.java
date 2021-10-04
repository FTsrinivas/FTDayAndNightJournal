package com.example.demo.generator.models.info;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class FTWeekInfo {
    public List<FTDayInfo> dayInfos = new ArrayList<>();
    public FTDayFormatInfo format;
    public Locale locale;

    public FTWeekInfo(Locale locale, FTDayFormatInfo formatInfo) {
        this.locale = locale;
        this.format = formatInfo;
    }

    public void generate(Date date) {
        Calendar weekStartDate = new GregorianCalendar(locale);
        weekStartDate.setTime(date);

        Calendar nextWeekStartDate = new GregorianCalendar();
        nextWeekStartDate.setTime(weekStartDate.getTime());
        nextWeekStartDate.add(Calendar.DAY_OF_YEAR, 7);

        Calendar curDate = weekStartDate;
        while (curDate.before(nextWeekStartDate)) {
            FTDayInfo dayInfo = new FTDayInfo(locale, format);
            dayInfo.populateDateInfo(curDate.getTime());
            this.dayInfos.add(dayInfo);
            curDate.add(Calendar.DAY_OF_YEAR, 1);
        }
    }
}
