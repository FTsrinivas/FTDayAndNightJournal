package com.example.demo

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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
import java.util.*

import android.content.res.Resources
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.dd.plist.NSArray
import com.dd.plist.NSDictionary
import com.dd.plist.PropertyListParser
import com.example.demo.databinding.ActivityMainBinding
import com.example.demo.generator.models.QuoteItem
import org.w3c.dom.Element

import org.w3c.dom.Node

import kotlin.collections.ArrayList
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Environment
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.demo.generator.models.ScreenResolutions
import com.example.demo.generator.models.ScreenSizeAdapter


private const val x_axis_Margin = 40f
const val CREATE_FILE = 1

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
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
    lateinit var binding: ActivityMainBinding
    lateinit var spinnerList: ArrayList<ScreenResolutions>
    val densitySpinnerArray = arrayOf(0.75f, 1f, 1.25f, 1.5f, 2f, 2.25f, 2.5f)
    lateinit var selectedItem: ScreenResolutions
    var screenDensity = 1f

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
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
        isStoragePermissionGranted()
        setUpUI()

        var quotesList = ArrayList<QuoteItem>()
        val res: Resources = this.getResources()
        val istream = res.assets.open("quotes.plist")
        var rootDict = PropertyListParser.parse(istream)
        (rootDict as NSArray).array.forEachIndexed { index, nsObject ->
            val item = QuoteItem(
                (nsObject as NSDictionary).get("Quote").toString(),
                (nsObject as NSDictionary).get("Author").toString()
            )
            quotesList.add(item)
        }

    }

    private fun setUpUI() {
        var densityAdapter =
            ArrayAdapter<Float>(this, android.R.layout.simple_spinner_item, densitySpinnerArray)
        densityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDensity.adapter = densityAdapter
        binding.spinnerDensity.onItemSelectedListener = this

        spinnerList = ArrayList<ScreenResolutions>()
        spinnerList.add(ScreenResolutions("Samsung Galaxy Tab S7 Plus", 2800, 1752, 12.4f))
        spinnerList.add(ScreenResolutions("Lenovo Tab P11 Pro", 2560, 1600, 11.5f))
        spinnerList.add(ScreenResolutions("Samsung Galaxy Tab S6", 2560, 1600, 10.5f))
        spinnerList.add(ScreenResolutions("Huawei MatePad Pro", 2560, 1600, 10.8f))
        spinnerList.add(ScreenResolutions("Samsung Galaxy Tab S6 Lite", 2000, 1200, 10.4f))
        spinnerList.add(ScreenResolutions("Amazon Fire HD 10 (2019)", 1920, 1200, 10.10f))
        spinnerList.add(ScreenResolutions("Amazon Fire HD 8 Plus", 1280, 800, 8.0f))
        spinnerList.add(ScreenResolutions("Amazon Fire HD 8 (2020)", 1280, 800, 8.0f))
        val adapter = ScreenSizeAdapter(this, R.layout.item_screen_sizes, spinnerList)
        adapter.setDropDownViewResource(R.layout.item_screen_sizes)
        binding.spinnerScreenSize.adapter = adapter
        binding.spinnerScreenSize.onItemSelectedListener = this

        binding.btnCreatePDF.setOnClickListener {
            if (selectedItem == null)
                selectedItem = spinnerList.get(0)

            SaveFileInExternalStorage(this, selectedItem,screenDensity).execute()

        }
    }

    protected fun getNodeValue(tag: String?, element: Element): String {
        val nodeList = element.getElementsByTagName(tag)
        val node = nodeList.item(0)
        if (node != null) {
            if (node.hasChildNodes()) {
                val child = node.firstChild
                while (child != null) {
                    if (child.nodeType == Node.TEXT_NODE) {
                        return child.nodeValue
                    }
                }
            }
        }
        return ""
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
        DrawingTextTemplates.drawCenterText(
            xPosition = xPos.toFloat(),
            145f,
            canvas,
            "-Robert G. Ingersoll"
        )
        mTop = 239f
        mBottom = 50f
        dailyQuestionnaire(canvas, "My affirmations for the day")
        dailyQuestionnaire(canvas, "Today I will accomplish")
        dailyQuestionnaire(canvas, "I am thankful for")
        dailyQuestionnaire(canvas, "Three things that made me happy today")
        dailyQuestionnaire(canvas, "Today I learnt")
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


    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            ) {
                true
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    1
                )
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Log.v("##TAG", "Permission: " + permissions[0] + "was " + grantResults[0])
            //resume tasks needing this permission
        } else if (grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED) {
            isStoragePermissionGranted()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (view?.id) {
            R.id.spinnerScreenSize ->{
                Toast.makeText(
                    applicationContext,
                    spinnerList[position].name,
                    Toast.LENGTH_LONG
                )
                    .show()
            selectedItem = spinnerList[position]
            }
            R.id.spinnerDensity ->{
                screenDensity = densitySpinnerArray[position]
            }
        }
    }
    override fun onNothingSelected(parent: AdapterView<*>?) {
//        TODO("Not yet implemented")
    }

    class SaveFileInExternalStorage(context: Context, selectedItem: ScreenResolutions,screenDensity : Float) :
        AsyncTask<Void, Int, Long>() {
        var dialog: ProgressDialog? = null
        var context: Context? = null
        var selectedItem: ScreenResolutions? = null
        var density  =1f

        init {
            this.context = context
            this.selectedItem = selectedItem
            this.density = screenDensity
        }

        override fun doInBackground(vararg params: Void?): Long {

            var folder = File(Environment.getExternalStorageState())
            val firstDayOfCurrentYear = Calendar.getInstance()
            firstDayOfCurrentYear[Calendar.DATE] = 1
            firstDayOfCurrentYear[Calendar.MONTH] = 0
            firstDayOfCurrentYear[Calendar.HOUR_OF_DAY] = 1
            firstDayOfCurrentYear[Calendar.MINUTE] = 1
            firstDayOfCurrentYear[Calendar.SECOND] = 1
            val startDate = firstDayOfCurrentYear.time

            val lastDayOfCurrentYear = Calendar.getInstance()
            lastDayOfCurrentYear[Calendar.DATE] = 31
            lastDayOfCurrentYear[Calendar.MONTH] = 11
            lastDayOfCurrentYear[Calendar.HOUR_OF_DAY] = 23
            lastDayOfCurrentYear[Calendar.MINUTE] = 59
            lastDayOfCurrentYear[Calendar.SECOND] = 58
            val lastDate = lastDayOfCurrentYear.time

            val yearFormatInfo =
                FTYearFormatInfo(startDate, lastDate)
            val dairyGenerator = FTDiaryGeneratorV2(context, null, yearFormatInfo)
            dairyGenerator.generate(selectedItem,2.2f)
            return 0
        }

        override fun onPreExecute() {
            super.onPreExecute()
            dialog = ProgressDialog(context)
            dialog?.setMessage("Creating PDF File..")
            dialog?.setCancelable(false)
            dialog?.show()

        }

        override fun onPostExecute(result: Long) {
            super.onPostExecute(result)
            // ...
            dialog?.dismiss()
        }
    }
}