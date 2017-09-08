package com.expedia.vm.flights

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightSearchResponse
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.dialog.DialogFactory
import com.expedia.bookings.services.FlightServices
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.FeatureToggleUtil
import com.expedia.bookings.utils.RetrofitUtils
import rx.Observer
import rx.Subscription
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
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
    val isRoundTripSearchSubject = BehaviorSubject.create<Boolean>()
    val flightCabinClassSubject = BehaviorSubject.create<String>()
    val nonStopSearchFilterAppliedSubject = BehaviorSubject.create<Boolean>()
    val refundableFilterAppliedSearchSubject = BehaviorSubject.create<Boolean>()
    var isOutboundSearch = true
    var totalOutboundResults = 0
    var totalInboundResults = 0
    var isSubPub = false

    protected var isRoundTripSearch = true
    protected lateinit var flightOfferModels: HashMap<String, FlightTripDetails.FlightOffer>

    protected var flightOutboundSearchSubscription: Subscription? = null
    protected var flightInboundSearchSubscription: Subscription? = null

    init {
        searchParamsObservable.subscribe { params ->
            isRoundTripSearchSubject.onNext(params.isRoundTrip())
            val cabinClass = params.flightCabinClass
            if (cabinClass != null) {
                flightCabinClassSubject.onNext(cabinClass)
            }
            refundableFilterAppliedSearchSubject.onNext(params.showRefundableFlight ?: false)
            nonStopSearchFilterAppliedSubject.onNext(params.nonStopFlight?: false)
            searchingForFlightDateTime.onNext(Unit)
            flightOutboundSearchSubscription = flightServices.flightSearch(params, makeResultsObserver(), resultsReceivedDateTimeObservable)
        }
        cancelOutboundSearchObservable.subscribe {
            flightOutboundSearchSubscription?.unsubscribe()
        }
        cancelInboundSearchObservable.subscribe {
            flightInboundSearchSubscription?.unsubscribe()
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
            flightOfferSelected.onNext(offer)
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

        return object : Observer<FlightSearchResponse> {

            override fun onNext(response: FlightSearchResponse) {
                if (response.hasErrors()) {
                    errorObservable.onNext(response.firstError)
                } else if (response.offers.isEmpty() || response.legs.isEmpty()) {
                    errorObservable.onNext(ApiError(ApiError.Code.FLIGHT_SEARCH_NO_RESULTS))
                } else {
                    obFeeDetailsUrlObservable.onNext(response.obFeesDetails)
                    if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightSubpubChange)) {
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

            override fun onCompleted() {
            }
        }
    }

    abstract protected fun selectOutboundFlight(legId: String)
    abstract protected fun createFlightMap(response: FlightSearchResponse)
    abstract protected fun makeFlightOffer(response: FlightSearchResponse)
    abstract protected fun setSubPubAvailability(boolean: Boolean)
}
