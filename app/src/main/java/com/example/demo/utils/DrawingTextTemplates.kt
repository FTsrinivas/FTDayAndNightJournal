package com.example.demo.utils

import android.content.Context
import android.graphics.*

object DrawingTextTemplates {

    /**
     *  Exactly center in the whole screen
     * */
   /* fun drawCenterText(xPosition: Float, yPosition: Float, canvas: Canvas, text: String) {
        val rect = Rect()
        *//**
         * I have had some problems with Canvas.getHeight() if API < 16. That's why I use Canvas.getClipBounds(Rect) instead. (Do not use Canvas.getClipBounds().getHeight() as it allocates memory for a Rect.)
         * *//*
        canvas.getClipBounds(rect)
        val cHeight = rect.height()
        val cWidth = rect.width()
        var paint = Paint()
        paint.textAlign = Paint.Align.LEFT
        paint.getTextBounds(text, 0, text.length, rect);
        val x: Float = cWidth / 2f - rect.width() / 2f - rect.left
        val y: Float = cHeight / 2f + rect.height() / 2f - rect.bottom
        canvas.drawText(text, x, y, paint)
    }
*/
    fun drawCenterText(xPosition: Float, yPosition: Float, canvas: Canvas, text: String) {
        val rect = Rect()
        /**
         * I have had some problems with Canvas.getHeight() if API < 16. That's why I use Canvas.getClipBounds(Rect) instead. (Do not use Canvas.getClipBounds().getHeight() as it allocates memory for a Rect.)
         * */
        canvas.getClipBounds(rect)
        var paint = Paint()
        paint.textAlign = Paint.Align.CENTER
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.FILL);
        paint.getTextBounds(text, 0, text.length, rect);

        canvas.drawText(text, xPosition, yPosition, paint)
    }

    fun drawCenterText(canvas: Canvas, text: String) {
        val rect = Rect()
        /**
         * I have had some problems with Canvas.getHeight() if API < 16. That's why I use Canvas.getClipBounds(Rect) instead. (Do not use Canvas.getClipBounds().getHeight() as it allocates memory for a Rect.)
         * */
        canvas.getClipBounds(rect)
        val cHeight = rect.height()
        val cWidth = rect.width()
        var paint = Paint()
        paint.textAlign = Paint.Align.LEFT
        paint.getTextBounds(text, 0, text.length, rect);
        val x: Float = cWidth / 2f - rect.width() / 2f - rect.left
        val y: Float = cHeight / 2f + rect.height() / 2f - rect.bottom
        canvas.drawText(text, x, y, paint)
    }

    fun isTablet(context: Context): Boolean {
        return context.resources.configuration.smallestScreenWidthDp >= 600
    }

    fun isMobile(context: Context): Boolean {
        return context.resources.configuration.smallestScreenWidthDp <= 600
    }
}