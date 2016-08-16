package com.expedia.bookings.presenter.flight

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.TripResponse
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.otto.Events
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.tracking.FlightsV2Tracking
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeText
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.BaseCreateTripViewModel
import com.expedia.vm.FlightCheckoutViewModel
import com.expedia.vm.flights.FlightCostSummaryBreakdownViewModel
import com.expedia.vm.flights.FlightCreateTripViewModel
import com.squareup.otto.Subscribe
import rx.Observable

class FlightCheckoutPresenter(context: Context, attr: AttributeSet) : BaseCheckoutPresenter(context, attr) {

    val debitCardsNotAcceptedTextView: TextView by bindView(R.id.flights_debit_cards_not_accepted)
    var cardType: PaymentType? = null

    init {
        getCheckoutViewModel().cardFeeTextSubject.subscribeText(cardProcessingFeeTextView)
        Observable.combineLatest( getCheckoutViewModel().paymentTypeSelectedHasCardFee,
                                    paymentWidget.viewmodel.showingPaymentForm,
                                    { haveCardFee, showingGuestPaymentForm ->
                                        val cardFeeVisibility = if (haveCardFee && showingGuestPaymentForm) View.VISIBLE else View.GONE
                                        cardProcessingFeeTextView.visibility = cardFeeVisibility
                                        if (cardFeeVisibility == VISIBLE) { // only show. hide handled in BaseCheckoutPresenter
                                            toolbarDropShadow.visibility = visibility
                                        }
                                    }).subscribe()

        getCheckoutViewModel().cardFeeWarningTextSubject.subscribeTextAndVisibility(cardFeeWarningTextView)
        setupDontShowDebitCardVisibility()

        getCheckoutViewModel().priceChangeObservable.subscribe { it as FlightCheckoutResponse
            handlePriceChange(it)
        }
        getCreateTripViewModel().priceChangeObservable.subscribe { it as FlightCreateTripResponse
            handlePriceChange(it)
        }

        getCheckoutViewModel().receivedCheckoutResponse.subscribe {
            checkoutDialog.hide()
        }

        paymentWidgetViewModel.cardTypeSubject.subscribe { paymentType ->
            cardType = paymentType
        }

    }

    override fun setupCreateTripViewModel(vm : BaseCreateTripViewModel) {
        vm as FlightCreateTripViewModel

        insuranceWidget.viewModel.updatedTripObservable.subscribe(vm.tripResponseObservable)

        vm.tripParams.subscribe {
            userAccountRefresher.ensureAccountIsRefreshed()
        }

        vm.tripResponseObservable.subscribe { response -> response as FlightCreateTripResponse
            loginWidget.updateRewardsText(getLineOfBusiness())
            insuranceWidget.viewModel.tripObservable.onNext(response)
            totalPriceWidget.viewModel.total.onNext(response.tripTotalPayableIncludingFeeIfZeroPayableByPoints())
            totalPriceWidget.viewModel.costBreakdownEnabledObservable.onNext(true)
            (totalPriceWidget.breakdown.viewmodel as FlightCostSummaryBreakdownViewModel).flightCostSummaryObservable.onNext(response)
            isPassportRequired(response)
            trackShowBundleOverview()
        }

        vm.tripResponseObservable.map { it.validFormsOfPayment }
                                 .subscribe(getCheckoutViewModel().validFormsOfPaymentSubject)
    }

    private fun handlePriceChange(tripResponse: FlightCreateTripResponse) {
        val flightTripDetails = tripResponse.details

        // TODO - we may have to change from totalFarePrice -> totalPrice in order to support SubPub fares
        if (flightTripDetails.oldOffer != null) {
            val originalPrice = flightTripDetails.oldOffer.totalFarePrice
            val newPrice = flightTripDetails.offer.totalFarePrice
            priceChangeWidget.viewmodel.originalPrice.onNext(originalPrice)
            priceChangeWidget.viewmodel.newPrice.onNext(newPrice)
        }

        // TODO - update to totalPrice when checkout response starts returning totalPrice (required for SubPub fare support)
        totalPriceWidget.viewModel.total.onNext(flightTripDetails.offer.totalFarePrice)
        totalPriceWidget.viewModel.costBreakdownEnabledObservable.onNext(true)
        (totalPriceWidget.breakdown.viewmodel as FlightCostSummaryBreakdownViewModel).flightCostSummaryObservable.onNext(tripResponse)
    }

    @Subscribe fun onUserLoggedIn( @Suppress("UNUSED_PARAMETER") event: Events.LoggedInSuccessful) {
        onLoginSuccess()
    }

    override fun isPassportRequired(response: TripResponse) {
        val flightOffer = (response as FlightCreateTripResponse).details.offer
        travelerPresenter.viewModel.passportRequired.onNext(flightOffer.isInternational || flightOffer.isPassportNeeded)
    }

    override fun getLineOfBusiness() : LineOfBusiness {
        return LineOfBusiness.FLIGHTS_V2
    }

    override fun updateDbTravelers() {
        val params = Db.getFlightSearchParams()
        travelerManager.updateDbTravelers(params, context)
    }

    override fun trackShowSlideToPurchase() {
        FlightsV2Tracking.trackSlideToPurchase(cardType ?: PaymentType.UNKNOWN)
    }

    override fun trackShowBundleOverview() {
        val flightSearchParams = Db.getFlightSearchParams()
        FlightsV2Tracking.trackShowFlightOverView(flightSearchParams)
    }

    override fun makeCheckoutViewModel(): FlightCheckoutViewModel {
        return FlightCheckoutViewModel(context, getFlightServices(), paymentWidgetViewModel.cardTypeSubject)
    }

    override fun makeCreateTripViewModel(): FlightCreateTripViewModel {
        return FlightCreateTripViewModel(context, getFlightServices(), getCheckoutViewModel().cardFeeForSelectedCard)
    }

    override fun getCheckoutViewModel(): FlightCheckoutViewModel {
        return ckoViewModel as FlightCheckoutViewModel
    }

    override fun getCreateTripViewModel(): FlightCreateTripViewModel {
        return tripViewModel as FlightCreateTripViewModel
    }

    override fun getCostSummaryBreakdownViewModel(): FlightCostSummaryBreakdownViewModel {
        return FlightCostSummaryBreakdownViewModel(context)
    }

    override fun showMainTravelerMinimumAgeMessaging(): Boolean {
        return false
    }

    private fun getFlightServices(): FlightServices {
        return Ui.getApplication(context).flightComponent().flightServices()
    }

    private fun setupDontShowDebitCardVisibility() {
        Observable.combineLatest(getCheckoutViewModel().showDebitCardsNotAcceptedSubject,
                ckoViewModel.showingPaymentWidgetSubject,
                { showDebitCards, showingPaymentWidget ->
                    val visibility = if (showDebitCards && showingPaymentWidget) VISIBLE else GONE
                    debitCardsNotAcceptedTextView.visibility = visibility
                    if (visibility == VISIBLE) { // only show. hide handled in BaseCheckoutPresenter
                        toolbarDropShadow.visibility = visibility
                    }
                }).subscribe()
    }

    override fun clearCCNumber() {
        clearCVV()
        super.clearCCNumber()
    }
}
