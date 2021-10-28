package com.example.demo

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Size
import android.util.SizeF
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import com.example.demo.generator.FTDiaryFormat
import com.example.demo.generator.models.QuoteItem
import com.example.demo.generator.models.info.FTMonthInfo
import com.example.demo.generator.models.info.FTYearFormatInfo
import com.example.demo.generator.models.info.rects.FTMobileDayNightJournalRect
import com.example.demo.utils.FTDairyTextPaints
import com.example.demo.utils.ScreenUtils
import com.example.demo.utils.ScreenUtils.getQuotesList
import com.example.demo.utils.Utils
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.interactive.action.PDActionGoTo
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

// Request code for creating a PDF document.
const val CREATE_FILE: Int = 1

class FTDayAndNightJournalMobile(
    private val context: Context,
    private var screenSize: Size,
    private val isLandScape: Boolean
) : FTDiaryFormat(context, screenSize, isLandScape) {


    private var quotesList = ArrayList<QuoteItem>()
    private val document = PdfDocument()
    private lateinit var canvas: Canvas
    private var scaleFactor = 0f
    private var pageNumber = 3


    private var heightPercent: Float =
        (screenSize.height - FTDayAndNightJournal.STATUS_BAR_HEIGHT) / 100f
    private var widthPercent: Float = screenSize.width / 100f
    private var pageTopPadding = 0f
    private var pageLeftPadding = 0f
    private var pageBottomPadding = 0f

    //Intro page Measurements

    //Calendar page Measurements
    private var maxRows = 4
    private var maxColumns = 3
    private var calendarVerticalSpacing = 0f
    private var calendarHorizontalSpacing = 0f
    private var calendarTopManualSpace = 0f
    private var calendarBoxHeight = 0f
    private var calendarBoxWidth = 0f
    private var calendarMonthTextTopMargins = 0f
    private var calendarMonthTextLeftMargins = 0f

    private var calendarMonthTextSize = 0f
    private var calendarYearTextSize = 0f

    //Calendar Month page measurements
    private var calendarMonthPageHeadingTextSize = 0f
    private var datePartInMonthPage = 0f
    private var weekDaysTopMargin = 11.73f * heightPercent
    private var weekDaysTextSize = 0f

    //calendar Day Page Measurements
    private var topScaling: Float = 0f
    private var dayPageNumber = 13


    override fun renderYearPage(
        context: Context,
        months: MutableList<FTMonthInfo>,
        calendarYear: FTYearFormatInfo
    ) {
        super.renderYearPage(context, months, calendarYear)
        val thSize = SizeF(screenSize.width.toFloat(), screenSize.height.toFloat())
        val aspectSize: SizeF = ScreenUtils.aspectSize(
            thSize,
            SizeF(360f, 720f)

        )
        scaleFactor = thSize.getWidth() / aspectSize.width
        calenderDynamicSizes
        quotesList = getQuotesList(context)
        createIntroPage()
        createCalendarPage(months, calendarYear)
        createMonthsPages(months, calendarYear)
        createDaysTemplate(months, calendarYear)
        val fos: FileOutputStream
        try {
            fos = context.openFileOutput("demo.pdf", Context.MODE_PRIVATE)
            document.writeTo(fos)
            document.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        createPageHyperLinks()
    }


    private val calenderDynamicSizes: Unit
        get() {
            pageTopPadding = heightPercent * 3.19f
            pageLeftPadding = widthPercent * 5.83f
            pageBottomPadding = heightPercent * 4.86f
            calendarVerticalSpacing = heightPercent * 1.381f
            calendarHorizontalSpacing = widthPercent * 2.25f
            calendarTopManualSpace = heightPercent * 10.58f

            calendarMonthTextSize = 11 * scaleFactor
            calendarYearTextSize = 20 * scaleFactor
            weekDaysTextSize = 9 * scaleFactor
            findBoxHeight(maxRows)
            findBoxWidth(maxColumns, pageTopPadding)
            calendarMonthTextTopMargins = 1.104f * heightPercent
            calendarMonthTextLeftMargins = 1.858f * widthPercent
            datePartInMonthPage =
                (screenSize.width - 2 * pageLeftPadding) / 7
            /*templateDottedLineGap = heightPercent * 2.91f*/

        }

    private fun createIntroPage() {
        val pageInfo = PdfDocument.PageInfo.Builder(screenSize.width, screenSize.height, 1).create()
        val startingPage = document.startPage(pageInfo)
        canvas = startingPage.canvas
        canvas.drawRect(
            RectF(0f, 0f, screenSize.width.toFloat(), screenSize.height.toFloat()),
            FTDairyTextPaints.background_Paint
        )
        FTDairyTextPaints.introQuote_Paint.textSize = 11 * scaleFactor

        val introQuoteBoxHeight = heightPercent * 20.13f

        canvas.drawRect(
            RectF(0f, 0f, screenSize.width.toFloat(), introQuoteBoxHeight),
            FTDairyTextPaints.coloredBoxPaint
        )
        var paint = Paint().apply {
            style = Paint.Style.FILL
            color = FTApp.getInstance().resources.getColor(
                R.color.text_color,
                FTApp.getInstance().theme
            )
        }
        canvas.drawText(
            context.resources.getString(R.string.ftdairy_quote),
            (screenSize.width / 2).toFloat(),
            introQuoteBoxHeight / 2,
            FTDairyTextPaints.introQuote_Paint
        )
        FTDairyTextPaints.introAuthorText_Paint.textSize = 10 * scaleFactor
        canvas.drawText(
            context.resources.getString(R.string.ftdairy_author_name),
            (screenSize.width / 2).toFloat(),
            (24 * scaleFactor + 30 + introQuoteBoxHeight / 2),
            FTDairyTextPaints.introAuthorText_Paint
        )
        var introPageHeight = introQuoteBoxHeight + 8.05f * heightPercent
        FTDairyTextPaints.introText_Paint.textSize = 18 * scaleFactor
        canvas.drawText(
            context.resources.getString(R.string.ftdairy_title),
            (screenSize.width / 2).toFloat(),
            introPageHeight,
            FTDairyTextPaints.introText_Paint
        )
        introPageHeight += introPageHeight + 5f * heightPercent
        var dy = 39.02f * heightPercent

        dy += drawMultiLineText(
            context.resources.getString(R.string.ftdairy_points_newline),
            dy,
            isTextHeightRequired = true
        )
        dy += 5.5f * heightPercent

        drawMultiLineText(
            context.resources.getString(R.string.ftdairy_static_para),
            dy,
            isTextHeightRequired = false
        )
        document.finishPage(startingPage)
    }

    private fun createCalendarPage(months: List<FTMonthInfo>, calendarYear: FTYearFormatInfo) {
        val calendarPageInfo =
            PdfDocument.PageInfo.Builder(screenSize.width, screenSize.height, 2).create()
        val thirdPage = document.startPage(calendarPageInfo)
        canvas = thirdPage.canvas
        canvas.drawRect(
            RectF(
                0f, 0f, screenSize.width.toFloat(), screenSize.height
                    .toFloat()
            ), FTDairyTextPaints.background_Paint
        )
        FTDairyTextPaints.calendar_Year_Paint.textSize = calendarYearTextSize
        FTDairyTextPaints.calendar_Month_Mobile_Paint.textSize = calendarMonthTextSize

        canvas.drawText(
            Utils.getYearHeading(calendarYear.startMonth),
            pageLeftPadding,
            pageTopPadding + calendarYearTextSize,
            FTDairyTextPaints.calendar_Year_Paint
        )

        var boxLeft: Float
        var boxTop = calendarTopManualSpace
        var boxRight: Float
        var boxBottom = boxTop + calendarBoxHeight
        var month_Of_Year = 0

        FTMobileDayNightJournalRect.yearPageRectInfo = ArrayList()
        for (rows in 1..maxRows) {
            boxLeft = pageLeftPadding
            boxRight = pageLeftPadding + calendarBoxWidth
            if (rows > 1) {
                boxTop += calendarBoxHeight + calendarVerticalSpacing
                boxBottom = boxTop + calendarBoxHeight
            }
            for (columns in 1..maxColumns) {

                val monthName = months[month_Of_Year].monthTitle.uppercase()
                var dateLeftSpace = boxLeft
                var dateTopSpace = boxTop + calendarVerticalSpacing * 3
                val r = RectF(boxLeft, boxTop, boxRight, boxBottom)

                canvas.drawRoundRect(r, 10f, 10f, FTDairyTextPaints.coloredBoxPaint)

                canvas.drawText(
                    monthName,
                    boxLeft + calendarMonthTextLeftMargins,
                    boxTop + calendarVerticalSpacing + calendarMonthTextTopMargins,
                    FTDairyTextPaints.calendar_Month_Mobile_Paint
                )
                boxLeft = boxRight + calendarHorizontalSpacing
                boxRight += calendarBoxWidth + calendarHorizontalSpacing
                month_Of_Year++

                FTMobileDayNightJournalRect.yearPageRectInfo.add(r)
            }
        }
        document.finishPage(thirdPage)
    }


    private fun createMonthsPages(
        months: MutableList<FTMonthInfo>,
        calendarYear: FTYearFormatInfo
    ) {
        FTMobileDayNightJournalRect.monthPageRectInfo = ArrayList()
        months.forEachIndexed { index, ftMonthInfo ->
            pageNumber += index
            var dy = 2 * pageTopPadding
            val monthsPageInfo =
                PdfDocument.PageInfo.Builder(screenSize.width, screenSize.height, pageNumber)
                    .create()
            val mPage = document.startPage(monthsPageInfo)
            canvas = mPage.canvas
            canvas.drawRect(
                RectF(
                    0f, 0f, screenSize.width.toFloat(), screenSize.height
                        .toFloat()
                ), FTDairyTextPaints.background_Paint
            )
            FTDairyTextPaints.calendarColorText_Paint.textSize = calendarYearTextSize
            canvas.drawText(
                Utils.getYearHeading(calendarYear.startMonth),
                pageLeftPadding,
                dy,
                FTDairyTextPaints.calendarColorText_Paint
            )

            var tempRect = Rect()
            FTDairyTextPaints.calendar_Year_Paint.getTextBounds(
                Utils.getYearHeading(calendarYear.startMonth),
                0,
                Utils.getYearHeading(calendarYear.startMonth).length,
                tempRect
            )

            canvas.drawText(
                " / " + ftMonthInfo.monthTitle,
                pageLeftPadding + tempRect.width() + (2 * widthPercent),
                dy,
                FTDairyTextPaints.calendar_Year_Paint
            )
            showWeekDays(months, 0, pageLeftPadding, weekDaysTopMargin)
            showDates(months, index)

            document.finishPage(mPage)
        }

    }

    private fun createDaysTemplate(
        months: MutableList<FTMonthInfo>,
        calendarYear: FTYearFormatInfo
    ) {
        months.forEachIndexed { index, ftMonthInfo ->
            ftMonthInfo.dayInfos.forEachIndexed { dayIndex, ftDayInfo ->
                if (ftDayInfo.belongsToSameMonth) {
                    val dairyDate =
                        ftMonthInfo.monthTitle + " " + FTApp.getDayOfMonthSuffix(ftDayInfo.dayString.toInt()) + ","
                    dayTemplateOfDayAndNight(dayIndex, calendarYear, dairyDate)
                }
            }
        }
    }

    private fun dayTemplateOfDayAndNight(
        index: Int,
        calendarYear: FTYearFormatInfo,
        dairyDate: String
    ) {
        pageNumber += index
        val dayPageInfo =
            PdfDocument.PageInfo.Builder(screenSize.width, screenSize.height, pageNumber)
                .create()
        val mPage = document.startPage(dayPageInfo)
        canvas = mPage.canvas
        canvas.drawRect(
            RectF(
                0f, 0f, screenSize.width.toFloat(), screenSize.height
                    .toFloat()
            ), FTDairyTextPaints.background_Paint
        )
        FTDairyTextPaints.calendarColorText_Paint.textSize = 14 * scaleFactor
        val yearName = Utils.getYearHeading(calendarYear.startMonth)
        canvas.drawText(
            yearName,
            pageLeftPadding,
            pageTopPadding * 2,
            FTDairyTextPaints.calendarColorText_Paint
        )
        val rect = Rect()
        FTDairyTextPaints.calendarColorText_Paint.getTextBounds(yearName, 0, yearName.length, rect)
        FTMobileDayNightJournalRect.yearTextRectInfo = RectF(
            pageLeftPadding,
            pageTopPadding,
            pageLeftPadding + rect.width() + 6,
            pageTopPadding * 2
        )
        canvas.drawText(
            " / " + dairyDate.split(" ")[0],
            pageLeftPadding + rect.width() + 6,
            pageTopPadding * 2,
            FTDairyTextPaints.calendarColorText_Paint
        )
        val monthTextLeft = pageLeftPadding + rect.width() + 6
        FTDairyTextPaints.calendarColorText_Paint.getTextBounds(
            yearName + " / " + dairyDate.split(" ")[0],
            0,
            yearName.length + (" / " + dairyDate.split(" ")[0]).length,
            rect
        )
        FTMobileDayNightJournalRect.monthTextRectInfo = RectF(
            monthTextLeft,
            pageTopPadding,
            pageLeftPadding + rect.width() + 6,
            pageTopPadding * 2
        )
        canvas.drawRect(
            RectF(
                monthTextLeft,
                pageTopPadding,
                pageLeftPadding + rect.width() + 6,
                pageTopPadding * 2
            ), FTDairyTextPaints.coloredDayRectBoxPaint
        )
        FTDairyTextPaints.calendar_Year_Paint.textSize = 14 * scaleFactor
        canvas.drawText(
            " " + dairyDate.split(" ")[1],
            pageLeftPadding + rect.width() + 6,
            pageTopPadding * 2,
            FTDairyTextPaints.calendar_Year_Paint
        )
        val quoteItem = ScreenUtils.pickRandomQuote(context = context, quotesList)
        FTDairyTextPaints.dayPageQuoteAndAutorTextPaint.textSize = 14 * scaleFactor

        var dy = 9.02f * heightPercent
        val quoteTextHeight = drawMultiLineQuote(
            quoteItem.quote,
            screenSize.width.toFloat(),
            dy, 14
        )
        dy += quoteTextHeight + (14 * scaleFactor)
        ScreenUtils.drawCenterText(
            context,
            (screenSize.width / 2).toFloat(),
            dy,
            canvas,
            "-" + quoteItem.author,
            13,
            scaleFactor
        )
        topScaling = dy + (14 * scaleFactor) + (3.61f * heightPercent)
        dailyQuestionnaire(canvas, "My affirmations for the day")
        dailyQuestionnaire(canvas, "Today I will accomplish")
        dailyQuestionnaire(canvas, "I am thankful for")
        val rectPaint = Paint()
        rectPaint.style = Paint.Style.FILL
        rectPaint.color = context.resources.getColor(R.color.box_background, context.theme)
        canvas.drawRect(
            0f,
            topScaling,
            screenSize.width.toFloat(),
            screenSize.height.toFloat(),
            rectPaint
        )
        topScaling += 5.69f * heightPercent
        dailyQuestionnaire(canvas, "Three things that made me happy today")
        dailyQuestionnaire(canvas, "Today I learnt")

        document.finishPage(mPage)
    }



    private fun createPageHyperLinks() {
        try {
            val pdfFilePath: String = context.getFilesDir().toString() + "/" + "demo.pdf"
            val pdfFile = File(pdfFilePath)
            val pdDocument: PDDocument = PDDocument.load(pdfFile)
            var pageIndex = 1
            val yearPage = pdDocument.getPage(pageIndex)

            FTMobileDayNightJournalRect.yearPageRectInfo.forEachIndexed { monthNumber, arrayList ->
                pageIndex++
                val monthPage = pdDocument.getPage(pageIndex)

                if (monthPage != null) {
                    navigateDestinationPage(false, monthPage, yearPage)
                    createLinksOfMonthPage(pdDocument, monthNumber, monthPage, yearPage)
                    val link = PDAnnotationLink()
                    val destination: PDPageDestination = PDPageFitWidthDestination()
                    val action = PDActionGoTo()
                    destination.page = monthPage
                    action.destination = destination
                    link.action = action
                    val pdRectangle = PDRectangle()
                    pdRectangle.lowerLeftX = arrayList.left
                    pdRectangle.lowerLeftY = monthPage.mediaBox.height - arrayList.top
                    pdRectangle.upperRightX = arrayList.right
                    pdRectangle.upperRightY = monthPage.mediaBox.height - arrayList.bottom
                    link.rectangle = pdRectangle
                    yearPage.getAnnotations().add(link)
                }
            }
            //save PDF file in Internal storage
            val fileName = Calendar.getInstance().timeInMillis.toString()
            val fname =
                screenSize.width.toString() + " x " + screenSize.height + "--" + fileName + ".pdf";

            if (Build.VERSION_CODES.Q < Build.VERSION.SDK_INT) {
                var relativePath = Environment.DIRECTORY_DOWNLOADS
                relativePath += File.separator + "DayNightJournal_Pdfs"

                val values = ContentValues()
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fname)
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                val cr = context.contentResolver
                val uri = cr.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                uri?.let {
                    val outputStream = cr.openOutputStream(uri)
                    pdDocument.save(outputStream)
                }

            } else {
                val root = Environment.getExternalStorageDirectory().toString()
                val myDir = File("$root/DayNightJournal_Pdfs")
                if (!myDir.exists()) {
                    myDir.mkdirs()
                }
                val file = File(myDir, fname);

                pdDocument.save(file)
                pdDocument.close()

                val out = FileOutputStream(file);
                out.flush();
                out.close();
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createLinksOfMonthPage(
        pdDocument: PDDocument,
        monthNumber: Int,
        monthPage: PDPage,
        yearPage: PDPage?
    ) {
        FTMobileDayNightJournalRect.monthPageRectInfo.get(monthNumber)
            .forEachIndexed { index, rectF ->
                dayPageNumber++
                val dayPage = pdDocument.getPage(dayPageNumber)
                if (dayPage != null) {

                    navigateDestinationPage(true, dayPage, monthPage)
                    navigateDestinationPage(false, dayPage, yearPage)

                    val link = PDAnnotationLink()
                    val destination: PDPageDestination = PDPageFitWidthDestination()
                    val action = PDActionGoTo()

                    destination.page = dayPage
                    action.destination = destination
                    link.action = action

                    val pdRectangle = PDRectangle()
                    pdRectangle.lowerLeftX = rectF.left
                    pdRectangle.lowerLeftY = dayPage.mediaBox.height - rectF.top
                    pdRectangle.upperRightX = rectF.right
                    pdRectangle.upperRightY = dayPage.mediaBox.height - rectF.bottom
                    link.rectangle = pdRectangle
                    monthPage.getAnnotations().add(link)
                }
            }
    }

    private fun navigateDestinationPage(
        isLinkForMonth: Boolean,
        sourcePage: PDPage,
        destinationPage: PDPage?
    ) {

        val link = PDAnnotationLink()
        val destination: PDPageDestination = PDPageFitWidthDestination()
        val action = PDActionGoTo()

        destination.page = destinationPage
        action.destination = destination
        link.action = action

        if (isLinkForMonth) {
            val yearPDRect = PDRectangle()
            yearPDRect.lowerLeftX = FTMobileDayNightJournalRect.monthTextRectInfo.left
            yearPDRect.lowerLeftY =
                sourcePage.mediaBox.height - FTMobileDayNightJournalRect.monthTextRectInfo.top
            yearPDRect.upperRightX = FTMobileDayNightJournalRect.monthTextRectInfo.right
            yearPDRect.upperRightY =
                sourcePage.mediaBox.height - FTMobileDayNightJournalRect.monthTextRectInfo.bottom
            link.rectangle = yearPDRect
            sourcePage.getAnnotations().add(link)
            return
        }
        val yearPDRect = PDRectangle()
        yearPDRect.lowerLeftX = FTMobileDayNightJournalRect.yearTextRectInfo.left
        yearPDRect.lowerLeftY =
            sourcePage.mediaBox.height - FTMobileDayNightJournalRect.yearTextRectInfo.top
        yearPDRect.upperRightX = FTMobileDayNightJournalRect.yearTextRectInfo.right
        yearPDRect.upperRightY =
            sourcePage.mediaBox.height - FTMobileDayNightJournalRect.yearTextRectInfo.bottom
        link.rectangle = yearPDRect
        sourcePage.getAnnotations().add(link)
    }


    private fun showDates(months: List<FTMonthInfo>, month_Of_Year: Int) {
        var boxLeft: Float = pageLeftPadding
        var boxRight: Float = pageLeftPadding + datePartInMonthPage
        var boxTop: Float = weekDaysTopMargin + pageTopPadding
        var boxBottom: Float

        var dayBoxHeight = 2.5f * heightPercent
        var dayBoxVerticalGap = 1.1f * heightPercent
        var dayBoxHorizontalGaps = (datePartInMonthPage / 7)

        var dateLeftSpace = boxLeft
        var dateTopSpace = boxTop + calendarVerticalSpacing
        var dayRectList = ArrayList<RectF>()
        for (days in 0 until months[month_Of_Year].dayInfos.size) {

            if (days % 7 == 0) {
                dateTopSpace += dayBoxHeight * 2 + dayBoxVerticalGap
                dateLeftSpace = boxLeft
            }
            val date =
                if (months[month_Of_Year].dayInfos[days].belongsToSameMonth) months[month_Of_Year].dayInfos[days].dayString else ""

            val xPosition = dateLeftSpace + (datePartInMonthPage - dayBoxHorizontalGaps * 2) / 2
            val dayRectLeft = xPosition - (datePartInMonthPage - dayBoxHorizontalGaps * 2) / 2

            canvas.drawRoundRect(
                RectF(
                    dayRectLeft,
                    dateTopSpace - dayBoxHeight,
                    dayRectLeft + datePartInMonthPage - dayBoxHorizontalGaps,
                    dateTopSpace + dayBoxHeight - 5
                ), 25f, 25f, FTDairyTextPaints.coloredBoxPaint
            )
            if (!date.trim().isEmpty()) {
                dayRectList.add(
                    RectF(
                        dayRectLeft,
                        dateTopSpace - dayBoxHeight,
                        dayRectLeft + datePartInMonthPage - dayBoxHorizontalGaps,
                        dateTopSpace + dayBoxHeight - 5
                    )
                )
            }
            FTDairyTextPaints.calendar_Month_Mobile_Paint.textSize = weekDaysTextSize
            canvas.drawText(
                date,
                xPosition,
                dateTopSpace,
                FTDairyTextPaints.calendar_Month_Mobile_Paint
            )
            dateLeftSpace += datePartInMonthPage
        }
        canvas.drawRect(
            0f,
            dateTopSpace + dayBoxHeight + dayBoxVerticalGap + pageTopPadding,
            screenSize.width.toFloat(),
            screenSize.height.toFloat(),
            FTDairyTextPaints.coloredBoxPaint
        )
        FTMobileDayNightJournalRect.monthPageRectInfo.add(dayRectList)

    }


    private fun showWeekDays(
        months: List<FTMonthInfo>,
        month_Of_Year: Int,
        boxLeft: Float,
        topMargin: Float
    ) {
        var dayLeftMargin = boxLeft
        FTDairyTextPaints.calendar_WeekDays_Paint.textSize = weekDaysTextSize
        for (i in 0..6) {
            val dayName = months[month_Of_Year].dayInfos[i].weekDay
            val xPosition = dayLeftMargin + datePartInMonthPage / 2
            canvas.drawText(
                dayName,
                xPosition,
                topMargin + pageTopPadding,
                FTDairyTextPaints.calendar_WeekDays_Paint
            )
            dayLeftMargin += datePartInMonthPage
        }
    }

    private fun drawMultiLineText(text: String, dy: Float, isTextHeightRequired: Boolean): Int {
        val pointTextSize = (14 * scaleFactor)
        val textPaint = TextPaint().apply {
            letterSpacing = 0.025f
            textSize = pointTextSize
            typeface = ResourcesCompat.getFont(FTApp.getInstance(), R.font.lora_regular)
            color = context.resources.getColor(R.color.text_color, context.theme)
        }
        val textWidth = screenSize.width - (12.625f * widthPercent)
        val staticLayout = StaticLayout.Builder.obtain(
            text,
            0,
            text.length,
            textPaint,
            textWidth.toInt()
        ).setLineSpacing(0f, 1.25f).build()
        canvas.save()
        val dx = if (isTextHeightRequired) {
            8.125f * widthPercent
        } else {
            4.16f * widthPercent
        }
        canvas.translate(dx, dy)
        staticLayout.draw(canvas)
        canvas.restore()
        if (isTextHeightRequired) {
            return staticLayout.height
        }
        return 0
    }

    private fun drawMultiLineQuote(
        text: String,
        dx: Float,
        dy: Float,
        quoteTextSize: Int
    ): Int {
        val pointTextSize = (quoteTextSize * scaleFactor)
        val textPaint = TextPaint().apply {
            textSize = pointTextSize
            typeface = ResourcesCompat.getFont(FTApp.getInstance(), R.font.lora_italic)
            color = Color.GRAY
            textAlign = Paint.Align.LEFT
        }
        val staticLayout: StaticLayout = StaticLayout.Builder.obtain(
            text,
            0,
            text.length,
            textPaint,
            dx.toInt() - 2 * pageLeftPadding.toInt()
        ).setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0f, 1.2f).build()

        canvas.save()
        canvas.translate(pageLeftPadding, dy)
        staticLayout.draw(canvas)
        canvas.restore()
        return staticLayout.height
    }

    private fun dailyQuestionnaire(canvas: Canvas, question: String) {
        FTDairyTextPaints.dairyText_Paint.textSize = 14 * scaleFactor

        canvas.drawText(question, pageLeftPadding, topScaling, FTDairyTextPaints.dairyText_Paint)
        topScaling = topScaling + (4.16f * heightPercent)
        var fgPaintSel: Paint
        for (i in 1..2) {
            fgPaintSel = Paint()
            fgPaintSel.setARGB(255, 0, 0, 0)
            fgPaintSel.style = Paint.Style.STROKE
            fgPaintSel.pathEffect = DashPathEffect(floatArrayOf(2f, 5f), 0f)
            canvas.drawLine(
                pageLeftPadding,
                topScaling,
                screenSize.width - pageLeftPadding,
                topScaling,
                fgPaintSel
            )
            topScaling = topScaling + (4.86f * heightPercent)
        }
    }


    private fun findBoxHeight(rows: Int): Float {
        /*  The following are the major factors here
        Height and Width
        isLandscape
        No of Rows
        No of columns
        left margins & right margins for either orientations
        box vertical spacing
        box horizontal spacing*/
        val leftOverSpace =
            screenSize.height - FTDayAndNightJournal.STATUS_BAR_HEIGHT - calendarTopManualSpace - pageBottomPadding - (rows - 1) * calendarVerticalSpacing
        calendarBoxHeight = leftOverSpace / rows
        return calendarBoxHeight
    }

    private fun findBoxWidth(columns: Int, topScaling: Float): Float {
        /*The following are the major factors here
        Height and Width
        isLandscape
        No of Rows
        No of columns
        left margins & right margins for either orientations
        box vertical spacing
        box horizontal spacing*/
        val leftOverSpace =
            screenSize.width - (2 * pageLeftPadding) - (columns - 1) * calendarHorizontalSpacing
        calendarBoxWidth = leftOverSpace / columns
        return calendarBoxWidth
    }

    private fun createFile(context: Context, pickerInitialUri: Uri, fileName: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, fileName)

            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker before your app creates the document.
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }
        startActivityForResult(context as MainActivity, intent, CREATE_FILE, null)
    }

}
