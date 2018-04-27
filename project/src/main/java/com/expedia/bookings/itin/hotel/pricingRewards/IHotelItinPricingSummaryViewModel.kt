package com.expedia.bookings.itin.hotel.pricingRewards

import io.reactivex.subjects.PublishSubject

interface IHotelItinPricingSummaryViewModel {
    val roomPriceBreakdownSubject: PublishSubject<List<HotelItinRoomPrices>>
    val multipleGuestItemSubject: PublishSubject<HotelItinPriceLineItem>
    val taxesAndFeesItemSubject: PublishSubject<HotelItinPriceLineItem>
    val couponsItemSubject: PublishSubject<HotelItinPriceLineItem>
    val pointsItemSubject: PublishSubject<HotelItinPriceLineItem>
}
