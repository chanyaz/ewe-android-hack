package com.expedia.bookings.widget.packages

import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.packages.FlightLeg
import java.util.ArrayList

public open class FlightLayoverWidget(context: Context, attrs: AttributeSet?): View(context, attrs) {
    val OFF_BY_ONE_BUFFER = 2

    lateinit var airportCodePaint: Paint
    lateinit var durationBarPaint: Paint
    lateinit var layoverBorderPaint: Paint
    val layoverDrawable: BitmapDrawable
    val layoverStrokeWidth = 2f

    val airportCodeTextColor: Int
    val airportCodeTextSize: Float
    val durationBarColor: Int
    val durationBarPadding: Float
    val durationBarCornerRadius = 3f

    var durationBarTopY = 0f
    var durationBarBottomY = 0f

    var flightLegDuration = 0f
    var maxLegDuration = 0f

    var totalWidthForDurationBars = 0f

    var flightSegmentList: ArrayList<FlightLeg.FlightSegment> = ArrayList()
    var flightDrawObjects: ArrayList<FlightLayoverDrawObject> = ArrayList()

    init {
        val attrSet = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FlightLayoverWidget, 0, 0);
        try {
            airportCodeTextColor = attrSet.getColor(R.styleable.FlightLayoverWidget_airport_text_color,
                    R.color.packages_primary_color);
            airportCodeTextSize = attrSet.getDimension(R.styleable.FlightLayoverWidget_airport_text_size, 0f)

            durationBarColor = attrSet.getColor(R.styleable.FlightLayoverWidget_duration_bar_color,
                    R.color.packages_primary_color);
            durationBarPadding = attrSet.getDimension(R.styleable.FlightLayoverWidget_duration_bar_padding, 0f)
            layoverDrawable = attrSet.getDrawable(R.styleable.FlightLayoverWidget_layover_background) as BitmapDrawable
        } finally {
            attrSet.recycle();
        }

        initPaints()
    }

    open fun initPaints() {
        airportCodePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        airportCodePaint.color = airportCodeTextColor
        airportCodePaint.textSize = airportCodeTextSize

        durationBarPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        durationBarPaint.color = durationBarColor

        layoverBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        layoverBorderPaint.style = Paint.Style.STROKE
        layoverBorderPaint.strokeWidth = layoverStrokeWidth
        layoverBorderPaint.color = context.resources.getColor(R.color.packages_primary_color)
    }

    public fun update(flightSegments: List<FlightLeg.FlightSegment>, legDurationHours: Int, legDurationMinutes: Int,
                      longestLegDuration: Int) {
        if (flightSegmentList.isNotEmpty()) {
            flightSegmentList.clear()
        }
        flightSegmentList.addAll(flightSegments)
        flightLegDuration = (legDurationHours * 60 + legDurationMinutes).toFloat()
        maxLegDuration = longestLegDuration.toFloat()

        if (width > 0) {
            totalWidthForDurationBars = width - calculateAirportsAndPaddingWidth()
            createDrawObjects()
            if (isLaidOut) {
                postInvalidate()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, odlW: Int, oldH: Int) {
        if (flightSegmentList.isNotEmpty()) {
            totalWidthForDurationBars = w - calculateAirportsAndPaddingWidth()
            createDrawObjects()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (flightDrawObjects.isNotEmpty()) {
            for (flightDrawObject in flightDrawObjects) {
                canvas.drawText(flightDrawObject.airportCode, 0, flightDrawObject.airportCode.length,
                        flightDrawObject.airportCodeX, flightDrawObject.airportCodeY, airportCodePaint)

                if (flightDrawObject.flightDurationBar != null) {
                    canvas.drawRoundRect(
                            flightDrawObject.flightDurationBar.leftX,
                            durationBarTopY,
                            flightDrawObject.flightDurationBar.rightX,
                            durationBarBottomY,
                            durationBarCornerRadius, durationBarCornerRadius, durationBarPaint)
                }

                if (flightDrawObject.layoverDurationBar != null) {
                    layoverDrawable.setBounds(
                            flightDrawObject.layoverDurationBar.leftX.toInt(),
                            durationBarTopY.toInt(),
                            flightDrawObject.layoverDurationBar.rightX.toInt(),
                            durationBarBottomY.toInt())

                    layoverDrawable.draw(canvas)
                    canvas.drawRoundRect(
                            flightDrawObject.layoverDurationBar.leftX,
                            durationBarTopY,
                            flightDrawObject.layoverDurationBar.rightX,
                            durationBarBottomY,
                            durationBarCornerRadius, durationBarCornerRadius, layoverBorderPaint)
                }
            }
        }
    }

    private fun createDrawObjects() {
        if (flightDrawObjects.isNotEmpty()) {
            flightDrawObjects.clear()
        }

        var airportCodeX = paddingLeft.toFloat()
        val airportCodeBounds = calculateTextBounds(flightSegmentList[0].departureAirportCode)
        val airportCodeY = (height / 2).toFloat() + (airportCodeBounds.height() / 2).toFloat()
        flightDrawObjects.add(FlightLayoverDrawObject(flightSegmentList[0].departureAirportCode, airportCodeX, airportCodeY, null, null))

        durationBarTopY = airportCodeY - airportCodeBounds.height()
        durationBarBottomY = airportCodeY
        var durationBarX = airportCodeX + airportCodeBounds.width() + durationBarPadding

        for (flightSegment in flightSegmentList) {

            var layoverBar: DurationBar? = null
            var durationBar = createDurationBar(flightSegment.durationHours, flightSegment.durationMinutes, durationBarX)

            airportCodeX = durationBar.rightX + durationBarPadding

            if (flightSegment.layoverDurationHours > 0 || flightSegment.layoverDurationMinutes > 0) {
                val airportCodeWidth = calculateTextBounds(flightSegment.arrivalAirportCode).width()
                durationBarX = airportCodeX + airportCodeWidth + durationBarPadding
                layoverBar = createDurationBar(flightSegment.layoverDurationHours, flightSegment.layoverDurationMinutes, durationBarX)
                durationBarX = layoverBar.rightX + durationBarPadding
            }
            var flightObject = FlightLayoverDrawObject(flightSegment.arrivalAirportCode, airportCodeX, airportCodeY,
                    durationBar, layoverBar)
            flightDrawObjects.add(flightObject)
        }
    }

    fun createDurationBar(hours: Int, minutes: Int, xCoord: Float): DurationBar {
        val durationMinutes = hours * 60 + minutes
        val relativeWidth = Math.min(1f, (flightLegDuration / maxLegDuration))
        val durationWidth = (durationMinutes / flightLegDuration) * totalWidthForDurationBars * relativeWidth

        return DurationBar(xCoord, xCoord + durationWidth)
    }

    fun calculateAirportsAndPaddingWidth(): Float {
        val totalLayoverPadding: Float = durationBarPadding * (flightSegmentList.size - 1)
        val totalDurationPadding: Float = durationBarPadding * 2 * flightSegmentList.size
        var totalAirportCodeWidth = 0f

        totalAirportCodeWidth += calculateTextBounds(flightSegmentList[0].departureAirportCode).width()
        for (flightSegment in flightSegmentList) {
            totalAirportCodeWidth += calculateTextBounds(flightSegment.arrivalAirportCode).width()
        }

        return paddingLeft + totalLayoverPadding + totalDurationPadding + totalAirportCodeWidth + paddingRight + OFF_BY_ONE_BUFFER
    }

    open fun calculateTextBounds(airportCode: String): Rect {
        val airportCodeRectBounds = Rect()
        airportCodePaint.getTextBounds(airportCode, 0, airportCode.length, airportCodeRectBounds)
        return airportCodeRectBounds
    }

    class FlightLayoverDrawObject(val airportCode: String, val airportCodeX: Float, val airportCodeY: Float,
                                  val flightDurationBar: DurationBar?, val layoverDurationBar: DurationBar?) {
    }

    class DurationBar(val leftX: Float, val rightX: Float) {
    }
}