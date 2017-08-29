package com.expedia.bookings.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import kotlin.properties.Delegates

class MessageProgressView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    val linesPaint: Paint = Paint()
    //In case the view isn't a perfect rectangle, maintain both x and y values
    var verticalPaddingGap: Int by Delegates.notNull()
    var horizontalPaddingGap: Int by Delegates.notNull()
    var lineHeight: Int by Delegates.notNull()
    var lineGap: Int by Delegates.notNull()
    var primaryLineMaxWidth: Int by Delegates.notNull()
    var shortLineMaxWidth: Int by Delegates.notNull()

    val longLinePercentLimit: Float = .375f
    val shortLinePercentLimit: Float = .25f

    val line1Rect: Rect = Rect()
    val line2Rect: Rect = Rect()
    val line3Rect: Rect = Rect()

    var drawLine1: Boolean = false
    var drawLine2: Boolean = false
    var drawLine3: Boolean = false

    var progress: Float = 0f
        set(value) {
            field = value
            var remainingProgress = value

            drawLine1 = false
            drawLine2 = false
            drawLine3 = false

            if (remainingProgress <= 0) {
                invalidate()
                return
            }
            var currentLineWidth: Float = if (remainingProgress > longLinePercentLimit) 1f else remainingProgress / longLinePercentLimit
            line1Rect.set(horizontalPaddingGap, verticalPaddingGap,
                    horizontalPaddingGap + (currentLineWidth * primaryLineMaxWidth).toInt(),
                    verticalPaddingGap + lineHeight)
            remainingProgress -= longLinePercentLimit
            drawLine1 = true

            if (remainingProgress <= 0) {
                invalidate()
                return
            }
            currentLineWidth = if (remainingProgress > longLinePercentLimit) 1f else remainingProgress / longLinePercentLimit
            line2Rect.set(horizontalPaddingGap,
                    verticalPaddingGap + lineHeight + lineGap,
                    horizontalPaddingGap + (currentLineWidth * primaryLineMaxWidth).toInt(),
                    verticalPaddingGap + lineHeight * 2 + lineGap)
            remainingProgress -= longLinePercentLimit
            drawLine2 = true

            if (remainingProgress <= 0) {
                invalidate()
                return
            }
            currentLineWidth = if (remainingProgress > shortLinePercentLimit) 1f else remainingProgress / shortLinePercentLimit
            line3Rect.set(horizontalPaddingGap,
                    verticalPaddingGap + lineHeight * 2 + lineGap * 2,
                    horizontalPaddingGap + (currentLineWidth * shortLineMaxWidth).toInt(),
                    verticalPaddingGap + lineHeight * 3 + lineGap * 2)

            drawLine3 = true
            invalidate()
        }

    init {
        setBackgroundResource(R.drawable.bg_message_loading)

        val ta = context.obtainStyledAttributes(attrs, R.styleable.MessageProgressView, 0, 0)
        var bgTint: Int = -1
        try {
            linesPaint.color = ta.getColor(R.styleable.MessageProgressView_line_color, Color.rgb(250, 250, 250))
            bgTint = ta.getColor(R.styleable.MessageProgressView_background_tint, bgTint)
        } finally {
            ta.recycle()
        }
        if (Build.VERSION.SDK_INT < 21 && bgTint != -1) {
            val bg = background
            bg.mutate()
            bg.setColorFilter(bgTint, PorterDuff.Mode.SRC_IN)
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (drawLine1) {
            canvas.drawRect(line1Rect, linesPaint)
        }

        if (drawLine2) {
            canvas.drawRect(line2Rect, linesPaint)
        }

        if (drawLine3) {
            canvas.drawRect(line3Rect, linesPaint)
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        verticalPaddingGap = height / 5
        horizontalPaddingGap = width / 5
        lineHeight = height / 10
        lineGap = height / 20
        primaryLineMaxWidth = width * 3 / 5
        shortLineMaxWidth = width * 2 / 5
    }

}
