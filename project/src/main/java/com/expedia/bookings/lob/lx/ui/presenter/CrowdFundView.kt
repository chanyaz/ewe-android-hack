package com.expedia.bookings.lob.lx.ui.presenter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FilterRangeSeekBar
import com.expedia.bookings.widget.FrameLayout

class CrowdFundView(context: Context, attr: AttributeSet?) : FrameLayout(context, attr) {
    val departureRangeBar: FilterRangeSeekBar by bindView(R.id.departure_range_bar)
    val departureRangeMinText: TextView by bindView(R.id.departure_range_min_text)
    val departureRangeMaxText: TextView by bindView(R.id.departure_range_max_text)
    val fundTotal: TextView by bindView(R.id.fund_total)

    override fun onFinishInflate() {
        super.onFinishInflate()
        View.inflate(context, R.layout.crowd_fund, this)

        val timeRange = TimeRange(context, 0, 100)
        departureRangeBar.a11yStartName = context.getString(R.string.departure_time_range_start)
        departureRangeBar.a11yEndName = context.getString(R.string.departure_time_range_end)
        departureRangeBar.currentA11yStartValue = timeRange.defaultMinText
        departureRangeBar.currentA11yEndValue = timeRange.defaultMaxText

        val minValue = 10
        val maxValue = 100
        departureRangeBar.upperLimit = timeRange.notches
        departureRangeBar.minValue = minValue
        departureRangeBar.maxValue = maxValue
        departureRangeMinText.text = timeRange.formatValue(minValue)
        departureRangeMaxText.text = timeRange.formatValue(maxValue)
        fundTotal.text = "How much funds you want to raise: $${maxValue - minValue}"
        timeRange.update(minValue, maxValue)

        departureRangeBar.setOnRangeSeekBarChangeListener(object : FilterRangeSeekBar.OnRangeSeekBarChangeListener {
            override fun onRangeSeekBarDragChanged(bar: FilterRangeSeekBar?, minValue: Int, maxValue: Int) {
                departureRangeMinText.text = timeRange.formatValue(minValue)
                departureRangeMaxText.text = timeRange.formatValue(maxValue)
                fundTotal.text = "How much funds you want to raise: $${maxValue - minValue}"
                timeRange.update(minValue, maxValue)
            }

            override fun onRangeSeekBarValuesChanged(bar: FilterRangeSeekBar?, minValue: Int, maxValue: Int, thumb: FilterRangeSeekBar.Thumb) {
                departureRangeMinText.text = timeRange.formatValue(minValue)
                departureRangeMaxText.text = timeRange.formatValue(maxValue)
                departureRangeBar.currentA11yStartValue = departureRangeMinText.text.toString()
                departureRangeBar.currentA11yEndValue = departureRangeMaxText.text.toString()
                timeRange.update(minValue, maxValue)
            }
        })
    }

    data class TimeRange(val context: Context, val min: Int, val max: Int) {
        val notches = max - min
        val defaultMinText = formatValue(toValue(min))
        val defaultMaxText = formatValue(toValue(max))

        private fun toValue(hour: Int): Int = hour - min
        private fun toHour(value: Int): Int = value + min

        fun formatValue(value: Int): String {
            return "$ $value"
        }

        fun update(minValue: Int, maxValue: Int): Pair<Int, Int> {
            val newMaxDuration = toHour(maxValue)
            val newMinDuration = toHour(minValue)
            val min = if (newMinDuration == min) 0 else newMinDuration
            val max = if (newMaxDuration == max) 0 else newMaxDuration
            return Pair(min, max)
        }
    }


}