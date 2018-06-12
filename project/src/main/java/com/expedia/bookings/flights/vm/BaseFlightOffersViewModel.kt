package com.expedia.bookings.flights.vm

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
import com.expedia.bookings.flights.utils.FlightServicesManager
import com.expedia.bookings.tracking.ApiCallFailing
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.isFlightGreedySearchEnabled
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

abstract class BaseFlightOffersViewModel(val context: Context, val flightServicesManager: FlightServicesManager) {
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
    val outboundResultsObservable = BehaviorSubject.create<List<FlightLeg>>()
    val inboundResultsObservable = BehaviorSubject.create<List<FlightLeg>>()
    val obFeeDetailsUrlObservable = BehaviorSubject.create<String>()
    val cancelSearchObservable = PublishSubject.create<Unit>()
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

    protected var isRoundTripSearch = true
    protected lateinit var flightOfferModels: HashMap<String, FlightTripDetails.FlightOffer>

    protected var flightSearchSubscription: Disposable? = null
    protected var flightGreedySearchSubscription: Disposable? = null

    val successResponseHandler = PublishSubject.create<Pair<FlightSearchType, FlightSearchResponse>>()
    val errorResponseHandler = PublishSubject.create<Pair<FlightSearchType, ApiError>>()

    init {
        successResponseHandler.subscribe { (type, response) ->
            obFeeDetailsUrlObservable.onNext(response.obFeesDetails)
            if (AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightSubpubChange)) {
                setSubPubAvailability(response.hasSubPub)
            }
            mayChargePaymentFeesSubject.onNext(response.mayChargePaymentFees)
            makeFlightOffer(type, response)
        }

        errorResponseHandler.subscribe { (type, error) ->
            if (error.errorCode == ApiError.Code.NO_INTERNET) {
                handleNetworkError(type)
            } else {
                when (type) {
                    FlightSearchType.GREEDY -> {
                        errorObservableForGreedyCall.onNext(error)
                        hasUserClickedSearchObservable.onNext(searchParamsObservable.value != null)
                    }
                    FlightSearchType.NORMAL -> {
                        errorObservable.onNext(error)
                    }
                }
            }
        }

        searchParamsObservable.subscribe { params ->
            isRoundTripSearchSubject.onNext(params.isRoundTrip())
            params.flightCabinClass?.let {
                flightCabinClassSubject.onNext(it)
            }
            refundableFilterAppliedSearchSubject.onNext(params.showRefundableFlight ?: false)
            nonStopSearchFilterAppliedSubject.onNext(params.nonStopFlight ?: false)
            searchingForFlightDateTime.onNext(Unit)
            if (!isFlightGreedySearchEnabled(context) || isGreedyCallAborted) {
                flightSearchSubscription?.dispose()
                flightSearchSubscription = flightServicesManager.doFlightSearch(params, FlightSearchType.NORMAL, successResponseHandler, errorResponseHandler)
                showDebugToast("Normal Search call is triggerred")
            }
            if (isFlightGreedySearchEnabled(context) && isGreedyCallAborted) {
                cancelGreedyCalls()
            }
        }

        if (isFlightGreedySearchEnabled(context)) {
            greedyFlightSearchObservable.subscribe { params ->
                flightGreedySearchSubscription?.dispose()
                flightGreedySearchSubscription = flightServicesManager.doFlightSearch(params, FlightSearchType.GREEDY, successResponseHandler, errorResponseHandler)
                showDebugToast("Greedy call is triggerred")
            }

            cancelGreedySearchObservable.subscribe {
                flightGreedySearchSubscription?.dispose()
            }
        }

        cancelSearchObservable.subscribe {
            if (flightSearchSubscription != null && !flightSearchSubscription!!.isDisposed) {
                flightSearchSubscription?.dispose()
            }
        }

        isRoundTripSearchSubject.subscribe {
            isRoundTripSearch = it
        }

        setupFlightSelectionObservables()

        flightOfferSelected.subscribe { selectedOffer ->
            if (selectedOffer.mayChargeOBFees) {
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

    private fun handleNetworkError(type: FlightSearchType) {
        if (type == FlightSearchType.GREEDY && searchParamsObservable.value == null) {
            isGreedyCallAborted = true
        } else {
            val retryFun = fun() {
                retrySearchObservable.onNext(Unit)
                flightServicesManager.doFlightSearch(searchParamsObservable.value, FlightSearchType.NORMAL, successResponseHandler, errorResponseHandler)
            }
            val cancelFun = fun() {
                noNetworkObservable.onNext(Unit)
            }
            DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
            FlightsV2Tracking.trackFlightShoppingError(ApiCallFailing.FlightSearch(Constants.NO_INTERNET_ERROR_CODE))
        }
    }

    // Adding Toast to make it easier for testing cached results on debug builds.
    fun showDebugToast(message: String) {
        if (BuildConfig.DEBUG) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    protected fun sendOutboundFlights(type: FlightSearchType, outBoundFlights: LinkedHashSet<FlightLeg>) {
        if (isFlightGreedySearchEnabled(context) && type == FlightSearchType.GREEDY && !isGreedyCallAborted) {
            greedyOutboundResultsObservable.onNext(outBoundFlights.toList())
            hasUserClickedSearchObservable.onNext(searchParamsObservable.value != null)
        } else if (searchParamsObservable.value != null) {
            outboundResultsObservable.onNext(outBoundFlights.toList())
        }
    }

    private fun getMinimumTicketsLeft(outboundTicketsLeft: Int, inboundTicketsLeft: Int): Int {
        if (outboundTicketsLeft < inboundTicketsLeft) return outboundTicketsLeft
        return inboundTicketsLeft
    }

    private fun setSubPubAvailability(hasSubPub: Boolean) {
        isSubPub = hasSubPub
    }

    protected abstract fun selectOutboundFlight(legId: String)
    protected abstract fun createFlightMap(type: FlightSearchType, response: FlightSearchResponse)
    protected abstract fun makeFlightOffer(type: FlightSearchType, response: FlightSearchResponse)
}
