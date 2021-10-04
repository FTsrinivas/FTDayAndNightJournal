package com.example.demo.generator.models.screenInfo;

public class FTScreenFontsInfo {
    public FTScreenYearFontsInfo yearFontsInfo = new FTScreenYearFontsInfo();
    public FTScreenMonthFontsInfo monthFontsInfo = new FTScreenMonthFontsInfo();
    public FTScreenWeekFontsInfo weekFontsInfo = new FTScreenWeekFontsInfo();
    public FTScreenDayFontsInfo dayFontsInfo = new FTScreenDayFontsInfo();

    public static class FTScreenYearFontsInfo {
        public float yearFontSize;
        public float titleMonthFontSize;
        public float outMonthFontSize;
    }

    public static class FTScreenMonthFontsInfo {
        public float monthFontSize;
        public float yearFontSize;
        public float weekFontSize;
        public float dayFontSize;
    }

    public static class FTScreenWeekFontsInfo {
        public float monthFontSize;
        public float yearFontSize;
        public float weekFontSize;
        public float dayFontSize;
    }

    public static class FTScreenDayFontsInfo {
        public float dayFontSize;
        public float monthFontSize;
        public float weekFontSize;
        public float yearFontSize;
    }
}