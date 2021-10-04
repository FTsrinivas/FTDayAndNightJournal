package com.example.demo.generator;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.util.Log;

import com.example.demo.generator.models.info.FTDayInfo;
import com.example.demo.generator.models.info.FTMonthlyCalendarInfo;
import com.example.demo.generator.models.info.FTWeekInfo;
import com.example.demo.generator.models.info.FTYearFormatInfo;
import com.example.demo.generator.models.info.FTYearInfoMonthly;
import com.example.demo.generator.models.info.FTYearInfoWeekly;
import com.example.demo.generator.models.info.rects.FTDiaryRectsInfo;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FTDiaryGenerator {

    private Context context;
    private FTDairyRenderFormat format;
    private FTYearInfoMonthly monthlyFormatter;
    private FTYearInfoWeekly weeklyFormatter;
    private FTYearFormatInfo formatInfo;

    private RectF pageRect = new RectF();
    private boolean isLinking = false;
    int offsetCount = 76;

    public FTDiaryGenerator(Context context, FTDairyRenderFormat format, FTYearFormatInfo formatInfo) {
        this.context = context;
        this.format = format;
        this.formatInfo = formatInfo;
        monthlyFormatter = new FTYearInfoMonthly(formatInfo);
        weeklyFormatter = new FTYearInfoWeekly(formatInfo);
    }

    public String generate() {
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
        format.setDocument(document);

//        format.renderYearPage(context, monthlyFormatter.monthInfos, formatInfo);

        List<FTMonthlyCalendarInfo> calendarMonths = monthlyFormatter.monthlyCalendarInfos;
        for (FTMonthlyCalendarInfo calendarMonth : calendarMonths) {
            format.renderMonthPage(context, calendarMonth, formatInfo);
        }

        List<FTWeekInfo> weekInfos = weeklyFormatter.weeklyInfos;
        for (FTWeekInfo weekInfo : weekInfos) {
            format.renderWeekPage(context, weekInfo);
        }

        List<FTMonthlyCalendarInfo> monthInfos = monthlyFormatter.monthlyCalendarInfos;
        for (FTMonthlyCalendarInfo eachMonth : monthInfos) {
            List<FTDayInfo> dayInfos = eachMonth.dayInfos;
            for (FTDayInfo eachDayInfo : dayInfos) {
                if (eachDayInfo.belongsToSameMonth)
                    format.renderDayPage(context, eachDayInfo);
            }
        }

        String url = writeToTheDocument(document, startDate.get(Calendar.YEAR) + "-" + endDate.get(Calendar.YEAR) + ".pdf");
        document.close();
        addLink(url, (FTDiaryFormat) format, monthlyFormatter.yearFormatInfo);

        return url;
        //
    }

    private void addLink(String url, FTDiaryFormat format, FTYearFormatInfo calendarYear) {
        try {
            File pdfFile = new File(url);
            PDDocument document = PDDocument.load(pdfFile);

            int pageIndex = 0;
            int nextIndex = 0;
            int offset = 0;
            Calendar startDate = new GregorianCalendar(calendarYear.locale);
            startDate.setTime(calendarYear.startMonth);
            startDate.set(startDate.get(Calendar.YEAR), startDate.get(Calendar.MONTH), startDate.getActualMinimum(Calendar.DAY_OF_MONTH));
            Calendar endDate = new GregorianCalendar(calendarYear.locale);
            endDate.setTime(calendarYear.endMonth);
            endDate.set(endDate.get(Calendar.YEAR), endDate.get(Calendar.MONTH), endDate.getActualMaximum(Calendar.DAY_OF_MONTH));

            //Linking year page
            nextIndex = 1;
            PDPage yearPage = document.getPage(pageIndex);
            int yearMonthsCount = 0;
            for (RectF monthRect : format.yearRectsInfo.monthRects) {
                PDPage page = document.getPage(yearMonthsCount + nextIndex + offset);
                if (page != null) {
                    PDAnnotationLink link = new PDAnnotationLink();
                    PDPageDestination destination = new PDPageFitWidthDestination();
                    PDActionGoTo action = new PDActionGoTo();
                    destination.setPage(page);
                    action.setDestination(destination);
                    link.setAction(action);

                    PDRectangle pdRectangle = new PDRectangle();
                    pdRectangle.setLowerLeftX(monthRect.left);
                    pdRectangle.setLowerLeftY(page.getMediaBox().getHeight() - monthRect.top);
                    pdRectangle.setUpperRightX(monthRect.right);
                    pdRectangle.setUpperRightY(page.getMediaBox().getHeight() - monthRect.bottom);

                    link.setRectangle(pdRectangle);
                    yearPage.getAnnotations().add(link);
                }
                yearMonthsCount += 1;
            }
            pageIndex += 1;

            //Linking month pages
            pageIndex = linkMonthPages(document, pageIndex, format, false, startDate, endDate);

            pageIndex = linkWeekPages(document, nextIndex, yearMonthsCount, pageIndex, format, startDate, endDate);

            linkDayPages(document, startDate, endDate, pageIndex, format, yearMonthsCount);

            document.save(pdfFile);
            document.close();
        } catch (IOException e) {

        }
    }

    private int linkMonthPages(PDDocument document, int index, FTDiaryFormat format, boolean isToDisplayOutOfMonthDate, Calendar startDate, Calendar endDate) throws IOException {
        int pageIndex = index;
        List<FTMonthlyCalendarInfo> calendarMonths = monthlyFormatter.monthlyCalendarInfos;
        int monthRectsCount = 0;

        Date lastDate = formatInfo.endMonth;

        int daysBeforeCount = 1 + monthsBetween(startDate.getTime(), endDate.getTime()) + (weeksBetween(calendarMonths.get(0).dayInfos.get(0).date, lastDate));
        if (daysBetween(endDate.getTime(), lastDate) + 1 > 7) {
            daysBeforeCount -= 1;
        }
        this.offsetCount = daysBeforeCount;

        for (FTMonthlyCalendarInfo eachMonth : calendarMonths) {
            PDPage monthPage = document.getPage(pageIndex);
            FTDiaryRectsInfo.FTDiaryMonthRectsInfo monthRectsInfo = format.monthRectsInfos.get(monthRectsCount);
            PDAnnotationLink link = new PDAnnotationLink();
            PDPageDestination destination = new PDPageFitWidthDestination();
            PDActionGoTo action = new PDActionGoTo();
            destination.setPage(document.getPage(0));
            action.setDestination(destination);
            link.setAction(action);

            PDRectangle pdRectangle = new PDRectangle();
            pdRectangle.setLowerLeftX(monthRectsInfo.yearRect.left);
            pdRectangle.setLowerLeftY(monthPage.getMediaBox().getHeight() - monthRectsInfo.yearRect.top);
            pdRectangle.setUpperRightX(monthRectsInfo.yearRect.right);
            pdRectangle.setUpperRightY(monthPage.getMediaBox().getHeight() - monthRectsInfo.yearRect.bottom);

            link.setRectangle(pdRectangle);
            monthPage.getAnnotations().add(link);

            int dayRectsCount = 0;
            for (FTDayInfo eachDay : eachMonth.dayInfos) {
                if (format.isBelongToCalendarYear(eachDay)) {
                    if (isToDisplayOutOfMonthDate) {
                        if (monthRectsInfo.dayRects.size() > dayRectsCount) {
                            PDPage page = document.getPage(daysBetween(startDate.getTime(), eachDay.date) + daysBeforeCount);
                            if (page != null) {
                                PDAnnotationLink dayLink = new PDAnnotationLink();
                                PDPageDestination dayDestination = new PDPageFitWidthDestination();
                                PDActionGoTo dayAction = new PDActionGoTo();
                                dayDestination.setPage(page);
                                dayAction.setDestination(dayDestination);
                                dayLink.setAction(dayAction);

                                RectF dayRect = monthRectsInfo.dayRects.get(dayRectsCount);
                                PDRectangle dayRectangle = new PDRectangle();
                                dayRectangle.setLowerLeftX(dayRect.left);
                                dayRectangle.setLowerLeftY(monthPage.getMediaBox().getHeight() - dayRect.top);
                                dayRectangle.setUpperRightX(dayRect.right);
                                dayRectangle.setUpperRightY(monthPage.getMediaBox().getHeight() - dayRect.bottom);

                                dayLink.setRectangle(dayRectangle);
                                monthPage.getAnnotations().add(dayLink);
                            }
                        }
                        dayRectsCount += 1;
                    } else {
                        if (monthRectsInfo.dayRects.size() > dayRectsCount) {
                            PDPage page = document.getPage(daysBetween(startDate.getTime(), eachDay.date) + daysBeforeCount);
                            if (page != null) {
                                PDAnnotationLink dayLink = new PDAnnotationLink();
                                PDPageDestination dayDestination = new PDPageFitWidthDestination();
                                PDActionGoTo dayAction = new PDActionGoTo();
                                dayDestination.setPage(page);
                                dayAction.setDestination(dayDestination);
                                dayLink.setAction(dayAction);

                                RectF dayRect = monthRectsInfo.dayRects.get(dayRectsCount);
                                PDRectangle dayRectangle = new PDRectangle();
                                dayRectangle.setLowerLeftX(dayRect.left);
                                dayRectangle.setLowerLeftY(monthPage.getMediaBox().getHeight() - dayRect.top);
                                dayRectangle.setUpperRightX(dayRect.right);
                                dayRectangle.setUpperRightY(monthPage.getMediaBox().getHeight() - dayRect.bottom);

                                dayLink.setRectangle(dayRectangle);
                                monthPage.getAnnotations().add(dayLink);
                            }
                        }
                        dayRectsCount += 1;
                    }
                }
            }
            pageIndex += 1;
            monthRectsCount += 1;
        }
        return pageIndex;
    }

    private int linkWeekPages(PDDocument document, int _nextIndex, int yearMonthsCount, int index, FTDiaryFormat format, Calendar startDate, Calendar endDate) throws IOException {
        int pageIndex = index;
        int nextIndex = _nextIndex;
        nextIndex = 1 + yearMonthsCount + weeklyFormatter.weeklyInfos.size();
        int weekRectsCount = 0;
        for (FTWeekInfo weekInfo : weeklyFormatter.weeklyInfos) {
            PDPage weekPage = document.getPage(pageIndex);
            FTDiaryRectsInfo.FTDiaryWeekRectsInfo weekRectsInfo = format.weekRectsInfos.get(weekRectsCount);

            int monthTo = monthsBetween(startDate.getTime(), weekInfo.dayInfos.get(0).date) - 1;

            if (format.isBelongToCalendarYear(weekInfo.dayInfos.get(0))) {
                PDAnnotationLink monthLink = new PDAnnotationLink();
                PDPageDestination monthDestination = new PDPageFitWidthDestination();
                PDActionGoTo monthAction = new PDActionGoTo();
                monthDestination.setPage(document.getPage(1 + monthTo));
                monthAction.setDestination(monthDestination);
                monthLink.setAction(monthAction);

                RectF monthRect = weekRectsInfo.monthRect;
                PDRectangle monthRectangle = new PDRectangle();
                monthRectangle.setLowerLeftX(monthRect.left);
                monthRectangle.setLowerLeftY(weekPage.getMediaBox().getHeight() - monthRect.top);
                monthRectangle.setUpperRightX(monthRect.right);
                monthRectangle.setUpperRightY(weekPage.getMediaBox().getHeight() - monthRect.bottom);

                monthLink.setRectangle(monthRectangle);
                weekPage.getAnnotations().add(monthLink);
            }
            PDAnnotationLink yearLink = new PDAnnotationLink();
            PDPageDestination yearDestination = new PDPageFitWidthDestination();
            PDActionGoTo yearAction = new PDActionGoTo();
            yearDestination.setPage(document.getPage(0));
            yearAction.setDestination(yearDestination);
            yearLink.setAction(yearAction);

            RectF yearRect = weekRectsInfo.yearRect;
            PDRectangle yearRectangle = new PDRectangle();
            yearRectangle.setLowerLeftX(yearRect.left);
            yearRectangle.setLowerLeftY(weekPage.getMediaBox().getHeight() - yearRect.top);
            yearRectangle.setUpperRightX(yearRect.right);
            yearRectangle.setUpperRightY(weekPage.getMediaBox().getHeight() - yearRect.bottom);

            yearLink.setRectangle(yearRectangle);
            weekPage.getAnnotations().add(yearLink);

            int currentWeekDaysCount = 0;
            for (RectF weekDayRect : weekRectsInfo.weekDayRects) {
                PDPage page = document.getPage(currentWeekDaysCount + nextIndex);
                if (page != null) {
                    PDAnnotationLink weekDayLink = new PDAnnotationLink();
                    PDPageDestination dayDestination = new PDPageFitWidthDestination();
                    PDActionGoTo dayAction = new PDActionGoTo();
                    dayDestination.setPage(page);
                    dayAction.setDestination(dayDestination);
                    weekDayLink.setAction(dayAction);

                    PDRectangle weekDayRectangle = new PDRectangle();
                    weekDayRectangle.setLowerLeftX(weekDayRect.left);
                    weekDayRectangle.setLowerLeftY(weekPage.getMediaBox().getHeight() - weekDayRect.top);
                    weekDayRectangle.setUpperRightX(weekDayRect.right);
                    weekDayRectangle.setUpperRightY(weekPage.getMediaBox().getHeight() - weekDayRect.bottom);

                    weekDayLink.setRectangle(weekDayRectangle);
                    weekPage.getAnnotations().add(weekDayLink);
                }
                currentWeekDaysCount += 1;
            }
            nextIndex += currentWeekDaysCount;

            pageIndex += 1;
            weekRectsCount += 1;
        }
        return pageIndex;
    }

    private void linkDayPages(PDDocument document, Calendar startDate, Calendar endDate, int pageIndex, FTDiaryFormat format, int yearMonthsCount) throws IOException {
        int dayRectsCount = 0;

        int startWeekDay = startDate.get(Calendar.DAY_OF_WEEK);
        int startOffset = 1 - startWeekDay;
        if (formatInfo.weekFormat.equals("2")) {
            if (startWeekDay == 1) {
                startOffset = -6;
            } else {
                startOffset = 2 - startWeekDay;
            }
        }

        Calendar weekcalStartDate = new GregorianCalendar(formatInfo.locale);
        weekcalStartDate.setTime(startDate.getTime());
        weekcalStartDate.add(Calendar.DATE, startOffset);

        int helperOffset = startDate.get(Calendar.DAY_OF_WEEK) - 1;
        for (FTMonthlyCalendarInfo eachMonth : monthlyFormatter.monthlyCalendarInfos) {
            for (FTDayInfo eachDayInfo : eachMonth.dayInfos) {
                if (eachDayInfo.belongsToSameMonth) {
                    PDPage dayPage = document.getPage(pageIndex);
                    FTDiaryRectsInfo.FTDiaryDayRectsInfo dayRectsInfo = format.dayRectsInfos.get(dayRectsCount);

                    int monthTo = eachDayInfo.month;
                    int year = eachDayInfo.year;

                    if (monthTo == startDate.get(Calendar.MONTH) && year == startDate.get(Calendar.YEAR)) {
                        monthTo = 0;
                    } else if (monthTo == endDate.get(Calendar.MONTH) && year == endDate.get(Calendar.YEAR)) {
                        monthTo = 13;
                    }

                    int monthPage = monthsBetween(startDate.getTime(), eachDayInfo.date);
                    PDPage page = document.getPage(monthPage);
                    if (page != null) {
                        PDAnnotationLink monthLink = new PDAnnotationLink();
                        PDPageDestination dayDestination = new PDPageFitWidthDestination();
                        PDActionGoTo monthAction = new PDActionGoTo();
                        dayDestination.setPage(page);
                        monthAction.setDestination(dayDestination);
                        monthLink.setAction(monthAction);

                        PDRectangle monthRectangle = new PDRectangle();
                        monthRectangle.setLowerLeftX(dayRectsInfo.monthRect.left);
                        monthRectangle.setLowerLeftY(dayPage.getMediaBox().getHeight() - dayRectsInfo.monthRect.top);
                        monthRectangle.setUpperRightX(dayRectsInfo.monthRect.right);
                        monthRectangle.setUpperRightY(dayPage.getMediaBox().getHeight() - dayRectsInfo.monthRect.bottom);

                        monthLink.setRectangle(monthRectangle);
                        dayPage.getAnnotations().add(monthLink);
                    }

                    int weekPageIndex = (int) (TimeUnit.MILLISECONDS.toDays(eachDayInfo.date.getTime() - weekcalStartDate.getTime().getTime())) / 7;
                    PDPage weekPage = document.getPage(1 + yearMonthsCount + weekPageIndex);
                    if (weekPage != null) {
                        PDAnnotationLink weekLink = new PDAnnotationLink();
                        PDPageDestination dayDestination = new PDPageFitWidthDestination();
                        PDActionGoTo weekAction = new PDActionGoTo();
                        dayDestination.setPage(weekPage);
                        weekAction.setDestination(dayDestination);
                        weekLink.setAction(weekAction);

                        PDRectangle weekRectangle = new PDRectangle();
                        weekRectangle.setLowerLeftX(dayRectsInfo.weekRect.left);
                        weekRectangle.setLowerLeftY(weekPage.getMediaBox().getHeight() - dayRectsInfo.weekRect.top);
                        weekRectangle.setUpperRightX(dayRectsInfo.weekRect.right);
                        weekRectangle.setUpperRightY(weekPage.getMediaBox().getHeight() - dayRectsInfo.weekRect.bottom);

                        weekLink.setRectangle(weekRectangle);
                        dayPage.getAnnotations().add(weekLink);
                    }

                    PDAnnotationLink yearLink = new PDAnnotationLink();
                    PDPageDestination yearDestination = new PDPageFitWidthDestination();
                    PDActionGoTo weekAction = new PDActionGoTo();
                    yearDestination.setPage(document.getPage(0));
                    weekAction.setDestination(yearDestination);
                    yearLink.setAction(weekAction);

                    PDRectangle yearRectangle = new PDRectangle();
                    yearRectangle.setLowerLeftX(dayRectsInfo.yearRect.left);
                    yearRectangle.setLowerLeftY(weekPage.getMediaBox().getHeight() - dayRectsInfo.yearRect.top);
                    yearRectangle.setUpperRightX(dayRectsInfo.yearRect.right);
                    yearRectangle.setUpperRightY(weekPage.getMediaBox().getHeight() - dayRectsInfo.yearRect.bottom);

                    yearLink.setRectangle(yearRectangle);
                    dayPage.getAnnotations().add(yearLink);

                    pageIndex += 1;
                    dayRectsCount += 1;
                }
            }
        }
    }

    private String writeToTheDocument(PdfDocument document, String filename) {
        String rootPath = context.getDataDir().getPath() + "/" + filename;
        File rootFile = new File(rootPath);
        if (!rootFile.exists()) {
            rootFile.mkdirs();
        }
        String filePath = rootPath + filename;

        try {
            document.writeTo(new FileOutputStream(filePath));
        } catch (IOException e) {
            Log.e(FTDiaryGenerator.class.getName(), e.toString());
        }
        return filePath;
    }

    public int daysBetween(Date day1, Date day2) {
        Calendar d1 = new GregorianCalendar(formatInfo.locale);
        d1.setTime(day1);
        Calendar d2 = new GregorianCalendar(formatInfo.locale);
        d2.setTime(day2);

        Calendar dayOne = (Calendar) d1.clone(),
                dayTwo = (Calendar) d2.clone();

        if (dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)) {
            return Math.abs(dayOne.get(Calendar.DAY_OF_YEAR) - dayTwo.get(Calendar.DAY_OF_YEAR));
        } else {
            if (dayTwo.get(Calendar.YEAR) > dayOne.get(Calendar.YEAR)) {
                //swap them
                Calendar temp = dayOne;
                dayOne = dayTwo;
                dayTwo = temp;
            }
            int extraDays = 0;

            int dayOneOriginalYearDays = dayOne.get(Calendar.DAY_OF_YEAR);

            while (dayOne.get(Calendar.YEAR) > dayTwo.get(Calendar.YEAR)) {
                dayOne.add(Calendar.YEAR, -1);
                // getActualMaximum() important for leap years
                extraDays += dayOne.getActualMaximum(Calendar.DAY_OF_YEAR);
            }

            return Math.abs(extraDays - dayTwo.get(Calendar.DAY_OF_YEAR) + dayOneOriginalYearDays);
        }
    }

    private int weeksBetween(Date x, Date y) {
        long daysDifference = (TimeUnit.MILLISECONDS.toDays(getBase(y).getTime() - getBase(x).getTime()) + 1);
        return (int) daysDifference / 7 + (daysDifference % 7 == 0 ? 0 : 1);
    }

    private Date getBase(Date date) {
        Calendar calendar = new GregorianCalendar(formatInfo.locale);
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private int monthsBetween(Date startDate, Date endDate) {
        Calendar start = new GregorianCalendar(formatInfo.locale);
        start.setTime(startDate);
        Calendar end = new GregorianCalendar(formatInfo.locale);
        end.setTime(endDate);

        int year = end.get(Calendar.YEAR) - start.get(Calendar.YEAR);
        int months = (end.get(Calendar.MONTH) + 1) - ((start.get(Calendar.MONTH) + 1));
        if (end.get(Calendar.DAY_OF_MONTH) < (start.get(Calendar.DAY_OF_MONTH))) {
            months--;
        }
        return Math.abs(months + (year * 12) + 1);
    }
}


