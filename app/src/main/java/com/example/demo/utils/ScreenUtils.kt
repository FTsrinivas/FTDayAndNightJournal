package com.example.demo.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Paint
import android.util.Log
import android.util.TypedValue
import com.dd.plist.NSArray
import com.dd.plist.NSDictionary
import com.dd.plist.PropertyListParser
import com.example.demo.R
import com.example.demo.generator.models.QuoteItem
import java.lang.Exception
import kotlin.random.Random

object ScreenUtils {
     var quotesList = ArrayList<QuoteItem>()


    fun calculateFontSizeByBoundingRect(
        context: Context,
        text: String,
        textWidth: Int,
        textHeight: Int
    ): Int {
        if (textHeight <= 0 || textWidth <= 0) {
            return 12
        }
        val VERTICAL_FONT_SCALING_FACTOR = 0.9f
        val mTestPaint: Paint
        mTestPaint = Paint()
        val margin = context.resources.getDimensionPixelOffset(R.dimen._10sdp)
        val displayMetrics = context.resources.displayMetrics

        // Find target width
        val targetWidth = (textWidth - margin * 2).toFloat()
        var hi = 800f
        var lo = 10f
        var targetTextSizeHorizontal = hi
        val textPerLines = text.split("\n").toTypedArray()
        var targetTextSizeVertical = (textHeight - margin * 2) * VERTICAL_FONT_SCALING_FACTOR
        targetTextSizeVertical = targetTextSizeVertical / textPerLines.size
        for (i in textPerLines.indices) {
            val subText = textPerLines[i]
            while (hi > 10) {
                val size = hi
                mTestPaint.textSize = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_PX, size,
                    displayMetrics
                )
                if (mTestPaint.measureText(subText) <= targetWidth) {
                    lo = size // too small
                    break
                }
                hi = hi - 5
            }
            if (targetTextSizeHorizontal > lo) targetTextSizeHorizontal = lo
        }

        // Set the text size
        val targetTextSize = Math.min(targetTextSizeVertical, targetTextSizeHorizontal)
        return convertPxToDp(context, targetTextSize.toInt())
    }

     fun convertPxToDp(context: Context, px: Int): Int {
        return (px / context.resources.displayMetrics.density).toInt()
    }

    fun findYearMargins(text : String, size : Int):Float{
        var yearmargin = 5f
        for (i in 0..text.length+1){
            yearmargin  += size/2
        }
        return yearmargin
    }


    fun getQuotesList(context: Context) :ArrayList<QuoteItem>{
        val res: Resources = context.resources
        val istream = res.assets.open("quotes.plist")
        var rootDict = PropertyListParser.parse(istream)
        Log.d("##Size", "" + rootDict.toJavaObject())

        (rootDict as NSArray).array.forEachIndexed { index, nsObject ->
            val item = QuoteItem(
                (nsObject as NSDictionary).get("Quote").toString(),
                (nsObject as NSDictionary).get("Author").toString()
            )
            quotesList.add(item)
        }
        return quotesList
    }

     fun pickRandomQuote(context: Context, mQuotesList: ArrayList<QuoteItem>): QuoteItem {
         return try {
             var randomIndex: Int = Random.nextInt(mQuotesList.size)
             val randomElement = mQuotesList[randomIndex]
             mQuotesList.removeAt(randomIndex)
             println(randomElement)
             randomElement
         } catch (e: Exception) {
             quotesList = getQuotesList(context)
             QuoteItem(
                 "The place to be happy is here. The time to be happy is now.",
                 "-Robert G. Ingersoll"
             )
         }
     }

    fun findColumnCount(isLandScape : Boolean): Int {
        return if (!isLandScape/*orientation == Configuration.ORIENTATION_PORTRAIT*/) 3 else 4
    }

    fun findRowCount(isLandScape : Boolean): Int {
        return if (!isLandScape/*orientation == Configuration.ORIENTATION_PORTRAIT*/) 4 else 3
    }

    fun findMaxLines(isLandScape : Boolean): Int {
        return if (!isLandScape/*orientation == Configuration.ORIENTATION_PORTRAIT*/) 3 else 2

    }
}