package com.example.demo.generator.formats;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.util.Size;

import com.example.demo.R;
import com.example.demo.generator.FTDiaryFormat;
import com.example.demo.generator.models.info.FTDayInfo;
import com.example.demo.generator.models.info.FTMonthInfo;
import com.example.demo.generator.models.info.FTMonthlyCalendarInfo;
import com.example.demo.generator.models.info.FTWeekInfo;
import com.example.demo.generator.models.info.FTYearFormatInfo;
import com.example.demo.generator.models.info.rects.FTDiaryRectsInfo;
import com.example.demo.generator.models.screenInfo.FTScreenFontsInfo;
import com.example.demo.generator.models.screenInfo.FTScreenSpacesInfo;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class FTDiaryFormat2020 extends FTDiaryFormat {
    public FTDiaryFormat2020(Context context, Size screenSize, boolean isLandscape) {
        super(context, screenSize, isLandscape);
    }

    @Override
    public void renderYearPage(Context context, List<FTMonthInfo> months, FTYearFormatInfo calendarYear) throws IOException {
        super.renderYearPage(context, months, calendarYear);
        PdfDocument.Page page = getPage(0);

        FTScreenSpacesInfo.FTScreenYearSpacesInfo spacesInfo = screenInfo.spacesInfo.yearSpacesInfo;
        FTScreenFontsInfo.FTScreenYearFontsInfo fontsInfo = screenInfo.fontsInfo.yearFontsInfo;
        int columnCount = getColumnCount();
        int rowCount = getRowCount();
        float boxWidth = (pageSize.getWidth() - ((columnCount - 1) * spacesInfo.cellOffsetX) - (2 * spacesInfo.baseBoxX)) / columnCount;
        float boxHeight = (pageSize.getHeight() - ((rowCount - 1) * spacesInfo.cellOffsetY) - spacesInfo.baseBoxY - spacesInfo.boxBottomOffset) / rowCount;

        final float monthTitleOffsetX = 6.1f * getDensity();
        final float monthTitleOffsetY = 5.8f * getDensity();

        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < columnCount; col++) {
                float monthCellLeft = spacesInfo.baseBoxX + (boxWidth * col) + (col * spacesInfo.cellOffsetX);
                float monthCellTop = spacesInfo.baseBoxY + (row * boxHeight) + (row * spacesInfo.cellOffsetY);
                RectF monthCellRect = new RectF(monthCellLeft, monthCellTop, monthCellLeft + boxWidth, monthCellTop + boxHeight);
                drawRectangle(page, monthCellRect);

                int index = columnCount * row + col;
                if (index < months.size()) {
                    String monthTitle = months.get(index).monthTitle;
                    RectF monthRect = new RectF();
                    monthRect.left = monthCellRect.left + monthTitleOffsetX;
                    monthRect.top = monthCellRect.top + monthTitleOffsetY;
                    Rect monthTitleBoundingRect = drawText(page, monthTitle, monthRect, fontsInfo.titleMonthFontSize, Color.GRAY, formatInfo.locale);

                    RectF monthLinkRect = new RectF();
                    monthLinkRect.left = monthCellRect.left;
                    monthLinkRect.top = monthCellRect.top;
                    monthLinkRect.right = monthLinkRect.left + monthTitleBoundingRect.width() + 30;
                    monthLinkRect.bottom = monthLinkRect.top + monthTitleBoundingRect.height() + 30;
                    yearRectsInfo.monthRects.add(monthLinkRect);
                }
            }
        }

        float yearLeft = spacesInfo.baseBoxX;
        float yearTop = spacesInfo.baseBoxY - (spacesInfo.baseBoxY * 0.8f);
        RectF yearRect = new RectF(yearLeft, yearTop, 0, 0);

        Calendar startCalendar = new GregorianCalendar(formatInfo.locale);
        startCalendar.setTime(formatInfo.startMonth);
        String startYear = String.valueOf(startCalendar.get(Calendar.YEAR));
        Calendar endCalendar = new GregorianCalendar(formatInfo.locale);
        endCalendar.setTime(formatInfo.endMonth);
        String endYear = String.valueOf(endCalendar.get(Calendar.YEAR));
        Rect yearBoundRect = drawText(page, startYear + (startYear.equals(endYear) ? "" : "-" + endYear.substring(2, 4)), yearRect, fontsInfo.yearFontSize, Color.GRAY, formatInfo.locale);

        yearRectsInfo.yearRect.left = yearRect.left;
        yearRectsInfo.yearRect.top = yearRect.top;
        yearRectsInfo.yearRect.right = yearRectsInfo.yearRect.left + yearBoundRect.width();
        yearRectsInfo.yearRect.bottom = yearRectsInfo.yearRect.top + yearBoundRect.height();

        finishPage(page);
    }

    @Override
    public void renderMonthPage(Context context, FTMonthlyCalendarInfo monthInfo, FTYearFormatInfo calendarYear) {
        super.renderMonthPage(context, monthInfo, calendarYear);
        PdfDocument.Page page = getPage(1);
        int columnCount = 7;
        int rowCount = 6;
        FTScreenSpacesInfo.FTScreenMonthSpacesInfo spacesInfo = screenInfo.spacesInfo.monthSpacesInfo;
        FTScreenFontsInfo.FTScreenMonthFontsInfo fontsInfo = screenInfo.fontsInfo.monthFontsInfo;
        float boxWidth = (pageSize.getWidth() - ((isLandscape ? spacesInfo.baseBoxX : 2 * spacesInfo.baseBoxX) + spacesInfo.boxRightOffset)) / columnCount;
        float boxHeight = (pageSize.getHeight() - spacesInfo.baseBoxY - spacesInfo.boxBottomOffset) / rowCount;

        FTDiaryRectsInfo.FTDiaryMonthRectsInfo monthRectsInfo = new FTDiaryRectsInfo.FTDiaryMonthRectsInfo();

        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < columnCount; col++) {
                float rectX = spacesInfo.baseBoxX + (boxWidth * col);
                float rectY = spacesInfo.baseBoxY + (row * boxHeight);
                RectF dayCellRect = new RectF(rectX, rectY, rectX + boxWidth, rectY + boxHeight);
                drawRectangle(page, dayCellRect);

                int index = row * columnCount + col;
                if (index < monthInfo.dayInfos.size()) {
                    FTDayInfo dayInfo = monthInfo.dayInfos.get(index);
                    if (row == 0) {
                        Rect weekBoundRect = getTextRect(dayInfo.weekDay, fontsInfo.weekFontSize, dayInfo.locale);
                        RectF weekRect = new RectF();
                        weekRect.left = dayCellRect.left + (4.9f * 2 * getDensity());
                        int extraHeight = weekBoundRect.height() < 10 ? 20 : 0;
                        weekRect.top = dayCellRect.top - weekBoundRect.height() - (6.5f * 2 * getDensity()) - extraHeight;
                        drawText(page, dayInfo.weekDay.toUpperCase(monthInfo.locale), weekRect, fontsInfo.weekFontSize, Color.GRAY, dayInfo.locale);
                    }
                    String date = String.valueOf(dayInfo.date.getDate());
                    RectF dayRect = new RectF();
                    dayRect.left = dayCellRect.left + (4.9f * 2 * getDensity());
                    dayRect.top = dayCellRect.top + (3.1f * 2 * getDensity());
                    Rect dayBoundRect = drawText(page, date, dayRect, fontsInfo.dayFontSize, dayInfo.belongsToSameMonth ? Color.GRAY : Color.GRAY, dayInfo.locale);

                    if (isBelongToCalendarYear(dayInfo)) {
                        RectF dayLinkRect = new RectF();
                        dayLinkRect.left = dayCellRect.left;
                        dayLinkRect.top = dayCellRect.top;
                        dayLinkRect.right = dayLinkRect.left + dayBoundRect.width() + 30;
                        dayLinkRect.bottom = dayLinkRect.top + dayBoundRect.height() + 30;
                        monthRectsInfo.dayRects.add(dayLinkRect);
                    }
                }
            }
        }

        boolean isLandscape = calendarYear.orientation == Configuration.ORIENTATION_LANDSCAPE;

        Rect monthBoundRect = getTextRect(monthInfo.shortMonth, fontsInfo.monthFontSize, monthInfo.locale);
        RectF monthRect = new RectF();
        monthRect.left = isLandscape ? 48 * getDensity() : spacesInfo.baseBoxX;
        monthRect.top = spacesInfo.baseBoxY - (isLandscape ? 57.8f * getDensity() : ((78.2f * 2 * getDensity()) + 215));
        monthBoundRect = drawText(page, monthInfo.shortMonth.toUpperCase(monthInfo.locale), monthRect, fontsInfo.monthFontSize, Color.GRAY, monthInfo.locale);

        RectF monthLinkRect = new RectF();
        monthLinkRect.left = monthRect.left;
        monthLinkRect.top = monthRect.top;
        monthLinkRect.right = monthLinkRect.left + monthBoundRect.width();
        monthLinkRect.bottom = monthLinkRect.top + monthBoundRect.height();
        monthRectsInfo.monthRect = monthLinkRect;

        Rect yearBoundRect = getTextRect(String.valueOf(monthInfo.year), fontsInfo.yearFontSize, formatInfo.locale);
        RectF yearRect = new RectF();

        if (isLandscape) {
            yearRect.left = monthRect.left;
            yearRect.top = monthRect.top + monthBoundRect.height() + 35 * getDensity();
        } else {
            yearRect.left = monthRect.left + monthBoundRect.width() + (yearBoundRect.width() / 2f);
            yearRect.top = monthRect.top + monthBoundRect.height() - yearBoundRect.height();
        }

        drawText(page, monthInfo.year, yearRect, fontsInfo.yearFontSize, Color.GRAY, monthInfo.locale);

        RectF yearLinkRect = new RectF();
        yearLinkRect.left = yearRect.left;
        yearLinkRect.top = yearRect.top;
        yearLinkRect.right = yearRect.left + yearBoundRect.width() + 30;
        yearLinkRect.bottom = yearRect.top + yearBoundRect.height() + 30;
        monthRectsInfo.yearRect = yearLinkRect;

        monthRectsInfos.add(monthRectsInfo);

        finishPage(page);
    }

    @Override
    public void renderWeekPage(Context context, FTWeekInfo weeklyInfo) {
        super.renderWeekPage(context, weeklyInfo);
        PdfDocument.Page page = getPage(2);
        int columnCount = 3;
        int rowCount = 2;
        FTScreenSpacesInfo.FTScreenWeekSpacesInfo spacesInfo = screenInfo.spacesInfo.weekSpacesInfo;
        FTScreenFontsInfo.FTScreenWeekFontsInfo fontsInfo = screenInfo.fontsInfo.weekFontsInfo;
        float boxWidth = (pageSize.getWidth() - ((columnCount - 1) * spacesInfo.cellOffsetX) - (2 * spacesInfo.baseBoxX)) / columnCount;
        float boxHeight = (pageSize.getHeight() - ((rowCount - 1) * spacesInfo.cellOffsetY) - (2 * spacesInfo.baseBoxY) - spacesInfo.lastCellHeight) / rowCount;
        float initialBoxYOffset = spacesInfo.baseBoxY + 1;

        float weekParentOffset = 12 * getDensity();

        FTDiaryRectsInfo.FTDiaryWeekRectsInfo weekRectsInfo = new FTDiaryRectsInfo.FTDiaryWeekRectsInfo();

        float lineWidth = 0;
        float lastBoxTop = 0;
        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < columnCount; col++) {
                float rectX = spacesInfo.baseBoxX + (boxWidth * col) + (col * spacesInfo.cellOffsetX);
                float rectY = initialBoxYOffset + (row * boxHeight) + (row * spacesInfo.cellOffsetY);
                RectF dayCellRect = new RectF(rectX, rectY, rectX + boxWidth, rectY + boxHeight);
                drawRectangle(page, dayCellRect);

                int index = row * columnCount + col;
                if (index < weeklyInfo.dayInfos.size()) {
                    FTDayInfo dayInfo = weeklyInfo.dayInfos.get(index);
                    RectF dayRect = new RectF();
                    dayRect.left = dayCellRect.left + weekParentOffset;
                    dayRect.top = dayCellRect.top + weekParentOffset;
                    Rect dayBoundRect = drawText(page, dayInfo.weekString.toUpperCase(weeklyInfo.locale), dayRect, fontsInfo.dayFontSize, Color.GRAY, dayInfo.locale);

                    if (isBelongToCalendarYear(dayInfo)) {
                        RectF dayLinkRect = new RectF();
                        dayLinkRect.left = dayCellRect.left;
                        dayLinkRect.top = dayCellRect.top;
                        dayLinkRect.right = dayLinkRect.left + dayBoundRect.width() + 30;
                        dayLinkRect.bottom = dayLinkRect.top + dayBoundRect.height() + 30;
                        weekRectsInfo.weekDayRects.add(dayLinkRect);
                    }

                    if (row == rowCount - 1 && col == columnCount - 1) {
                        lastBoxTop = dayCellRect.bottom;
                        lineWidth = dayCellRect.width() + (dayCellRect.width() / 2);
                    }
                }
            }
        }

        drawLine(page, spacesInfo.baseBoxX, spacesInfo.titleLineY, spacesInfo.baseBoxX + lineWidth, spacesInfo.titleLineY + 1);

        RectF dateRect = new RectF();
        dateRect.left = spacesInfo.baseBoxX;
        FTDayInfo startWeekDayInfo = weeklyInfo.dayInfos.get(0);
        String startDate = startWeekDayInfo.date.getDate() + " " + startWeekDayInfo.monthString;
        FTDayInfo endWeekDayInfo = weeklyInfo.dayInfos.get(weeklyInfo.dayInfos.size() - 1);
        String endDate = endWeekDayInfo.date.getDate() + " " + endWeekDayInfo.monthString;
        String monthTitle = startDate + " - " + endDate;
        Rect dateBoundRect = getTextRect(monthTitle, fontsInfo.monthFontSize, startWeekDayInfo.locale);
        dateRect.top = spacesInfo.titleLineY - 8 - dateBoundRect.height();
        drawText(page, monthTitle, dateRect, fontsInfo.monthFontSize, Color.GRAY, weeklyInfo.locale);

        RectF monthRect = new RectF();
        monthRect.left = spacesInfo.baseBoxX;
        monthRect.top = spacesInfo.titleLineY + 1;
        Rect monthBoundRect = drawText(page, startWeekDayInfo.fullMonthString.toUpperCase(weeklyInfo.locale) + " ", monthRect, fontsInfo.yearFontSize, Color.GRAY, weeklyInfo.locale);

        weekRectsInfo.monthRect.left = monthRect.left - 30;
        weekRectsInfo.monthRect.top = spacesInfo.titleLineY;
        weekRectsInfo.monthRect.right = monthRect.left + monthBoundRect.width();
        weekRectsInfo.monthRect.bottom = weekRectsInfo.monthRect.top + monthBoundRect.height() + 30;

        RectF yearRect = new RectF();
        yearRect.left = spacesInfo.baseBoxX + monthBoundRect.width();
        yearRect.top = spacesInfo.titleLineY + 1;
        Rect yearBoundRect = drawText(page, " " + startWeekDayInfo.yearString, yearRect, fontsInfo.yearFontSize, Color.GRAY, weeklyInfo.locale);

        weekRectsInfo.yearRect.left = yearRect.left;
        weekRectsInfo.yearRect.top = spacesInfo.titleLineY;
        weekRectsInfo.yearRect.right = yearRect.left + yearBoundRect.width() + 30;
        weekRectsInfo.yearRect.bottom = weekRectsInfo.yearRect.top + yearBoundRect.height() + 30;

        float lastBoxWidth = pageSize.getWidth() - (2 * spacesInfo.baseBoxX);
        RectF lastBox = new RectF();
        lastBox.left = spacesInfo.baseBoxX;
        lastBox.top = lastBoxTop + spacesInfo.cellOffsetY;
        lastBox.right = lastBox.left + lastBoxWidth;
        lastBox.bottom = lastBox.top + spacesInfo.lastCellHeight;
        drawRectangle(page, lastBox);

        FTDayInfo lastdayInfo = weeklyInfo.dayInfos.get(weeklyInfo.dayInfos.size() - 1);
        RectF lastDayRect = new RectF();
        lastDayRect.left = lastBox.left + weekParentOffset;
        lastDayRect.top = lastBox.top + weekParentOffset;
        Rect lastDayBoundRect = drawText(page, lastdayInfo.weekString.toUpperCase(weeklyInfo.locale), lastDayRect, fontsInfo.dayFontSize, Color.GRAY, weeklyInfo.locale);

        if (isBelongToCalendarYear(lastdayInfo)) {
            RectF lastDayLinkRect = new RectF();
            lastDayLinkRect.left = lastBox.left;
            lastDayLinkRect.top = lastBox.top;
            lastDayLinkRect.right = lastDayRect.left + lastDayBoundRect.width() + 30;
            lastDayLinkRect.bottom = lastDayRect.top + lastDayBoundRect.height() + 30;
            weekRectsInfo.weekDayRects.add(lastDayLinkRect);
        }

        weekRectsInfos.add(weekRectsInfo);

        finishPage(page);
    }

    @Override
    public void renderDayPage(Context context, FTDayInfo dayInfo) {
        if (!isBelongToCalendarYear(dayInfo) && !dayInfo.belongsToSameMonth) return;
        super.renderDayPage(context, dayInfo);
        PdfDocument.Page page = getPage(3);

        FTScreenSpacesInfo.FTScreenDaySpacesInfo spacesInfo = screenInfo.spacesInfo.daySpacesInfo;
        FTScreenFontsInfo.FTScreenDayFontsInfo fontsInfo = screenInfo.fontsInfo.dayFontsInfo;

        FTDiaryRectsInfo.FTDiaryDayRectsInfo dayRectsInfo = new FTDiaryRectsInfo.FTDiaryDayRectsInfo();

        RectF dateRect = new RectF();
        getTextRect(dayInfo.dayString, fontsInfo.dayFontSize, dayInfo.locale);
        dateRect.top = spacesInfo.baseY;
        dateRect.left = spacesInfo.baseX;
        Rect dateBoundRect = drawText(page, dayInfo.dayString, dateRect, fontsInfo.dayFontSize, Color.GRAY, dayInfo.locale);

        RectF monthRect = new RectF();
        Rect monthBoundRect = getTextRect(dayInfo.fullMonthString, fontsInfo.monthFontSize, dayInfo.locale);
        monthRect.top = dateRect.top + dateBoundRect.height() + monthBoundRect.height() + (monthBoundRect.height() / 2f);
        monthRect.left = spacesInfo.baseX;
        drawText(page, dayInfo.fullMonthString.toUpperCase(dayInfo.locale), monthRect, fontsInfo.monthFontSize, Color.GRAY, dayInfo.locale);

        dayRectsInfo.monthRect.left = monthRect.left;
        dayRectsInfo.monthRect.top = monthRect.top;
        dayRectsInfo.monthRect.right = dayRectsInfo.monthRect.left + monthBoundRect.width();
        dayRectsInfo.monthRect.bottom = dayRectsInfo.monthRect.top + monthBoundRect.height();

        RectF weekRect = new RectF();
        Rect weekBoundRect = getTextRect(dayInfo.weekString, fontsInfo.monthFontSize, dayInfo.locale);
        weekRect.top = monthRect.top + monthBoundRect.height() + (weekBoundRect.height() / 2f);
        weekRect.left = spacesInfo.baseX;
        drawText(page, dayInfo.weekString.toUpperCase(dayInfo.locale), weekRect, fontsInfo.weekFontSize, Color.BLACK, dayInfo.locale);

        dayRectsInfo.weekRect.left = weekRect.left;
        dayRectsInfo.weekRect.top = weekRect.top;
        dayRectsInfo.weekRect.right = dayRectsInfo.weekRect.left + weekBoundRect.width();
        dayRectsInfo.weekRect.bottom = dayRectsInfo.weekRect.top + weekBoundRect.height();

        RectF yearRect = new RectF();
        yearRect.top = monthRect.top + fontsInfo.monthFontSize;
        Rect yearBoundRect = getTextRect(String.valueOf(dayInfo.year), fontsInfo.monthFontSize, dayInfo.locale);
        yearRect.left = pageSize.getWidth() - spacesInfo.baseX - yearBoundRect.width();
        drawText(page, String.valueOf(dayInfo.year), yearRect, fontsInfo.yearFontSize, Color.BLACK, dayInfo.locale);

        dayRectsInfo.yearRect.left = yearRect.left - 15;
        dayRectsInfo.yearRect.top = yearRect.top - 15;
        dayRectsInfo.yearRect.right = yearRect.left + yearBoundRect.width() + 15;
        dayRectsInfo.yearRect.bottom = yearRect.top + yearBoundRect.height() + 15;

        for (float y = yearRect.top + fontsInfo.yearFontSize + spacesInfo.baseX; y < pageSize.getHeight() - spacesInfo.baseY; y += spacesInfo.baseY) {
            drawLine(page, spacesInfo.baseX, y, pageSize.getWidth() - (spacesInfo.baseX), y + 1);
        }

        dayRectsInfos.add(dayRectsInfo);

        finishPage(page);
    }
}