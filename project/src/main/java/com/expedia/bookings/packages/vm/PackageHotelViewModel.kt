package com.expedia.bookings.packages.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.hotel.vm.HotelViewModel
import com.expedia.bookings.utils.SpannableBuilder
import com.expedia.bookings.utils.isPackagesHSRPriceDisplayEnabled
import com.squareup.phrase.Phrase
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class PackageHotelViewModel(var context: Context) : HotelViewModel(context) {
    val unrealDealMessageObservable = BehaviorSubject.create<String>()
    val unrealDealMessageVisibilityObservable = BehaviorSubject.create<Boolean>()
    val shouldDisplayPricingViews = PublishSubject.create<Boolean>()

    override fun bindHotelData(hotel: Hotel) {
        super.bindHotelData(hotel)

        val unrealDeal = getUnrealDeal()
        unrealDealMessageObservable.onNext(unrealDeal)
        unrealDealMessageVisibilityObservable.onNext(unrealDeal.isNotEmpty())
        shouldDisplayPricingViews.onNext(isPackagesHSRPriceDisplayEnabled(context))
    }

    override fun getHotelContentDesc(): CharSequence {
        val result = SpannableBuilder()
        if (unrealDealMessageVisibilityObservable.value) {
            result.append(Phrase.from(context, R.string.hotel_unreal_deal_cont_desc_TEMPLATE)
                    .put("unrealdeal", getUnrealDeal().replace("\\p{P}", ""))
                    .format()
                    .toString())
        }
        val packagesHotelDescription = if (hotel.hotelGuestRating <= 0f) R.string.packages_hotel_details_cont_desc_zero_guestrating_TEMPLATE else R.string.packages_hotel_details_cont_desc_TEMPLATE
        result.append(Phrase.from(context, packagesHotelDescription)
                .put("hotel", hotelName)
                .put("starrating", hotelStarRating.toString())
                .putOptional("guestrating", hotelGuestRating.toString())
                .put("price", pricePerNight)
                .format()
                .toString())

        if (hotelStrikeThroughPriceFormatted != null) {
            result.append(Phrase.from(context, R.string.hotel_price_cont_desc_TEMPLATE)
                    .put("strikethroughprice", hotelStrikeThroughPriceFormatted)
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
