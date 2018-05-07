package com.expedia.bookings.hotel.widget

import android.widget.LinearLayout
import android.content.Context
import android.support.annotation.VisibleForTesting
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.extensions.setTextAndVisibility
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView

class HotelSelectARoomBar(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {
    @VisibleForTesting
    var selectRoomContainer = View.inflate(context, R.layout.hotel_select_a_room_bar_layout, this)
    @VisibleForTesting
    val selectRoomStrikeThroughPrice: TextView by bindView(R.id.select_a_room_strikethrough_price)
    @VisibleForTesting
    val selectRoomPrice: TextView by bindView(R.id.select_a_room_price)
    val viewModel = HotelSelectARoomBarViewModel(context)

    fun bindRoomOffer(response: HotelOffersResponse) {
        viewModel.response = response
        selectRoomContainer.contentDescription = viewModel.getContainerContentDescription()
        selectRoomStrikeThroughPrice.setTextAndVisibility(viewModel.getStrikeThroughPriceString())
        selectRoomPrice.text = viewModel.getPriceString()
    }
}
