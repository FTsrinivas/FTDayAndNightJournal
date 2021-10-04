package com.example.demo

import android.content.Context
import android.content.res.Configuration
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import android.util.Size
import androidx.core.content.res.ResourcesCompat
import com.example.demo.generator.FTDiaryFormat
import com.example.demo.generator.models.info.FTMonthInfo
import com.example.demo.generator.models.info.FTYearFormatInfo
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
import java.util.*

class FTDayAndNightJournal(
    private val context: Context,
    private val screenSize: Size,
    private val isLandScape: Boolean
) : FTDiaryFormat(context, screenSize, isLandScape) {

    private val document = PdfDocument()
    var screenDensity = 0.0f
    var orientation: Int = 1
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
    private var lineGap = 0f

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
        screenDensity = context.resources.displayMetrics.density
        orientation = context.resources.configuration.orientation
        Log.d(
            "density:",
            " - " + screenDensity + " && " + screenSize.width + " x " + screenSize.height
        )
        calenderDynamicSizes
        createIntroPage()
        createCalendarPage(months, calendarYear)
        createTemplate(months)

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
            getYearHeading(calendarYear.startMonth),
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
                var dateTopSpace = boxTop + calendarVerticalSpacing * 4

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
                    dateLeftSpace += individualDayinCalendar
                }

                canvas.drawText(
                    monthName,
                    boxLeft + individualDayinCalendar / 2,
                    boxTop + calendarVerticalSpacing * 2,
                    FTDairyTextPaints.calendar_Month_Paint
                )
                boxLeft = boxRight + calendarHorizontalSpacing
                boxRight += calendarBoxWidth + calendarHorizontalSpacing
                month_Of_Year++
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
            (22 * screenDensity + 15 + introQuoteBoxHeight / 2).toFloat(),
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
            context.resources.getString(R.string.ftdairy_point_one),
            dy,
            isBulletPoint = true
        )
        dy += 6.91f * heightPercent
        drawMultiLineText(
            context.resources.getString(R.string.ftdairy_point_two),
            dy,
            isBulletPoint = true
        )
        dy += 6.91f * heightPercent
        drawMultiLineText(
            context.resources.getString(R.string.ftdairy_point_three),
            dy,
            isBulletPoint = true
        )
        dy += 11.33f * heightPercent
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
            findRowCount()
            findColumnCount()
            findMaxLines()
            lineGap = heightPercent * 2.91f
            if (isLandScape || orientation == Configuration.ORIENTATION_LANDSCAPE) {
                /* maxRows = 3
                 maxColumns = 4
                 maxLines = 2*/
                pageLeftPadding = widthPercent * 3.125f
                pageTopPadding = heightPercent * 5.5f
                calendarVerticalSpacing = heightPercent * 2.79f
                calendarHorizontalSpacing = widthPercent * 1.95f
                calendarTopManualSpace = heightPercent * 15.27f
                lineGap = heightPercent * 4.44f
            }
            findBoxWidth(maxColumns, pageTopPadding)
            findBoxHeight(maxRows)
            calendarDayTextSize = 10 * screenDensity
            calendarMonthTextSize = 13 * screenDensity
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
//        dayLeftMargin = y + dayLeftMargin
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

    private fun createTemplate(months: List<FTMonthInfo>) {
        var pageNumber = 3
        months.forEach { ftMonthInfo ->
            ftMonthInfo.dayInfos.forEach { ftDayInfo ->
                if (ftDayInfo.belongsToSameMonth) {
                    val dairyDate =
                        ftMonthInfo.monthTitle + " " + ftDayInfo.dayString + ", " + ftMonthInfo.year
                    templateDayAndNight(pageNumber, dairyDate)
                    pageNumber += 1
                }
            }
        }

    }

    private fun templateDayAndNight(pageNumber: Int, date: String) {
        var pixelBasedTextSize = 20 * screenDensity
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
        val paint = Paint()
        paint.color = context.resources.getColor(R.color.text_color, context.theme)
//        paint.textSize = dairyTextSize
        paint.textSize = pixelBasedTextSize
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.strokeWidth = .1f

        canvas.drawText(
            "Date : $date",
            pageLeftPadding,
            pageTopPadding + dairyTextSize,
            paint
        )
        val xPosition = (canvas.width / 2).toFloat()
        var yPosition = heightPercent * 12.08f
        drawCenterText(
            xPosition,
            yPosition,
            canvas,
            "The place to be happy is here. The time to be happy is now."
        )
        yPosition = heightPercent * 15.03f
        drawCenterText(xPosition, yPosition, canvas, "-Robert G. Ingersoll")

        mTop = if (!isLandScape && orientation != Configuration.ORIENTATION_LANDSCAPE) {
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
        mTop += if (!isLandScape && orientation != Configuration.ORIENTATION_LANDSCAPE) {
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

    private fun getYearHeading(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return "" + calendar[Calendar.YEAR]
    }

    private fun dailyQuestionnaire(canvas: Canvas?, question: String) {
        FTDairyTextPaints.dairyText_Paint.textSize = 20 * screenDensity
        mTop += if (!isLandScape && orientation != Configuration.ORIENTATION_LANDSCAPE) {
            pageTopPadding + dairyTextSize
        } else {
            pageTopPadding
        }
        canvas!!.drawText(question, pageLeftPadding, mTop, FTDairyTextPaints.dairyText_Paint)
        var fgPaintSel: Paint
        for (i in 1..maxLines) {
            mTop = mTop + lineGap
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
        text: String
    ) {
        val rect = Rect()
        /*
         * I have had some problems with Canvas.getHeight() if API < 16. That's why I use Canvas.getClipBounds(Rect) instead. (Do not use Canvas.getClipBounds().getHeight() as it allocates memory for a Rect.)
         * */canvas!!.getClipBounds(rect)
        val paint = Paint()
        paint.textAlign = Paint.Align.CENTER
        /* paint.textSize = dairyTextSize*/
        paint.textSize = 20 * screenDensity
        paint.color = Color.GRAY
        paint.style = Paint.Style.FILL
        paint.getTextBounds(text, 0, text.length, rect)
        canvas.drawText(text, xPosition, yPosition, paint)
    }

    private fun drawMultiLineText(text: String, dy: Float, isBulletPoint: Boolean) {
        val pointTextSize = 25 * screenDensity
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
            text.length - 1,
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
    }

    fun findColumnCount(): Int {
        maxColumns = if (orientation == Configuration.ORIENTATION_PORTRAIT) 3 else 4
        return maxColumns
    }

    fun findRowCount(): Int {
        maxRows = if (orientation == Configuration.ORIENTATION_PORTRAIT) 4 else 3
        return maxRows
    }

    fun findMaxLines(): Int {
        maxLines = if (orientation == Configuration.ORIENTATION_PORTRAIT) 3 else 2
        return maxRows
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
        val pdfFilePath: String = context.getFilesDir().toString() + "/" + "demo.pdf"
        val pdfFile = File(pdfFilePath)
        val pdDoc: PDDocument = PDDocument.load(pdfFile)
        val pdPage = PDPage()
        pdDoc.addPage(pdPage)

        var pager = pdDoc.getPage(0)
        val link = PDAnnotationLink()
        val destination: PDPageDestination = PDPageFitWidthDestination()
        val action = PDActionGoTo()

        destination.page = pdDoc.getPage(5)
        action.destination = destination
        link.action = action
        link.page = pager

        link.rectangle = PDRectangle((screenSize.width/2).toFloat(),(screenSize.height/2).toFloat())
        pager.annotations.add(link)
        //page.annotations.add(link)
        pdDoc.save(pdfFile)
        pdDoc.close()

    }
}