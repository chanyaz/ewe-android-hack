package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.vm.hotel.HotelViewModel
import rx.subjects.BehaviorSubject

class PackageHotelViewModel(context: Context, hotel: Hotel) : HotelViewModel(context, hotel) {
    val unrealDealMessageObservable = BehaviorSubject.create(getUnrealDeal())
    val unrealDealMessageVisibilityObservable = BehaviorSubject.create<Boolean>(getUnrealDeal().isNotEmpty())

    val priceIncludesFlightsObservable = BehaviorSubject.create<Boolean>(hotel.isPackage)

    override fun hasMemberDeal(): Boolean {
        return false
    }

    private fun getUnrealDeal() : String {
        return hotel.packageOfferModel?.brandedDealData?.dealVariation ?: ""
    }
}