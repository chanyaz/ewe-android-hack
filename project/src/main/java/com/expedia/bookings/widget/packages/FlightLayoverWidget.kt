package com.expedia.bookings.widget.packages

import android.content.Context
import android.graphics.RectF
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.widget.BaseLayoverWidget
import java.util.ArrayList

open class FlightLayoverWidget(context: Context, attrs: AttributeSet?) : BaseLayoverWidget(context, attrs) {

    override fun hasItems(): Boolean {
        return flightSegmentList.isNotEmpty()
    }

    var flightSegmentList: ArrayList<FlightLeg.FlightSegment> = ArrayList()

    fun update(flightSegments: List<FlightLeg.FlightSegment>, legDurationHours: Int, legDurationMinutes: Int,
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
            if (ViewCompat.isLaidOut(this)) {
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

        val durationBarTopY = airportCodeY - airportCodeBounds.height()
        val durationBarBottomY = airportCodeY
        var durationBarX = airportCodeX + airportCodeBounds.width() + durationBarPadding

        for (flightSegment in flightSegmentList) {

            var layoverBar: RectF? = null
            val durationBar = createDurationBar(flightSegment.durationHours, flightSegment.durationMinutes,
                    durationBarX, durationBarTopY, durationBarBottomY)

            airportCodeX = durationBar.right + durationBarPadding

            if (flightSegment.layoverDurationHours > 0 || flightSegment.layoverDurationMinutes > 0) {
                val airportCodeWidth = calculateTextBounds(flightSegment.arrivalAirportCode).width()
                durationBarX = airportCodeX + airportCodeWidth + durationBarPadding
                layoverBar = createDurationBar(flightSegment.layoverDurationHours, flightSegment.layoverDurationMinutes,
                        durationBarX, durationBarTopY, durationBarBottomY)
                durationBarX = layoverBar.right + durationBarPadding
            }
            val flightObject = LayoverDrawObject(flightSegment.arrivalAirportCode, airportCodeX, airportCodeY,
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