package com.example.demo

import android.content.Context
import android.graphics.Canvas
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
import com.example.demo.utils.FTDairyTextPaints
import com.example.demo.utils.ScreenUtils
import com.example.demo.utils.ScreenUtils.getQuotesList
import java.io.FileNotFoundException
import java.io.FileOutputStream

class FTDayAndNightJournalMobile(private val context: Context,
                                 private var screenSize: Size,
                                 private val isLandScape: Boolean) : FTDiaryFormat(context, screenSize, isLandScape){

    private var quotesList = ArrayList<QuoteItem>()
    private val document = PdfDocument()
    private lateinit var canvas: Canvas

    private var heightPercent: Float = (screenSize.height - FTDayAndNightJournal.STATUS_BAR_HEIGHT) / 100f
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


    override fun renderYearPage(
        context: Context,
        months: MutableList<FTMonthInfo>?,
        calendarYear: FTYearFormatInfo?
    ) {
        super.renderYearPage(context, months, calendarYear)

        calenderDynamicSizes
        quotesList = getQuotesList(context)
        createIntroPage()
       /* createCalendarPage(months, calendarYear)
        createTemplate(months)
*/
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
           /* calendarVerticalSpacing = heightPercent * 1.381f
            calendarHorizontalSpacing = widthPercent * 2.25f
            calendarTopManualSpace = heightPercent * 10.58f
            templateDottedLineGap = heightPercent * 2.91f

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


}
