package com.expedia.bookings.presenter.shared

import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject


class StoredCouponWidgetViewModel {

    val storedCouponsSubject = PublishSubject.create<List<StoredCouponAdapter>>()
    val enableStoredCouponsSubject = PublishSubject.create<Boolean>()
    val hasStoredCoupons = PublishSubject.create<Boolean>()
}

data class StoredCouponAdapter(val savedCoupon: HotelCreateTripResponse.SavedCoupon, var savedCouponStatus: StoredCouponAppliedStatus)

enum class StoredCouponAppliedStatus {
    DEFAULT,
    SUCCESS,
    FAILURE
}
