package com.expedia.bookings.test

import android.app.Activity
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.StoredCreditCard
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.packages.MultiItemCreateTripParams
import com.expedia.bookings.data.packages.MultiItemApiCreateTripResponse
import com.expedia.bookings.data.packages.PackageOfferModel
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.services.PackageProductSearchType
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.testrule.ServicesRule
import org.joda.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class MockPackageServiceTestRule : ServicesRule<PackageServices>(PackageServices::class.java) {

    var activity: Activity by Delegates.notNull()
    lateinit var hotelRooms: List<HotelOffersResponse.HotelRoomResponse>

    fun getMIDCreateTripResponse(): MultiItemApiCreateTripResponse {
        val observer = TestObserver<MultiItemApiCreateTripResponse>()
        val packagePrice = PackageOfferModel.PackagePrice()
        packagePrice.packageTotalPrice = Money()
        val params = MultiItemCreateTripParams("mid_create_trip", "", "", "", "", packagePrice, "", "", 0, null, null)

        services?.multiItemCreateTrip(params)!!.subscribe(observer)
        observer.awaitTerminalEvent(10, TimeUnit.SECONDS)
        return observer.values()[0]
    }

    fun getHotelInfo(): HotelOffersResponse {
        val observer = TestObserver<HotelOffersResponse>()
        services?.hotelInfo("happy")?.subscribe(observer)
        observer.awaitTerminalEvent()
        return observer.values()[0]
    }

    fun getMIDHotelResponse(): BundleSearchResponse {
        val observer = TestObserver<BundleSearchResponse>()
        val params = getPackageParams("happy")
        Db.setPackageParams(params)

        services?.packageSearch(params, PackageProductSearchType.MultiItemHotels)?.subscribe(observer)
        observer.awaitTerminalEvent()
        Db.setPackageResponse(observer.values()[0])

        return observer.values()[0]
    }

    fun getMIDRoomsResponse(): BundleSearchResponse {
        val observer = TestObserver<BundleSearchResponse>()
        val params = getPackageParams()
        params.latestSelectedOfferInfo.hotelId = "happy_room"
        Db.setPackageParams(params)

        services?.multiItemRoomSearch(params)?.subscribe(observer)
        observer.awaitTerminalEvent()
        Db.setPackageResponse(observer.values()[0])

        return observer.values()[0]
    }

    fun getMIDFlightsResponse(): BundleSearchResponse {
        val observer = TestObserver<BundleSearchResponse>()
        val params = getPackageParams()
        params.latestSelectedOfferInfo.ratePlanCode = "flight_outbound_happy"
        Db.setPackageParams(params)

        services?.packageSearch(params, PackageProductSearchType.MultiItemOutboundFlights)?.subscribe(observer)
        observer.awaitTerminalEvent()
        Db.setPackageResponse(observer.values()[0])

        return observer.values()[0]
    }

    fun getPackageParams(originAirportCode: String = ""): PackageSearchParams {
        val packageParams = PackageSearchParams.Builder(26, 329)
                .flightCabinClass("coach")
                .infantSeatingInLap(true)
                .children(listOf(16, 10, 1))
                .origin(getDummySuggestion(originAirportCode))
                .destination(getDummySuggestion("LHR"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build() as PackageSearchParams
        return packageParams
    }

    private fun getDummySuggestion(airportCode: String = ""): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = airportCode
        suggestion.regionNames.fullName = airportCode
        suggestion.regionNames.shortName = airportCode
        suggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
        suggestion.hierarchyInfo!!.airport = SuggestionV4.Airport()
        suggestion.hierarchyInfo!!.airport!!.airportCode = airportCode
        suggestion.hierarchyInfo!!.airport!!.multicity = "happy"
        return suggestion
    }

    private fun getBillingInfo(): BillingInfo {
        val info = BillingInfo()
        info.email = "qa-ehcc@mobiata.com"
        info.firstName = "JexperCC"
        info.lastName = "MobiataTestaverde"
        info.nameOnCard = info.firstName + " " + info.lastName
        info.setNumberAndDetectType("4111111111111111", activity)
        info.securityCode = "111"
        info.telephone = "4155555555"
        info.telephoneCountryCode = "1"
        info.expirationDate = LocalDate.now()

        val location = Location()
        location.streetAddress = arrayListOf("123 street")
        location.city = "city"
        location.stateCode = "CA"
        location.countryCode = "US"
        location.postalCode = "12334"
        info.location = location

        return info
    }

    fun getTraveler(): Traveler {
        val traveler = Traveler()
        traveler.firstName = "Mahak"
        traveler.lastName = "Swami"
        traveler.gender = Traveler.Gender.FEMALE
        traveler.phoneNumber = "12345678"
        traveler.birthDate = LocalDate.now().minusYears(18)
        traveler.seatPreference = Traveler.SeatPreference.WINDOW
        traveler.redressNumber = "123456"
        traveler.knownTravelerNumber = "TN123456"
        return traveler
    }

    fun getStoredCard(): StoredCreditCard {
        val card = StoredCreditCard()
        card.id = "12345"
        card.cardNumber = "4111111111111111"
        card.type = PaymentType.CARD_VISA
        card.description = "Visa 4111"
        return card
    }

    fun getOriginDestSuggestions(origin: String = "happyOrigin", destination: String = "happyDestination"): Pair<SuggestionV4, SuggestionV4> {
        val suggestionDest = SuggestionV4()
        val suggestionOrigin = SuggestionV4()
        suggestionDest.gaiaId = "12345"
        suggestionDest.regionNames = SuggestionV4.RegionNames()
        suggestionDest.regionNames.displayName = "London"
        suggestionDest.regionNames.fullName = "London, England"
        suggestionDest.regionNames.shortName = "London"
        suggestionDest.hierarchyInfo = SuggestionV4.HierarchyInfo()
        suggestionDest.hierarchyInfo!!.airport = SuggestionV4.Airport()
        suggestionDest.hierarchyInfo!!.airport!!.airportCode = destination
        suggestionDest.hierarchyInfo!!.airport!!.multicity = destination
        suggestionDest.coordinates = SuggestionV4.LatLng()

        suggestionOrigin.coordinates = SuggestionV4.LatLng()
        suggestionOrigin.gaiaId = "67891"
        suggestionOrigin.regionNames = SuggestionV4.RegionNames()
        suggestionOrigin.regionNames.displayName = "Paris"
        suggestionOrigin.regionNames.fullName = "Paris, France"
        suggestionOrigin.regionNames.shortName = "Paris"
        suggestionOrigin.hierarchyInfo = SuggestionV4.HierarchyInfo()
        suggestionOrigin.hierarchyInfo!!.airport = SuggestionV4.Airport()
        suggestionOrigin.hierarchyInfo!!.airport!!.airportCode = origin
        suggestionOrigin.hierarchyInfo!!.airport!!.multicity = origin

        return Pair<SuggestionV4, SuggestionV4>(suggestionDest, suggestionOrigin)
    }
}
