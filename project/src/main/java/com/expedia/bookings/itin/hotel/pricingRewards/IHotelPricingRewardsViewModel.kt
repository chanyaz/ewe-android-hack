package com.expedia.bookings.itin.hotel.pricingRewards

import io.reactivex.subjects.PublishSubject

interface IHotelPricingRewardsViewModel {
    val hideWidgetSubject: PublishSubject<Unit>
    val rewardsButtonClickSubject: PublishSubject<Unit>

    val logoSubject: PublishSubject<String>
    val earnedPointsSubject: PublishSubject<String>
    val basePointsSubject: PublishSubject<String>
    val bonusPointsSubject: PublishSubject<List<String>>
}
