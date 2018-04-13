package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.packages.vm.BundleOverviewViewModel
import com.expedia.bookings.packages.vm.PackageSearchType
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class BundleOverviewViewModelTests {
    val context = RuntimeEnvironment.application
    var serviceRule = ServicesRule(PackageServices::class.java)
        @Rule get

    lateinit var sut: BundleOverviewViewModel

    @Before
    fun setup() {
        setUpParams()
        sut = BundleOverviewViewModel(context, serviceRule.services!!)
    }

    @Test
    fun testHotels() {
        val resultsSubscriber = TestObserver<PackageSearchType>()
        sut.autoAdvanceObservable.subscribe(resultsSubscriber)
        val params = setUpParams("happy")

        sut.hotelParamsObservable.onNext(params)

        resultsSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        resultsSubscriber.assertNotTerminated()
        resultsSubscriber.assertNoErrors()
        resultsSubscriber.assertValueCount(1)

        assertEquals(PackageSearchType.HOTEL, resultsSubscriber.values()[0])
    }

    @Test
    fun testHotelsError() {
        val errorSubscriber = TestObserver<PackageApiError.Code>()
        sut.errorObservable.subscribe(errorSubscriber)

        sut.hotelParamsObservable.onNext(setUpParams("error"))

        errorSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        errorSubscriber.assertNotTerminated()
        errorSubscriber.assertNoErrors()

        assertEquals(PackageApiError.Code.mid_could_not_find_results, errorSubscriber.values()[0])
    }

    @Test
    fun testFlightsInbound() {
        val resultsSubscriber = TestObserver<PackageSearchType>()
        sut.autoAdvanceObservable.subscribe(resultsSubscriber)
        val params = setUpParams()
        params.selectedLegId = "flight_outbound_happy"
        sut.flightParamsObservable.onNext(params)

        resultsSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        resultsSubscriber.assertNotTerminated()
        resultsSubscriber.assertNoErrors()
        resultsSubscriber.assertValueCount(1)

        assertEquals(PackageSearchType.INBOUND_FLIGHT, resultsSubscriber.values()[0])
    }

    @Test
    fun testFlightsInboundError() {
        val errorSubscriber = TestObserver<PackageApiError.Code>()
        sut.errorObservable.subscribe(errorSubscriber)

        val params = setUpParams()
        params.selectedLegId = "error"
        sut.flightParamsObservable.onNext(params)

        errorSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        errorSubscriber.assertNotTerminated()
        errorSubscriber.assertNoErrors()

        assertEquals(PackageApiError.Code.mid_could_not_find_results, errorSubscriber.values()[0])
    }

    @Test
    fun testFlightsOutbound() {
        val resultsSubscriber = TestObserver<PackageSearchType>()
        sut.autoAdvanceObservable.subscribe(resultsSubscriber)
        val params = setUpParams()
        params.latestSelectedOfferInfo.ratePlanCode = "flight_outbound_happy"
        sut.flightParamsObservable.onNext(params)

        resultsSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        resultsSubscriber.assertNotTerminated()
        resultsSubscriber.assertNoErrors()
        resultsSubscriber.assertValueCount(1)

        assertEquals(PackageSearchType.OUTBOUND_FLIGHT, resultsSubscriber.values()[0])
    }

    @Test
    fun testFlightsOutboundError() {
        val errorSubscriber = TestObserver<PackageApiError.Code>()
        sut.errorObservable.subscribe(errorSubscriber)

        val params = setUpParams()
        params.latestSelectedOfferInfo.ratePlanCode = "error"
        sut.flightParamsObservable.onNext(params)

        errorSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        errorSubscriber.assertNotTerminated()
        errorSubscriber.assertNoErrors()

        assertEquals(PackageApiError.Code.mid_could_not_find_results, errorSubscriber.values()[0])
    }

    @Test
    fun testStepTitle() {
        val stepOneTestSubscriber = TestObserver<String>()
        val stepTwoTestSubscriber = TestObserver<String>()
        val stepThreeTestSubscriber = TestObserver<String>()

        sut.stepOneTextObservable.subscribe(stepOneTestSubscriber)
        sut.stepTwoTextObservable.subscribe(stepTwoTestSubscriber)
        sut.stepThreeTextObservale.subscribe(stepThreeTestSubscriber)

        sut.searchParamsChangeObservable.onNext(Unit)

        stepOneTestSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(stepOneTestSubscriber.values()[0], "Step 1: Select hotel")

        stepTwoTestSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(stepTwoTestSubscriber.values()[0], "Step 2: Select flights")

        stepThreeTestSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(stepThreeTestSubscriber.values()[0], "")
    }

    @Test
    fun testStepTitleWithBreadcrumbs() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppPackagesBreadcrumbsForNav)
        val stepOneTestSubscriber = TestObserver<String>()
        val stepTwoTestSubscriber = TestObserver<String>()
        val stepThreeTestSubscriber = TestObserver<String>()

        sut.stepOneTextObservable.subscribe(stepOneTestSubscriber)
        sut.stepTwoTextObservable.subscribe(stepTwoTestSubscriber)
        sut.stepThreeTextObservale.subscribe(stepThreeTestSubscriber)

        sut.searchParamsChangeObservable.onNext(Unit)

        stepOneTestSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(stepOneTestSubscriber.values()[0], "Step 1: Select hotel")

        stepTwoTestSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(stepTwoTestSubscriber.values()[0], "Step 2: Select outbound flight")

        stepThreeTestSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(stepThreeTestSubscriber.values()[0], "Step 3: Select inbound flight")
    }

    @Test
    fun testStepTitleAfterCreateTrip() {
        val stepOneTestSubscriber = TestObserver<String>()
        val stepTwoTestSubscriber = TestObserver<String>()
        val stepThreeTestSubscriber = TestObserver<String>()

        sut.stepOneTextObservable.subscribe(stepOneTestSubscriber)
        sut.stepTwoTextObservable.subscribe(stepTwoTestSubscriber)
        sut.stepThreeTextObservale.subscribe(stepThreeTestSubscriber)

        setUpParams("JFK")

        sut.searchParamsChangeObservable.onNext(Unit)
        val createTripResponse = PackageTestUtil.getCreateTripResponse(currency = "USD",
                bundleTotal = 1000,
                packageTotal = 950,
                hotelLargeThumbnailUrl = "/testurl",
                hotelCity = "New York",
                hotelStateProvince = "NY",
                hotelCountry = "USA",
                hotelCheckinDate = "2017-12-29",
                hotelCheckoutOutDate = "2017-12-29",
                hotelNumberOfNights = "1")
        sut.createTripObservable.onNext(createTripResponse)

        stepOneTestSubscriber.awaitValueCount(2, 1, TimeUnit.SECONDS)
        assertEquals(stepOneTestSubscriber.values()[1], "Hotel in New York - 1 room, 1 night")

        stepTwoTestSubscriber.awaitValueCount(2, 1, TimeUnit.SECONDS)
        assertEquals(stepTwoTestSubscriber.values()[1], "Flights - JFK to LHR, round trip")

        stepThreeTestSubscriber.awaitValueCount(2, 1, TimeUnit.SECONDS)
        assertEquals(stepThreeTestSubscriber.values()[1], "")
    }

    private fun setUpParams(originAirportCode: String = ""): PackageSearchParams {
        val packageParams = PackageSearchParams.Builder(26, 329)
                .origin(getDummySuggestion(originAirportCode))
                .destination(getDummySuggestion("LHR"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build() as PackageSearchParams
        Db.setPackageParams(packageParams)
        return packageParams
    }

    private fun getDummySuggestion(airportCode: String = ""): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = ""
        suggestion.regionNames.fullName = ""
        suggestion.regionNames.shortName = ""
        suggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
        suggestion.hierarchyInfo!!.airport = SuggestionV4.Airport()
        suggestion.hierarchyInfo!!.airport!!.airportCode = airportCode
        suggestion.hierarchyInfo!!.airport!!.multicity = "happy"
        return suggestion
    }

    private fun makeCreateTripResponse(): PackageCreateTripResponse {
        val trip = PackageCreateTripResponse()
        val packageDetails = PackageCreateTripResponse.PackageDetails()
        packageDetails.pricing = PackageCreateTripResponse.Pricing()
        packageDetails.pricing.bundleTotal = Money(1000, "USD")
        packageDetails.pricing.packageTotal = Money(950, "USD")
        val hotel = HotelCreateTripResponse.HotelProductResponse()
        hotel.largeThumbnailUrl = "/testurl"
        hotel.hotelCity = "New York"
        hotel.hotelStateProvince = "NY"
        hotel.hotelCountry = "USA"
        hotel.checkInDate = "2017-12-29"
        hotel.checkOutDate = "2017-12-29"
        hotel.numberOfNights = "1"

        trip.packageDetails = packageDetails
        packageDetails.hotel = hotel

        return trip
    }
}
