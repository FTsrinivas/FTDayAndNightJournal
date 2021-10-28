package com.example.demo

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Size
import androidx.core.content.res.ResourcesCompat
import com.example.demo.generator.FTDiaryFormat
import com.example.demo.generator.models.QuoteItem
import com.example.demo.generator.models.info.FTMonthInfo
import com.example.demo.generator.models.info.FTYearFormatInfo
import com.example.demo.generator.models.info.rects.FTDairyDayPageRect
import com.example.demo.generator.models.info.rects.FTDairyYearPageRect
import com.example.demo.utils.FTDairyTextPaints
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
import java.io.IOException
import java.lang.Exception
import java.util.*
import android.os.Environment
import android.text.Layout
import kotlin.collections.ArrayList

import com.example.demo.utils.ScreenUtils
import com.example.demo.utils.Utils


class FTDayAndNightJournal(
    private val context: Context,
    private var screenSize: Size,
    private val isLandScape: Boolean,
) : FTDiaryFormat(context, screenSize, isLandScape) {

    private var quotesList = ArrayList<QuoteItem>()
    private val document = PdfDocument()

    //    var screenDensity = 0.0f
//    private var orientation: Int = 1
    private var heightPercent: Float = (screenSize.height - STATUS_BAR_HEIGHT) / 100f
    private var widthPercent: Float = screenSize.width / 100f
    private var pageTopPadding = 0f
    private var pageLeftPadding = 0f
    private var pageBottomPadding = 0f

    private var maxRows = 0
    private var maxColumns = 0
    private var calendarTopManualSpace = 0f
    private var calendarVerticalSpacing = 0f
    private var calendarHorizontalSpacing = 0f
    private var calendarDayTextSize = 0f
    private var calendarMonthTextSize = 0f
    private var calendarYearTextSize = 0f
    private var dayLeftMargin = 0f
    private var dayTopMargin = 0f
    private var calendarBoxWidth = 0f
    private var calendarBoxHeight = 0f
    private var individualDayinCalendar = 0f
    private var dairyTextSize = 0f
    private var mTop = 0f
    private var mBottom = 0f
    private lateinit var canvas: Canvas

    private var maxLines = 0
    private var templateDottedLineGap = 0f
    private var bulletPointsTextHeight = 0
    private var quoteTextHeight = 0

    companion object {
        const val STATUS_BAR_HEIGHT = 88f
    }

    @Throws(IOException::class)
    override fun renderYearPage(
        context: Context,
        months: List<FTMonthInfo>,
        calendarYear: FTYearFormatInfo
    ) {
        super.renderYearPage(context, months, calendarYear)
        /*  screenDensity = context.resources.displayMetrics.density
          orientation = context.resources.configuration.orientation*/

        if (isLandScape) {
            var mSize = Size(screenSize.height, screenSize.width)
            screenSize = mSize
            heightPercent = (screenSize.height - STATUS_BAR_HEIGHT) / 100f
            widthPercent = screenSize.width / 100f
        }
        calenderDynamicSizes
        quotesList = ScreenUtils.getQuotesList(context)
        createIntroPage()
        createCalendarPage(months, calendarYear)
        createDayPageTemplate(months)

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
        FTDairyTextPaints.calendar_Month_Paint.textSize = calendarMonthTextSize
        FTDairyTextPaints.calendar_WeekDays_Paint.textSize = calendarDayTextSize
        FTDairyTextPaints.calendar_Days_Paint.textSize = calendarDayTextSize
        canvas.drawText(
            Utils.getYearHeading(calendarYear.startMonth),
            pageLeftPadding,
            pageTopPadding + calendarYearTextSize,
            FTDairyTextPaints.calendar_Year_Paint
        )
        /* findBoxHeight(maxRows)
         findBoxWidth(maxColumns, pageLeftPadding)*/

        var boxLeft: Float
        var boxTop = calendarTopManualSpace
        var boxRight: Float
        var boxBottom = boxTop + calendarBoxHeight
        var month_Of_Year = 0
//        val yearRectInfoList = FTDairyYearPageRect.yearRectInfo
        FTDairyYearPageRect.yearRectInfo = ArrayList()

        for (rows in 1..maxRows) {
            boxLeft = pageLeftPadding
            boxRight = pageLeftPadding + calendarBoxWidth
            if (rows > 1) {
                boxTop += calendarBoxHeight + calendarVerticalSpacing
                boxBottom = boxTop + calendarBoxHeight
            }
            for (columns in 1..maxColumns) {
                val monthRectsList = FTDairyYearPageRect().monthRectInfo
                var dayRectInfo = FTDairyYearPageRect().dayRectInfo
                val monthName = months[month_Of_Year].monthTitle.uppercase()
                var dateLeftSpace = boxLeft
                var dateTopSpace = boxTop + calendarVerticalSpacing * 3
                val r = RectF(boxLeft, boxTop, boxRight, boxBottom)
                canvas.drawRoundRect(r, 10f, 10f, FTDairyTextPaints.coloredBoxPaint)

                //show Week Day Names
                showWeekDays(months, month_Of_Year, boxLeft, dateTopSpace)
                for (days in 0 until months[month_Of_Year].dayInfos.size) {
                    if (days % 7 == 0) {
                        dateTopSpace += dayTopMargin
                        dateLeftSpace = boxLeft
                    }
                    val date =
                        if (months[month_Of_Year].dayInfos[days].belongsToSameMonth) months[month_Of_Year].dayInfos[days].dayString else ""
                    val xPosition = dateLeftSpace + individualDayinCalendar / 2
                    canvas.drawText(
                        date,
                        xPosition,
                        dateTopSpace,
                        FTDairyTextPaints.calendar_Days_Paint
                    )
                    val dayRectLeft = xPosition - (individualDayinCalendar / 2)

                    if (date.isNotEmpty()) {
                        dayRectInfo = RectF(
                            dayRectLeft,
                            dateTopSpace - calendarDayTextSize,
                            dayRectLeft + individualDayinCalendar - 5,
                            dateTopSpace
                        )
                        monthRectsList.add(dayRectInfo)
                    }

                    canvas.drawRect(
                        dayRectLeft,
                        dateTopSpace - calendarDayTextSize,
                        dayRectLeft + individualDayinCalendar - 5,
                        dateTopSpace,
                        FTDairyTextPaints.coloredDayRectBoxPaint
                    )

                    dateLeftSpace += individualDayinCalendar
                }

                canvas.drawText(
                    monthName,
                    boxLeft + individualDayinCalendar / 2,
                    boxTop + calendarVerticalSpacing,
                    FTDairyTextPaints.calendar_Month_Paint
                )
                boxLeft = boxRight + calendarHorizontalSpacing
                boxRight += calendarBoxWidth + calendarHorizontalSpacing
                month_Of_Year++
                FTDairyYearPageRect.yearRectInfo.add(monthRectsList)
            }
        }
        document.finishPage(thirdPage)
    }

    private fun createIntroPage() {
        val pageInfo = PdfDocument.PageInfo.Builder(screenSize.width, screenSize.height, 1).create()
        val startingPage = document.startPage(pageInfo)
        canvas = startingPage.canvas
        canvas.drawRect(
            RectF(0f, 0f, screenSize.width.toFloat(), screenSize.height.toFloat()),
            FTDairyTextPaints.background_Paint
        )
        FTDairyTextPaints.introQuote_Paint.textSize = 25 * screenDensity

        val introQuoteBoxHeight = if (!isLandscape) {
            heightPercent * 29.91f
        } else {
            heightPercent * 32.22f
        }
        canvas.drawRect(
            RectF(0f, 0f, screenSize.width.toFloat(), introQuoteBoxHeight),
            FTDairyTextPaints.coloredBoxPaint
        )
        canvas.drawText(
            context.resources.getString(R.string.ftdairy_quote),
            (screenSize.width / 2).toFloat(),
            introQuoteBoxHeight / 2,
            FTDairyTextPaints.introQuote_Paint
        )
        FTDairyTextPaints.introAuthorText_Paint.textSize = 22 * screenDensity
        canvas.drawText(
            context.resources.getString(R.string.ftdairy_author_name),
            (screenSize.width / 2).toFloat(),
            (24 * screenDensity + 30 + introQuoteBoxHeight / 2),
            FTDairyTextPaints.introAuthorText_Paint
        )
        var introPageHeight = introQuoteBoxHeight + 9.91f * heightPercent
        FTDairyTextPaints.introText_Paint.textSize = 35 * screenDensity
        canvas.drawText(
            context.resources.getString(R.string.ftdairy_title),
            (screenSize.width / 2).toFloat(),
            introPageHeight,
            FTDairyTextPaints.introText_Paint
        )
        introPageHeight += introPageHeight + 5f * heightPercent
        var dy = 48 * heightPercent
        drawMultiLineText(
            context.resources.getString(R.string.ftdairy_points),
            dy,
            isBulletPoint = true
        )
        dy += bulletPointsTextHeight
        dy += 4.1f * heightPercent

        drawMultiLineText(
            context.resources.getString(R.string.ftdairy_static_para),
            dy,
            isBulletPoint = false
        )
        document.finishPage(startingPage)
    }

    private val calenderDynamicSizes: Unit
        get() {
            //Portrait
            pageTopPadding = heightPercent * 3.5f
            pageLeftPadding = widthPercent * 5.155f
            pageBottomPadding = heightPercent * 3.58f
            calendarVerticalSpacing = heightPercent * 1.81f
            calendarHorizontalSpacing = widthPercent * 1.81f
            calendarTopManualSpace = heightPercent * 10.58f
            ScreenUtils.findRowCount(isLandScape)
            ScreenUtils.findColumnCount(isLandScape)
            ScreenUtils.findMaxLines(isLandScape)
            templateDottedLineGap = heightPercent * 2.91f
            if (isLandScape /*|| orientation == Configuration.ORIENTATION_LANDSCAPE*/) {
                /* maxRows = 3
                 maxColumns = 4
                 maxLines = 2*/
                pageLeftPadding = widthPercent * 3.125f
                pageTopPadding = heightPercent * 5.5f
                calendarVerticalSpacing = heightPercent * 2.79f
                calendarHorizontalSpacing = widthPercent * 1.95f
                calendarTopManualSpace = heightPercent * 15.27f
                templateDottedLineGap = heightPercent * 4.44f
            }
            findBoxWidth(maxColumns, pageTopPadding)
            findBoxHeight(maxRows)
            calendarDayTextSize = 10 * screenDensity
            calendarMonthTextSize = 15 * screenDensity
            calendarYearTextSize = 40 * screenDensity
            dayLeftMargin = widthPercent * 3f
            dayTopMargin = heightPercent * 1.94f
            individualDayinCalendar = calendarBoxWidth / 7
            dairyTextSize = heightPercent * 1.66f
        }

    private fun showWeekDays(
        months: List<FTMonthInfo>,
        month_Of_Year: Int,
        boxLeft: Float,
        topMargin: Float
    ) {
        var dayLeftMargin = boxLeft
        for (i in 0..6) {
            val dayName = months[month_Of_Year].dayInfos[i].weekDay
            val xPosition = dayLeftMargin + individualDayinCalendar / 2
            canvas.drawText(
                dayName,
                xPosition,
                topMargin,
                FTDairyTextPaints.calendar_WeekDays_Paint
            )
            dayLeftMargin += individualDayinCalendar
        }
    }

    private fun createDayPageTemplate(months: List<FTMonthInfo>) {
        var pageNumber = 3
        FTDairyDayPageRect.yearPageRect = Rect(
            (27.25 * widthPercent).toInt(),
            (pageTopPadding).toInt(),
            (32.25 * widthPercent).toInt(),
            (pageTopPadding + dairyTextSize).toInt()
        )
        //27.25 * widthPercentage
        months.forEach { ftMonthInfo ->
            ftMonthInfo.dayInfos.forEach { ftDayInfo ->
                if (ftDayInfo.belongsToSameMonth) {
                    val dairyDate =
                        ftMonthInfo.monthTitle + " " + FTApp.getDayOfMonthSuffix(ftDayInfo.dayString.toInt()) + ","
                    val dairyYear = "" + ftMonthInfo.year
                    templateDayAndNight(pageNumber, dairyDate, dairyYear)
                    pageNumber += 1
                }
            }
        }

    }

    private fun templateDayAndNight(pageNumber: Int, date: String, year: String) {
//        val pixelBasedTextSize = 23 * screenDensity
        var pixelBasedTextSize = 0
        val pageInfo =
            PdfDocument.PageInfo.Builder(screenSize.width, screenSize.height, pageNumber)
                .create()
        val page = document.startPage(pageInfo)
        canvas = page.canvas
        canvas.drawRect(
            RectF(
                0f, 0f, screenSize.width.toFloat(), screenSize.height
                    .toFloat()
            ), FTDairyTextPaints.background_Paint
        )
        pixelBasedTextSize = ScreenUtils.calculateFontSizeByBoundingRect(
            context,
            "Date : $date,",
            (17.5 * widthPercent).toInt(),
            (2.166 * heightPercent).toInt()
        )

        val dayPaint = Paint()
        dayPaint.color = context.resources.getColor(R.color.text_color, context.theme)
        dayPaint.textSize = 23 * screenDensity
        dayPaint.style = Paint.Style.FILL_AND_STROKE
        dayPaint.strokeWidth = .1f
        dayPaint.typeface = ResourcesCompat.getFont(context, R.font.lora_regular)

        val yearPaint = Paint()
        yearPaint.color = context.resources.getColor(R.color.day_text_color, context.theme)
        yearPaint.textSize = 23 * screenDensity
        yearPaint.style = Paint.Style.FILL_AND_STROKE
        yearPaint.strokeWidth = .1f
        yearPaint.typeface = ResourcesCompat.getFont(context, R.font.lora_regular)

        var rect = Rect()
        var mDate = "Date : $date,"
        dayPaint.getTextBounds(mDate, 0, mDate.length, rect)

        canvas.drawText("Date : $date", pageLeftPadding, pageTopPadding + dairyTextSize, dayPaint)
//        canvas.drawRect(FTDairyDayPageRect.yearPageRect, FTDairyTextPaints.coloredDayRectBoxPaint)
        canvas.drawText(
            year,
            pageLeftPadding + rect.width().toFloat(),
            pageTopPadding + dairyTextSize,
            yearPaint
        )

        var xPosition = (canvas.width / 2).toFloat()
//        var xPosition =   canvas.width.toFloat()
        var yPosition = heightPercent * 12.08f

        val item = ScreenUtils.pickRandomQuote(context,quotesList)

        if (isLandScape) {
            xPosition = (canvas.width).toFloat()
            yPosition = pageTopPadding
            drawMultiLineQuote(
                item.quote,
                (canvas.width).toFloat(),
                yPosition,
                20
            )
            yPosition = (heightPercent * 13.66f) + quoteTextHeight
            drawCenterText(
                canvas.width.toFloat() - pageLeftPadding * 3,
                yPosition,
                canvas,
                "-" + item.author,
                18
            )
        } else {
            drawMultiLineQuote(
                item.quote,
                canvas.width.toFloat(),
                yPosition,
                20
            )
            yPosition = (heightPercent * 15.03f) + quoteTextHeight
            drawCenterText(xPosition, yPosition, canvas, "-" + item.author, 18)

        }

        mTop = if (!isLandScape /*&& orientation != Configuration.ORIENTATION_LANDSCAPE*/) {
            heightPercent * 22.41f
        } else {
            heightPercent * 18.61f
        }
        dailyQuestionnaire(canvas, "My affirmations for the day")
        dailyQuestionnaire(canvas, "Today I will accomplish")
        dailyQuestionnaire(canvas, "I am thankful for")
        val rectPaint = Paint()
        rectPaint.style = Paint.Style.FILL
        rectPaint.color = context.resources.getColor(R.color.box_background, context.theme)
        mTop += if (!isLandScape /*&& orientation != Configuration.ORIENTATION_LANDSCAPE*/) {
            4.08f * heightPercent
        } else {
            4.86f * heightPercent
        }
        canvas.drawRect(
            RectF(
                0f, mTop, screenSize.width.toFloat(), screenSize.height
                    .toFloat()
            ), rectPaint
        )
        dailyQuestionnaire(canvas, "Three things that made me happy today")
        dailyQuestionnaire(canvas, "Today I learnt")
        document.finishPage(page)
    }



    private fun dailyQuestionnaire(canvas: Canvas?, question: String) {
        FTDairyTextPaints.dairyText_Paint.textSize = 20 * screenDensity
        mTop += if (!isLandScape /*&& orientation != Configuration.ORIENTATION_LANDSCAPE*/) {
            pageTopPadding + dairyTextSize
        } else {
            pageTopPadding
        }
        canvas!!.drawText(question, pageLeftPadding, mTop, FTDairyTextPaints.dairyText_Paint)
        var fgPaintSel: Paint
        for (i in 1..maxLines) {
            mTop = mTop + templateDottedLineGap
            mBottom = mBottom + heightPercent * 2.91f
            fgPaintSel = Paint()
            fgPaintSel.setARGB(255, 0, 0, 0)
            fgPaintSel.style = Paint.Style.STROKE
            fgPaintSel.pathEffect = DashPathEffect(floatArrayOf(2f, 5f), 0f)
            canvas.drawLine(
                pageLeftPadding,
                mTop,
                screenSize.width - pageLeftPadding,
                mTop,
                fgPaintSel
            )
        }
    }

    private fun drawCenterText(
        xPosition: Float,
        yPosition: Float,
        canvas: Canvas?,
        text: String,
        textSize: Int
    ) {
        val rect = Rect()
        /*
         * I have had some problems with Canvas.getHeight() if API < 16. That's why I use Canvas.getClipBounds(Rect) instead. (Do not use Canvas.getClipBounds().getHeight() as it allocates memory for a Rect.)
         * */canvas!!.getClipBounds(rect)
        val paint = Paint()
        paint.textAlign = Paint.Align.CENTER
        /* paint.textSize = dairyTextSize*/
        paint.textSize = textSize * screenDensity
        paint.color = Color.GRAY
        paint.style = Paint.Style.FILL
        paint.typeface = ResourcesCompat.getFont(context, R.font.lora_italic)
        paint.getTextBounds(text, 0, text.length, rect)
        canvas.drawText(text, xPosition, yPosition, paint)
    }

    private fun drawMultiLineText(text: String, dy: Float, isBulletPoint: Boolean) {
        val pointTextSize = (25 * screenDensity)
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
        val dx = if (isBulletPoint) {
            8.125f * widthPercent
        } else {
            4.16f * widthPercent
        }
        canvas.translate(dx, dy)
        staticLayout.draw(canvas)
        canvas.restore()
        if (isBulletPoint) {
            bulletPointsTextHeight = staticLayout.height
        }
    }

    private fun drawMultiLineQuote(
        text: String,
        dx: Float,
        dy: Float,
        quoteTextSize: Int
    ) {
        val pointTextSize = (quoteTextSize * screenDensity)
        val textPaint = TextPaint().apply {
            textSize = pointTextSize
            typeface = ResourcesCompat.getFont(FTApp.getInstance(), R.font.lora_italic)
            color = Color.GRAY
            textAlign = Paint.Align.LEFT
        }
        var staticLayout: StaticLayout? = null
        if (isLandScape) {
            staticLayout = StaticLayout.Builder.obtain(
                text,
                0,
                text.length,
                textPaint,
                dx.toInt() - pageLeftPadding.toInt()
            ).setAlignment(Layout.Alignment.ALIGN_OPPOSITE)
                .setLineSpacing(0f, 1.25f).build()
        } else {
            staticLayout = StaticLayout.Builder.obtain(
                text,
                0,
                text.length,
                textPaint,
                dx.toInt() - pageLeftPadding.toInt()
            ).setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1.25f).build()

        }
        canvas.save()
        canvas.translate(pageLeftPadding, dy-pageLeftPadding)
        staticLayout.draw(canvas)
        canvas.restore()
        quoteTextHeight = staticLayout.height
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
            screenSize.height - STATUS_BAR_HEIGHT - calendarTopManualSpace - pageBottomPadding - (rows - 1) * calendarVerticalSpacing
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

    private fun createPageHyperLinks() {
        try {
            val pdfFilePath: String = context.getFilesDir().toString() + "/" + "demo.pdf"
            val pdfFile = File(pdfFilePath)
            val pdDocument: PDDocument = PDDocument.load(pdfFile)
            var pdPage = PDPage()
            var pageIndex = 1
            var yearPage = pdDocument.getPage(pageIndex)

            FTDairyYearPageRect.yearRectInfo.forEachIndexed { monthIndex, arrayList ->
                arrayList.forEachIndexed { dayIndex, monthRect ->

                    pageIndex++
                    val page = pdDocument.getPage(pageIndex)
                    if (page != null) {
                        linkDayPageToYear(page, yearPage)
                        val link = PDAnnotationLink()
                        val destination: PDPageDestination = PDPageFitWidthDestination()
                        val action = PDActionGoTo()

                        destination.page = page
                        action.destination = destination
                        link.action = action

                        val pdRectangle = PDRectangle()
                        pdRectangle.lowerLeftX = monthRect.left
                        pdRectangle.lowerLeftY = page.mediaBox.height - monthRect.top
                        pdRectangle.upperRightX = monthRect.right
                        pdRectangle.upperRightY = page.mediaBox.height - monthRect.bottom
                        link.rectangle = pdRectangle
                        yearPage.getAnnotations().add(link)
                    }

                }
            }
            /* pdDocument.save(pdfFile)
             pdDocument.close()
 */
            val root = Environment.getExternalStorageDirectory().toString()
            val myDir = File("$root/Journal_PDF")
            if (!myDir.exists()) {
                myDir.mkdirs()
            }
            val fileName = Calendar.getInstance().timeInMillis.toString()
            val fname =
                screenSize.width.toString() + " x " + screenSize.height + "--" + fileName + ".pdf";
            val file = File(myDir, fname);
            if (file.exists())
                file.delete();

            val out: FileOutputStream = FileOutputStream(file);
            pdDocument.save(file)
            pdDocument.close()
            out.flush();
            out.close();

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun linkDayPageToYear(page: PDPage, yearPage: PDPage) {
        val link = PDAnnotationLink()
        val destination: PDPageDestination = PDPageFitWidthDestination()
        val action = PDActionGoTo()

        destination.page = yearPage
        action.destination = destination
        link.action = action

        val pdRectangle = PDRectangle()
        pdRectangle.lowerLeftX = FTDairyDayPageRect.yearPageRect.left.toFloat()
        pdRectangle.lowerLeftY = page.mediaBox.height - FTDairyDayPageRect.yearPageRect.top
        pdRectangle.upperRightX = FTDairyDayPageRect.yearPageRect.right.toFloat()
        pdRectangle.upperRightY = page.mediaBox.height - FTDairyDayPageRect.yearPageRect.bottom

        link.rectangle = pdRectangle
        page.getAnnotations().add(link)

    }

}