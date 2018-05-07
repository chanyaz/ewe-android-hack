package com.expedia.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.tracking.hotel.HotelTracking
import com.expedia.bookings.utils.HotelUtils
import com.expedia.util.endlessObserver
import io.reactivex.Observer
import io.reactivex.subjects.BehaviorSubject

class HotelReviewsViewModel(val context: Context, val lob: LineOfBusiness = LineOfBusiness.HOTELS) {

    val toolbarTitleObservable = BehaviorSubject.create<String>()
    val toolbarSubtitleObservable = BehaviorSubject.create<String>()
    val hotelIdObservable = BehaviorSubject.create<String>()
    val hotelOfferObservable = BehaviorSubject.create<HotelOffersResponse>()
    val soldOutObservable = BehaviorSubject.create<Boolean>()
    val scrollToRoomListener = BehaviorSubject.create<Unit>()

    var hotelOfferObserver: Observer<HotelOffersResponse> = endlessObserver { offer ->
        toolbarTitleObservable.onNext(offer.hotelName)
        toolbarSubtitleObservable.onNext(context.resources.getString(R.string.n_reviews_TEMPLATE, HotelUtils.formattedReviewCount(offer.totalReviews)))
        hotelIdObservable.onNext(offer.hotelId)
        hotelOfferObservable.onNext(offer)
    }

    private var pageLoadTracked = false

    fun trackReviewPageLoad() {
        if (!pageLoadTracked) {
            if (lob == LineOfBusiness.PACKAGES) {
                PackagesTracking().trackHotelReviewPageLoad()
            } else {
                HotelTracking.trackHotelReviews()
            }
            pageLoadTracked = true
        }
    }

    fun resetTracking() {
        pageLoadTracked = false
    }

    fun setTrackPageLoad(track: Boolean) {
        pageLoadTracked = !track
    }
}
