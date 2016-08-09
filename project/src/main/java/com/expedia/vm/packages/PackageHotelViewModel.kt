package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.vm.hotel.HotelViewModel
import com.squareup.phrase.Phrase
import rx.subjects.BehaviorSubject

class PackageHotelViewModel(var context: Context, hotel: Hotel) : HotelViewModel(context, hotel) {
    val unrealDealMessageObservable = BehaviorSubject.create(getUnrealDeal())
    val unrealDealMessageVisibilityObservable = BehaviorSubject.create<Boolean>(getUnrealDeal().isNotEmpty())

    val priceIncludesFlightsObservable = BehaviorSubject.create<Boolean>(hotel.isPackage)
    var contentDescription = getHotelContentDesc()

    private fun getHotelContentDesc(): CharSequence {
        var result = SpannableBuilder()
        if (unrealDealMessageVisibilityObservable.value) {
            result.append(Phrase.from(context, R.string.hotel_unreal_deal_cont_desc_TEMPLATE)
                    .put("unrealdeal", getUnrealDeal().replace("\\p{P}", ""))
                    .format()
                    .toString())
        }
        result.append(Phrase.from(context, R.string.hotel_details_cont_desc_TEMPLATE)
                .put("hotel", hotel.localizedName)
                .put("starrating", hotelStarRatingObservable.value.toString())
                .put("guestrating", hotelGuestRatingObservable.value.toString())
                .put("price", pricePerNightObservable.value)
                .format()
                .toString())

        if (hotelStrikeThroughPriceVisibility.value) {
            result.append(Phrase.from(context, R.string.hotel_price_cont_desc_TEMPLATE)
                    .put("strikethroughprice", hotelStrikeThroughPriceFormatted.value)
                    .format()
                    .toString())
        }
        result.append(Phrase.from(context.resources.getString(R.string.accessibility_cont_desc_role_button)).format().toString())

        return result.build()
    }

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