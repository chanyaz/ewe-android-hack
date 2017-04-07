package com.expedia.bookings.widget.hotel.hotelCellModules

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextColor
import com.expedia.util.subscribeVisibility
import com.expedia.vm.hotel.HotelViewModel

class HotelCellPriceTopAmenity(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val priceContainer: LinearLayout by bindView(R.id.price_container)
    val strikeThroughPriceTextView: TextView by bindView(R.id.strike_through_price)
    val pricePerNightTextView: TextView by bindView(R.id.price_per_night)
    val topAmenityTextView: TextView by bindView(R.id.top_amenity)

    init {
        View.inflate(context, R.layout.hotel_cell_price_top_amenity, this)

        val attrSet = context.obtainStyledAttributes(attrs, R.styleable.HotelCellPriceTopAmenity, 0, 0)
        val orientation = attrSet.getInt(R.styleable.HotelCellPriceTopAmenity_strike_through_price_orientation, VERTICAL)
        priceContainer.orientation = orientation

        attrSet.recycle()
    }

    fun bindHotelViewModel(viewModel: HotelViewModel) {
        viewModel.hotelStrikeThroughPriceVisibility.subscribeVisibility(strikeThroughPriceTextView)
        viewModel.hotelStrikeThroughPriceFormatted.subscribeText(strikeThroughPriceTextView)

        viewModel.pricePerNightObservable.subscribeText(pricePerNightTextView)
        viewModel.pricePerNightColorObservable.subscribeTextColor(pricePerNightTextView)

        viewModel.topAmenityVisibilityObservable.subscribeVisibility(topAmenityTextView)
        viewModel.topAmenityTitleObservable.subscribeText(topAmenityTextView)
    }
}
