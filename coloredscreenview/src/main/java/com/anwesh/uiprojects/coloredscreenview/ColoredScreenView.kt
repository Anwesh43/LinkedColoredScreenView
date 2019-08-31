package com.anwesh.uiprojects.coloredscreenview

/**
 * Created by anweshmishra on 31/08/19.
 */

import android.app.Activity
import android.content.Context
import android.graphics.RectF
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Canvas
import android.view.View
import android.view.MotionEvent

val scGap : Float = 0.05f
val delay : Long = 20
val colors : Array<String> = arrayOf("#4CAF50", "#01579B", "#1A237E", "#E65100", "#f44336")
val backColor : Int = Color.parseColor("#BDBDBD")

fun Canvas.drawColoredScreen(i : Int, scale : Float, sc : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    var x : Float = 0f
    if (sc != 0f) {
        x = w * (1 - sc)
    }
    save()
    translate(-w * scale + x, 0f)
    paint.color = Color.parseColor(colors[i])
    drawRect(RectF(0f, 0f, w, h), paint)
    restore()
}

class ColoredScreenView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }
}