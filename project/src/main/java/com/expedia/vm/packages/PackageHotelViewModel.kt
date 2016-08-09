package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.vm.hotel.HotelViewModel
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject

class PackageHotelViewModel(context: Context, hotel: Hotel) : HotelViewModel(context, hotel) {
    val unrealDealMessageObservable = BehaviorSubject.create(getUnrealDeal())
    val unrealDealMessageVisibilityObservable = BehaviorSubject.create<Boolean>(getUnrealDeal().isNotEmpty())

    val priceIncludesFlightsObservable = BehaviorSubject.create<Boolean>(hotel.isPackage)

    override fun hasMemberDeal(): Boolean {
        return false
    }

    private fun getUnrealDeal(): String {
        if (hotel.packageOfferModel?.featuredDeal ?: false) {
            if (PointOfSale.getPointOfSale().shouldShowFreeUnrealDeal()) {
                val dealVariation = hotel.packageOfferModel?.brandedDealData?.dealVariation ?: ""
                return when (dealVariation) {
                    PackageOfferModel.DealVariation.FreeHotel -> resources.getString(R.string.free_hotel_deal)
                    PackageOfferModel.DealVariation.FreeFlight -> resources.getString(R.string.free_flight_deal)
                    PackageOfferModel.DealVariation.HotelDeal -> getHotelDealMessage()
                    PackageOfferModel.DealVariation.FreeOneNightHotel -> getFreeNightHotelMessage()
                    else -> ""
                }
            } else {
                return getHotelDealMessage()
            }
        }
        return ""
    }

    private fun getHotelDealMessage(): String {
        return Phrase.from(resources.getString(R.string.hotel_deal_TEMPLATE))
                .put("price", hotel.packageOfferModel?.brandedDealData?.savingsAmount)
                .put("savings", hotel.packageOfferModel?.brandedDealData?.savingPercentageOverPackagePrice)
                .format().toString()
    }

    private fun getFreeNightHotelMessage(): String {
        val numberOfNights = hotel.packageOfferModel?.brandedDealData?.freeNights
        return Phrase.from(resources.getQuantityString(R.plurals.free_one_night_hotel_deal_TEMPLATE, numberOfNights?.toInt() ?: 1))
                .put("night", numberOfNights)
                .format().toString()
    }
}