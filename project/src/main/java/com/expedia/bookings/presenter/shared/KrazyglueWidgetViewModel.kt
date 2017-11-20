package com.expedia.bookings.presenter.shared;

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.KrazyglueResponse
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import org.joda.time.DateTime

class KrazyglueWidgetViewModel(context: Context) {
    val cityObservable = PublishSubject.create<String>()
    val hotelsObservable = PublishSubject.create<List<KrazyglueResponse.KrazyglueHotel>>()
    val hotelSearchParamsObservable = BehaviorSubject.create<HotelSearchParams>()
    val headerTextObservable = cityObservable.map {
        Phrase.from(context, R.string.because_you_booked_a_flight_save_on_hotels_TEMPLATE)
                .put("city", it)
                .format().toString()
    }
    val destinationObservable = BehaviorSubject.create<DateTime>()
}
