package com.example.demo.generator.models.info.rects

import android.graphics.Rect
import android.graphics.RectF

class FTMobileDayNightJournalRect {
    companion object {

        // Year text & Month Text is placed at same rect
        var yearTextRectInfo = RectF()
        var monthTextRectInfo = RectF()

        //Year page Rects for navigate to "Month with dates" pages
        var yearPageRectInfo : MutableList<RectF> = ArrayList()

        //Each month page contains all the dates so rects need to be create for each date
        var monthPageRectInfo : MutableList<ArrayList<RectF>> = ArrayList()

    }
}