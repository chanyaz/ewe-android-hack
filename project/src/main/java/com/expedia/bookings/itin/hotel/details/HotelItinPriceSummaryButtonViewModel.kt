package com.expedia.bookings.itin.hotel.details

import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.itin.common.ItinBookingInfoCardViewModel
import com.expedia.bookings.itin.hotel.pricingRewards.HotelItinPricingRewardsActivity
import com.expedia.bookings.itin.scopes.HasAbacusProvider
import com.expedia.bookings.itin.scopes.HasActivityLauncher
import com.expedia.bookings.itin.scopes.HasHotel
import com.expedia.bookings.itin.scopes.HasItin
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.tripstore.data.PaymentModel
import com.expedia.bookings.itin.tripstore.extensions.packagePrice

class HotelItinPriceSummaryButtonViewModel<S>(scope: S) : ItinBookingInfoCardViewModel where S : HasItin, S : HasHotel, S : HasStringProvider, S : HasWebViewLauncher, S : HasTripsTracking, S : HasActivityLauncher, S : HasAbacusProvider {

    override val iconImage = R.drawable.ic_itin_credit_card_icon
    override val headingText: String
    override val subheadingText: String?
    override val cardClickListener: () -> Unit

    init {
        val hotel = scope.hotel

        headingText = scope.strings.fetch(R.string.itin_hotel_details_price_summary_rewards_heading)

        val paymentModel = hotel.paymentModel
        var formattedPrice = hotel.totalPriceDetails?.totalFormatted
        if (paymentModel != null && formattedPrice != null) {
            when (paymentModel) {
                PaymentModel.EXPEDIA_COLLECT -> {
                    subheadingText = scope.strings.fetchWithPhrase(R.string.itin_hotel_details_price_summary_pay_now_TEMPLATE, mapOf("amount" to formattedPrice))
                }
                PaymentModel.HOTEL_COLLECT -> {
                    subheadingText = scope.strings.fetchWithPhrase(R.string.itin_hotel_details_price_summary_pay_later_TEMPLATE, mapOf("amount" to formattedPrice))
                }
            }
        } else if (!scope.itin.packagePrice().isNullOrEmpty()) {
            formattedPrice = scope.itin.packagePrice()!!
            subheadingText = scope.strings.fetchWithPhrase(R.string.itin_hotel_details_price_summary_pay_now_TEMPLATE, mapOf("amount" to formattedPrice))
        } else {
            subheadingText = null
        }

        val detailsUrl = scope.itin.webDetailsURL
        val tripNumber = scope.itin.tripNumber
        val isGuest: Boolean = scope.itin.isGuest
        val tripId = scope.itin.tripId
        cardClickListener = {
            if (scope.abacus.isBucketedForTest(AbacusUtils.EBAndroidAppTripsHotelPricing) && tripId != null) {
                scope.activityLauncher.launchActivity(HotelItinPricingRewardsActivity, tripId)
            } else if (detailsUrl != null && tripNumber != null) {
                scope.webViewLauncher.launchWebViewActivity(R.string.itin_hotel_details_price_summary_heading, detailsUrl, "price-header", tripNumber, isGuest = isGuest)
            }
            scope.tripsTracking.trackHotelItinPricingRewardsClick()
        }
    }
}
