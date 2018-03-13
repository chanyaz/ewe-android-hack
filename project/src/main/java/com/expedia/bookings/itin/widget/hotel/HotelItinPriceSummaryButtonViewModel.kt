package com.expedia.bookings.itin.widget.hotel

import com.expedia.bookings.R
import com.expedia.bookings.itin.scopes.HasHotel
import com.expedia.bookings.itin.scopes.HasItin
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasWebViewLauncher
import com.expedia.bookings.itin.tripstore.data.PaymentModel
import com.expedia.bookings.itin.widget.common.ItinBookingInfoCardViewModel
import com.expedia.bookings.tracking.TripsTracking

class HotelItinPriceSummaryButtonViewModel<S>(scope: S) : ItinBookingInfoCardViewModel where S : HasItin, S : HasHotel, S : HasStringProvider, S : HasWebViewLauncher {

    override val iconImage = R.drawable.ic_itin_credit_card_icon
    override val headingText: String
    override val subheadingText: String?
    override val cardClickListener: () -> Unit

    init {
        val hotel = scope.hotel

        headingText = scope.strings.fetch(R.string.itin_hotel_details_price_summary_rewards_heading)

        val paymentModel = hotel.paymentModel
        val formattedPrice = hotel.totalPriceDetails?.totalFormatted
        if (paymentModel != null && formattedPrice != null) {
            when (paymentModel) {
                PaymentModel.EXPEDIA_COLLECT -> {
                    subheadingText = scope.strings.fetch(R.string.itin_hotel_details_price_summary_pay_now_TEMPLATE, mapOf("amount" to formattedPrice))
                }
                PaymentModel.HOTEL_COLLECT -> {
                    subheadingText = scope.strings.fetch(R.string.itin_hotel_details_price_summary_pay_later_TEMPLATE, mapOf("amount" to formattedPrice))
                }
            }
        } else {
            subheadingText = null
        }

        val detailsUrl = scope.itin.webDetailsURL
        val tripNumber = scope.itin.tripNumber
        cardClickListener = {
            if (detailsUrl != null && tripNumber != null) {
                scope.webViewLauncher.launchWebViewActivity(R.string.itin_hotel_details_price_summary_heading, detailsUrl, "price-header", tripNumber)
                TripsTracking.trackHotelItinPricingRewardsClick()
            }
        }
    }
}
