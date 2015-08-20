package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.util.endlessObserver
import rx.Observer
import rx.subjects.BehaviorSubject

class HotelReviewsViewModel(val context: Context) {

    val toolbarTitleObservable = BehaviorSubject.create<String>()
    val toolbarSubtitleObservable = BehaviorSubject.create<String>()
    val hotelReviewsObservable = BehaviorSubject.create<Hotel>()

    var hotelObserver: Observer<Hotel> = endlessObserver { hotel ->
        toolbarTitleObservable.onNext(hotel.localizedName)
        toolbarSubtitleObservable.onNext(context.getResources().getString(R.string.n_reviews_TEMPLATE, hotel.totalReviews))
        hotelReviewsObservable.onNext(hotel)
    }

}