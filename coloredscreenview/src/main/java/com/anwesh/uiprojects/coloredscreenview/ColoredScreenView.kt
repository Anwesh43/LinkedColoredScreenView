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
val rFactor : Float = 6f

fun Canvas.drawSweepArc(x : Float, y : Float, sc : Float, r : Float, paint : Paint) {
    paint.color = Color.WHITE
    save()
    translate(x, y)
    drawArc(RectF(-r, -r, r, r), 0f, 360f * (1 - sc), true, paint)
    restore()
}

fun Canvas.drawColoredScreen(i : Int, scale : Float, sc : Float, paint : Paint) : Float {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    var x : Float = 0f
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    val r : Float = Math.min(w, h) / rFactor
    if (sc != 0f) {
        x = w * (1 - sc)
    }
    save()
    translate(-w * sc2 + x, 0f)
    drawSweepArc(w / 2, h / 2, sc1, r, paint)
    paint.color = Color.parseColor(colors[i])
    drawRect(RectF(0f, 0f, w, h), paint)
    restore()
    return sc2
}

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n

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
            val sck : Float = canvas.drawColoredScreen(i, state.scale, sc, paint)
            if (sck > 0f) {
                next?.draw(canvas, sck, paint)
            }
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : CSNode {
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

    data class ColoredScreen(var i : Int) {

        private val root : CSNode = CSNode(0)
        private var curr : CSNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, 0f, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : ColoredScreenView) {

        private val cs : ColoredScreen = ColoredScreen(0)
        private val animator : Animator = Animator(view)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            cs.draw(canvas, paint)
            animator.animate {
                cs.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            cs.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : ColoredScreenView {
            val view : ColoredScreenView = ColoredScreenView(activity)
            activity.setContentView(view)
            return view 
        }
    }
}