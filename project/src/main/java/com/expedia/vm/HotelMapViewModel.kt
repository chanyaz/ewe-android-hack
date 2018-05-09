package com.expedia.vm

import android.content.Context
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.utils.HotelsV2DataUtil
import com.expedia.util.endlessObserver
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.subjects.BehaviorSubject

val ROOMS_LEFT_CUTOFF = 5

class HotelMapViewModel(val context: Context, val selectARoomObserver: Observer<Unit>, val hotelSoldOut: Observable<Boolean>, val lob: LineOfBusiness) {
    //Outputs for View
    val hotelName = BehaviorSubject.create<String>()
    val hotelStarRating = BehaviorSubject.create<Float>()
    val hotelStarRatingVisibility = BehaviorSubject.create<Boolean>()
    val hotelStarRatingContentDescription = BehaviorSubject.create<String>()
    val hotelLatLng = BehaviorSubject.create<DoubleArray>()
    val selectARoomInvisibility = BehaviorSubject.createDefault<Boolean>(false)
    val roomResponseObservable = BehaviorSubject.create<HotelOffersResponse>()

    //Setup the data I need to behave as a View Model for my View
    val offersObserver = endlessObserver<HotelOffersResponse> { response ->
        hotelName.onNext(response.hotelName ?: "")
        hotelStarRating.onNext(response.hotelStarRating.toFloat())
        hotelStarRatingContentDescription.onNext(HotelsV2DataUtil.getHotelRatingContentDescription(context, response.hotelStarRating))
        hotelStarRatingVisibility.onNext(response.hotelStarRating > 0)
        hotelLatLng.onNext(doubleArrayOf(response.latitude, response.longitude))

        roomResponseObservable.onNext(response)
    }

    init {
        hotelSoldOut.subscribe(selectARoomInvisibility)
    }
}
