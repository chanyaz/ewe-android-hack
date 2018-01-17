package com.expedia.bookings.data.trips

import com.expedia.bookings.services.TripsServicesInterface
import okio.Okio
import org.json.JSONObject
import rx.Observable
import java.io.File

open class MockTripServices(val error: Boolean) : TripsServicesInterface {
    val dataFlight = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/flight_trip_details.json"))).readUtf8()
    val jsonFlightObject = JSONObject(dataFlight)
    val dataHotel = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/hotel_trip_details.json"))).readUtf8()
    val jsonHotelObject = JSONObject(dataHotel)
    val dataPackage = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/package_trip_details.json"))).readUtf8()
    val jsonPackageObject = JSONObject(dataPackage)
    val errorData = Okio.buffer(Okio.source(File("../lib/mocked/templates/api/trips/error_trip_response.json"))).readUtf8()
    val errorJsonObject = JSONObject(errorData)

    override fun getTripDetails(tripId: String, useCache: Boolean): JSONObject? =
            if (!error) jsonFlightObject else errorJsonObject

    override fun getSharedTripDetails(sharedTripUrl: String): JSONObject? =
            if (!error) jsonHotelObject else errorJsonObject

    override fun getGuestTrip(tripId: String, guestEmail: String, useCache: Boolean): JSONObject? =
            if (!error) jsonPackageObject else errorJsonObject

    override fun getTripDetailsObservable(tripId: String, useCache: Boolean): Observable<JSONObject> =
            if (!error) Observable.just(jsonFlightObject) else Observable.just(errorJsonObject)

    override fun getSharedTripDetailsObservable(sharedTripUrl: String): Observable<JSONObject> =
            if (!error) Observable.just(jsonHotelObject) else Observable.just(errorJsonObject)

    override fun getGuestTripObservable(tripId: String, guestEmail: String, useCache: Boolean): Observable<JSONObject> =
            if (!error) Observable.just(jsonPackageObject) else Observable.just(errorJsonObject)
}
