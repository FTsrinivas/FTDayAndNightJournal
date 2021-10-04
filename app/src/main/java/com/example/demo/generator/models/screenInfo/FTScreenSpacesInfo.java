package com.example.demo.generator.models.screenInfo;

public class FTScreenSpacesInfo {

    public final FTScreenYearSpacesInfo yearSpacesInfo = new FTScreenYearSpacesInfo();
    public final FTScreenMonthSpacesInfo monthSpacesInfo = new FTScreenMonthSpacesInfo();
    public final FTScreenWeekSpacesInfo weekSpacesInfo = new FTScreenWeekSpacesInfo();
    public final FTScreenDaySpacesInfo daySpacesInfo = new FTScreenDaySpacesInfo();

    public static class FTScreenYearSpacesInfo {
        public float baseBoxX;
        public float baseBoxY;
        public float cellOffsetX;
        public float cellOffsetY;
        public float boxBottomOffset;
    }

    public static class FTScreenMonthSpacesInfo {
        public float baseBoxX;
        public float baseBoxY;
        public float boxBottomOffset;
        public float boxRightOffset;
    }

    public static class FTScreenWeekSpacesInfo {
        public float baseBoxX;
        public float baseBoxY;
        public float titleLineY;
        public float cellOffsetX;
        public float cellOffsetY;
        public float cellHeight;
        public float lastCellHeight;
    }

    public static class FTScreenDaySpacesInfo {
        public float baseX;
        public float baseY;
    }
}
