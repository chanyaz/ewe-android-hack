package com.expedia.vm

import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import rx.subjects.PublishSubject

class StoredCouponViewHolderViewModel {
    val couponName = PublishSubject.create<String>()
}
