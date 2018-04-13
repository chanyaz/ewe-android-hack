package com.expedia.vm.packages

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.utils.isDisplayBasicEconomyTooltipForPackagesEnabled
import com.expedia.vm.AbstractFlightOverviewViewModel
import io.reactivex.subjects.BehaviorSubject
import com.squareup.phrase.Phrase
import java.util.Arrays.asList

class PackageFlightOverviewViewModel(context: Context) : AbstractFlightOverviewViewModel(context) {

    init {
        selectedFlightLegSubject.subscribe { selectedFlight ->
            updateOBFees(selectedFlight)
        }
    }

    override val showBundlePriceSubject = BehaviorSubject.createDefault(true)
    override val showEarnMessage = BehaviorSubject.createDefault(false)

    override fun shouldShowSeatingClassAndBookingCode(): Boolean {
        return true
    }

    override fun shouldShowUrgencyMessaging(): Boolean {
        return false
    }

    override fun pricePerPersonString(selectedFlight: FlightLeg): String {
        return selectedFlight.packageOfferModel.price.differentialPriceFormatted
    }

    override fun shouldShowBasicEconomyMessage(selectedFlight: FlightLeg): Boolean {
        return selectedFlight.isBasicEconomy && isDisplayBasicEconomyTooltipForPackagesEnabled(context)
    }

    override fun shouldShowDeltaPositive(): Boolean {
        return true
    }

    //convert keys to values for packages
    override fun convertTooltipInfo(selectedFlight: FlightLeg): List<FlightLeg.BasicEconomyTooltipInfo> {
        val tooltipInfo = FlightLeg.BasicEconomyTooltipInfo()
        tooltipInfo.fareRulesTitle = getTooltipHeader(selectedFlight.carrierName)
        if (selectedFlight.basicEconomyRuleLocIds.isNotEmpty()) {
            tooltipInfo.fareRules = selectedFlight.basicEconomyRuleLocIds.map { getRuleFromKey(it) }.filter { it.isNotEmpty() }.toTypedArray()
        } else {
            tooltipInfo.fareRules = arrayOf<String>()
        }
        selectedFlight.basicEconomyTooltipInfo = asList(tooltipInfo)
        return selectedFlight.basicEconomyTooltipInfo
    }

    private fun getTooltipHeader(flightShortName: String): String {
        return Phrase.from(context, R.string.basic_economy_tooltip_header_TEMPLATE)
                .put("airlineshortname", flightShortName)
                .format().toString()
    }

    fun updateOBFees(selectedFlight: FlightLeg) {
        resetPaymentFeeViews()
        if ((selectedFlight.airlineMessageModel?.hasAirlineWithCCfee ?: false || selectedFlight.mayChargeObFees) && PointOfSale.getPointOfSale().showAirlinePaymentMethodFeeLegalMessage()) {
            val hasAirlineFeeLink = !selectedFlight.airlineMessageModel?.airlineFeeLink.isNullOrBlank()
            if (hasAirlineFeeLink) {
                val paymentFeeText = context.resources.getString(R.string.payment_and_baggage_fees_may_apply)
                chargesObFeesTextSubject.onNext(paymentFeeText)
                obFeeDetailsUrlObservable.onNext(e3EndpointUrl + selectedFlight.airlineMessageModel.airlineFeeLink)
            } else {
                val airlineFeeWarningText = context.resources.getString(R.string.airline_additional_fee_notice)
                airlineFeesWarningTextSubject.onNext(airlineFeeWarningText)
            }
        }
    }

    private fun getRuleFromKey(key: String): String {
        when (key) {
            "seatsAssignedAtCheckin" -> return context.getString(R.string.basic_economy_rule_1)
            "seatsAssignedAfterCheckin" -> return context.getString(R.string.basic_economy_rule_2)
            "changesNotPermitted" -> return context.getString(R.string.basic_economy_rule_3)
            "boardInLastGroup" -> return context.getString(R.string.basic_economy_rule_4)
            "noUpgrades" -> return context.getString(R.string.basic_economy_rule_5)
            "onePersonalItemNoOverheadAccess" -> return context.getString(R.string.basic_economy_rule_6)
            else -> return ""
        }
    }

    private fun resetPaymentFeeViews() {
        chargesObFeesTextSubject.onNext("")
        obFeeDetailsUrlObservable.onNext("")
        airlineFeesWarningTextSubject.onNext("")
    }
}
