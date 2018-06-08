package com.expedia.bookings.itin.hotel.pricingRewards

import io.reactivex.subjects.PublishSubject

interface IHotelItinPricingBundleDescriptionViewModel {
    val bundleContainerResetSubject: PublishSubject<Unit>
    val bundleContainerViewVisibilitySubject: PublishSubject<Boolean>
    val bundleProductDescriptionSubject: PublishSubject<String>
}
