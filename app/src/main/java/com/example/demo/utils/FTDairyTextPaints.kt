package com.example.demo.utils

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.Typeface.*
import androidx.core.content.res.ResourcesCompat
import com.example.demo.FTApp
import com.example.demo.R

class FTDairyTextPaints {
    companion object {
        var introQuote_Paint = Paint().apply {
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
            color = FTApp.getInstance().resources.getColor(R.color.text_color, FTApp.getInstance().theme)
            typeface = ResourcesCompat.getFont(FTApp.getInstance(),R.font.lora_italic)
        }
         var introText_Paint = Paint().apply {
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
            color = FTApp.getInstance().resources.getColor(R.color.text_color, FTApp.getInstance().theme)
            typeface = ResourcesCompat.getFont(FTApp.getInstance(),R.font.lora_regular)
        }
         var introAuthorText_Paint = Paint().apply {
            style = Paint.Style.FILL
            textAlign = Paint.Align.CENTER
            color = FTApp.getInstance().resources.getColor(R.color.text_color, FTApp.getInstance().theme)
            typeface = ResourcesCompat.getFont(FTApp.getInstance(),R.font.montserrat_regular)
        }
        var background_Paint = Paint().apply {
            style = Paint.Style.FILL
            color = FTApp.getInstance().resources.getColor(R.color.page_background, FTApp.getInstance().theme)
        }
        var coloredBoxPaint = Paint().apply {
            style = Paint.Style.FILL
            color = FTApp.getInstance().resources.getColor(R.color.box_background, FTApp.getInstance().theme)
        }
        var coloredDayRectBoxPaint = Paint().apply {
            style = Paint.Style.FILL
            color = FTApp.getInstance().resources.getColor(R.color.red, FTApp.getInstance().theme)
        }
        var calendar_Year_Paint = Paint().apply {
            isAntiAlias = true
            color = Color.GRAY
            typeface = ResourcesCompat.getFont(FTApp.getInstance(),R.font.lora_regular)
        }
        var calendar_Month_Paint = Paint().apply {
            isAntiAlias = true
            color = Color.GRAY
            typeface = ResourcesCompat.getFont(FTApp.getInstance(),R.font.montserrat_bold)
        }
        var calendar_WeekDays_Paint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            color = Color.GRAY
            typeface = ResourcesCompat.getFont(FTApp.getInstance(),R.font.montserrat_bold)
            typeface.isBold
        }
        var calendar_Days_Paint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            color = FTApp.getInstance().resources.getColor(R.color.day_text_color, FTApp.getInstance().theme)
            typeface = ResourcesCompat.getFont(FTApp.getInstance(),R.font.montserrat_bold)
        }
        var dairyText_Paint = Paint().apply {
            isAntiAlias = true
            color = FTApp.getInstance().resources.getColor(R.color.text_color, FTApp.getInstance().theme)
            typeface = ResourcesCompat.getFont(FTApp.getInstance(),R.font.lora_regular)
        }
    }
}