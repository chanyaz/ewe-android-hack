package com.expedia.bookings.rail.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R

class TicketStubTearView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    var leftArcBounds = RectF()
    var rightArcBounds = RectF()

    val topLeftPoint = PointF(0f, 0f)
    var bottomRightPoint = PointF()

    var tearLineBounds = RectF()

    val fillPaint: Paint
    val tearLinePaint: Paint

    init {
        fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        fillPaint.color = ContextCompat.getColor(context, R.color.white)
        fillPaint.style = Paint.Style.FILL_AND_STROKE

        tearLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        tearLinePaint.color = ContextCompat.getColor(context, R.color.rail_divider_bar_color)
        tearLinePaint.style = Paint.Style.STROKE
    }

    override fun onSizeChanged(newW: Int, newH: Int, odlW: Int, oldH: Int) {
        super.onSizeChanged(newW, newH, odlW, oldH)
        val widthF = newW.toFloat()
        val heightF = newH.toFloat()

        val radius = heightF / 2

        leftArcBounds.set(-radius, 0f, radius, heightF)
        rightArcBounds.set(widthF - radius, 0f, widthF + radius, heightF)

        bottomRightPoint.set(widthF, heightF)

        tearLineBounds.set(0f + radius, heightF / 2, widthF - radius, heightF / 2)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val arcedPath = Path()

        arcedPath.moveTo(topLeftPoint.x, topLeftPoint.y)
        arcedPath.arcTo(leftArcBounds, 270f, 180f, true)
        arcedPath.lineTo(bottomRightPoint.x, bottomRightPoint.y)
        arcedPath.arcTo(rightArcBounds, 90f, 180f, true)
        arcedPath.lineTo(topLeftPoint.x, topLeftPoint.y)

        canvas.drawPath(arcedPath, fillPaint)
        canvas.drawLine(tearLineBounds.left, tearLineBounds.top, tearLineBounds.right, tearLineBounds.bottom, tearLinePaint)
    }
}
