package com.expedia.bookings.itin.widget

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.itin.vm.ItinTimeDurationViewModel
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.squareup.phrase.Phrase

class ItinTimeDurationWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    @VisibleForTesting
    val durationText: TextView by bindView(R.id.itin_duration_text)

    var viewModel: ItinTimeDurationViewModel by notNullAndObservable { vm ->
        vm.createTimeDurationWidgetSubject.subscribe { (formattedDuration, contDescDuration, drawable, durationType) ->
            if (formattedDuration.isNotBlank() && contDescDuration.isNotBlank() && durationType != ItinTimeDurationViewModel.DurationType.NONE) {
                if(durationType == ItinTimeDurationViewModel.DurationType.LAYOVER) {
                    val text = Phrase.from(context, R.string.itin_flight_layover_TEMPLATE).put("layover", formattedDuration).format().toString()
                    val contDesc = Phrase.from(context, R.string.itin_flight_layover_TEMPLATE).put("layover", contDescDuration).format().toString()
                    durationText.text = text
                    durationText.contentDescription = contDesc
                }
                else if (durationType == ItinTimeDurationViewModel.DurationType.TOTAL_DURATION) {

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
}
