package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import rx.Observer

class ResortFeeWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val resortFeeText: TextView by bindView(R.id.resort_fees_text)
    val feesIncludedNotIncluded: TextView by bindView(R.id.fees_included_not_included_label)

    init {
        View.inflate(getContext(), R.layout.resort_fee_widget, this)
    }

}