package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.HotelUtils
import com.expedia.util.endlessObserver
import com.squareup.phrase.Phrase
import rx.Observer
import rx.subjects.BehaviorSubject

class HotelReviewsViewModel(val context: Context, val lob: LineOfBusiness = LineOfBusiness.HOTELS) {

    val toolbarTitleObservable = BehaviorSubject.create<String>()
    val toolbarSubtitleObservable = BehaviorSubject.create<String>()
    val hotelReviewsObservable = BehaviorSubject.create<String>()

    var hotelObserver: Observer<HotelOffersResponse> = endlessObserver { hotel ->
        toolbarTitleObservable.onNext(hotel.hotelName)
        toolbarSubtitleObservable.onNext(Phrase.from(context, R.string.n_reviews_TEMPLATE)
                .put("review_count", HotelUtils.formattedReviewCount(hotel.totalReviews))
                .toString())
        hotelReviewsObservable.onNext(hotel.hotelId)
        if (lob == LineOfBusiness.PACKAGES) {
            PackagesTracking().trackHotelReviewPageLoad()
        }
        else {
            HotelTracking.trackHotelReviews()
        }
    }

}