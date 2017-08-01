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
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.RetrofitUtils
import com.expedia.bookings.utils.Ui
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.HashMap

abstract class BaseFlightOffersViewModel(val context: Context, val flightServices: FlightServices) {

    val searchParamsObservable = BehaviorSubject.create<FlightSearchParams>()
    val errorObservable = PublishSubject.create<ApiError>()
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
    val cancelCachedSearchObservable = PublishSubject.create<Unit>()
    val isCachedCallCompleted = PublishSubject.create<Boolean>()
    val isRoundTripSearchSubject = BehaviorSubject.create<Boolean>()
    val flightCabinClassSubject = BehaviorSubject.create<String>()
    val nonStopSearchFilterAppliedSubject = BehaviorSubject.create<Boolean>()
    val refundableFilterAppliedSearchSubject = BehaviorSubject.create<Boolean>()
    val cachedFlightSearchObservable = PublishSubject.create<FlightSearchParams>()
    val cachedSearchTrackingString = PublishSubject.create<String>()
    var isOutboundSearch = true
    var totalOutboundResults = 0
    var totalInboundResults = 0
    var isSubPub = false

    protected var isRoundTripSearch = true
    protected lateinit var flightOfferModels: HashMap<String, FlightTripDetails.FlightOffer>

    protected var flightOutboundSearchSubscription: Disposable? = null
    protected var flightInboundSearchSubscription: Disposable? = null
    protected var flightCacheSearchSubscription: Disposable? = null

    init {
        searchParamsObservable.subscribe { params ->
            isRoundTripSearchSubject.onNext(params.isRoundTrip())
            params.flightCabinClass?.let {
                flightCabinClassSubject.onNext(it)
            }
            refundableFilterAppliedSearchSubject.onNext(params.showRefundableFlight ?: false)
            nonStopSearchFilterAppliedSubject.onNext(params.nonStopFlight ?: false)
            searchingForFlightDateTime.onNext(Unit)
            flightOutboundSearchSubscription = flightServices.flightSearch(params, makeResultsObserver(), resultsReceivedDateTimeObservable)
        }

        cachedFlightSearchObservable.subscribe { params ->
            isCachedCallCompleted.onNext(false)
            flightCacheSearchSubscription = flightServices.cachedFlightSearch(params, makeResultsObserver(), resultsReceivedDateTimeObservable)
        }

        cancelOutboundSearchObservable.subscribe {
            flightOutboundSearchSubscription?.dispose()
        }

        cancelInboundSearchObservable.subscribe {
            flightInboundSearchSubscription?.dispose()
        }

        cancelCachedSearchObservable.withLatestFrom(isCachedCallCompleted, { _, cachedCallCompleted -> cachedCallCompleted })
                .filter { cachedCallCompleted -> !cachedCallCompleted }
                .subscribe {
                    // Normal API call returned before cache call.
                    flightCacheSearchSubscription?.unsubscribe()
                    cachedSearchTrackingString.onNext("CL")
                    showDebugToast("Normal api call returned before cached call.")
                }

        isRoundTripSearchSubject.subscribe {
            isRoundTripSearch = it
        }

        setupFlightSelectionObservables()

        outboundSelected.subscribe { flightLeg ->
            showChargesObFeesSubject.onNext(flightLeg.mayChargeObFees)
        }

        inboundSelected.subscribe { flightLeg ->
            showChargesObFeesSubject.onNext(flightLeg.mayChargeObFees)
        }

        showChargesObFeesSubject.subscribe { hasObFee ->
            if (hasObFee || doAirlinesChargeAdditionalFees()) {
                val stringID = if (doAirlinesChargeAdditionalFees()) {
                    R.string.airline_fee_apply
                } else {
                    R.string.payment_and_baggage_fees_may_apply
                }
                val paymentFeeText = context.getString(stringID)
                offerSelectedChargesObFeesSubject.onNext(paymentFeeText)
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
            }
        }

        // return trip flights
        confirmedInboundFlightSelection.subscribe {
            val inboundLegId = it.legId
            val outboundLegId = confirmedOutboundFlightSelection.value.legId
            selectFlightOffer(outboundLegId, inboundLegId)
        }

        // fires offer selected before flight selection confirmed to lookup terms, fees etc. in offer
        inboundSelected.subscribe {
            val offer = getFlightOffer(outboundSelected.value.legId, it.legId)
            flightOfferSelected.onNext(offer!!)
        }
    }

    protected fun doAirlinesChargeAdditionalFees(): Boolean {
        return PointOfSale.getPointOfSale().showAirlinePaymentMethodFeeLegalMessage()
    }

    protected fun getFlightOffer(outboundLegId: String, inboundLegId: String): FlightTripDetails.FlightOffer? {
        return flightOfferModels[makeFlightOfferKey(outboundLegId, inboundLegId)]
    }

    protected fun makeFlightOfferKey(outboundId: String, inboundId: String): String {
        return outboundId + inboundId
    }

    protected fun makeOffer(offer: FlightTripDetails.FlightOffer, isOutbound: Boolean): PackageOfferModel {
        val offerModel = PackageOfferModel()
        val urgencyMessage = PackageOfferModel.UrgencyMessage()
        urgencyMessage.ticketsLeft = offer.seatsRemaining
        val price = PackageOfferModel.PackagePrice()
        price.packageTotalPrice = offer.totalPrice
        price.differentialPriceFormatted = offer.totalPrice.formattedPrice
        price.packageTotalPriceFormatted = offer.totalPrice.formattedPrice
        price.averageTotalPricePerTicket = offer.averageTotalPricePerTicket
        price.discountAmount = offer.discountAmount
        price.pricePerPersonFormatted = offer.averageTotalPricePerTicket.formattedWholePrice
        offerModel.urgencyMessage = urgencyMessage
        offerModel.price = price
        offerModel.segmentsSeatClassAndBookingCode = if (isOutbound) offer.offersSeatClassAndBookingCode[0] else offer.offersSeatClassAndBookingCode[1]
        offerModel.loyaltyInfo = offer.loyaltyInfo
        return offerModel
    }

    protected fun makeResultsObserver(): Observer<FlightSearchResponse> {

        return object : DisposableObserver<FlightSearchResponse>() {

            override fun onNext(response: FlightSearchResponse) {
                if (FeatureToggleUtil.isUserBucketedAndFeatureEnabled(context, AbacusUtils.EBAndroidAppFlightsSearchResultCaching, R.string.preference_flight_search_from_cache) ) {
                    // Check for cached api response
                    if (response.isResponseCached()) {
                        isCachedCallCompleted.onNext(true)
                        if (!response.cachedResultsFound!!) {
                            // Cache is null
                            cachedSearchTrackingString.onNext("CN")
                            showDebugToast("Cached results not found")
                            return
                        } else if (response.areCachedResultsBookable()) {
                            // Bookable cache found
                            cachedSearchTrackingString.onNext("B")
                            cancelOutboundSearchObservable.onNext(Unit)
                            showDebugToast("Showing bookable cached results")
                        } else if (response.areCachedResultsNonBookable()) {
                            // Non bookable cache found
                            cachedSearchTrackingString.onNext("NB")
                            showDebugToast("Non bookable cached results found but not displayed")
                            return
                        }
                    } else {
                        cancelCachedSearchObservable.onNext(Unit)
                    }
                }

                if (response.hasErrors()) {
                    errorObservable.onNext(response.firstError)
                } else if (response.offers.isEmpty() || response.legs.isEmpty()) {
                    errorObservable.onNext(ApiError(ApiError.Code.FLIGHT_SEARCH_NO_RESULTS))
                } else {
                    obFeeDetailsUrlObservable.onNext(response.obFeesDetails)
                    if (AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightSubpubChange)) {
                        setSubPubAvailability(response.hasSubPub)
                    }
                    makeFlightOffer(response)
                }
            }

            override fun onError(e: Throwable) {
                if (RetrofitUtils.isNetworkError(e)) {
                    val retryFun = fun() {
                        flightServices.flightSearch(searchParamsObservable.value, makeResultsObserver(), resultsReceivedDateTimeObservable)
                    }
                    val cancelFun = fun() {
                        noNetworkObservable.onNext(Unit)
                    }
                    DialogFactory.showNoInternetRetryDialog(context, retryFun, cancelFun)
                    FlightsV2Tracking.trackFlightSearchAPINoResponseError()
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

    abstract protected fun selectOutboundFlight(legId: String)
    abstract protected fun createFlightMap(response: FlightSearchResponse)
    abstract protected fun makeFlightOffer(response: FlightSearchResponse)
    abstract protected fun setSubPubAvailability(boolean: Boolean)
}
