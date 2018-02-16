package com.expedia.bookings.rail.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.TicketDeliverySelectionImageView
import com.expedia.util.notNullAndObservable
import com.expedia.vm.rail.RailTicketDeliveryOptionViewModel

class RailTicketDeliveryOptionWidget(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    val statusIcon: TicketDeliverySelectionImageView by bindView(R.id.status_icon)
    val deliveryIcon: ImageView by bindView(R.id.delivery_icon)
    val deliveryLabelText: TextView by bindView(R.id.delivery_label_text)
    val deliveryDetailsText: TextView by bindView(R.id.delivery_details_text)
    val deliveryFeesText: TextView by bindView(R.id.delivery_fees_text)

    var viewModel: RailTicketDeliveryOptionViewModel by notNullAndObservable { vm ->
        vm.statusChanged.subscribe { status ->
            statusIcon.status = status
        }
    }

    init {
        View.inflate(context, R.layout.ticket_delivery_option_widget, this)

        if (attrs != null) {
            val attrSet = context.obtainStyledAttributes(attrs, R.styleable.RailTicketDeliveryOptionWidget, 0, 0)
            try {
                deliveryIcon.setImageDrawable(attrSet.getDrawable(R.styleable.RailTicketDeliveryOptionWidget_delivery_icon))
                deliveryLabelText.text = attrSet.getString(R.styleable.RailTicketDeliveryOptionWidget_label_text)
                deliveryDetailsText.text = attrSet.getString(R.styleable.RailTicketDeliveryOptionWidget_details_text)
                deliveryFeesText.visibility = if (attrSet.getBoolean(R.styleable.RailTicketDeliveryOptionWidget_fees_visible, true)) View.VISIBLE else View.GONE
            } finally {
                attrSet.recycle()
            }
        }
    }
}
