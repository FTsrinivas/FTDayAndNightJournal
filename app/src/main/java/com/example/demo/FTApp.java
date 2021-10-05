package com.example.demo;

import static androidx.core.util.Preconditions.checkArgument;

import android.app.Application;


public class FTApp extends Application {
    private static FTApp mInstance;

    public static FTApp getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static String getDayOfMonthSuffix(final int n) {
        try {
            if (n >= 11 && n <= 13) {
                return n + "th";
            }
            switch (n % 10) {
                case 1:
                    return n + "st";
                case 2:
                    return n + "nd";
                case 3:
                    return n + "rd";
                default:
                    return n + "th";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return n + "";
        }
    }
}
