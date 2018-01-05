package com.expedia.bookings.presenter.shared

import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject


class StoredCouponWidgetViewModel {

    val storedCouponsSubject = PublishSubject.create<List<HotelCreateTripResponse.SavedCoupon>>()

    val hasStoredCoupons = PublishSubject.create<Boolean>()
}
