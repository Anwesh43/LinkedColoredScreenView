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
val scf : Float = 0.5f

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

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += prevScale + dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
         }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(50)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }


        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class CSNode(var i : Int, val state : State = State()) {

        private var next : CSNode? = null
        private var prev : CSNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = CSNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, sc : Float, paint : Paint) {
            canvas.drawColoredScreen(i, state.scale, sc, paint)
            if (state.scale >= scf) {
                next?.draw(canvas, state.scale, paint)
            }
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(cb : () -> Unit, dir : Int) : CSNode {
            var curr : CSNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this 
        }
    }
}