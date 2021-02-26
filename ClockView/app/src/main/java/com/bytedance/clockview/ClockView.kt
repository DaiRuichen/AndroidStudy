package com.bytedance.clockview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.os.Handler
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import java.util.*
import kotlin.math.abs


class ClockView : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    companion object {
        private const val DEFAULT_MIN_WIDTH = 200
        private const val DEFAULT_BORDER_WIDTH = 6f
        private const val DEFAULT_POINT_BACK_LENGTH = 40f
        private const val DEFAULT_LONG_DEGREE_LENGTH = 40f
        private const val DEFAULT_SHORT_DEGREE_LENGTH = 30f
    }


    private var secondPointerLength = 0f
    private var minutePointerLength = 0f
    private var hourPointerLength = 0f
    private var isReset = false

    private fun getRadius(): Float {
        return (height / 2).coerceAtMost(width / 2) - DEFAULT_BORDER_WIDTH
    }

    private fun reset() {
        val r = getRadius()
        secondPointerLength = r * 0.9f
        minutePointerLength = r * 0.7f
        hourPointerLength = r * 0.6f
    }

    private fun drawClock(canvas: Canvas) {
        //画圆
        val r = getRadius()
        val paintCircle = Paint()
        paintCircle.style = Paint.Style.STROKE
        paintCircle.isAntiAlias = true
        paintCircle.strokeWidth = DEFAULT_BORDER_WIDTH
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), r, paintCircle)

        //画刻度
        var degreeLength = 0f
        val paintDegree = Paint()
        paintDegree.isAntiAlias = true
        for (i in 0..59) {
            if (i % 5 == 0) {
                paintDegree.strokeWidth = 6f
                degreeLength = DEFAULT_LONG_DEGREE_LENGTH
            } else {
                paintDegree.strokeWidth = 3f
                degreeLength = DEFAULT_SHORT_DEGREE_LENGTH
            }
            canvas.drawLine(
                (width / 2).toFloat(),
                abs(height / 2 - r),
                (width / 2).toFloat(),
                abs(height / 2 - r) + degreeLength,
                paintDegree
            )
            canvas.rotate(
                (360 / 60).toFloat(),
                (width / 2).toFloat(),
                (height / 2).toFloat()
            )
        }

        //画指针
        val now = Calendar.getInstance()
        val second = if(isReset)  0f else now.get(Calendar.SECOND).toFloat()
        val minute = if(isReset) 0f else now.get(Calendar.MINUTE).toFloat()
        val hour = if(isReset) 0f else now.get(Calendar.HOUR_OF_DAY) + minute / 60
        isReset = false

        canvas.translate((width / 2).toFloat(), (height / 2).toFloat())

        val paintSecond = Paint()
        paintSecond.isAntiAlias = true
        paintSecond.strokeWidth = 5f
        canvas.save()
        canvas.rotate(second / 60 * 360)
        canvas.drawLine(0f, DEFAULT_POINT_BACK_LENGTH, 0f, -secondPointerLength, paintSecond)
        canvas.restore()

        val paintMinute = Paint()
        paintMinute.isAntiAlias = true
        paintMinute.strokeWidth = 10f
        canvas.save()
        canvas.rotate(minute / 60 * 360)
        canvas.drawLine(0f, DEFAULT_POINT_BACK_LENGTH, 0f, -minutePointerLength, paintMinute)
        canvas.restore()

        val paintHour = Paint()
        paintHour.isAntiAlias = true
        paintHour.strokeWidth = 15f
        canvas.save()
        canvas.rotate(hour / 12 * 360)
        canvas.drawLine(0f, DEFAULT_POINT_BACK_LENGTH, 0f, -hourPointerLength, paintHour)
        canvas.restore()

        //画圆心
        val paintCenter = Paint()
        paintCenter.color = Color.WHITE
        canvas.drawCircle(0f, 0f, 2f, paintCenter)
    }


    override fun onDraw(canvas: Canvas) {
        reset()
        drawClock(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(measure(widthMeasureSpec), measure(heightMeasureSpec))
    }

    private fun measure(origin: Int): Int {
        var result = DEFAULT_MIN_WIDTH
        val specMode = MeasureSpec.getMode(origin)
        val specSize = MeasureSpec.getSize(origin)
        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize)
            }
        }
        return result
    }

    private val mTicker: Runnable = object : Runnable {
        override fun run() {
            invalidate()
            val now = SystemClock.elapsedRealtime()
            val next = now + (1000 - now % 1000)
            if (handler != null) {
                handler.postAtTime(this, next)
            }
        }
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        if (isVisible) {
            mTicker.run()
        } else {
            handler.removeCallbacks(mTicker)
        }
    }

    fun resetClock() {
        isReset = true
        invalidate()
    }
}