package com.expedia.bookings.hotel.vm

import android.content.Context
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.tracking.AdImpressionTracking
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

open class BaseHotelResultsViewModel(protected val context: Context) {

    // Inputs
    val paramsSubject = PublishSubject.create<HotelSearchParams>()

    // Outputs
    val hotelResultsObservable = PublishSubject.create<HotelSearchResponse>()

    val errorObservable = PublishSubject.create<ApiError>()
    val titleSubject = BehaviorSubject.create<String>()
    val subtitleSubject = PublishSubject.create<CharSequence>()
    val subtitleContDescSubject = PublishSubject.create<String>()
    val showHotelSearchViewObservable = PublishSubject.create<Unit>()

    init {
        hotelResultsObservable.subscribe {
            trackAdImpression(it.pageViewBeaconPixelUrl)
        }
    }

    protected fun trackAdImpression(url: String) {
        AdImpressionTracking.trackAdClickOrImpression(context, url, null)
    }
}
