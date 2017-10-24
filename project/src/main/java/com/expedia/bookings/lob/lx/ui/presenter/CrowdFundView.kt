package com.expedia.bookings.lob.lx.ui.presenter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.LXState
import com.expedia.bookings.otto.Events
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.FilterRangeSeekBar
import com.expedia.bookings.widget.FilterSeekBar
import com.expedia.bookings.widget.FrameLayout
import com.squareup.otto.Subscribe
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class CrowdFundView(context: Context, attr: AttributeSet?) : FrameLayout(context, attr) {
    val departureRangeBar: FilterRangeSeekBar by bindView(R.id.departure_range_bar)
    val departureRangeMinText: TextView by bindView(R.id.departure_range_min_text)
    val departureRangeMaxText: TextView by bindView(R.id.departure_range_max_text)
    val fundTotal: TextView by bindView(R.id.fund_total)
    val activityName: TextView by bindView(R.id.activity_name)
    val durationSeekBar: FilterSeekBar by bindView(R.id.fund_range_seek)
    val availableBalance: TextView by bindView(R.id.available_balance)
    val requiredBalance: TextView by bindView(R.id.required_balance)
    val fundMaxLimit: TextView by bindView(R.id.fund_max_limit)

    lateinit var lxState: LXState
        @Inject set

    override fun onFinishInflate() {
        super.onFinishInflate()
        View.inflate(context, R.layout.crowd_fund, this)
        Ui.getApplication(context).lxComponent().inject(this)
        Events.register(this)
    }

    @Subscribe
    fun onCreateTripSucceeded(event: Events.LXCreateTripSucceeded) {
        activityName.text = lxState.activity.title
        val userStateManager = Ui.getApplication(context).appComponent().userStateManager()
        val user = userStateManager.userSource.user

        val maxValue = 5000
        val minValue = user?.loyaltyMembershipInformation?.loyaltyMonetaryValue?.amount?.setScale(0, RoundingMode.DOWN)?.intValueExact() ?: 0

        availableBalance.text = user?.loyaltyMembershipInformation?.loyaltyMonetaryValue?.formattedMoney
        requiredBalance.text = BigDecimal(lxState.activity.fromPriceValue).minus(user?.loyaltyMembershipInformation?.loyaltyMonetaryValue?.amount ?: BigDecimal.ZERO).toPlainString()
        fundMaxLimit.text = requiredBalance.text

        val timeRange = TimeRange(context, 0, maxValue)
        departureRangeBar.currentA11yStartValue = timeRange.defaultMinText
        departureRangeBar.currentA11yEndValue = timeRange.defaultMaxText

        departureRangeBar.upperLimit = timeRange.notches
        departureRangeBar.minValue = minValue
        departureRangeBar.maxValue = maxValue
        departureRangeMinText.text = timeRange.formatValue(minValue)
        departureRangeMaxText.text = timeRange.formatValue(maxValue)
        fundTotal.text = "$${maxValue - minValue} needed?"
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
        durationSeekBar.upperLimit = BigDecimal(lxState.activity.fromPriceValue).minus(user?.loyaltyMembershipInformation?.loyaltyMonetaryValue?.amount ?: BigDecimal.ZERO).setScale(0, RoundingMode.DOWN).intValueExact()

        durationSeekBar.setOnSeekBarChangeListener { seekBar, progress, fromUser ->
            requiredBalance.text = "$" + progress.toString()
        }
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