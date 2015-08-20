package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import rx.Observer

/**
 * Created by mohsharma on 8/14/15.
 */

class ResortFeeWidget(context: Context,attrs: AttributeSet): LinearLayout(context, attrs) {

    val resortFeeText: TextView by bindView(R.id.resort_fees_text)

    init {
        View.inflate(getContext(), R.layout.resort_fee_widget, this)
    }

}