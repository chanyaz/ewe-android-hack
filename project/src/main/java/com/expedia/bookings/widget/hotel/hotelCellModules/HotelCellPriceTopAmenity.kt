package com.expedia.bookings.widget.hotel.hotelCellModules

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.setInverseVisibility
import com.expedia.util.setTextAndVisibility
import com.expedia.util.updateVisibility
import com.expedia.vm.hotel.HotelViewModel

class HotelCellPriceTopAmenity(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val priceContainer: LinearLayout by bindView(R.id.price_container)
    val strikeThroughPriceTextView: TextView by bindView(R.id.strike_through_price)
    val pricePerNightTextView: TextView by bindView(R.id.price_per_night)
    val topAmenityTextView: TextView by bindView(R.id.top_amenity)
    val soldOutTextView: TextView by bindView(R.id.sold_out_text)

    init {
        View.inflate(context, R.layout.hotel_cell_price_top_amenity, this)

        val attrSet = context.obtainStyledAttributes(attrs, R.styleable.HotelCellPriceTopAmenity, 0, 0)
        val orientation = attrSet.getInt(R.styleable.HotelCellPriceTopAmenity_strike_through_price_orientation, VERTICAL)
        priceContainer.orientation = orientation

        attrSet.recycle()
    }

    fun update(viewModel: HotelViewModel) {
        soldOutTextView.updateVisibility(viewModel.isHotelSoldOut)
        priceContainer.setInverseVisibility(viewModel.isHotelSoldOut)
        if (viewModel.isHotelSoldOut) {
            topAmenityTextView.visibility = View.GONE
        } else {
            strikeThroughPriceTextView.setTextAndVisibility(viewModel.hotelStrikeThroughPriceFormatted)

            pricePerNightTextView.text = viewModel.pricePerNight
            pricePerNightTextView.setTextColor(viewModel.pricePerNightColor)

            topAmenityTextView.setTextAndVisibility(viewModel.topAmenityTitle)
        }
    }
}
