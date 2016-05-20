package com.expedia.bookings.services

import com.expedia.bookings.data.flights.Airline
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
import org.joda.time.Hours
import org.joda.time.Minutes
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.Observable
import rx.Scheduler
import java.util.ArrayList
import java.util.regex.Pattern

class FlightServices(endpoint: String, okHttpClient: OkHttpClient, interceptor: Interceptor, val observeOn: Scheduler, val subscribeOn: Scheduler) {
    val patternHour = Pattern.compile("(?<=PT)([0-9]+)(?=H)")
    val patternMin = Pattern.compile("(?<=PT)([0-9]+)(?=M)")
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
        return flightApi.flightSearch(params.toQueryMap()).observeOn(observeOn)
                .subscribeOn(subscribeOn)
                .doOnNext { response ->
                    response.legs.forEach { leg ->
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
                        for (segment in leg.flightSegments) {
                            segment.carrier = segment.airlineName
                            segment.airplaneType = segment.equipmentDescription ?: "" // not always returned by API
                            segment.departureCity = segment.departureAirportLocation
                            segment.arrivalCity = segment.arrivalAirportLocation
                            segment.departureDateTimeISO = segment.departureTimeRaw
                            segment.arrivalDateTimeISO = segment.arrivalTimeRaw

                            val arrvalTime = DateUtils.yyyyMMddTHHmmssToDateTimeSafe(segment.arrivalTime, DateTime.now())
                            val depTime = DateUtils.yyyyMMddTHHmmssToDateTimeSafe(segment.departureTime, DateTime.now())
                            segment.elapsedDays = Days.daysBetween(arrvalTime, depTime).days

                            val airline = Airline(segment.airlineName, segment.airlineLogoURL)
                            airlines.add(airline)

                            val matcherHour = patternHour.matcher(segment.duration);
                            if (matcherHour.find()) {
                                val hours =  matcherHour.group(1).toInt()
                                segment.durationHours = hours
                                leg.durationHour += hours
                            }

                            val matcherMin = patternMin.matcher(segment.duration);
                            if (matcherMin.find()) {
                                val minutes = matcherMin.group(1).toInt()
                                segment.durationMinutes = minutes
                                leg.durationMinute += minutes
                            }

                            if (lastSegment != null) {
                                val nextFlight = DateUtils.yyyyMMddTHHmmssToDateTimeSafe(segment.departureTime, DateTime.now())
                                val lastFlight = DateUtils.yyyyMMddTHHmmssToDateTimeSafe(lastSegment.arrivalTime, DateTime.now())
                                segment.layoverDurationHours = Hours.hoursBetween(nextFlight, lastFlight).hours
                                segment.layoverDurationMinutes = Minutes.minutesBetween(nextFlight, lastFlight).minutes
                            }
                            lastSegment = segment
                        }
                        leg.airlines = airlines
                    }

                }
    }

    fun createTrip(params: FlightCreateTripParams): Observable<FlightCreateTripResponse> {
        return flightApi.createTrip(params.toQueryMap()).observeOn(observeOn)
                .subscribeOn(subscribeOn)
    }
}
