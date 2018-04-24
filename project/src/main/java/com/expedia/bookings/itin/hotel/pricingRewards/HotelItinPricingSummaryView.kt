package com.expedia.bookings.itin.hotel.pricingRewards

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable

class HotelItinPricingSummaryView(context: Context?, attrs: AttributeSet?) : CardView(context, attrs) {
    val roomContainerView: LinearLayout by bindView(R.id.hotel_itin_pricing_summary_room_container)
    val multipleGuestView: PriceSummaryItemView by bindView(R.id.hotel_itin_pricing_summary_multiple_guest_view)
    val taxesAndFeesView: PriceSummaryItemView by bindView(R.id.hotel_itin_pricing_summary_taxes_fees_view)
    val couponsView: PriceSummaryItemView by bindView(R.id.hotel_itin_pricing_summary_coupons_view)
    val pointsView: PriceSummaryItemView by bindView(R.id.hotel_itin_pricing_summary_points_view)

    var viewModel: IHotelItinPricingSummaryViewModel by notNullAndObservable {
        it.roomPriceBreakdownSubject.subscribe { roomPrices ->
            roomContainerView.removeAllViews()

            roomPrices.forEach { roomPrice ->
                val totalRoomPriceView: PriceSummaryItemView = Ui.inflate(R.layout.hotel_itin_price_summary_item_view, roomContainerView, false)
                totalRoomPriceView.labelTextView.text = roomPrice.totalRoomPriceItem.labelString
                totalRoomPriceView.priceTextView.text = roomPrice.totalRoomPriceItem.priceString
                totalRoomPriceView.setPriceColor(roomPrice.totalRoomPriceItem.colorRes)

                roomContainerView.addView(totalRoomPriceView)

                for (perDayPrice in roomPrice.perDayRoomPriceItems) {
                    val roomPricePerDayView: PriceSummaryItemView = Ui.inflate(R.layout.hotel_itin_price_summary_item_view, roomContainerView, false)
                    roomPricePerDayView.labelTextView.text = perDayPrice.labelString
                    roomPricePerDayView.priceTextView.text = perDayPrice.priceString
                    roomPricePerDayView.setPriceColor(perDayPrice.colorRes)

                    roomContainerView.addView(roomPricePerDayView)
                }
            }
        }

        it.multipleGuestItemSubject.subscribe { item ->
            setupPriceLineItem(multipleGuestView, item)
        }

        it.taxesAndFeesItemSubject.subscribe { item ->
            setupPriceLineItem(taxesAndFeesView, item)
        }

        it.couponsItemSubject.subscribe { item ->
            setupPriceLineItem(couponsView, item)
        }

        it.pointsItemSubject.subscribe { item ->
            setupPriceLineItem(pointsView, item)
        }
    }

    private fun setupPriceLineItem(view: PriceSummaryItemView, item: HotelItinPriceLineItem) {
        view.visibility = View.VISIBLE
        view.labelTextView.text = item.labelString
        view.priceTextView.text = item.priceString
        view.setPriceColor(item.colorRes)
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
