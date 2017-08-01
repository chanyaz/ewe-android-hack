package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.HotelUtils
import com.expedia.util.endlessObserver
import io.reactivex.Observer
import io.reactivex.subjects.BehaviorSubject

class HotelReviewsViewModel(val context: Context, val lob: LineOfBusiness = LineOfBusiness.HOTELS) {

    val toolbarTitleObservable = BehaviorSubject.create<String>()
    val toolbarSubtitleObservable = BehaviorSubject.create<String>()
    val hotelReviewsObservable = BehaviorSubject.create<String>()

    var hotelObserver: Observer<HotelOffersResponse> = endlessObserver { hotel ->
        toolbarTitleObservable.onNext(hotel.hotelName)
        toolbarSubtitleObservable.onNext(context.resources.getString(R.string.n_reviews_TEMPLATE, HotelUtils.formattedReviewCount(hotel.totalReviews)))
        hotelReviewsObservable.onNext(hotel.hotelId)
        if (lob == LineOfBusiness.PACKAGES) {
            PackagesTracking().trackHotelReviewPageLoad()
        }
        else {
            HotelTracking.trackHotelReviews()
        }
    }

}