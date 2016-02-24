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
import com.expedia.bookings.widget.BaseLayoverWidget
import java.util.ArrayList

public open class FlightLayoverWidget(context: Context, attrs: AttributeSet?) : BaseLayoverWidget(context, attrs) {

    override fun hasItems(): Boolean {
        return flightSegmentList.isNotEmpty()
    }

    var flightSegmentList: ArrayList<FlightLeg.FlightSegment> = ArrayList()

    public fun update(flightSegments: List<FlightLeg.FlightSegment>, legDurationHours: Int, legDurationMinutes: Int,
                      longestLegDuration: Int) {
        if (flightSegmentList.isNotEmpty()) {
            flightSegmentList.clear()
        }
        flightSegmentList.addAll(flightSegments)
        legDuration = (legDurationHours * 60 + legDurationMinutes).toFloat()
        maxLegDuration = longestLegDuration.toFloat()

        if (width > 0) {
            totalWidthForDurationBars = width - calculateLocationsAndPaddingWidth()
            createDrawObjects()
            if (isLaidOut) {
                postInvalidate()
            }
        }
    }

    override fun createDrawObjects() {
        if (drawObjects.isNotEmpty()) {
            drawObjects.clear()
        }

        var airportCodeX = paddingLeft.toFloat()
        val airportCodeBounds = calculateTextBounds(flightSegmentList[0].departureAirportCode)
        val airportCodeY = (height / 2).toFloat() + (airportCodeBounds.height() / 2).toFloat()
        drawObjects.add(LayoverDrawObject(flightSegmentList[0].departureAirportCode, airportCodeX, airportCodeY, null, null))

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
            var flightObject = LayoverDrawObject(flightSegment.arrivalAirportCode, airportCodeX, airportCodeY,
                    durationBar, layoverBar)
            drawObjects.add(flightObject)
        }
    }

    override fun calculateLocationsAndPaddingWidth(): Float {
        val totalLayoverPadding: Float = durationBarPadding * (flightSegmentList.size - 1)
        val totalDurationPadding: Float = durationBarPadding * 2 * flightSegmentList.size
        var totalAirportCodeWidth = 0f

        totalAirportCodeWidth += calculateTextBounds(flightSegmentList[0].departureAirportCode).width()
        for (flightSegment in flightSegmentList) {
            totalAirportCodeWidth += calculateTextBounds(flightSegment.arrivalAirportCode).width()
        }

        return paddingLeft + totalLayoverPadding + totalDurationPadding + totalAirportCodeWidth + paddingRight + OFF_BY_ONE_BUFFER
    }
}