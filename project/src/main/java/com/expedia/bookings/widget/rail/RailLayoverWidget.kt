package com.expedia.bookings.widget.rail

import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import com.expedia.bookings.widget.BaseLayoverWidget
import org.joda.time.DateTime
import org.joda.time.Period
import java.util.ArrayList

public open class RailLayoverWidget(context: Context, attrs: AttributeSet?) : BaseLayoverWidget(context, attrs) {

    var railSegmentList: ArrayList<RailSearchResponse.RailSegment> = ArrayList()

    fun update(railSegments: List<RailSearchResponse.RailSegment>, legDurationHours: Int, legDurationMinutes: Int,
                      longestLegDuration: Int) {
        if (railSegmentList.isNotEmpty()) {
            railSegmentList.clear()
        }
        railSegmentList.addAll(railSegments)
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

    override fun hasItems(): Boolean {
        return railSegmentList.isNotEmpty()
    }

    override fun createDrawObjects() {
        if (drawObjects.isNotEmpty()) {
            drawObjects.clear()
        }

        var stationCodeX = paddingLeft.toFloat()
        val stationCodeBounds = calculateTextBounds(railSegmentList[0].departureStationDetails.shortStationCode())
        val stationCodeY = (height / 2).toFloat() + (stationCodeBounds.height() / 2).toFloat()
        drawObjects.add(LayoverDrawObject(railSegmentList[0].departureStationDetails.shortStationCode(), stationCodeX, stationCodeY, null, null))

        val durationBarTopY = stationCodeY - stationCodeBounds.height()
        val durationBarBottomY = stationCodeY
        var durationBarX = stationCodeX + stationCodeBounds.width() + durationBarPadding

        for ((index, railSegment) in railSegmentList.withIndex()) {

            var layoverBar: RectF? = null
            var durationBar = createDurationBar(railSegment.durationHours(), railSegment.durationMinutes(), durationBarX,
                    durationBarTopY, durationBarBottomY)

            stationCodeX = durationBar.right + durationBarPadding

            if (index < railSegmentList.size - 1) {
                //all but last one will have "layover"
                val stationCodeWidth = calculateTextBounds(railSegment.arrivalStationDetails.shortStationCode()).width()
                durationBarX = stationCodeX + stationCodeWidth + durationBarPadding
                val first = railSegmentList.get(index)
                val second = railSegmentList.get(index + 1)
                layoverBar = createDurationBar(hoursBetween(first, second), minutesBetween(first, second), durationBarX,
                        durationBarTopY, durationBarBottomY)
                durationBarX = layoverBar.right + durationBarPadding
            }
            var railObject = LayoverDrawObject(railSegment.arrivalStationDetails.shortStationCode(), stationCodeX, stationCodeY,
                    durationBar, layoverBar)
            drawObjects.add(railObject)
        }
    }

    override fun calculateLocationsAndPaddingWidth(): Float {
        val totalLayoverPadding: Float = durationBarPadding * (railSegmentList.size - 1)
        val totalDurationPadding: Float = durationBarPadding * 2 * railSegmentList.size
        var totalStationCodeWidth = 0f

        totalStationCodeWidth += calculateTextBounds(railSegmentList[0].departureStationDetails.shortStationCode()).width()
        for (railSegment in railSegmentList) {
            totalStationCodeWidth += calculateTextBounds(railSegment.arrivalStationDetails.shortStationCode()).width()
        }

        return paddingLeft + totalLayoverPadding + totalDurationPadding + totalStationCodeWidth + paddingRight + OFF_BY_ONE_BUFFER
    }

    fun hoursBetween(first: RailSearchResponse.RailSegment, second: RailSearchResponse.RailSegment): Int {
        val firstDateTime = DateTime.parse(first.arrivalDateTime)
        val secondDateTime = DateTime.parse(second.departureDateTime)
        val period = Period(firstDateTime, secondDateTime);
        return period.hours;
    }

    fun minutesBetween(first: RailSearchResponse.RailSegment, second: RailSearchResponse.RailSegment): Int {
        val firstDateTime = DateTime.parse(first.arrivalDateTime)
        val secondDateTime = DateTime.parse(second.departureDateTime)
        val period = Period(firstDateTime, secondDateTime);
        return period.minutes;
    }
}