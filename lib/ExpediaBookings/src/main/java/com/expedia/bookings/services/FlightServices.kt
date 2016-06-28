package com.expedia.bookings.services

import com.expedia.bookings.data.flights.Airline
import com.expedia.bookings.data.flights.FlightCheckoutResponse
import com.expedia.bookings.data.flights.FlightCreateTripParams
import com.expedia.bookings.data.flights.FlightCreateTripResponse
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.data.flights.FlightSearchResponse
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
import org.joda.time.format.DateTimeFormat
import rx.Observable
import rx.Scheduler
import java.util.ArrayList
import java.util.regex.Pattern

// "open" so we can mock for unit tests
open class FlightServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {
    val hourMinuteFormatter = DateTimeFormat.forPattern("hh:mma")
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

    fun flightSearch(params: FlightSearchParams): Observable<FlightSearchResponse> {
        return flightApi.flightSearch(params.toQueryMap(), params.children).observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .doOnNext { response ->
                    response.legs.forEach { leg ->
                        leg.carrierName = leg.segments.first().airlineName
                        leg.flightSegments = leg.segments
                        val departure = leg.flightSegments.first()
                        val arrival = leg.flightSegments.last()
                        val arrivalTime = DateUtils.dateyyyyMMddHHmmSSSZToDateTime(arrival.arrivalTimeRaw)
                        val departureTime = DateUtils.dateyyyyMMddHHmmSSSZToDateTime(departure.departureTimeRaw)
                        leg.elapsedDays = Days.daysBetween(departureTime.toLocalDate(), arrivalTime.toLocalDate()).days
                        leg.departureDateTimeISO = departure.departureTimeRaw
                        leg.arrivalDateTimeISO = arrival.arrivalTimeRaw
                        val airlines = ArrayList<Airline>()
                        var lastSegment: FlightLeg.FlightSegment? = null
                        var lastArrival: DateTime? = null
                        leg.stopCount = leg.flightSegments.size - 1;
                        if(leg.stopCount > 0) {
                            leg.hasLayover = true
                        }
                        for (segment in leg.flightSegments) {
                            segment.carrier = segment.airlineName
                            segment.airplaneType = segment.equipmentDescription ?: "" // not always returned by API
                            segment.departureCity = segment.departureAirportLocation
                            segment.arrivalCity = segment.arrivalAirportLocation
                            segment.departureDateTimeISO = segment.departureTimeRaw
                            segment.arrivalDateTimeISO = segment.arrivalTimeRaw

                            val segmentArrivalTime = DateUtils.dateyyyyMMddHHmmSSSZToDateTime(segment.arrivalTimeRaw)
                            val segmentDepartureTime = DateUtils.dateyyyyMMddHHmmSSSZToDateTime(segment.departureTimeRaw)
                            segment.elapsedDays = Days.daysBetween(segmentArrivalTime.toLocalDate(), segmentDepartureTime.toLocalDate()).days

                            val airline = Airline(segment.airlineName, segment.airlineLogoURL)
                            airlines.add(airline)

                            val travelPeriod = Period(segmentDepartureTime, segmentArrivalTime)
                            segment.durationHours = travelPeriod.hours
                            segment.durationMinutes = travelPeriod.minutes
                            leg.durationHour += travelPeriod.hours
                            leg.durationMinute += travelPeriod.minutes

                            // set departure and arrival time to be compatible with packages format
                            segment.departureTime = segmentDepartureTime.toString(hourMinuteFormatter)
                            segment.arrivalTime = segmentArrivalTime.toString(hourMinuteFormatter)

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
                        leg.baggageFeesUrl = leg.baggageFeesUrl.replace("http://www.expedia.com/", "")
                        if (leg.durationMinute > 59) {
                            val extraHours: Int = leg.durationMinute / 60
                            leg.durationHour += extraHours
                            leg.durationMinute -= (extraHours * 60)
                        }
                    }

                }
    }

    // open so we can use Mockito to mock FlightServices
    open fun createTrip(params: FlightCreateTripParams): Observable<FlightCreateTripResponse> {
        return flightApi.createTrip(params.toQueryMap()).observeOn(observeOn)
                .subscribeOn(subscribeOn)
    }

    // open so we can use Mockito to mock FlightServices
    open fun checkout(params: Map<String, Any>): Observable<FlightCheckoutResponse> {
        return flightApi.checkout(params).observeOn(observeOn)
                .subscribeOn(subscribeOn)
    }
}
