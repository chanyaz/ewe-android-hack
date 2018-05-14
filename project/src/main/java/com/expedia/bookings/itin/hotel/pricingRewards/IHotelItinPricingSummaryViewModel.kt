package com.expedia.bookings.itin.hotel.pricingRewards

import io.reactivex.subjects.PublishSubject

interface IHotelItinPricingSummaryViewModel {
    val roomContainerClearSubject: PublishSubject<Unit>
    val roomContainerItemSubject: PublishSubject<HotelItinPriceLineItem>
    val multipleGuestItemSubject: PublishSubject<HotelItinPriceLineItem>
    val taxesAndFeesItemSubject: PublishSubject<HotelItinPriceLineItem>
    val couponsItemSubject: PublishSubject<HotelItinPriceLineItem>
    val pointsItemSubject: PublishSubject<HotelItinPriceLineItem>
    val totalPriceItemSubject: PublishSubject<HotelItinPriceLineItem>
    val totalPriceInPosCurrencyItemSubject: PublishSubject<HotelItinPriceLineItem>
    val currencyDisclaimerSubject: PublishSubject<String>
    val additionalPricingInfoSubject: PublishSubject<Unit>
}
