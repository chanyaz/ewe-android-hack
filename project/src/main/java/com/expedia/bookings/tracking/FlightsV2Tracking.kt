package com.expedia.bookings.tracking

object FlightsV2Tracking {
    fun trackCheckoutConfirmationPageLoad() {
        OmnitureTracking.trackFlightCheckoutConfirmationPageLoad()
    }

    fun trackCheckoutInfoPageLoad() {
        OmnitureTracking.trackFlightCheckoutInfoPageLoad()
    }

    fun trackInsuranceAdd() {
        OmnitureTracking.trackFlightInsuranceAdd()
    }

    fun trackInsuranceBenefitsClick() {
        OmnitureTracking.trackFlightInsuranceBenefitsClick()
    }

    fun trackInsuranceError(message: String?) {
        OmnitureTracking.trackFlightInsuranceError(message)
    }

    fun trackInsuranceRemove() {
        OmnitureTracking.trackFlightInsuranceRemove()
    }

    fun trackInsuranceTermsClick() {
        OmnitureTracking.trackFlightInsuranceTermsClick()
    }

    fun trackSearchPageLoad() {
        OmnitureTracking.trackPageLoadFlightSearchV2()
    }

    fun trackTravelerPickerClick(actionLabel: String) {
        OmnitureTracking.trackFlightTravelerPickerClick(actionLabel)
    }

    fun trackFlightBaggageFeeClick() {
        OmnitureTracking.trackFlightBaggageFeesClick()
    }

    fun trackPaymentFeesClick() {
        OmnitureTracking.trackFlightPaymentFeesClick()
    }
}
