package com.expedia.bookings.services

import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightSearchResponse
import com.expedia.bookings.extensions.subscribeObserver
import com.expedia.bookings.utils.ApiDateUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Strings
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Period
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.ArrayList

// "open" so we can mock for unit tests
open class FlightServices(val endpoint: String, okHttpClient: OkHttpClient, interceptors: List<Interceptor>, val observeOn: Scheduler, val subscribeOn: Scheduler) {
    val flightApi: FlightApi by lazy {
        val gson = GsonBuilder()
                .registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter())
                .create()

        val okHttpClientBuilder = okHttpClient.newBuilder()

        for (interceptor in interceptors) {
            okHttpClientBuilder.addInterceptor(interceptor)
        }

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClientBuilder.build())
                .build()

        adapter.create(FlightApi::class.java)
    }
    var createTripRequestSubscription: Disposable? = null
    var checkoutRequestSubscription: Disposable? = null

    // open so we can use Mockito to mock FlightServices
    open fun flightSearch(params: FlightSearchParams, resultsResponseReceivedObservable: PublishSubject<Unit>? = null): Observable<FlightSearchResponse> {
        return flightApi.flightSearch(params.toQueryMap(), params.children, params.flightCabinClass, params.legNo,
                params.selectedOutboundLegId, params.showRefundableFlight, params.nonStopFlight, params.featureOverride, params.maxOfferCount)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .doOnNext { resultsResponseReceivedObservable?.onNext(Unit) }
                .doOnNext { response ->
                    processSearchResponse(response)
                }
    }

    protected fun processSearchResponse(response: FlightSearchResponse) {
        if (response.hasErrors() || response.legs.isEmpty() || response.offers.isEmpty()) return
        response.legs.forEach { leg ->
            if (Strings.isEmpty(leg.naturalKey)) {
                val filteredOffers = response.offers.filter { it.legIds.contains(leg.legId) && Strings.isNotEmpty(it.naturalKey) }
                if (filteredOffers.isNotEmpty()) {
                    leg.naturalKey = filteredOffers[0].naturalKey
                }
            }
            leg.carrierName = leg.segments.first().airlineName
            leg.flightSegments = leg.segments
            val departure = leg.flightSegments.first()
            val arrival = leg.flightSegments.last()

            val arrivalTime = ApiDateUtils.dateyyyyMMddHHmmSSSZToDateTimeWithTimeZone(arrival.arrivalTimeRaw)
            val departureTime = ApiDateUtils.dateyyyyMMddHHmmSSSZToDateTimeWithTimeZone(departure.departureTimeRaw)

            leg.elapsedDays = Days.daysBetween(departureTime.toLocalDate(), arrivalTime.toLocalDate()).days
            leg.departureDateTimeISO = departure.departureTimeRaw
            leg.arrivalDateTimeISO = arrival.arrivalTimeRaw
            leg.destinationAirportCode = arrival.arrivalAirportCode
            leg.originAirportCode = departure.departureAirportCode
            leg.destinationCity = arrival.arrivalAirportAddress.city
            leg.originCity = departure.departureAirportAddress.city

            val airlines = ArrayList<Airline>()
            var lastSegment: FlightLeg.FlightSegment? = null
            var lastArrival: DateTime? = null
            leg.stopCount = leg.flightSegments.size - 1
            if (leg.stopCount > 0) {
                leg.hasLayover = true
            }
            for (segment in leg.flightSegments) {
                segment.carrier = segment.airlineName
                segment.airplaneType = segment.equipmentDescription ?: "" // not always returned by API
                segment.departureCity = segment.departureAirportLocation
                segment.arrivalCity = segment.arrivalAirportLocation
                segment.departureDateTimeISO = segment.departureTimeRaw
                segment.arrivalDateTimeISO = segment.arrivalTimeRaw
                setAirlineLogoUrl(segment)

                val segmentArrivalTime = ApiDateUtils.dateyyyyMMddHHmmSSSZToDateTimeWithTimeZone(segment.arrivalTimeRaw)
                val segmentDepartureTime = ApiDateUtils.dateyyyyMMddHHmmSSSZToDateTimeWithTimeZone(segment.departureTimeRaw)
                segment.elapsedDays = Days.daysBetween(segmentDepartureTime.toLocalDate(), segmentArrivalTime.toLocalDate()).days
                val airline = Airline(segment.airlineName, segment.airlineLogoURL)
                airlines.add(airline)

                val travelPeriod = Period(segmentDepartureTime, segmentArrivalTime)
                segment.durationHours = travelPeriod.hours
                segment.durationMinutes = travelPeriod.minutes
                leg.durationHour += travelPeriod.hours
                leg.durationMinute += travelPeriod.minutes

                if (lastSegment != null) {
                    val layOverPeriod = Period(lastArrival, segmentDepartureTime)
                    leg.durationHour += layOverPeriod.hours
                    leg.durationMinute += layOverPeriod.minutes
                    lastSegment.layoverDurationHours = layOverPeriod.hours
                    lastSegment.layoverDurationMinutes = layOverPeriod.minutes
                }
                lastArrival = segmentArrivalTime
                lastSegment = segment
            }
            leg.airlines = airlines
            leg.carrierLogoUrl = leg.segments.first().airlineLogoURL
            if (leg.durationMinute > 59) {
                val extraHours: Int = leg.durationMinute / 60
                leg.durationHour += extraHours
                leg.durationMinute -= (extraHours * 60)
            }
        }
    }

    // open so we can use Mockito to mock FlightServices
    open fun createTrip(params: FlightCreateTripParams, observer: Observer<FlightCreateTripResponse>): Disposable {
        createTripRequestSubscription?.dispose()

        val createTripRequestSubscription = flightApi.oldCreateTrip(params.flexEnabled, params.queryParamsForOldCreateTrip(), params.featureOverride, params.fareFamilyCode, params.fareFamilyTotalPrice)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribeObserver(observer)
        this.createTripRequestSubscription = createTripRequestSubscription
        return createTripRequestSubscription
    }

    // open so we can use Mockito to mock FlightServices
    open fun checkout(params: Map<String, Any>, featureOverride: String?, observer: Observer<FlightCheckoutResponse>): Disposable {
        checkoutRequestSubscription?.dispose()

        checkoutRequestSubscription = flightApi.checkout(params, featureOverride)
                                        .observeOn(observeOn)
                                        .subscribeOn(subscribeOn)
                                        .doOnNext { response ->
                                            if (response.hasErrors()
                                                    || response.getFirstFlightTripDetails().legs.isEmpty()
                                                    || response.getFirstFlightTripDetails().offer == null)
                                                return@doOnNext

                                            response.getFirstFlightTripDetails().getLegs().forEach { leg ->
                                                val airlines = ArrayList<Airline>()
                                                leg.stopCount = leg.segments.size - 1
                                                leg.segments.forEach { segment ->
                                                    setAirlineLogoUrl(segment)
                                                    airlines.add(Airline(segment.airlineName, segment.airlineLogoURL))
                                                }
                                                leg.airlines = airlines
                                            }
                                        }
                                        .subscribeObserver(observer)

        return checkoutRequestSubscription as Disposable
    }
    private fun setAirlineLogoUrl(segment: FlightLeg.FlightSegment) {
        if (segment.airlineCode != null) {
            segment.airlineLogoURL = Constants.AIRLINE_SQUARE_LOGO_BASE_URL.replace("**", segment.airlineCode)
        }
    }
}
