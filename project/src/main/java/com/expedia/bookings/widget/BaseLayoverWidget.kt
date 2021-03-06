package com.expedia.bookings.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import java.util.ArrayList

public abstract class BaseLayoverWidget(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    val OFF_BY_ONE_BUFFER = 2

    val layoverDrawable: BitmapDrawable
    val layoverStrokeWidth = 2f
    val durationBarColor: Int
    val durationBarPadding: Float
    val durationBarCornerRadius = 3f

    val locationCodeTextColor: Int
    val locationCodeTextSize: Float

    var durationBarTopY = 0f
    var durationBarBottomY = 0f

    var legDuration = 0f
    var maxLegDuration = 0f

    var totalWidthForDurationBars = 0f

    var drawObjects: ArrayList<LayoverDrawObject> = ArrayList()


    lateinit var locationCodePaint: Paint
    lateinit var durationBarPaint: Paint
    lateinit var layoverBorderPaint: Paint

    abstract fun hasItems(): Boolean;
    abstract fun calculateLocationsAndPaddingWidth(): Float;
    abstract fun createDrawObjects();

    init {
        val attrSet = context.getTheme().obtainStyledAttributes(attrs, R.styleable.LayoverWidget, 0, 0);
        try {
            locationCodeTextColor = attrSet.getColor(R.styleable.LayoverWidget_location_text_color,
                    R.color.packages_primary_color);
            locationCodeTextSize = attrSet.getDimension(R.styleable.LayoverWidget_location_text_size, 0f)
            durationBarColor = attrSet.getColor(R.styleable.LayoverWidget_duration_bar_color, R.color.packages_primary_color);
            durationBarPadding = attrSet.getDimension(R.styleable.LayoverWidget_duration_bar_padding, 0f)
            layoverDrawable = attrSet.getDrawable(R.styleable.LayoverWidget_layover_background) as BitmapDrawable
        } finally {
            attrSet.recycle();
        }
        initPaints()
    }

    open fun initPaints() {
        locationCodePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        locationCodePaint.color = locationCodeTextColor
        locationCodePaint.textSize = locationCodeTextSize

        durationBarPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        durationBarPaint.color = durationBarColor

        layoverBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        layoverBorderPaint.style = Paint.Style.STROKE
        layoverBorderPaint.strokeWidth = layoverStrokeWidth
        layoverBorderPaint.color = durationBarColor
    }

    override fun onSizeChanged(w: Int, h: Int, odlW: Int, oldH: Int) {
        if (hasItems()) {
            totalWidthForDurationBars = w - calculateLocationsAndPaddingWidth()
            createDrawObjects()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (drawObjects.isNotEmpty()) {
            for (drawObject in drawObjects) {
                canvas.drawText(drawObject.locationCode, 0, drawObject.locationCode.length,
                        drawObject.locationCodeX, drawObject.locationCodeY, locationCodePaint)

                if (drawObject.durationBar != null) {
                    canvas.drawRoundRect(
                            drawObject.durationBar.leftX,
                            durationBarTopY,
                            drawObject.durationBar.rightX,
                            durationBarBottomY,
                            durationBarCornerRadius, durationBarCornerRadius, durationBarPaint)
                }

                if (drawObject.layoverDurationBar != null) {
                    layoverDrawable.setBounds(
                            drawObject.layoverDurationBar.leftX.toInt(),
                            durationBarTopY.toInt(),
                            drawObject.layoverDurationBar.rightX.toInt(),
                            durationBarBottomY.toInt())

                    layoverDrawable.draw(canvas)
                    canvas.drawRoundRect(
                            drawObject.layoverDurationBar.leftX,
                            durationBarTopY,
                            drawObject.layoverDurationBar.rightX,
                            durationBarBottomY,
                            durationBarCornerRadius, durationBarCornerRadius, layoverBorderPaint)
                }
            }
        }
    }


    open fun calculateTextBounds(locationCode: String): Rect {
        val locationCodeRectBounds = Rect()
        locationCodePaint.getTextBounds(locationCode, 0, locationCode.length, locationCodeRectBounds)
        return locationCodeRectBounds
    }

    fun createDurationBar(hours: Int, minutes: Int, xCoord: Float): DurationBar {
        val durationMinutes = hours * 60 + minutes
        val relativeWidth = Math.min(1f, (legDuration / maxLegDuration))
        val durationWidth = (durationMinutes / legDuration) * totalWidthForDurationBars * relativeWidth

        return DurationBar(xCoord, xCoord + durationWidth)
    }

    class DurationBar(val leftX: Float, val rightX: Float) {
    }

    class LayoverDrawObject(val locationCode: String, val locationCodeX: Float, val locationCodeY: Float,
                            val durationBar: DurationBar?, val layoverDurationBar: DurationBar?) {
    }
}