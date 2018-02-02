package com.expedia.bookings.hotel.vm

import android.content.Context
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.tracking.AdImpressionTracking
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

open class BaseHotelResultsViewModel(protected val context: Context) {
    protected val titleSubject = BehaviorSubject.create<String>()
    protected val subtitleSubject = PublishSubject.create<CharSequence>()
    protected val subtitleContDescSubject = PublishSubject.create<String>()

    // Inputs
    val paramsSubject = PublishSubject.create<HotelSearchParams>()

    // Outputs
    val hotelResultsObservable = PublishSubject.create<HotelSearchResponse>()

    // errorObservable never had any data pushed into it?
//    val errorObservable = PublishSubject.create<ApiError>()
    val titleObservable = titleSubject as Observable<String>
    val subtitleObservable = subtitleSubject as Observable<CharSequence>
    val subtitleContDescObservable = subtitleContDescSubject as Observable<String>

    init {
        hotelResultsObservable.subscribe {
            trackAdImpression(it.pageViewBeaconPixelUrl)
        }
    }

    private fun trackAdImpression(url: String) {
        AdImpressionTracking.trackAdClickOrImpression(context, url, null)
    }
}
