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

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    lateinit var binding: ActivityMainBinding

    lateinit var spinnerList: ArrayList<ScreenResolutions>
    lateinit var selectedItem: ScreenResolutions

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        PDFBoxResourceLoader.init(getApplicationContext())

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

            SaveFileInExternalStorage(this, selectedItem,binding.checkBoxOrientation.isChecked).execute()

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
        when (parent?.id) {
            R.id.spinnerScreenSize -> {
                Toast.makeText(
                    applicationContext,
                    spinnerList[position].name,
                    Toast.LENGTH_LONG
                )
                    .show()
                selectedItem = spinnerList[position]
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
//        TODO("Not yet implemented")
    }

    class SaveFileInExternalStorage(
        context: Context,
        selectedItem: ScreenResolutions,
        isLandScape: Boolean
    ) :
        AsyncTask<Void, Int, Long>() {
        var dialog: ProgressDialog? = null
        var context: Context? = null
        var selectedItem: ScreenResolutions? = null
        var isLandscape: Boolean = false

        init {
            this.context = context
            this.selectedItem = selectedItem
            this.isLandscape = isLandScape
        }

        override fun doInBackground(vararg params: Void?): Long {

            var folder = File(Environment.getExternalStorageState())
            val firstDayOfCurrentYear = Calendar.getInstance()
            firstDayOfCurrentYear[Calendar.DATE] = 1
            firstDayOfCurrentYear[Calendar.MONTH] = 5
            firstDayOfCurrentYear[Calendar.HOUR_OF_DAY] = 1
            firstDayOfCurrentYear[Calendar.MINUTE] = 1
            firstDayOfCurrentYear[Calendar.SECOND] = 1
            val startDate = firstDayOfCurrentYear.time

            val lastDayOfCurrentYear = Calendar.getInstance()
            lastDayOfCurrentYear[Calendar.YEAR] = 2022
            lastDayOfCurrentYear[Calendar.DATE] = 31
            lastDayOfCurrentYear[Calendar.MONTH] = 4
            lastDayOfCurrentYear[Calendar.HOUR_OF_DAY] = 23
            lastDayOfCurrentYear[Calendar.MINUTE] = 59
            lastDayOfCurrentYear[Calendar.SECOND] = 58
            val lastDate = lastDayOfCurrentYear.time

            val yearFormatInfo =
                FTYearFormatInfo(startDate, lastDate)
            val dairyGenerator = FTDiaryGeneratorV2(context, null, yearFormatInfo)
            dairyGenerator.generate()
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