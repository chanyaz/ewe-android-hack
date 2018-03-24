package com.expedia.bookings.itin.hotel.pricingRewards

import android.content.Context
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.itin.scopes.HotelItinPricingSummaryScope
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable

class HotelItinPricingSummaryView(context: Context?, attrs: AttributeSet?) : CardView(context, attrs) {
    var viewModel: HotelItinPricingSummaryViewModel<HotelItinPricingSummaryScope> by notNullAndObservable {
        it.lineItemViewModelSubject.subscribe { summaries ->
            containerView.removeAllViews()

            summaries.forEach { summary ->
                val totalItemView: HotelItinLineItemView = Ui.inflate(R.layout.hotel_itin_total_line_item_view, containerView, false)
                totalItemView.labelTextView.text = summary.totalLineItem.labelString
                totalItemView.priceTextView.text = summary.totalLineItem.priceString

                containerView.addView(totalItemView)

                for (item in summary.perDayLineItems) {
                    val itemView: HotelItinLineItemView = Ui.inflate(R.layout.hotel_itin_line_item_view, containerView, false)
                    itemView.labelTextView.text = item.labelString
                    itemView.priceTextView.text = item.priceString

                    containerView.addView(itemView)
                }
            }
        }
    }

    val containerView: LinearLayout by bindView(R.id.hotel_itin_pricing_summary_container)
}

class HotelItinLineItemView(context: Context?, attrs: AttributeSet?) : RelativeLayout(context, attrs) {
    val labelTextView: TextView by bindView(R.id.hotel_itin_line_item_view_label_text)
    val priceTextView: TextView by bindView(R.id.hotel_itin_line_item_view_price_text)
}
