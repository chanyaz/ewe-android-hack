package com.expedia.vm


import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.hotels.HotelApplyCouponParams
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.services.HotelServices
import com.expedia.util.endlessObserver
import com.mobiata.android.Log
import rx.Observer
import rx.subjects.PublishSubject

class HotelCouponViewModel(val context: Context, val hotelServices: HotelServices?) {

    val applyObservable = PublishSubject.create<String>()
    val couponObservable = PublishSubject.create<HotelCreateTripResponse>()

    val doneButtonObservable: Observer<String> = endlessObserver {
        val trip = Db.getTripBucket().getHotelV2()
        applyObservable.onNext(it)
        hotelServices?.applyCoupon(HotelApplyCouponParams(trip.mHotelTripResponse.tripId, it), couponListener)
    }

    val couponListener: Observer<HotelCreateTripResponse> = object : Observer<HotelCreateTripResponse> {
        override fun onNext(trip: HotelCreateTripResponse) {
            couponObservable.onNext(trip)
        }

        override fun onCompleted() {
            Log.d("Hotel Coupon Completed")
        }

        override fun onError(e: Throwable?) {
            Log.d("Hotel Coupon Error", e)
        }
    }
}
