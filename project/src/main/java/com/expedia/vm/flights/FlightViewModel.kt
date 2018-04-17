package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.RouteHappyUtils
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.isRouteHappyEnabled
import com.expedia.bookings.utils.isRouteHappyShowAmenityEnabled
import com.expedia.bookings.utils.isRouteHappyShowFlightScoreEnabled
import com.expedia.vm.AbstractFlightViewModel
import com.squareup.phrase.Phrase

open class FlightViewModel(context: Context, flightLeg: FlightLeg, val isOutboundSearch: Boolean = true) : AbstractFlightViewModel(context, flightLeg) {

    init {
        if (isRouteHappyEnabled(context)) {
            setRichContentVisibility()
        }
    }

    override fun price(): String {
        val priceToShow = StringBuilder()
        var price = flightLeg.packageOfferModel.price.averageTotalPricePerTicket
        if (!isOutboundSearch) {
            if (flightLeg.packageOfferModel.price.deltaPositive) {
                priceToShow.append("+ ")
            }
            price = flightLeg.packageOfferModel.price.deltaPrice
        }
        return priceToShow.append(Money.getFormattedMoneyFromAmountAndCurrencyCode(price.roundedAmount, price.currencyCode, Money.F_NO_DECIMAL)).toString()
    }

    override fun getUrgencyMessageVisibility(seatsLeft: String): Boolean {
        return Strings.isNotEmpty(seatsLeft)
    }

    override fun getFlightCabinPreferenceVisibility(): Boolean {
        return Strings.isNotEmpty(updateflightCabinPreferenceObservable.value)
    }

    override fun isEarnMessageVisible(earnMessage: String): Boolean {
        return Strings.isNotEmpty(earnMessage) && PointOfSale.getPointOfSale().isEarnMessageEnabledForFlights
    }

    override fun getFlightDetailCardContDescriptionStringID(): Int {
        return R.string.flight_detail_card_cont_desc_without_price_diff_TEMPLATE
    }

    private fun setRichContentVisibility() {
        val legRichContent = flightLeg.richContent
        if (legRichContent != null) {
            if (isRouteHappyShowAmenityEnabled()) {
                val legAmenities = legRichContent.legAmenities
                if (legAmenities != null) {
                    richContentWifiViewVisibility = legAmenities.wifi
                    richContentEntertainmentViewVisibility = legAmenities.entertainment
                    richContentPowerViewVisibility = legAmenities.power
                    richContentDividerViewVisibility = (richContentWifiViewVisibility || richContentEntertainmentViewVisibility || richContentPowerViewVisibility)
                }
            }
            if (isRouteHappyShowFlightScoreEnabled()) {
                routeScoreText = Phrase.from(context, RouteHappyUtils.ScoreExpression.valueOf(legRichContent.scoreExpression).stringResId)
                        .put("flight_score", legRichContent.score.toString())
                        .format().toString()
                routeScoreViewVisibility = true
            }
        }
    }
}
