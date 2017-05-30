package com.expedia.bookings.services

import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightSearchResponse
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.DateUtils
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Period
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observer
import rx.Scheduler
import rx.Subscription
import rx.subjects.PublishSubject
import java.util.ArrayList

// "open" so we can mock for unit tests
open class FlightServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {

    val flightApi: FlightApi by lazy {
        val gson = GsonBuilder()
                .registerTypeAdapter(DateTime::class.java, DateTimeTypeAdapter())
                .create()

        val adapter = Retrofit.Builder()
                .baseUrl(endpoint)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpClient.newBuilder().addInterceptor(interceptor).build())
                .build()

        adapter.create(FlightApi::class.java)
    }
    var searchRequestSubscription: Subscription? = null
    var createTripRequestSubscription: Subscription? = null
    var checkoutRequestSubscription: Subscription? = null

    // open so we can use Mockito to mock FlightServices
    open fun flightSearch(params: FlightSearchParams, observer: Observer<FlightSearchResponse>,
                          resultsResponseReceivedObservable: PublishSubject<Unit>? = null): Subscription {
        searchRequestSubscription?.unsubscribe()

        searchRequestSubscription = flightApi.flightSearch(params.toQueryMap(), params.children, params.flightCabinClass, params.legNo, params.selectedOutboundLegId)
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .doOnNext { resultsResponseReceivedObservable?.onNext(Unit) }
                .doOnNext { response ->
                    if (response.hasErrors() || response.legs.isEmpty() || response.offers.isEmpty()) return@doOnNext
                    response.legs.forEach { leg ->
                        leg.mayChargeObFees = response.offers.filter { it.legIds.contains(leg.legId) }
                                .filter { it.mayChargeOBFees == true }
                                .isNotEmpty()
                        leg.carrierName = leg.segments.first().airlineName
                        leg.flightSegments = leg.segments
                        val departure = leg.flightSegments.first()
                        val arrival = leg.flightSegments.last()

                        val arrivalTime = DateUtils.dateyyyyMMddHHmmSSSZToDateTimeWithTimeZone(arrival.arrivalTimeRaw)
                        val departureTime = DateUtils.dateyyyyMMddHHmmSSSZToDateTimeWithTimeZone(departure.departureTimeRaw)

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

                            val segmentArrivalTime = DateUtils.dateyyyyMMddHHmmSSSZToDateTimeWithTimeZone(segment.arrivalTimeRaw)
                            val segmentDepartureTime = DateUtils.dateyyyyMMddHHmmSSSZToDateTimeWithTimeZone(segment.departureTimeRaw)
                            segment.elapsedDays = Days.daysBetween(segmentDepartureTime.toLocalDate(), segmentArrivalTime.toLocalDate()).days
                            val airline = Airline(segment.airlineName, segment.airlineLogoURL)
                            airlines.add(airline)

                            val travelPeriod = Period(segmentDepartureTime, segmentArrivalTime)
                            segment.durationHours = travelPeriod.hours
                            segment.durationMinutes = travelPeriod.minutes
                            leg.durationHour += travelPeriod.hours
                            leg.durationMinute += travelPeriod.minutes

                            if (lastSegment != null) {
                                val layOverPeriod = Period(lastArrival, segmentDepartureTime);
                                leg.durationHour += layOverPeriod.hours
                                leg.durationMinute += layOverPeriod.minutes
                                lastSegment.layoverDurationHours = layOverPeriod.hours
                                lastSegment.layoverDurationMinutes = layOverPeriod.minutes
                            }
                            lastArrival = segmentArrivalTime;
                            lastSegment = segment
                        }
                        leg.airlines = airlines
                        if (leg.durationMinute > 59) {
                            val extraHours: Int = leg.durationMinute / 60
                            leg.durationHour += extraHours
                            leg.durationMinute -= (extraHours * 60)
                        }
                    }

                }.subscribe(observer)

        return searchRequestSubscription as Subscription
    }

    // open so we can use Mockito to mock FlightServices
    open fun createTrip(params: FlightCreateTripParams, observer: Observer<FlightCreateTripResponse>): Subscription {
        createTripRequestSubscription?.unsubscribe()

        createTripRequestSubscription = flightApi.createTrip(params.flexEnabled, params.toQueryMap())
                .observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .subscribe(observer)

        return createTripRequestSubscription as Subscription
    }

    // open so we can use Mockito to mock FlightServices
    open fun checkout(params: Map<String, Any>, observer: Observer<FlightCheckoutResponse>): Subscription {
        checkoutRequestSubscription?.unsubscribe()

        checkoutRequestSubscription = flightApi.checkout(params)
                                        .observeOn(observeOn)
                                        .subscribeOn(subscribeOn)
                                        .doOnNext { response ->
                                            if (response.hasErrors()
                                                    || response.getFirstFlightTripDetails().legs.isEmpty()
                                                    || response.getFirstFlightTripDetails().offer == null)
                                                return@doOnNext

                                            response.getFirstFlightTripDetails().getLegs().forEach { leg ->
                                                val airlines = ArrayList<Airline>()
                                                leg.segments.forEach { segment ->
                                                    setAirlineLogoUrl(segment)
                                                    airlines.add(Airline(segment.airlineName, segment.airlineLogoURL))
                                                }
                                                leg.airlines = airlines
                                            }
                                        }
                                        .subscribe(observer)

        return checkoutRequestSubscription as Subscription
    }
    private fun setAirlineLogoUrl(segment: FlightLeg.FlightSegment){
        if (segment.airlineCode != null) {
            segment.airlineLogoURL = Constants.AIRLINE_SQUARE_LOGO_BASE_URL.replace("**", segment.airlineCode)
        }
    }
}
