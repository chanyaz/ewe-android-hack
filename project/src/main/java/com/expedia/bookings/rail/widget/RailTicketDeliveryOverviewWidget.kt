package com.expedia.bookings.rail.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.rail.RailTicketDeliveryOverviewViewModel

class RailTicketDeliveryOverviewWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val deliveryOptionIcon: ImageView by bindView(R.id.delivery_option_icon)
    val deliveryOptionDetailsText: TextView by bindView(R.id.delivery_option_label_text)

    var viewModel: RailTicketDeliveryOverviewViewModel by notNullAndObservable { vm ->
        vm.iconObservable.subscribe { icon ->
            deliveryOptionIcon.setImageDrawable(ContextCompat.getDrawable(context, icon))
        }
        vm.titleObservable.subscribeText(deliveryOptionDetailsText)
    }

    init {
        View.inflate(context, R.layout.ticket_delivery_overview_widget, this)
    }
}
