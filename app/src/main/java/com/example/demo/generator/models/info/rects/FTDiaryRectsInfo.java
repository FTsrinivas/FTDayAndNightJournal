package com.example.demo.generator.models.info.rects;

import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

public class FTDiaryRectsInfo {
    public static class FTDiaryYearRectsInfo {
        public RectF yearRect = new RectF();
        public List<RectF> monthRects = new ArrayList<>();
    }

    public static class FTDiaryMonthRectsInfo {
        public RectF monthRect = new RectF();
        public RectF yearRect = new RectF();
        public List<RectF> dayRects = new ArrayList<>();
    }

    public static class FTDiaryWeekRectsInfo {
        public RectF monthRect = new RectF();
        public RectF yearRect = new RectF();
        public List<RectF> weekDayRects = new ArrayList<>();
    }

    public static class FTDiaryDayRectsInfo {
        public RectF monthRect = new RectF();
        public RectF weekRect = new RectF();
        public RectF yearRect = new RectF();
    }

}
