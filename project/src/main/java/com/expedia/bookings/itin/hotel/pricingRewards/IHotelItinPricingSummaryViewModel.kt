package com.expedia.bookings.itin.hotel.pricingRewards

import io.reactivex.subjects.PublishSubject

interface IHotelItinPricingSummaryViewModel {
    val clearPriceSummaryContainerSubject: PublishSubject<Unit>
    val roomPriceBreakdownSubject: PublishSubject<List<HotelItinRoomPrices>>
    val priceLineItemSubject: PublishSubject<HotelItinPriceLineItem>
}
