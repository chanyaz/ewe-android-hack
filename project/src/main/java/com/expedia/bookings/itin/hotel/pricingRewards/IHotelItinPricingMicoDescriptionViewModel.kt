package com.expedia.bookings.itin.hotel.pricingRewards

import io.reactivex.subjects.PublishSubject

interface IHotelItinPricingMicoDescriptionViewModel {
    val micoContainerResetSubject: PublishSubject<Unit>
    val micoProductDescriptionSubject: PublishSubject<HotelItinMicoItem>
}
