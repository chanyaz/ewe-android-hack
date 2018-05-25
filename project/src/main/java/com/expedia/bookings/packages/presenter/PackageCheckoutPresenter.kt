package com.expedia.bookings.packages.presenter

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.otto.Events
import com.expedia.bookings.presenter.flight.FlightTravelersPresenter
import com.expedia.bookings.tracking.PackagesTracking
import com.expedia.bookings.utils.CurrencyUtils
import com.expedia.bookings.utils.Ui
import com.expedia.util.Optional
import com.expedia.vm.BaseCreateTripViewModel
import com.expedia.bookings.packages.vm.PackageCheckoutViewModel
import com.expedia.bookings.packages.vm.PackageCreateTripViewModel
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.vm.traveler.FlightTravelersViewModel
import com.expedia.vm.traveler.TravelersViewModel
import com.squareup.otto.Subscribe

class PackageCheckoutPresenter(context: Context, attr: AttributeSet?) : BaseCheckoutPresenter(context, attr) {

    override fun trackCreateTripPriceChange(diffPercentage: Int) {
        PackagesTracking().trackCreateTripPriceChange(diffPercentage)
    }

    override fun trackCheckoutPriceChange(diffPercentage: Int) {
        PackagesTracking().trackCheckoutPriceChange(diffPercentage)
    }

    override fun handleCheckoutPriceChange(tripResponse: TripResponse) {
        onCreateTripResponse(tripResponse)
    }

    override fun onCreateTripResponse(tripResponse: TripResponse?) {
    }

    override fun getDefaultToTravelerTransition(): DefaultToTraveler {
        return DefaultToTraveler(FlightTravelersPresenter::class.java)
    }

    override fun injectComponents() {
        Ui.getApplication(context).packageComponent().inject(this)
    }

    override fun setupCreateTripViewModel(vm: BaseCreateTripViewModel) {
        vm as PackageCreateTripViewModel
        getCheckoutViewModel().checkoutPriceChangeObservable.map { Optional(it) }.subscribe(getCreateTripViewModel().createTripResponseObservable)
    }

    @Subscribe fun onUserLoggedIn(@Suppress("UNUSED_PARAMETER") event: Events.LoggedInSuccessful) {
        onLoginSuccess()
    }

    override fun getLineOfBusiness(): LineOfBusiness {
        return LineOfBusiness.PACKAGES
    }

    override fun updateDbTravelers() {
        val params = Db.sharedInstance.packageParams
        travelerManager.updateDbTravelers(params)
        resetTravelers()
    }

    override fun trackShowSlideToPurchase() {
        val flexStatus = if (!getCheckoutViewModel().cardFeeFlexStatus.value.isNullOrEmpty()) "${getCheckoutViewModel().cardFeeFlexStatus.value}" else ""
        PackagesTracking().trackCheckoutSlideToPurchase(getPaymentType(), flexStatus)
    }

    override fun makeCheckoutViewModel(): PackageCheckoutViewModel {
        return PackageCheckoutViewModel(context, Ui.getApplication(context).packageComponent().packageServices())
    }

    override fun makeCreateTripViewModel(): PackageCreateTripViewModel {
        return PackageCreateTripViewModel(Ui.getApplication(context).packageComponent().packageServices(), context)
    }

    override fun getCheckoutViewModel(): PackageCheckoutViewModel {
        return ckoViewModel as PackageCheckoutViewModel
    }

    override fun getCreateTripViewModel(): PackageCreateTripViewModel {
        return tripViewModel as PackageCreateTripViewModel
    }

    override fun showMainTravelerMinimumAgeMessaging(): Boolean {
        return true
    }

    override fun createTravelersViewModel(): TravelersViewModel {
        return FlightTravelersViewModel(context, getLineOfBusiness(), showMainTravelerMinimumAgeMessaging())
    }

    private fun getPaymentType(): PaymentType {
        val billingInfo = Db.getBillingInfo()
        val scc = billingInfo.storedCard
        val type: PaymentType? = if (scc != null) {
            scc.type
        } else {
            CurrencyUtils.detectCreditCardBrand(billingInfo.number, context)
        }

        return type ?: PaymentType.CARD_UNKNOWN
    }
}
