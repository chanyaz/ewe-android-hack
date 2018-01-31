package com.expedia.bookings.test

import android.app.Activity
import com.expedia.bookings.data.BillingInfo
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Location
import com.expedia.bookings.data.PaymentType
import com.expedia.bookings.data.StoredCreditCard
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.packages.PackageCheckoutParams
import com.expedia.bookings.data.packages.PackageCheckoutResponse
import com.expedia.bookings.data.packages.PackageCreateTripParams
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.packages.PackageOffersResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.services.ProductSearchType
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.Constants
import org.joda.time.LocalDate
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class MockPackageServiceTestRule : ServicesRule<PackageServices>(PackageServices::class.java) {

    var activity: Activity by Delegates.notNull()

    fun getPSSHotelSearchResponse(): BundleSearchResponse {
        val observer = TestObserver<BundleSearchResponse>()
        val params = PackageSearchParams.Builder(26, 329)
                .flightCabinClass("coach")
                .infantSeatingInLap(true)
                .origin(getOriginDestSuggestions().second)
                .destination(getOriginDestSuggestions().first)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .adults(1)
                .children(listOf(16, 10, 1))
                .build() as PackageSearchParams

        Db.setPackageParams(params)

        services?.packageSearch(params, ProductSearchType.OldPackageSearch)!!.subscribe(observer)
        observer.awaitTerminalEvent()

        return observer.values()[0]
    }

    fun getPSSOffersSearchResponse(fileName: String): PackageOffersResponse {
        val observer = TestObserver<PackageOffersResponse>()
        val params = PackageSearchParams.Builder(0, 0).destination(getOriginDestSuggestions().first).origin(getOriginDestSuggestions().second)
                .adults(1).children(listOf(12, 14)).startDate(LocalDate.now()).endDate(LocalDate.now().plusDays(2)).build() as PackageSearchParams
        params.packagePIID = fileName
        Db.setPackageParams(params)

        services?.hotelOffer(params.packagePIID!!, params.startDate.toString(), params.endDate.toString(),
                params.latestSelectedOfferInfo.ratePlanCode, params.latestSelectedOfferInfo.roomTypeCode, params.adults, params.childAges!![0].toInt())!!.subscribe(observer)
        observer.awaitTerminalEvent()

        return observer.values()[0]
    }

    fun getPSSFlightOutboundSearchResponse(fileName: String): BundleSearchResponse? {
        val observer = TestObserver<BundleSearchResponse>()
        val params = PackageSearchParams.Builder(0, 0).destination(getOriginDestSuggestions().first).origin(getOriginDestSuggestions().second)
                .adults(1).children(listOf(12, 14)).startDate(LocalDate.now()).endDate(LocalDate.now().plusDays(2)).build() as PackageSearchParams
        params.packagePIID = fileName
        params.searchProduct = Constants.PRODUCT_FLIGHT
        params.currentFlights = arrayOf("legs")
        Db.setPackageParams(params)

        services?.packageSearch(params, ProductSearchType.OldPackageSearch)!!.subscribe(observer)
        services?.packageSearch(params, ProductSearchType.OldPackageSearch)!!.subscribe(observer)
        observer.awaitTerminalEvent()

        return observer.values()[0]
    }

    fun getPSSFlightInboundSearchResponse(fileName: String): BundleSearchResponse? {
        val observer = TestObserver<BundleSearchResponse>()
        val params = PackageSearchParams.Builder(0, 0).destination(getOriginDestSuggestions().first).origin(getOriginDestSuggestions().second)
                .adults(1).children(listOf(12, 14)).startDate(LocalDate.now()).endDate(LocalDate.now().plusDays(2)).build() as PackageSearchParams
        params.packagePIID = fileName
        params.searchProduct = Constants.PRODUCT_FLIGHT
        params.selectedLegId = "happy"
        Db.setPackageParams(params)

        services?.packageSearch(params, ProductSearchType.OldPackageSearch)!!.subscribe(observer)
        observer.awaitTerminalEvent()

        return observer.values()[0]
    }

    fun getPSSCreateTripResponse(fileName: String): PackageCreateTripResponse? {
        val observer = TestObserver<PackageCreateTripResponse>()
        val params = PackageCreateTripParams(fileName, "", 1, false, emptyList())

        services?.createTrip(params)!!.subscribe(observer)
        observer.awaitTerminalEvent(10, TimeUnit.SECONDS)
        return observer.values()[0]
    }

    fun getPSSCheckoutResponse(): PackageCheckoutResponse? {
        val obsever = TestObserver<PackageCheckoutResponse>()
        val travelers = arrayListOf(getTraveler(), getTraveler(), getTraveler())
        val billing = getBillingInfo()
        billing.storedCard = getStoredCard()
        val builder = PackageCheckoutParams.Builder()
                .billingInfo(billing)
                .travelers(travelers)
                .cvv("123") as PackageCheckoutParams.Builder
        val params = builder.bedType("")
                .expectedFareCurrencyCode("")
                .expectedTotalFare("")
                .tripId("")
                .build()
        services?.checkout(params.toQueryMap())!!.subscribe(obsever)
        obsever.awaitTerminalEvent(10, TimeUnit.SECONDS)
        return obsever.values()[0]
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

    fun getOriginDestSuggestions(): Pair<SuggestionV4, SuggestionV4> {
        val suggestionDest = SuggestionV4()
        val suggestionOrigin = SuggestionV4()
        suggestionDest.gaiaId = "12345"
        suggestionDest.regionNames = SuggestionV4.RegionNames()
        suggestionDest.regionNames.displayName = "London"
        suggestionDest.regionNames.fullName = "London, England"
        suggestionDest.regionNames.shortName = "London"
        suggestionDest.hierarchyInfo = SuggestionV4.HierarchyInfo()
        suggestionDest.hierarchyInfo!!.airport = SuggestionV4.Airport()
        suggestionDest.hierarchyInfo!!.airport!!.airportCode = "happyDest"
        suggestionDest.hierarchyInfo!!.airport!!.multicity = "happyDest"
        suggestionDest.coordinates = SuggestionV4.LatLng()

        suggestionOrigin.coordinates = SuggestionV4.LatLng()
        suggestionOrigin.gaiaId = "67891"
        suggestionOrigin.regionNames = SuggestionV4.RegionNames()
        suggestionOrigin.regionNames.displayName = "Paris"
        suggestionOrigin.regionNames.fullName = "Paris, France"
        suggestionOrigin.regionNames.shortName = "Paris"
        suggestionOrigin.hierarchyInfo = SuggestionV4.HierarchyInfo()
        suggestionOrigin.hierarchyInfo!!.airport = SuggestionV4.Airport()
        suggestionOrigin.hierarchyInfo!!.airport!!.airportCode = "happyOrigin"
        suggestionOrigin.hierarchyInfo!!.airport!!.multicity = "happyOrigin"

        return Pair<SuggestionV4, SuggestionV4>(suggestionDest, suggestionOrigin)
    }
}
