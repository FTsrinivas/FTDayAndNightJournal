package com.example.demo;

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
}
