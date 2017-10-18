package com.expedia.bookings.itin.widget

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.itin.vm.FlightItinLayoverViewModel
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable

class ItinTimeDurationWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    @VisibleForTesting
    val layoverText: TextView by bindView(R.id.itin_duration_text)

    var viewModel: FlightItinLayoverViewModel by notNullAndObservable { vm ->
        vm.createTimeDurationWidgetSubject.subscribe { (text, contDesc, drawable) ->
            if (!text.isNullOrEmpty() && !contDesc.isNullOrEmpty()) {
                layoverText.text = text
                layoverText.contentDescription = contDesc
                if (drawable != null) {
                    layoverText.setCompoundDrawablesWithIntrinsicBounds(drawable, 0, 0, 0)
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