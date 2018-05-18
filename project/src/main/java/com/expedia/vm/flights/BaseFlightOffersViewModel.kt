package com.expedia.vm.flights

import android.content.Context
import android.widget.Toast
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightSearchResponse
import com.expedia.bookings.data.flights.FlightSearchResponse.FlightSearchType
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.isFlightGreedySearchEnabled
import com.expedia.bookings.tracking.ApiCallFailing
import com.expedia.bookings.utils.Constants
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.HashMap

abstract class BaseFlightOffersViewModel(val context: Context, val flightServices: FlightServices) {

    val searchParamsObservable = BehaviorSubject.create<FlightSearchParams>()
    val errorObservable = PublishSubject.create<ApiError>()
    val errorObservableForGreedyCall = PublishSubject.create<ApiError>()
    val noNetworkObservable = PublishSubject.create<Unit>()

    val searchingForFlightDateTime = PublishSubject.create<Unit>()
    val resultsReceivedDateTimeObservable = PublishSubject.create<Unit>()
    val confirmedOutboundFlightSelection = BehaviorSubject.create<FlightLeg>()
    val confirmedInboundFlightSelection = BehaviorSubject.create<FlightLeg>()
    val outboundSelected = BehaviorSubject.create<FlightLeg>()
    val inboundSelected = PublishSubject.create<FlightLeg>()
    val offerSelectedChargesObFeesSubject = BehaviorSubject.create<String>()
    val flightOfferSelected = PublishSubject.create<FlightTripDetails.FlightOffer>()
    val flightProductId = PublishSubject.create<String>()
    val showChargesObFeesSubject = PublishSubject.create<Boolean>()
    val outboundResultsObservable = BehaviorSubject.create<List<FlightLeg>>()
    val inboundResultsObservable = BehaviorSubject.create<List<FlightLeg>>()
    val obFeeDetailsUrlObservable = BehaviorSubject.create<String>()
    val cancelOutboundSearchObservable = PublishSubject.create<Unit>()
    val cancelInboundSearchObservable = PublishSubject.create<Unit>()
    val cancelGreedySearchObservable = PublishSubject.create<Unit>()
    val retrySearchObservable = PublishSubject.create<Unit>()
    val ticketsLeftObservable = PublishSubject.create<Int>()

    val isRoundTripSearchSubject = BehaviorSubject.create<Boolean>()
    val flightCabinClassSubject = BehaviorSubject.create<String>()
    val nonStopSearchFilterAppliedSubject = BehaviorSubject.create<Boolean>()
    val greedyOutboundResultsObservable = PublishSubject.create<List<FlightLeg>>()
    val hasUserClickedSearchObservable = PublishSubject.create<Boolean>()
    val greedyFlightSearchObservable = PublishSubject.create<FlightSearchParams>()
    val refundableFilterAppliedSearchSubject = BehaviorSubject.create<Boolean>()
    val mayChargePaymentFeesSubject = PublishSubject.create<Boolean>()

    var isOutboundSearch = true
    var totalOutboundResults = 0
    var totalInboundResults = 0
    var isSubPub = false
    var isGreedyCallAborted = false
    var isGreedyCallCompleted = false

    protected var isRoundTripSearch = true
    protected lateinit var flightOfferModels: HashMap<String, FlightTripDetails.FlightOffer>

    protected var flightOutboundSearchSubscription: Disposable? = null
    protected var flightInboundSearchSubscription: Disposable? = null
    protected var flightGreedySearchSubscription: Disposable? = null

    init {
        searchParamsObservable.subscribe { params ->
            isRoundTripSearchSubject.onNext(params.isRoundTrip())
            params.flightCabinClass?.let {
                flightCabinClassSubject.onNext(it)
            }
            refundableFilterAppliedSearchSubject.onNext(params.showRefundableFlight ?: false)
            nonStopSearchFilterAppliedSubject.onNext(params.nonStopFlight ?: false)
            searchingForFlightDateTime.onNext(Unit)
            if (!isFlightGreedySearchEnabled(context) || isGreedyCallAborted) {
                flightOutboundSearchSubscription = flightServices.flightSearch(params, makeResultsObserver(), resultsReceivedDateTimeObservable)
                showDebugToast("Normal Search call is triggerred")
            }
            if (isFlightGreedySearchEnabled(context) && isGreedyCallAborted) {
                cancelGreedyCalls()
            }
        }

        if (isFlightGreedySearchEnabled(context)) {
            greedyFlightSearchObservable.subscribe { params ->
                flightGreedySearchSubscription = flightServices.greedyFlightSearch(params, makeResultsObserver(), resultsReceivedDateTimeObservable)
                showDebugToast("Greedy call is triggerred")
            }

            cancelGreedySearchObservable.subscribe {
                flightGreedySearchSubscription?.dispose()
            }
        }

        cancelOutboundSearchObservable.subscribe {
            flightOutboundSearchSubscription?.dispose()
        }

        cancelInboundSearchObservable.subscribe {
            flightInboundSearchSubscription?.dispose()
        }

        isRoundTripSearchSubject.subscribe {
            isRoundTripSearch = it
        }

        setupFlightSelectionObservables()

        flightOfferSelected.subscribe { selectedOffer ->
            showChargesObFeesSubject.onNext(selectedOffer.mayChargeOBFees)
        }

        showChargesObFeesSubject.subscribe { hasObFee ->
            if (hasObFee) {
                offerSelectedChargesObFeesSubject.onNext(context.getString(R.string.airline_fee_apply))
            } else {
                offerSelectedChargesObFeesSubject.onNext("")
            }
        }

        outboundResultsObservable.subscribe { totalOutboundResults = it.size }
        inboundResultsObservable.subscribe { totalInboundResults = it.size }
    }

    protected fun selectFlightOffer(outboundLegId: String, inboundLegId: String) {
        val offer = getFlightOffer(outboundLegId, inboundLegId)
        if (offer != null) {
            flightProductId.onNext(offer.productKey)
            flightOfferSelected.onNext(offer)
        }
    }

    protected fun setupFlightSelectionObservables() {
        confirmedOutboundFlightSelection.subscribe { flight ->
            if (isRoundTripSearch) {
                selectOutboundFlight(flight.legId)
            } else {
                // one-way flights
                val outboundLegId = flight.legId
                val inboundLegId = flight.legId // yes, they are the same. It will get us the flight offer
                selectFlightOffer(outboundLegId, inboundLegId)
                ticketsLeftObservable.onNext(flight.packageOfferModel.urgencyMessage.ticketsLeft)
            }
        }

        // return trip flights
        confirmedInboundFlightSelection.subscribe {
            val outboundFlight = confirmedOutboundFlightSelection.value

            val inboundLegId = it.legId
            val outboundLegId = outboundFlight.legId
            selectFlightOffer(outboundLegId, inboundLegId)
            val minimumTicketsLeft = getMinimumTicketsLeft(outboundFlight.packageOfferModel.urgencyMessage.ticketsLeft,
                    it.packageOfferModel.urgencyMessage.ticketsLeft)
            ticketsLeftObservable.onNext(minimumTicketsLeft)
        }

        // fires offer selected before flight selection confirmed to lookup terms, fees etc. in offer
        inboundSelected.subscribe {
            val offer = getFlightOffer(outboundSelected.value.legId, it.legId)
            flightOfferSelected.onNext(offer!!)
        }
    }

    protected fun getFlightOffer(outboundLegId: String, inboundLegId: String): FlightTripDetails.FlightOffer? {
        return flightOfferModels[makeFlightOfferKey(outboundLegId, inboundLegId)]
    }

    protected fun makeFlightOfferKey(outboundId: String, inboundId: String): String {
        return outboundId + inboundId
    }

    protected fun makeOffer(offer: FlightTripDetails.FlightOffer): PackageOfferModel {
        val offerModel = PackageOfferModel()
        val urgencyMessage = PackageOfferModel.UrgencyMessage()
        urgencyMessage.ticketsLeft = offer.seatsRemaining
        val price = PackageOfferModel.PackagePrice()
        price.packageTotalPrice = offer.totalPrice
        price.differentialPriceFormatted = offer.totalPrice.formattedPrice
        price.packageTotalPriceFormatted = offer.totalPrice.formattedPrice
        price.averageTotalPricePerTicket = offer.averageTotalPricePerTicket
        price.deltaPrice = offer.deltaPrice
        price.deltaPositive = offer.deltaPricePositive
        price.discountAmount = offer.discountAmount
        price.pricePerPersonFormatted = offer.averageTotalPricePerTicket.formattedWholePrice
        offerModel.urgencyMessage = urgencyMessage
        offerModel.price = price
        offerModel.loyaltyInfo = offer.loyaltyInfo
        return offerModel
    }

    fun cancelGreedyCalls() {
        cancelGreedySearchObservable.onNext(Unit)
    }

    protected fun makeResultsObserver(): Observer<FlightSearchResponse> {

        return object : DisposableObserver<FlightSearchResponse>() {
            override fun onNext(response: FlightSearchResponse) {
                if (isFlightGreedySearchEnabled(context) && !isGreedyCallAborted) {
                    isGreedyCallCompleted =
                            when (response.searchType) {
                                FlightSearchType.GREEDY -> true
                                FlightSearchType.NORMAL -> false
                            }
                }
                if (response.hasErrors()) {
                    if (isGreedyCallCompleted) {
                        errorObservableForGreedyCall.onNext(response.firstError)
                        hasUserClickedSearchObservable.onNext(searchParamsObservable.value != null)
                        isGreedyCallCompleted = false
                    } else {
                        errorObservable.onNext(response.firstError)
                    }
                } else if (response.offers.isEmpty() || response.legs.isEmpty()) {
                    if (isGreedyCallCompleted) {
                        errorObservableForGreedyCall.onNext(ApiError(ApiError.Code.FLIGHT_SEARCH_NO_RESULTS))
                        hasUserClickedSearchObservable.onNext(searchParamsObservable.value != null)
                        isGreedyCallCompleted = false
                    } else {
                        errorObservable.onNext(ApiError(ApiError.Code.FLIGHT_SEARCH_NO_RESULTS))
                    }
                } else {
                    obFeeDetailsUrlObservable.onNext(response.obFeesDetails)
                    if (AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightSubpubChange)) {
                        setSubPubAvailability(response.hasSubPub)
                    }
                    mayChargePaymentFeesSubject.onNext(response.mayChargePaymentFees)
                    makeFlightOffer(response)
                }
            }

            override fun onError(e: Throwable) {
                if (RetrofitUtils.isNetworkError(e)) {
                    if (isFlightGreedySearchEnabled(context) && searchParamsObservable.value == null) {
                        isGreedyCallAborted = true
                    } else {
                        val retryFun = fun() {
                            retrySearchObservable.onNext(Unit)
                            flightServices.flightSearch(searchParamsObservable.value, makeResultsObserver(), resultsReceivedDateTimeObservable)
                        }
                        val cancelFun = fun() {
                            noNetworkObservable.onNext(Unit)
                        }
                        DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
                        FlightsV2Tracking.trackFlightShoppingError(ApiCallFailing.FlightSearch(Constants.NO_INTERNET_ERROR_CODE))
                    }
                }
            }

            override fun onComplete() {
            }
        }
    }

    // Adding Toast to make it easier for testing cached results on debug builds.
    fun showDebugToast(message: String) {
        if (BuildConfig.DEBUG) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun getMinimumTicketsLeft(outboundTicketsLeft: Int, inboundTicketsLeft: Int): Int {
        if (outboundTicketsLeft < inboundTicketsLeft) return outboundTicketsLeft
        return inboundTicketsLeft
    }

    protected abstract fun selectOutboundFlight(legId: String)
    protected abstract fun createFlightMap(response: FlightSearchResponse)
    protected abstract fun makeFlightOffer(response: FlightSearchResponse)
    protected abstract fun setSubPubAvailability(hasSubPub: Boolean)
}
