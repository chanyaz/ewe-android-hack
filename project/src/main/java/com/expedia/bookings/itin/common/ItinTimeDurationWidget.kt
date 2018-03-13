package com.expedia.bookings.itin.common

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.squareup.phrase.Phrase

class ItinTimeDurationWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    @VisibleForTesting
    val durationText: TextView by bindView(R.id.itin_duration_text)

    var viewModel: ItinTimeDurationViewModel by notNullAndObservable { vm ->
        vm.createTimeDurationWidgetSubject.subscribe { (formattedDuration, contDesc, drawable, durationType) ->
            if (formattedDuration.isNotBlank() && !contDesc.isNullOrBlank() && durationType != ItinTimeDurationViewModel.DurationType.NONE) {
                if (durationType == ItinTimeDurationViewModel.DurationType.LAYOVER) {
                    setLayoverTextAndContDesc(formattedDuration, contDesc)
                } else if (durationType == ItinTimeDurationViewModel.DurationType.TOTAL_DURATION) {
                    setTotalDurationTextAndContDesc(formattedDuration, contDesc)
                }
                if (drawable != null) {
                    durationText.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0)
                }
            } else {
                this.visibility = View.GONE
            }
        }
    }

    init {
        View.inflate(context, R.layout.widget_itin_time_duration, this)
    }

    private fun setLayoverTextAndContDesc(formattedDuration: String, contDesc: String?) {
        val layoverText = Phrase.from(context, R.string.itin_flight_layover_TEMPLATE).put("layover", formattedDuration).format().toString()
        val layoverContDesc = Phrase.from(context, R.string.itin_flight_layover_TEMPLATE).put("layover", contDesc).format().toString()
        durationText.text = layoverText
        durationText.contentDescription = layoverContDesc
    }

    private fun setTotalDurationTextAndContDesc(formattedDuration: String, contDesc: String?) {
        val totalDurationText = Phrase.from(context, R.string.itin_flight_total_duration_TEMPLATE).put("duration", formattedDuration).format().toString()
        val totalDurationContDesc = Phrase.from(context, R.string.itin_flight_total_duration_TEMPLATE).put("duration", contDesc).format().toString()
        durationText.text = totalDurationText
        durationText.contentDescription = totalDurationContDesc
    }
}
