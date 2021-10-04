package com.example.demo.generator.models.info;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class FTDayInfo {
    public String dayString = "1";
    public String weekString = "Sunday";
    public String monthString = "Jan";
    public String fullMonthString = "January";
    public String yearString = "2019";
    public String weekDay = "S";
    public int month = 1;
    public int year = 1;
    public Date date = new Date();

    public boolean belongsToSameMonth = true;

    public Locale locale;

    private FTDayFormatInfo format;

    public FTDayInfo(Locale locale, FTDayFormatInfo dayFormatInfo) {
        this.locale = locale;
        this.format = dayFormatInfo;
    }

    public void populateDateInfo(Date date) {
        this.date = date;

        Calendar calendar = new GregorianCalendar(locale);
        calendar.setTime(date);
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format.dateFormat, locale);
        this.dayString = simpleDateFormat.format(date);

        simpleDateFormat = new SimpleDateFormat(format.dayFormat, locale);
        this.weekString = simpleDateFormat.format(date);

        simpleDateFormat = new SimpleDateFormat(format.yearFormat, locale);
        this.yearString = simpleDateFormat.format(date);

        Locale monthLocale = locale;
        if (locale.toLanguageTag().contains("ja")
                || locale.toLanguageTag().contains("zh_hans")
                || locale.toLanguageTag().contains("zh_hant")
                || locale.toLanguageTag().contains("ko")
                || locale.toLanguageTag().contains("zh")) {
            monthLocale = new Locale("en", "US");
        }
        simpleDateFormat = new SimpleDateFormat("MMMM", monthLocale);
        this.fullMonthString = simpleDateFormat.format(date);
        simpleDateFormat = new SimpleDateFormat(format.monthFormat, monthLocale);
        this.monthString = simpleDateFormat.format(date);

        this.weekDay = locale.toLanguageTag().contains("zh") ? new SimpleDateFormat("E", locale).format(date).substring(1, 2) : this.weekString.substring(0, 1);

        this.month = calendar.get(Calendar.MONTH);
        this.year = calendar.get(Calendar.YEAR);
    }
}