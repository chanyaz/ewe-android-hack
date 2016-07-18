package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.otto.Events
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.util.subscribeVisibility
import com.expedia.vm.BaseCreateTripViewModel
import com.expedia.vm.FlightCheckoutViewModel
import com.expedia.vm.flights.FlightCostSummaryBreakdownViewModel
import com.expedia.vm.flights.FlightCreateTripViewModel
import com.squareup.otto.Subscribe
import rx.Observable

class FlightCheckoutPresenter(context: Context, attr: AttributeSet) : BaseCheckoutPresenter(context, attr) {

    val debitCardsNotAcceptedTextView: TextView by bindView(R.id.flights_debit_cards_not_accepted)

    init {
        getCheckoutViewModel().cardFeeTextSubject.subscribeTextAndVisibility(cardProcessingFeeTextView)
        getCheckoutViewModel().cardFeeWarningTextSubject.subscribeTextAndVisibility(cardFeeWarningTextView)
        setupDontShowDebitCardVisibility()

        getCheckoutViewModel().priceChangeObservable.subscribe { it as FlightCheckoutResponse
            handlePriceChange(it)
        }
        getCreateTripViewModel().priceChangeObservable.subscribe { it as FlightCreateTripResponse
            handlePriceChange(it)
        }
    }

    override fun setupCreateTripViewModel(vm : BaseCreateTripViewModel) {
        vm as FlightCreateTripViewModel

        insuranceWidget.viewModel.updatedTripObservable.subscribe(vm.tripResponseObservable)

        vm.insuranceAvailabilityObservable.subscribeVisibility(insuranceWidget)

        vm.tripParams.subscribe {
            createTripDialog.show()
            userAccountRefresher.ensureAccountIsRefreshed()
        }

        vm.tripResponseObservable.subscribe { response -> response as FlightCreateTripResponse
            loginWidget.updateRewardsText(getLineOfBusiness())
            createTripDialog.hide()
            insuranceWidget.viewModel.newTripObservable.onNext(response.newTrip)
            insuranceWidget.viewModel.productObservable.onNext(response.availableInsuranceProducts)
            totalPriceWidget.viewModel.total.onNext(response.tripTotalPayableIncludingFeeIfZeroPayableByPoints())
            totalPriceWidget.viewModel.costBreakdownEnabledObservable.onNext(true)
            (totalPriceWidget.breakdown.viewmodel as FlightCostSummaryBreakdownViewModel).flightCostSummaryObservable.onNext(response)
        }

        vm.tripResponseObservable.map { it.validFormsOfPayment }
                                 .subscribe(getCheckoutViewModel().validFormsOfPaymentSubject)
    }

    private fun handlePriceChange(tripResponse: FlightCreateTripResponse) {
        val flightTripDetails = tripResponse.details
        // TODO - we may have to change from totalFarePrice -> totalPrice in order to support SubPub fares
        val originalPrice = flightTripDetails.oldOffer.totalFarePrice
        val newPrice = flightTripDetails.offer.totalFarePrice
        priceChangeWidget.viewmodel.originalPrice.onNext(originalPrice)
        priceChangeWidget.viewmodel.newPrice.onNext(newPrice)

        // TODO - update to totalPrice when checkout response starts returning totalPrice (required for SubPub fare support)
        totalPriceWidget.viewModel.total.onNext(flightTripDetails.offer.totalFarePrice)
        totalPriceWidget.viewModel.costBreakdownEnabledObservable.onNext(true)
        (totalPriceWidget.breakdown.viewmodel as FlightCostSummaryBreakdownViewModel).flightCostSummaryObservable.onNext(tripResponse)
    }

    @Subscribe fun onUserLoggedIn( @Suppress("UNUSED_PARAMETER") event: Events.LoggedInSuccessful) {
        onLoginSuccess()
    }

    override fun getLineOfBusiness() : LineOfBusiness {
        return LineOfBusiness.FLIGHTS_V2
    }

    override fun updateDbTravelers() {
        val params = Db.getFlightSearchParams()
        travelerManager.updateDbTravelers(params, context)
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

    private fun setupDontShowDebitCardVisibility() {
        Observable.combineLatest(getCheckoutViewModel().showDebitCardsNotAcceptedSubject,
                showingPaymentWidgetSubject,
                { showDebitCards, showingPaymentWidget ->
                    debitCardsNotAcceptedTextView.visibility = if (showDebitCards && showingPaymentWidget) VISIBLE else GONE
                }).subscribe()
    }
}
