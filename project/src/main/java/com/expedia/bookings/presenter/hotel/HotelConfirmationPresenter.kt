package com.expedia.bookings.presenter.hotel

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.R
import com.expedia.bookings.services.HotelCheckoutResponse
import com.expedia.bookings.utils.bindView

public class HotelConfirmationPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val myTextView: TextView by bindView(R.id.itin_text_view)

    init {
        View.inflate(getContext(), R.layout.widget_hotel_confirmation, this)
    }

    fun bind(response: HotelCheckoutResponse) {
        myTextView.setText(response.checkoutResponse.bookingResponse.itineraryNumber)
    }
}