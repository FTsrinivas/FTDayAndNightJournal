package com.example.demo.generator.models.info.rects

import android.graphics.Rect
import android.graphics.RectF

class FTDairyYearPageRect {
    var dayRectInfo = RectF()
    var monthRectInfo = ArrayList<RectF>()
    companion object RectFInfo{

       val yearRectInfo : MutableList<ArrayList<RectF>> = ArrayList()
    }
}