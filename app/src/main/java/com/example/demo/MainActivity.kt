package com.example.demo

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.demo.generator.FTDairyRenderFormat
import com.example.demo.generator.models.info.*
import com.example.demo.utils.DrawingTextTemplates
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.interactive.action.PDActionGoTo
import com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.Year
import java.util.*

private const val x_axis_Margin = 40f

class MainActivity : AppCompatActivity() {
    var screenHeight: Int = 0
    var screenWidth: Int = 0

    var mTop: Float = 0f
    var mBottom: Float = 0f

    //Calendar screen
    var topPadding = 0f
    var leftPadding = 0f
    var verticalSpacing = 0f
    var horizontalSpacing = 0f
    var boxWidth = 0f
    var boxHeight = 0f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PDFBoxResourceLoader.init(getApplicationContext())
        //To find the size of screen dynamically using display Metrics
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        //Screen Height and Width
        screenWidth = (displayMetrics.widthPixels)
        screenHeight = (displayMetrics.heightPixels)

        topPadding = (screenHeight / 100f) * 3.912f
        leftPadding = (screenWidth / 100f) * 5.155f
        verticalSpacing = (screenHeight / 100f) * 1.81f
        horizontalSpacing = (screenWidth / 100f) * 1.81f
        boxWidth = (screenWidth / 100f) * 28.53f
        boxHeight = (screenHeight / 100f) * 19.69f

//        templateDayAndNight()

        val firstDayOfCurrentYear = Calendar.getInstance()
        firstDayOfCurrentYear[Calendar.DATE] = 1
        firstDayOfCurrentYear[Calendar.MONTH] = 0
        val startDate = firstDayOfCurrentYear.time

        val lastDayOfCurrentYear = Calendar.getInstance()
        lastDayOfCurrentYear[Calendar.DATE] = 31
        lastDayOfCurrentYear[Calendar.MONTH] = 11

        val lastDate = lastDayOfCurrentYear.time

        val yearFormatInfo =
            FTYearFormatInfo(startDate, lastDate)
        val dairyGenerator = FTDiaryGeneratorV2(this, null, yearFormatInfo)
        dairyGenerator.generate()

    }

    fun createTomBoxPdf() {
        val pdfFilePath: String = this@MainActivity.getFilesDir().toString() + "/" + "demo.pdf"
        val pdfFile = File(pdfFilePath)
        val pdDoc: PDDocument = PDDocument.load(pdfFile)
        val pdPage = PDPage()
        pdDoc.addPage(pdPage)
        /*for (i in 0..3) {
            val pages = PDPage()
            pdDoc.addPage(pages)
            val stream = PDPageContentStream(pdDoc, pages)
            stream.addRect(10f, 10f, 400f, 400f)

            stream.setStrokingColor(10, 10, 100)
            stream.fill()
            stream.close()

        }
         val stream = PDPageContentStream(pdDoc, pdPage)
         stream.addRect(10f, 10f, 100f,203f)

         stream.setStrokingColor(0, 0, 255)

         stream.close()*/

        var pager = pdDoc.getPage(0)
        val link = PDAnnotationLink()
        val destination: PDPageDestination = PDPageFitWidthDestination()
        val action = PDActionGoTo()

        destination.page = pdDoc.getPage(5)
        action.destination = destination
        link.action = action
        link.page = pager

        link.rectangle = PDRectangle(100f, 100f);
        pager.annotations.add(link)
        //page.annotations.add(link)
        pdDoc.save(pdfFile)
        pdDoc.close()

        /*stream.fill()
        stream.close()
        try {
            val fos: FileOutputStream = openFileOutput("demo.pdf", Context.MODE_PRIVATE)
            document.writeTo(FileOutputStream(file))
        pdDoc.writeTo(fos)
            pdDoc.save(fos)
            pdDoc.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("##error", Log.getStackTraceString(e));
        }*/

    }

    private fun createDefaultPdf() {
        try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(screenWidth, screenHeight, 1).create()
            var page = document.startPage(pageInfo)
            var canvas: Canvas = page.canvas
            var paint = Paint()

            paint.setColor(Color.GRAY)
            paint.style = Paint.Style.FILL_AND_STROKE
            paint.strokeWidth = .1f
//            canvas.scale(1f, 1f)

            var top = 0f
            var bottom = 0f
            for (i in 0..9) {
                top = top.plus(50)
                bottom = bottom.plus(50)
                val fgPaintSel = Paint()
                fgPaintSel.setARGB(255, 0, 0, 0)
                fgPaintSel.style = Paint.Style.STROKE
                fgPaintSel.pathEffect = DashPathEffect(floatArrayOf(2f, 5f), 0f)
                canvas.drawRect(50f, top, screenWidth.toFloat().minus(50f), bottom, fgPaintSel)
            }
            canvas.drawText(
                "<a href='https://www.google.com/?client=safari'>some link</a>",
                50f,
                50f,
                paint
            );
            document.finishPage(page)
            var pageInfoNext: PdfDocument.PageInfo
            var pageNew: PdfDocument.Page

            for (i in 1..4) {
                pageInfoNext = PdfDocument.PageInfo.Builder(screenWidth, screenHeight, 1).create()
                pageNew = document.startPage(pageInfoNext)
                document.finishPage(pageNew)
            }
            val fos: FileOutputStream = openFileOutput("demo.pdf", Context.MODE_PRIVATE)
            document.writeTo(fos)
            document.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("##error", Log.getStackTraceString(e));
        }
    }

    private fun templateDayAndNight() {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(screenWidth, screenHeight, 1).create()
        var page = document.startPage(pageInfo)
        var canvas: Canvas = page.canvas
        var paint = Paint()
        paint.setColor(Color.GRAY)
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.strokeWidth = .1f
//        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(
            "Date : January 7th, 2021",
            x_axis_Margin,
            41f,
            paint
        )
        val xPos = canvas.width / 2
        val yPos = (canvas.height / 2 - (paint.descent() + paint.ascent()) / 2).toInt()

        DrawingTextTemplates.drawCenterText(
            xPosition = xPos.toFloat(),
            125f,
            canvas,
            "The place to be happy is here. The time to be happy is now."

        )
        DrawingTextTemplates.drawCenterText(xPosition = xPos.toFloat(), 145f, canvas, "-Robert G. Ingersoll")
        mTop = 239f
        mBottom = 50f
        dailyQuestionnaire( canvas, "My affirmations for the day")
        dailyQuestionnaire( canvas, "Today I will accomplish")
        dailyQuestionnaire( canvas, "I am thankful for")
        dailyQuestionnaire( canvas, "Three things that made me happy today")
        dailyQuestionnaire( canvas, "Today I learnt")
        drawRectangles(canvas)
        document.finishPage(page)
        try {
            val fos: FileOutputStream = openFileOutput("demo.pdf", Context.MODE_PRIVATE)
            document.writeTo(fos)
            document.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("##error", Log.getStackTraceString(e));
        }
    }

    private fun dailyQuestionnaire(
        canvas: Canvas,
        question: String

        ) {
        var paint = Paint()
        mTop += 36
        canvas.drawText(
            question,
            x_axis_Margin,
            mTop,
            paint
        )
        for (i in 0..2) {
            mTop = mTop.plus(26)
            mBottom = mBottom.plus(26)

            val fgPaintSel = Paint()
            fgPaintSel.setARGB(255, 0, 0, 0)
            fgPaintSel.style = Paint.Style.STROKE
            fgPaintSel.pathEffect = DashPathEffect(floatArrayOf(2f, 5f), 0f)
            canvas.drawLine(x_axis_Margin, mTop, screenWidth.toFloat().minus(50f), mTop, fgPaintSel)
        }
    }

    private fun drawRectangles(canvas: Canvas) {
        val monthName = arrayOf(
            "January", "February",
            "March", "April", "May", "June", "July",
            "August", "September", "October", "November",
            "December"
        )
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR);
        var month = calendar.get(Calendar.MONDAY)

        val textPaint = Paint()
        textPaint.textSize = topPadding
        textPaint.color = Color.GRAY
        canvas.drawText("" + year, leftPadding, topPadding * 2, textPaint)
        val paint = Paint()
        var rectLeft = 0f
        var rectTop = (screenHeight / 100) * 11.82f
        var rectRight = 0f
        var rectBottom = 0f
        rectBottom = rectTop + boxHeight

        for (rows in 1..4) {
            rectLeft = leftPadding
            rectRight = leftPadding + boxWidth
            if (rows > 1) {
                rectTop += (boxHeight.plus(verticalSpacing))
                rectBottom = rectTop + boxHeight
            }
            for (columns in 1..3) {
                val r = RectF(rectLeft, rectTop, rectRight, rectBottom)
                rectLeft = rectRight + horizontalSpacing
                rectRight += boxWidth + horizontalSpacing
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.parseColor("#E1E9E8"));
                canvas.drawRoundRect(r, 10f, 10f, paint);
            }
        }
    }
}