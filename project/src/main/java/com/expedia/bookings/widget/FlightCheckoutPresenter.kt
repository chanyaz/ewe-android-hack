package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.otto.Events
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.utils.Ui
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.FlightCheckoutViewModel
import com.expedia.vm.FlightCostSummaryBreakdownViewModel
import com.expedia.vm.flights.FlightCreateTripViewModel
import com.expedia.vm.packages.BaseCreateTripViewModel
import com.squareup.otto.Subscribe

class FlightCheckoutPresenter(context: Context, attr: AttributeSet) : BaseCheckoutPresenter(context, attr) {

    init {
        getCheckoutViewModel().cardFeeTextSubject.subscribeTextAndVisibility(cardProcessingFeeTextView)
        getCheckoutViewModel().cardFeeWarningTextSubject.subscribeTextAndVisibility(cardFeeWarningTextView)
    }

    override fun setupCreateTripViewModel(vm : BaseCreateTripViewModel) {
        vm as FlightCreateTripViewModel
        vm.tripParams.subscribe {
            createTripDialog.show()
            userAccountRefresher.ensureAccountIsRefreshed()
        }

        vm.tripResponseObservable.subscribe { response -> response as FlightCreateTripResponse
            // TODO - cache trip response and update total fare details when card is input

            loginWidget.updateRewardsText(getLineOfBusiness())
            createTripDialog.hide()
            priceChangeWidget.viewmodel.originalPrice.onNext(response.tripTotalPayableIncludingFeeIfZeroPayableByPoints())
            priceChangeWidget.viewmodel.newPrice.onNext(response.tripTotalPayableIncludingFeeIfZeroPayableByPoints())
            totalPriceWidget.viewModel.total.onNext(response.tripTotalPayableIncludingFeeIfZeroPayableByPoints())
            totalPriceWidget.viewModel.costBreakdownEnabledObservable.onNext(true)
            (totalPriceWidget.packagebreakdown.viewmodel as FlightCostSummaryBreakdownViewModel).flightCostSummaryObservable.onNext(response.details)

        }
        vm.tripResponseObservable.map { it.validFormsOfPayment }
                                 .subscribe(getCheckoutViewModel().validFormsOfPaymentSubject)
    }

    @Subscribe fun onUserLoggedIn( @Suppress("UNUSED_PARAMETER") event: Events.LoggedInSuccessful) {
        onLoginSuccess()
    }

    override fun getLineOfBusiness() : LineOfBusiness {
        return LineOfBusiness.FLIGHTS_V2
    }

    override fun updateDbTravelers() {
        val params = Db.getFlightSearchParams()
        travelerPresenter.viewModel.updateDbTravelers(params)
    }

    override fun updateTravelerPresenter() {
        travelerPresenter.visibility = View.VISIBLE
    }

    override fun trackShowSlideToPurchase() {
    }

    override fun trackShowBundleOverview() {
    }

    override fun makeCheckoutViewModel(): FlightCheckoutViewModel {
        return FlightCheckoutViewModel(context, getFlightServices(), paymentWidgetViewModel.cardTypeSubject)
    }

    override fun makeCreateTripViewModel(): FlightCreateTripViewModel {
        return FlightCreateTripViewModel(getFlightServices(), getCheckoutViewModel().cardFeeForSelectedCard)
    }

    override fun getCheckoutViewModel(): FlightCheckoutViewModel {
        return ckoViewModel as FlightCheckoutViewModel
    }

    override fun getCreateTripViewModel(): FlightCreateTripViewModel {
        return tripViewModel as FlightCreateTripViewModel
    }

    private fun getFlightServices(): FlightServices {
        return Ui.getApplication(context).flightComponent().flightServices()
    }
}
