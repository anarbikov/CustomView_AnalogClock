package com.example.timer.ui.home

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin


open class Clock(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var mHeight = 0
    private var mWidth = 0

    private val mClockHours = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)

    private var mPadding = 0
    private var mNumeralSpacing = 0

    private var mHandTruncation = 0
    private var mHourHandTruncation = 0

    private var mRadius = 0
    private var mPaint = Paint()
    private val mRect = Rect()
    private var isInit = false

    private var calendar: Calendar = Calendar.getInstance()
    private var hour = calendar.get(Calendar.HOUR_OF_DAY).toFloat()
    private var minute = calendar.get(Calendar.MINUTE).toFloat()
    private var second = calendar.get(Calendar.SECOND).toFloat()
    var isStarted = true
    var isTimer = false
    private var time = listOf<Float>()
    var counter = 0

    override fun onDraw(canvas: Canvas) {
        if (!isInit) {
            mPaint = Paint()
            mHeight = height
            mWidth = width
            mPadding = mNumeralSpacing + 50 // spacing from the circle border
            val minAttr = mHeight.coerceAtMost(mWidth)
            mRadius = minAttr / 2 - mPadding
            mHandTruncation = minAttr / 20
            mHourHandTruncation = minAttr / 17
            isInit = true
        }
        canvas.drawColor(Color.DKGRAY)
        mPaint.reset()
        mPaint.color = Color.WHITE
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = 4F
        mPaint.isAntiAlias = true
        canvas.drawCircle(
            (mWidth / 2).toFloat(),
            (mHeight / 2).toFloat(), (mRadius + mPadding - 10).toFloat(), mPaint
        )
        mPaint.style = Paint.Style.FILL
        canvas.drawCircle(
            (mWidth / 2).toFloat(),
            (mHeight / 2).toFloat(),
            12F,
            mPaint
        )  // the 03 clock hands will be rotated from this center point.
        val fontSize =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, resources.displayMetrics)
                .toInt()
        mPaint.textSize = fontSize.toFloat() // set font size (optional)
        for (hour in mClockHours) {
            val tmp = hour.toString()
            mPaint.getTextBounds(tmp, 0, tmp.length, mRect) // for circle-wise bounding

            // find the circle-wise (x, y) position as mathematical rule
            val angle = Math.PI / 6 * (hour - 3)
            val x = (mWidth / 2 + cos(angle) * mRadius - mRect.width() / 2).toInt()
            val y = (mHeight / 2 + sin(angle) * mRadius + mRect.height() / 2).toInt()
            canvas.drawText(
                hour.toString(),
                x.toFloat(),
                y.toFloat(),
                mPaint
            ) // you can draw dots to denote hours as alternative
        }
        fun drawHandLine(canvas: Canvas, moment: Float, isHour: Boolean, isSecond: Boolean) {
            val angle = Math.PI * moment / 30 - Math.PI / 2
            val handRadius =
                if (isHour) mRadius - mHandTruncation - mHourHandTruncation else mRadius - mHandTruncation
            if (isSecond) mPaint.color = Color.YELLOW
            canvas.drawLine(
                (mWidth / 2).toFloat(),
                (mHeight / 2).toFloat(),
                (mWidth / 2 + cos(angle) * handRadius).toFloat(),
                (mHeight / 2 + sin(angle) * handRadius).toFloat(),
                mPaint
            )
        }

        time = if (isStarted) currentTime() else time

        if (!isTimer) {
            hour = time[0]
            minute = time[1]
            second = time[2]
        }else {
            hour = (counter.toDouble() / 3600).toInt().toFloat()
            minute = ((counter - hour * 3600).toDouble() / 60).toInt().toFloat()
            second = ((counter - hour * 3600) - minute * 60).toInt().toFloat()
        }
            hour = if (hour > 12) hour - 12 else hour
            drawHandLine(
                canvas, (hour + minute / 60) * 5f,
                isHour = true,
                isSecond = false
            )
            drawHandLine(canvas, minute, isHour = false, isSecond = false)
            drawHandLine(canvas, second, isHour = false, isSecond = true)
            postInvalidateDelayed(500)
            invalidate()
    }

    private fun currentTime(): List<Float> {
        return listOf(
                Calendar.getInstance().get(Calendar.HOUR_OF_DAY).toFloat(),
                Calendar.getInstance().get(Calendar.MINUTE).toFloat(),
                Calendar.getInstance().get(Calendar.SECOND).toFloat()
            )
    }
}

