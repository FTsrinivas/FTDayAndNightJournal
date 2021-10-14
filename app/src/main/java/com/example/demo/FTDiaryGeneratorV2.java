package com.example.demo;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.util.Size;
import android.view.WindowManager;

import com.example.demo.generator.FTDairyRenderFormat;
import com.example.demo.generator.models.ScreenResolutions;
import com.example.demo.generator.models.info.FTYearFormatInfo;
import com.example.demo.generator.models.info.FTYearInfoMonthly;
import com.example.demo.generator.models.info.FTYearInfoWeekly;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class FTDiaryGeneratorV2 {
    private Context context;
    private FTDairyRenderFormat format;
    private FTYearInfoMonthly monthlyFormatter;
    private FTYearInfoWeekly weeklyFormatter;
    private FTYearFormatInfo formatInfo;

    private RectF pageRect = new RectF();
    private boolean isLinking = false;
    int offsetCount = 76;

    public FTDiaryGeneratorV2(Context context, FTDairyRenderFormat format, FTYearFormatInfo formatInfo) {
        this.context = context;
        this.format = format;
        this.formatInfo = formatInfo;
        monthlyFormatter = new FTYearInfoMonthly(formatInfo);
        weeklyFormatter = new FTYearInfoWeekly(formatInfo);
    }

    public String generate(ScreenResolutions resolutions) {
        Calendar startDate = new GregorianCalendar(formatInfo.locale);
        startDate.setTime(formatInfo.startMonth);
        startDate.set(startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH), startDate.getActualMinimum(Calendar.DAY_OF_MONTH));
        Calendar endDate = new GregorianCalendar(formatInfo.locale);
        endDate.setTime(formatInfo.endMonth);
        endDate.set(endDate.get(Calendar.YEAR), endDate.get(Calendar.MONTH), endDate.getActualMaximum(Calendar.DAY_OF_MONTH));

        this.monthlyFormatter.generate();
        this.weeklyFormatter.generate();

        // Core logic for generating the pdf with all formats (year, month...)
        PdfDocument document = new PdfDocument();

        //format.renderYearPage(context, monthlyFormatter.monthInfos, formatInfo);
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        int screenWidth = (windowManager.getDefaultDisplay().getWidth());
        int  screenHeight = (windowManager.getDefaultDisplay().getHeight());
//        Size size = new Size(screenWidth,screenHeight);
        Size size = new Size(resolutions.getWidth(),resolutions.getHeight());
        try {
            new FTDayAndNightJournal(context, size,false).renderYearPage(context, monthlyFormatter.monthInfos, formatInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
