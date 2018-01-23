package com.expedia.bookings.presenter.shared

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.KrazyglueResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.squareup.phrase.Phrase
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class KrazyglueWidgetViewModel(context: Context) {
    val cityObservable = PublishSubject.create<String>()
    val hotelsObservable = PublishSubject.create<List<KrazyglueResponse.KrazyglueHotel>>()
    val hotelSearchParamsObservable = BehaviorSubject.create<HotelSearchParams>()
    val regionIdObservable = BehaviorSubject.create<String>()
    val headerTextObservable = cityObservable.map {
        Phrase.from(context, R.string.because_you_booked_a_flight_save_on_hotels_TEMPLATE)
                .put("city", it)
                .format().toString()
    }
}
