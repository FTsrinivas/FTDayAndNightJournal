package com.example.demo.generator.models.screenInfo;

import android.content.Context;

import com.example.demo.generator.models.spacesInfo.DiaryPlistData;

public class FTScreenInfo {
    public FTScreenSpacesInfo spacesInfo;
    public FTScreenFontsInfo fontsInfo;

    public FTScreenInfo(Context context, String templateId, boolean isLandscape, boolean isTablet) {
        spacesInfo = new FTScreenSpacesInfo();
        fontsInfo = new FTScreenFontsInfo();

        if (templateId.equals("Modern")) {
            try {
                /*InputStream inputStream = context.getAssets().open("diaries/" + templateId + FTConstants.PLIST_EXTENSION);
                HashMap<String, Object> data = (HashMap<String, Object>) PropertyListParser.parse(inputStream).toJavaObject();
                Gson gson = new Gson();
                String json = gson.toJson(data);*/
                DiaryPlistData plistData = null;//gson.fromJson(json, DiaryPlistData.class);

                float density = context.getResources().getDisplayMetrics().density;
                //------------Spaces----------
                //Year spaces
                spacesInfo.yearSpacesInfo.baseBoxX = (isTablet ? (isLandscape ? plistData.android.land.year.baseBoxX : plistData.android.port.year.baseBoxX) : plistData.android.mobile.year.baseBoxX) * density;
                spacesInfo.yearSpacesInfo.baseBoxY = (isTablet ? (isLandscape ? plistData.android.land.year.baseBoxY : plistData.android.port.year.baseBoxY) : plistData.android.mobile.year.baseBoxY) * density;
                spacesInfo.yearSpacesInfo.cellOffsetX = (isTablet ? (isLandscape ? plistData.android.land.year.cellOffsetX : plistData.android.port.year.cellOffsetX) : plistData.android.mobile.year.cellOffsetX) * density;
                spacesInfo.yearSpacesInfo.cellOffsetY = (isTablet ? (isLandscape ? plistData.android.land.year.cellOffsetY : plistData.android.port.year.cellOffsetY) : plistData.android.mobile.year.cellOffsetY) * density;
                spacesInfo.yearSpacesInfo.boxBottomOffset = (isTablet ? (isLandscape ? plistData.android.land.year.boxBottomOffset : plistData.android.port.year.boxBottomOffset) : plistData.android.mobile.year.boxBottomOffset) * density;
                //Month spaces
                spacesInfo.monthSpacesInfo.baseBoxX = (isTablet ? (isLandscape ? plistData.android.land.month.baseBoxX : plistData.android.port.month.baseBoxX) : plistData.android.mobile.month.baseBoxX) * density;
                spacesInfo.monthSpacesInfo.baseBoxY = (isTablet ? (isLandscape ? plistData.android.land.month.baseBoxY : plistData.android.port.month.baseBoxY) : plistData.android.mobile.month.baseBoxY) * density;
                spacesInfo.monthSpacesInfo.boxBottomOffset = (isTablet ? (isLandscape ? plistData.android.land.month.boxBottomOffset : plistData.android.port.month.boxBottomOffset) : plistData.android.port.month.boxBottomOffset) * density;
                spacesInfo.monthSpacesInfo.boxRightOffset = (isTablet ? (isLandscape ? plistData.android.land.month.boxRightOffset : plistData.android.port.month.boxRightOffset) : plistData.android.port.month.boxRightOffset) * density;
                //Week spaces
                spacesInfo.weekSpacesInfo.baseBoxX = (isTablet ? (isLandscape ? plistData.android.land.week.baseBoxX : plistData.android.port.week.baseBoxX) : plistData.android.mobile.week.baseBoxX) * density;
                spacesInfo.weekSpacesInfo.baseBoxY = (isTablet ? (isLandscape ? plistData.android.land.week.baseBoxY : plistData.android.port.week.baseBoxY) : plistData.android.mobile.week.baseBoxY) * density;
                spacesInfo.weekSpacesInfo.cellOffsetX = (isTablet ? (isLandscape ? plistData.android.land.week.cellOffsetX : plistData.android.port.week.cellOffsetX) : plistData.android.mobile.week.cellOffsetX) * density;
                spacesInfo.weekSpacesInfo.cellOffsetY = (isTablet ? (isLandscape ? plistData.android.land.week.cellOffsetY : plistData.android.port.week.cellOffsetY) : plistData.android.mobile.week.cellOffsetY) * density;
                spacesInfo.weekSpacesInfo.cellHeight = (isTablet ? (isLandscape ? plistData.android.land.week.cellHeight : plistData.android.port.week.cellHeight) : plistData.android.mobile.week.cellHeight) * density;
                spacesInfo.weekSpacesInfo.titleLineY = (isTablet ? (isLandscape ? plistData.android.land.week.titleLineY : plistData.android.port.week.titleLineY) : plistData.android.mobile.week.titleLineY) * density;
                spacesInfo.weekSpacesInfo.lastCellHeight = (isTablet ? (isLandscape ? plistData.android.land.week.lastCellHeight : plistData.android.port.week.lastCellHeight) : plistData.android.mobile.week.lastCellHeight) * density;
                //Day spaces
                spacesInfo.daySpacesInfo.baseX = (isTablet ? (isLandscape ? plistData.android.land.day.baseX : plistData.android.port.day.baseX) : plistData.android.mobile.day.baseX) * density;
                spacesInfo.daySpacesInfo.baseY = (isTablet ? (isLandscape ? plistData.android.land.day.baseY : plistData.android.port.day.baseY) : plistData.android.mobile.day.baseY) * density;

                //------------Fonts----------
                //Year fonts
                fontsInfo.yearFontsInfo.yearFontSize = isTablet ? plistData.screenFontDetails.ipad.year.yearFontSize * density : plistData.screenFontDetails.iphone.year.yearFontSize * density;
                fontsInfo.yearFontsInfo.titleMonthFontSize = isTablet ? plistData.screenFontDetails.ipad.year.titleMonthFontSize * density : plistData.screenFontDetails.iphone.year.titleMonthFontSize * density;
                fontsInfo.yearFontsInfo.outMonthFontSize = isTablet ? plistData.screenFontDetails.ipad.year.outMonthFontSize * density : plistData.screenFontDetails.iphone.year.outMonthFontSize * density;
                //Month fonts
                fontsInfo.monthFontsInfo.monthFontSize = isTablet ? plistData.screenFontDetails.ipad.month.monthFontSize * density : plistData.screenFontDetails.iphone.month.monthFontSize * density;
                fontsInfo.monthFontsInfo.weekFontSize = isTablet ? plistData.screenFontDetails.ipad.month.weekFontSize * density : plistData.screenFontDetails.iphone.month.weekFontSize * density;
                fontsInfo.monthFontsInfo.dayFontSize = isTablet ? plistData.screenFontDetails.ipad.month.dayFontSize * density : plistData.screenFontDetails.iphone.month.dayFontSize * density;
                fontsInfo.monthFontsInfo.yearFontSize = isTablet ? plistData.screenFontDetails.ipad.month.yearFontSize * density : plistData.screenFontDetails.iphone.month.yearFontSize * density;
                //Week fonts
                fontsInfo.weekFontsInfo.monthFontSize = isTablet ? plistData.screenFontDetails.ipad.week.monthFontSize * density : plistData.screenFontDetails.iphone.week.monthFontSize * density;
                fontsInfo.weekFontsInfo.weekFontSize = isTablet ? plistData.screenFontDetails.ipad.week.weekFontSize * density : plistData.screenFontDetails.iphone.week.weekFontSize * density;
                fontsInfo.weekFontsInfo.dayFontSize = isTablet ? plistData.screenFontDetails.ipad.week.dayFontSize * density : plistData.screenFontDetails.iphone.week.dayFontSize * density;
                fontsInfo.weekFontsInfo.yearFontSize = isTablet ? plistData.screenFontDetails.ipad.week.yearFontSize * density : plistData.screenFontDetails.iphone.week.yearFontSize * density;
                //Day fonts
                fontsInfo.dayFontsInfo.monthFontSize = isTablet ? plistData.screenFontDetails.ipad.day.monthFontSize * density : plistData.screenFontDetails.iphone.day.monthFontSize * density;
                fontsInfo.dayFontsInfo.weekFontSize = isTablet ? plistData.screenFontDetails.ipad.day.weekFontSize * density : plistData.screenFontDetails.iphone.day.weekFontSize * density;
                fontsInfo.dayFontsInfo.dayFontSize = isTablet ? plistData.screenFontDetails.ipad.day.dayFontSize * density : plistData.screenFontDetails.iphone.day.dayFontSize * density;
                fontsInfo.dayFontsInfo.yearFontSize = isTablet ? plistData.screenFontDetails.ipad.day.yearFontSize * density : plistData.screenFontDetails.iphone.day.yearFontSize * density;
            } catch (Exception e) {
            }
        }
    }
}
