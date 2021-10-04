package com.example.demo.generator;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;

import com.example.demo.generator.models.info.FTDayInfo;
import com.example.demo.generator.models.info.FTMonthInfo;
import com.example.demo.generator.models.info.FTMonthlyCalendarInfo;
import com.example.demo.generator.models.info.FTWeekInfo;
import com.example.demo.generator.models.info.FTYearFormatInfo;

import java.io.IOException;
import java.util.List;

public interface FTDairyRenderFormat {
    boolean isToDisplayOutOfMonthDate();

    RectF pageRect();

    void setDocument(PdfDocument document);

    void renderYearPage(Context context, List<FTMonthInfo> months, FTYearFormatInfo calendarYear) throws IOException;

    void renderMonthPage(Context context, FTMonthlyCalendarInfo monthInfo, FTYearFormatInfo calendarYear);

    void renderWeekPage(Context context, FTWeekInfo weeklyInfo);

    void renderDayPage(Context context, FTDayInfo dayInfo);

    interface FTDairyRenderTemplate {
        String getDayTemplate();

        String getWeekTemplate();

        String getMonthTemplate();

        String getYearTemplate();
    }
}
