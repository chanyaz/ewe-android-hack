package com.expedia.bookings.itin.hotel.pricingRewards

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable

class HotelItinPricingSummaryView(context: Context?, attrs: AttributeSet?) : CardView(context, attrs) {
    val containerView: LinearLayout by bindView(R.id.hotel_itin_pricing_summary_container)

    var viewModel: IHotelItinPricingSummaryViewModel by notNullAndObservable {
        it.clearPriceSummaryContainerSubject.subscribe {
            containerView.removeAllViews()
        }

        it.roomPriceBreakdownSubject.subscribe { roomPrices ->
            roomPrices.forEach { roomPrice ->
                val totalRoomPriceView: PriceSummaryItemView = Ui.inflate(R.layout.hotel_itin_price_summary_item_view, containerView, false)
                totalRoomPriceView.labelTextView.text = roomPrice.totalRoomPriceItem.labelString
                totalRoomPriceView.priceTextView.text = roomPrice.totalRoomPriceItem.priceString
                totalRoomPriceView.setPriceColor(roomPrice.totalRoomPriceItem.colorRes)

                containerView.addView(totalRoomPriceView)

                for (perDayPrice in roomPrice.perDayRoomPriceItems) {
                    val roomPricePerDayView: PriceSummaryItemView = Ui.inflate(R.layout.hotel_itin_price_summary_item_view, containerView, false)
                    roomPricePerDayView.labelTextView.text = perDayPrice.labelString
                    roomPricePerDayView.priceTextView.text = perDayPrice.priceString
                    roomPricePerDayView.setPriceColor(perDayPrice.colorRes)

                    containerView.addView(roomPricePerDayView)
                }
            }
        }

        it.priceLineItemSubject.subscribe { item ->
            val priceLineItem: PriceSummaryItemView = Ui.inflate(R.layout.hotel_itin_price_summary_item_view, containerView, false)
            priceLineItem.labelTextView.text = item.labelString
            priceLineItem.priceTextView.text = item.priceString
            priceLineItem.setPriceColor(item.colorRes)

            containerView.addView(priceLineItem)
        }
    }
}

class PriceSummaryItemView(context: Context?, attrs: AttributeSet?) : RelativeLayout(context, attrs) {
    val labelTextView: TextView by bindView(R.id.hotel_itin_line_item_view_label_text)
    val priceTextView: TextView by bindView(R.id.hotel_itin_line_item_view_price_text)

    fun setPriceColor(colorRes: Int) {
        val color = ContextCompat.getColor(context, colorRes)
        labelTextView.setTextColor(color)
        priceTextView.setTextColor(color)
    }
}
