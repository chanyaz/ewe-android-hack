package com.expedia.bookings.presenter.flight

import android.content.Context
import android.text.Spanned
import android.text.SpannedString
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
import com.expedia.bookings.services.InsuranceServices
import com.expedia.bookings.tracking.FlightsV2Tracking
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.bookings.widget.TextView
import com.expedia.util.subscribeTextAndVisibility
import com.expedia.vm.BaseCreateTripViewModel
import com.expedia.vm.FlightCheckoutViewModel
import com.expedia.vm.InsuranceViewModel
import com.expedia.vm.PaymentViewModel
import com.expedia.vm.flights.FlightCostSummaryBreakdownViewModel
import com.expedia.vm.flights.FlightCreateTripViewModel
import com.squareup.otto.Subscribe
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import rx.subjects.Subject
import javax.inject.Inject

class FlightCheckoutPresenter(context: Context, attr: AttributeSet) : BaseCheckoutPresenter(context, attr) {
    lateinit var insuranceServices: InsuranceServices
        @Inject set

    lateinit var flightCheckoutViewModel: FlightCheckoutViewModel
        @Inject set

    lateinit var flightCreateTripViewModel: FlightCreateTripViewModel
        @Inject set

    lateinit var paymentViewModel: PaymentViewModel
        @Inject set

    init {
        val debitCardsNotAcceptedSubject = BehaviorSubject.create<Spanned>(SpannedString(context.getString(R.string.flights_debit_cards_not_accepted)))
        val flightCostSummaryObservable = (totalPriceWidget.breakdown.viewmodel as FlightCostSummaryBreakdownViewModel).flightCostSummaryObservable

        makePaymentErrorSubscriber(getCheckoutViewModel().showDebitCardsNotAcceptedSubject,  ckoViewModel.showingPaymentWidgetSubject,
                debitCardsNotAcceptedTextView, debitCardsNotAcceptedSubject)

        getCheckoutViewModel().priceChangeObservable.subscribe { it as FlightCheckoutResponse
            handlePriceChange(it)
        }
        getCreateTripViewModel().priceChangeObservable.subscribe { it as FlightCreateTripResponse
            handlePriceChange(it)
        }
        getCheckoutViewModel().receivedCheckoutResponse.subscribe {
            checkoutDialog.hide()
        }

        getCheckoutViewModel().tripResponseObservable.subscribe(flightCostSummaryObservable)
    }

    override fun injectComponents() {
        Ui.getApplication(context).flightComponent().inject(this)
    }

    override fun getPaymentWidgetViewModel(): PaymentViewModel {
        return paymentViewModel
    }

    override fun setupCreateTripViewModel(vm : BaseCreateTripViewModel) {
        vm as FlightCreateTripViewModel

        insuranceWidget.viewModel = InsuranceViewModel(context, insuranceServices)
        insuranceWidget.viewModel.updatedTripObservable.subscribe(vm.tripResponseObservable)

        vm.tripParams.subscribe {
            userAccountRefresher.ensureAccountIsRefreshed()
        }

        getCheckoutViewModel().tripResponseObservable.subscribe { response -> response as FlightCreateTripResponse
            loginWidget.updateRewardsText(getLineOfBusiness())
            insuranceWidget.viewModel.tripObservable.onNext(response)
            totalPriceWidget.viewModel.total.onNext(response.tripTotalPayableIncludingFeeIfZeroPayableByPoints())
            totalPriceWidget.viewModel.costBreakdownEnabledObservable.onNext(true)
            (totalPriceWidget.breakdown.viewmodel as FlightCostSummaryBreakdownViewModel).flightCostSummaryObservable.onNext(response)
            isPassportRequired(response)
            trackShowBundleOverview()
        }
    }

    private fun handlePriceChange(tripResponse: FlightCreateTripResponse) {
        val flightTripDetails = tripResponse.getDetails()

        // TODO - we may have to change from totalFarePrice -> totalPrice in order to support SubPub fares
        if (flightTripDetails.oldOffer != null) {
            val originalPrice = flightTripDetails.oldOffer.totalFarePrice
            val newPrice = flightTripDetails.offer.totalFarePrice
            priceChangeWidget.viewmodel.originalPrice.onNext(originalPrice)
            priceChangeWidget.viewmodel.newPrice.onNext(newPrice)
        }

        // TODO - update to totalPrice when checkout response starts returning totalPrice (required for SubPub fare support)
        totalPriceWidget.viewModel.total.onNext(tripResponse.tripTotalPayableIncludingFeeIfZeroPayableByPoints())
        totalPriceWidget.viewModel.costBreakdownEnabledObservable.onNext(true)
        (totalPriceWidget.breakdown.viewmodel as FlightCostSummaryBreakdownViewModel).flightCostSummaryObservable.onNext(tripResponse)
    }

    @Subscribe fun onUserLoggedIn( @Suppress("UNUSED_PARAMETER") event: Events.LoggedInSuccessful) {
        onLoginSuccess()
    }

    override fun isPassportRequired(response: TripResponse) {
        val flightOffer = (response as FlightCreateTripResponse).getDetails().offer
        travelerPresenter.viewModel.passportRequired.onNext(flightOffer.isInternational || flightOffer.isPassportNeeded)
    }

    override fun getLineOfBusiness() : LineOfBusiness {
        return LineOfBusiness.FLIGHTS_V2
    }

    override fun updateDbTravelers() {
        val params = Db.getFlightSearchParams()
        travelerManager.updateDbTravelers(params, context)
        resetTravelers()
    }

    override fun trackShowSlideToPurchase() {
        FlightsV2Tracking.trackSlideToPurchase(cardType ?: PaymentType.UNKNOWN)
    }

    override fun trackShowBundleOverview() {
        val flightSearchParams = Db.getFlightSearchParams()
        val flightCreateTripResponse = getCreateTripViewModel().tripResponseObservable.value as FlightCreateTripResponse
        FlightsV2Tracking.trackShowFlightOverView(flightSearchParams, flightCreateTripResponse)
    }

    override fun makeCheckoutViewModel(): FlightCheckoutViewModel {
        return flightCheckoutViewModel
    }

    override fun makeCreateTripViewModel(): FlightCreateTripViewModel {
        return flightCreateTripViewModel
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

    private fun makePaymentErrorSubscriber(fee: Subject<Boolean, Boolean>, show: PublishSubject<Boolean>, textView: TextView, text: Subject<Spanned, Spanned>) {
        Observable.combineLatest( fee, show, text,
                { fee, show, text ->
                    val cardFeeVisibility = if (fee && show) View.VISIBLE else View.GONE
                    if (cardFeeVisibility == VISIBLE) {
                        textView.visibility = cardFeeVisibility
                        AnimUtils.slideIn(textView)
                        textView.text = text
                        toolbarDropShadow.visibility = visibility
                    } else if (textView.visibility == View.VISIBLE) {
                        AnimUtils.slideOut(invalidPaymentTypeWarningTextView)
                    }
                }).subscribe()
    }

    override fun setInsuranceWidgetVisibility(visible: Boolean){
        insuranceWidget.viewModel.updateVisibility(visible)
    }

}
