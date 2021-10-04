package com.example.demo.generator.models.info;

import android.content.Context;
import android.content.res.Configuration;
import android.util.Size;

import com.example.demo.FTApp;
import com.example.demo.R;
import com.example.demo.utils.DrawingTextTemplates;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class FTYearFormatInfo {
    private final Context mContext = FTApp.getInstance().getApplicationContext();

    public FTDayFormatInfo dayFormat = new FTDayFormatInfo();
    public Date startMonth = new Date();
    public Date endMonth = new Date();
    public Locale locale = mContext.getResources().getConfiguration().getLocales().get(0);
    public Size screenSize = new Size(mContext.getResources().getDisplayMetrics().widthPixels, mContext.getResources().getDisplayMetrics().heightPixels);
    public boolean isTablet = DrawingTextTemplates.INSTANCE.isTablet(mContext);
    public String templateId = "Modern";
    public int orientation = Configuration.ORIENTATION_PORTRAIT;
    public String weekFormat = String.valueOf(new GregorianCalendar(locale).getFirstDayOfWeek());

    public FTYearFormatInfo(int year) {
        this.startMonth.setMonth(12);
        this.startMonth.setYear(year - 1);

        this.endMonth.setMonth(1);
        this.endMonth.setYear(year + 1);
    }

    public FTYearFormatInfo(Date startDate, Date endDate) {
        this.startMonth = startDate;
        this.endMonth = endDate;
    }

    public FTYearFormatInfo(Date startDate, Date endDate, String templateId, int orientation) {
        this.startMonth = startDate;
        this.endMonth = endDate;
        this.templateId = templateId;
        this.orientation = orientation;
    }
}