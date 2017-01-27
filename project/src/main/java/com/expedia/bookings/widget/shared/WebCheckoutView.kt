package com.expedia.bookings.widget.shared

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.HotelCreateTripParams
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.vm.HotelCreateTripViewModel
import rx.subjects.BehaviorSubject
import kotlin.properties.Delegates

class WebCheckoutView(context: Context, attrs: AttributeSet) : BaseWebViewWidget(context, attrs) {

    override fun onFinishInflate() {
        super.onFinishInflate()
        toolbar.title = context.getString(R.string.secure_checkout)
    }
}