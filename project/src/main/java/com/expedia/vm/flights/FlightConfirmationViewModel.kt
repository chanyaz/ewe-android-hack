package com.expedia.vm.flights

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.FlightItinDetailsResponse
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.KrazyglueResponse
import com.expedia.bookings.data.flights.KrazyglueSearchParams
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.server.DateTimeParser
import com.expedia.bookings.services.KrazyglueServices
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.RewardsUtil
import com.expedia.bookings.utils.Strings
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.isKrazyglueOnFlightsConfirmationEnabled
import com.expedia.bookings.utils.HMACUtil
import com.expedia.bookings.utils.HotelsV2DataUtil
import com.expedia.util.Optional
import com.mobiata.android.Log
import com.mobiata.android.util.SettingUtils
import com.squareup.phrase.Phrase
import io.fabric.sdk.android.services.network.UrlUtils
import rx.Observable
import rx.Observer
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject
import java.net.URI

class FlightConfirmationViewModel(val context: Context, isWebCheckout: Boolean = false) {

    val confirmationObservable = PublishSubject.create<Pair<FlightCheckoutResponse, String>>()
    val setRewardsPoints = PublishSubject.create<Optional<String>>()
    val itinNumberMessageObservable = BehaviorSubject.create<String>()
    val rewardPointsObservable = BehaviorSubject.create<String>()
    val destinationObservable = BehaviorSubject.create<String>()
    val itinNumContentDescriptionObservable = BehaviorSubject.create<String>()
    val inboundCardVisibility = BehaviorSubject.create<Boolean>()
    val crossSellWidgetVisibility = BehaviorSubject.create<Boolean>()
    val tripTotalPriceSubject = PublishSubject.create<String>()
    val numberOfTravelersSubject = PublishSubject.create<Int>()
    val formattedTravelersStringSubject = PublishSubject.create<String>()
    val showTripProtectionMessage = BehaviorSubject.create<Boolean>(false)
    val krazyglueHotelsObservable = PublishSubject.create<List<KrazyglueResponse.KrazyglueHotel>>()
    val krazyGlueRegionIdObservable = PublishSubject.create<String>()
    val flightSearchParamsObservable = PublishSubject.create<FlightSearchParams>()
    val krazyGlueHotelSearchParamsObservable = PublishSubject.create<HotelSearchParams>()
    val flightCheckoutResponseObservable = PublishSubject.create<FlightCheckoutResponse>()
    val itinDetailsResponseObservable = PublishSubject.create<FlightItinDetailsResponse>()

    private val userStateManager = Ui.getApplication(context).appComponent().userStateManager()

    private val krazyglueService: KrazyglueServices by lazy {
        Ui.getApplication(context).flightComponent().krazyglueService()
    }
    private val isKrazyglueEnabled = isKrazyglueOnFlightsConfirmationEnabled(context)

    init {
        if (isWebCheckout) {
            setupItinResponseSubscription()
        } else {
            setupCheckoutResponseSubscription()
        }

        numberOfTravelersSubject.subscribe { number ->
            formattedTravelersStringSubject.onNext(StrUtils.formatTravelerString(context, number))
        }

        setRewardsPoints.subscribe { points ->
            points.value?.let {
                if (userStateManager.isUserAuthenticated() && PointOfSale.getPointOfSale().shouldShowRewards()) {
                    val rewardPointText = RewardsUtil.buildRewardText(context, it, ProductFlavorFeatureConfiguration.getInstance(), isFlights = true)
                    if (Strings.isNotEmpty(rewardPointText)) {
                        rewardPointsObservable.onNext(rewardPointText)
                    }
                }
            }
        }
    }

    private fun setupCheckoutResponseSubscription() {
        confirmationObservable.subscribe { pair ->
            val email = pair.second
            val response = pair.first
            val itinNumber = response.newTrip!!.itineraryNumber
            val isQualified = response.airAttachInfo?.hasAirAttach ?: false
            val itinNumberMessage = Phrase.from(context, R.string.itinerary_sent_to_confirmation_TEMPLATE)
                    .put("itinerary", itinNumber)
                    .put("email", email)
                    .format().toString()
            itinNumberMessageObservable.onNext(itinNumberMessage)
            val itinContentDescription = Phrase.from(context, R.string.confirmation_number_TEMPLATE)
                    .put("number", itinNumber)
                    .format().toString()
            itinNumContentDescriptionObservable.onNext(itinContentDescription)
            if (!userStateManager.isUserAuthenticated()) {
                ItineraryManager.getInstance().addGuestTrip(email, itinNumber)
            }
            tripTotalPriceSubject.onNext(response.totalChargesPrice?.formattedMoneyFromAmountAndCurrencyCode ?: "")
            val hasInsurance = response.flightAggregatedResponse?.flightsDetailResponse?.first()?.
                    offer?.selectedInsuranceProduct != null

            showTripProtectionMessage.onNext(hasInsurance)
            crossSellWidgetVisibility.onNext(if (isKrazyglueEnabled) false else isQualified)
            SettingUtils.save(context, R.string.preference_user_has_booked_hotel_or_flight, true)
        }

        flightCheckoutResponseObservable.subscribe { response ->
            val destinationCity = response.getFirstFlightLeg().segments?.last()?.arrivalAirportAddress?.city ?: ""
            val numberOfGuests = response.passengerDetails.size

            destinationObservable.onNext(destinationCity)
            numberOfTravelersSubject.onNext(numberOfGuests)
        }

        if (isKrazyglueEnabled) {
            Observable.zip(flightCheckoutResponseObservable, flightSearchParamsObservable,  { response, params ->
                val flightLegs = response.getFirstFlightTripDetails().getLegs()
                val hotelSearchParams = HotelsV2DataUtil.getHotelV2ParamsFromFlightV2Params(context, flightLegs, params)
                krazyGlueHotelSearchParamsObservable.onNext(hotelSearchParams)

                val krazyglueParams = getKrazyglueSearchParams(response, params)
                val signedUrl = getSignedKrazyglueUrl(krazyglueParams)
                krazyglueService.getKrazyglueHotels(signedUrl, getKrazyglueResponseObserver())
            }).subscribe()

            Observable.zip(flightCheckoutResponseObservable, krazyglueHotelsObservable , { response, hotels ->
                val isAirAttachQualified = response.airAttachInfo?.hasAirAttach ?: false
                crossSellWidgetVisibility.onNext(hotels.isEmpty() && isAirAttachQualified)
            }).subscribe()
        }
    }

    private fun setupItinResponseSubscription() {
        itinDetailsResponseObservable.subscribe { response ->
            var email = response.responseData.flights.firstOrNull()?.passengers?.firstOrNull()?.emailAddress ?: ""
            val itinNumber = response.responseData.tripNumber?.toString()
            val isQualified = response.responseData.airAttachQualificationInfo.airAttachQualified
            val destinationCity = response.responseData.flights.firstOrNull()?.legs?.firstOrNull()?.segments?.last()?.arrivalLocation?.city ?: ""
            val numberOfGuests = response.responseData.flights[0].passengers.size
            val itinConfirmationTemplateWithEmail = if (email.isEmpty()) {
                Phrase.from(context, R.string.itinerary_sent_to_your_email_confirmation_TEMPLATE)
            } else Phrase.from(context, R.string.itinerary_sent_to_confirmation_TEMPLATE).put("email", email)
            val itinNumberMessage = itinConfirmationTemplateWithEmail
                    .put("itinerary", itinNumber)
                    .format().toString()
            itinNumberMessageObservable.onNext(itinNumberMessage)
            val itinContentDescription = Phrase.from(context, R.string.confirmation_number_TEMPLATE)
                    .put("number", itinNumber)
                    .format().toString()
            itinNumContentDescriptionObservable.onNext(itinContentDescription)
            if (!userStateManager.isUserAuthenticated() && !ExpediaBookingApp.isRobolectric()) {
                ItineraryManager.getInstance().addGuestTrip(email, itinNumber)
            }
            tripTotalPriceSubject.onNext(response.responseData.totalTripPrice?.totalFormatted)

            val hasInsurance = response.responseData.insurance != null
            showTripProtectionMessage.onNext(hasInsurance)
            crossSellWidgetVisibility.onNext(if (isKrazyglueEnabled) false else isQualified)
            destinationObservable.onNext(destinationCity)
            numberOfTravelersSubject.onNext(numberOfGuests)
            SettingUtils.save(context, R.string.preference_user_has_booked_hotel_or_flight, true)
        }
    }

    fun getKrazyglueResponseObserver(): Observer<KrazyglueResponse> {
        return object : Observer<KrazyglueResponse> {
            override fun onNext(response: KrazyglueResponse) {
                if (response.success) {
                    krazyglueHotelsObservable.onNext(response.krazyglueHotels)
                    response.destinationDeepLink?.let { url ->
                        if (url.contains("regionId=")) {
                            val regionId = UrlUtils.getQueryParams(URI(url), true).getValue("regionId")
                            krazyGlueRegionIdObservable.onNext(regionId)
                        }
                    }
                } else {
                    krazyglueHotelsObservable.onNext(emptyList())
                }
            }

            override fun onCompleted() {
            }

            override fun onError(e: Throwable) {
                Log.d("Error fetching krazy glue hotels:" + e.stackTrace)
                krazyglueHotelsObservable.onNext(emptyList())
            }
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getKrazyglueSearchParams(response: FlightCheckoutResponse, searchParams: FlightSearchParams) : KrazyglueSearchParams {
        val destinationCode = response.getFirstFlightLastSegment().arrivalAirportCode
        val destinationArrivalDateTime = response.getFirstFlightLastSegment().arrivalTimeRaw
        val returnDateTime = if (searchParams.isRoundTrip()) {
            response.getFirstSegmentOfLastFlightLeg().departureTimeRaw
        } else {
            DateTimeParser.parseISO8601DateTimeString(destinationArrivalDateTime).plusDays(1).toString()
        }
        val numOfAdults = searchParams.adults
        val numOfChildren = searchParams.children.count()
        val childrenAge = searchParams.children
        return KrazyglueSearchParams(destinationCode, destinationArrivalDateTime, returnDateTime, numOfAdults, numOfChildren, childrenAge)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getSignedKrazyglueUrl(krazyglueSearchParams: KrazyglueSearchParams) : String {
        val urlWithParams = Phrase.from(context, R.string.krazy_glue_base_url_TEMPLATE)
                .put("baseurl", krazyglueSearchParams.baseUrl)
                .put("partnerid", Constants.KRAZY_GLUE_PARTNER_ID)
                .put("arrivaldatetime", krazyglueSearchParams.arrivalDateTime)
                .put("returndatetime", krazyglueSearchParams.returnDateTime)
                .put("destinationcode", krazyglueSearchParams.destinationCode)
                .put("numofadults", krazyglueSearchParams.numOfAdults)
                .put("numofchildren", krazyglueSearchParams.numOfChildren)
                .put("childages", getChildAgesStringFromChildAgeList(krazyglueSearchParams.childAges))
                .format().toString()

        val signature = HMACUtil.createHmac(krazyglueSearchParams.apiKey, urlWithParams).replace("+", "-").replace("/", "_").removeSuffix("=")
        val signedUrl = Phrase.from(context, R.string.krazy_glue_signed_url_TEMPLATE)
                .put("urlwithparams", urlWithParams)
                .put("signature", signature)
                .format().toString()

        return signedUrl
    }

    fun getChildAgesStringFromChildAgeList(childAge: List<Int>): String {
        val childAgesStringBuilder = StringBuilder()
        val iter = childAge.iterator()
        while (iter.hasNext()) {
            childAgesStringBuilder.append(iter.next());
            if (iter.hasNext()){
                childAgesStringBuilder.append(",");
            }
        }
        return childAgesStringBuilder.toString();
    }
}
