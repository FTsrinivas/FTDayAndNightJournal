package com.example.demo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Size
import androidx.core.content.res.ResourcesCompat
import com.example.demo.generator.FTDiaryFormat
import com.example.demo.generator.models.QuoteItem
import com.example.demo.generator.models.info.FTMonthInfo
import com.example.demo.generator.models.info.FTYearFormatInfo
import com.example.demo.generator.models.info.rects.FTDairyYearPageRect
import com.example.demo.utils.FTDairyTextPaints
import com.example.demo.utils.ScreenUtils
import com.example.demo.utils.ScreenUtils.getQuotesList
import java.io.FileNotFoundException
import java.io.FileOutputStream

class FTDayAndNightJournalMobile(
    private val context: Context,
    private var screenSize: Size,
    private val isLandScape: Boolean
) : FTDiaryFormat(context, screenSize, isLandScape) {

    private var quotesList = ArrayList<QuoteItem>()
    private val document = PdfDocument()
    private lateinit var canvas: Canvas

    private var heightPercent: Float =
        (screenSize.height - FTDayAndNightJournal.STATUS_BAR_HEIGHT) / 100f
    private var widthPercent: Float = screenSize.width / 100f
    private var pageTopPadding = 0f
    private var pageLeftPadding = 0f
    private var pageBottomPadding = 0f

    //Intro page Measurements
    private var bulletPointsTextHeight = 0
    private var quoteTextHeight = 0

    //Calendar page Measurements
    private var maxRows = 4
    private var maxColumns = 3
    private var calendarVerticalSpacing = 0f
    private var calendarHorizontalSpacing = 0f
    private var calendarTopManualSpace = 0f
    private var calendarBoxHeight = 0f
    private var calendarBoxWidth = 0f

    private var calendarMonthTextSize = 0f
    private var calendarYearTextSize = 0f


    override fun renderYearPage(
        context: Context,
        months: MutableList<FTMonthInfo>,
        calendarYear: FTYearFormatInfo
    ) {
        super.renderYearPage(context, months, calendarYear)
        screenDensity = context.resources.displayMetrics.density
        calenderDynamicSizes
        quotesList = getQuotesList(context)
        createIntroPage()
        createCalendarPage(months, calendarYear)
        /*  createTemplate(months) */
        val fos: FileOutputStream
        try {
            fos = context.openFileOutput("demo.pdf", Context.MODE_PRIVATE)
            document.writeTo(fos)
            document.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
//        createPageHyperLinks()
    }

    private val calenderDynamicSizes: Unit
        get() {
            pageTopPadding = heightPercent * 2.638f
            pageLeftPadding = widthPercent * 5.83f
            pageBottomPadding = heightPercent * 4.86f
            calendarVerticalSpacing = heightPercent * 1.381f
            calendarHorizontalSpacing = widthPercent * 2.25f
            calendarTopManualSpace = heightPercent * 10.58f

            calendarMonthTextSize = 11 * screenDensity
            calendarYearTextSize = 20 * screenDensity
            /* templateDottedLineGap = heightPercent * 2.91f

             findBoxWidth(maxColumns, pageTopPadding)
             findBoxHeight(maxRows)
             calendarDayTextSize = 10 * screenDensity
             calendarMonthTextSize = 15 * screenDensity
             calendarYearTextSize = 40 * screenDensity
             dayLeftMargin = widthPercent * 3f
             dayTopMargin = heightPercent * 1.94f
             individualDayinCalendar = calendarBoxWidth / 7
             dairyTextSize = heightPercent * 1.66f*/
        }

    private fun createIntroPage() {
        val pageInfo = PdfDocument.PageInfo.Builder(screenSize.width, screenSize.height, 1).create()
        val startingPage = document.startPage(pageInfo)
        canvas = startingPage.canvas
        canvas.drawRect(
            RectF(0f, 0f, screenSize.width.toFloat(), screenSize.height.toFloat()),
            FTDairyTextPaints.background_Paint
        )
        FTDairyTextPaints.introQuote_Paint.textSize = 11 * screenDensity

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
        FTDairyTextPaints.introAuthorText_Paint.textSize = 10 * screenDensity
        canvas.drawText(
            context.resources.getString(R.string.ftdairy_author_name),
            (screenSize.width / 2).toFloat(),
            (24 * screenDensity + 30 + introQuoteBoxHeight / 2),
            FTDairyTextPaints.introAuthorText_Paint
        )
        var introPageHeight = introQuoteBoxHeight + 8.05f * heightPercent
        FTDairyTextPaints.introText_Paint.textSize = 18 * screenDensity
        canvas.drawText(
            context.resources.getString(R.string.ftdairy_title),
            (screenSize.width / 2).toFloat(),
            introPageHeight,
            FTDairyTextPaints.introText_Paint
        )
        introPageHeight += introPageHeight + 5f * heightPercent
        var dy = 39.02f * heightPercent
        drawMultiLineText(
            context.resources.getString(R.string.ftdairy_points_newline),
            dy,
            isBulletPoint = true
        )
        dy += bulletPointsTextHeight
        dy += 5.5f * heightPercent

        drawMultiLineText(
            context.resources.getString(R.string.ftdairy_static_para),
            dy,
            isBulletPoint = false
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
        FTDairyTextPaints.calendar_Month_Paint.textSize = calendarMonthTextSize

        canvas.drawText("2021",
//            getYearHeading(calendarYear.startMonth),
            pageLeftPadding,
            pageTopPadding + calendarYearTextSize,
            FTDairyTextPaints.calendar_Year_Paint
        )

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

                /*   //show Week Day Names
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

                dateLeftSpace += individualDayinCalendar*/


                canvas.drawText(
                    monthName,
                    boxLeft  ,
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


    private fun drawMultiLineText(text: String, dy: Float, isBulletPoint: Boolean) {
        val pointTextSize = (14 * screenDensity)
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


}
