package com.expedia.bookings.test.robolectric

import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelCreateTripResponse
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageCreateTripResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.vm.packages.BundleOverviewViewModel
import com.expedia.vm.packages.PackageSearchType
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
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
        val resultsSubscriber = TestSubscriber<PackageSearchType>()
        sut.autoAdvanceObservable.subscribe(resultsSubscriber)

        sut.hotelParamsObservable.onNext(setUpParams())

        resultsSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        resultsSubscriber.assertNoTerminalEvent()
        resultsSubscriber.assertNoErrors()
        resultsSubscriber.assertValueCount(1)

        assertEquals(PackageSearchType.HOTEL, resultsSubscriber.onNextEvents[0])
    }

    @Test
    fun testHotelsError() {
        AbacusTestUtils.bucketTestAndEnableFeature(context, AbacusUtils.EBAndroidAppPackagesMidApi, R.string.preference_packages_mid_api)
        val errorSubscriber = TestSubscriber<PackageApiError.Code>()
        sut.errorObservable.subscribe(errorSubscriber)

        sut.hotelParamsObservable.onNext(setUpParams("error"))

        errorSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        errorSubscriber.assertNoTerminalEvent()
        errorSubscriber.assertNoErrors()

        assertEquals(PackageApiError.Code.mid_could_not_find_results, errorSubscriber.onNextEvents[0])
    }

    @Test
    fun testFlightsInbound() {
        val resultsSubscriber = TestSubscriber<PackageSearchType>()
        sut.autoAdvanceObservable.subscribe(resultsSubscriber)

        sut.flightParamsObservable.onNext(setUpParams())

        resultsSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        resultsSubscriber.assertNoTerminalEvent()
        resultsSubscriber.assertNoErrors()
        resultsSubscriber.assertValueCount(1)

        assertEquals(PackageSearchType.INBOUND_FLIGHT, resultsSubscriber.onNextEvents[0])
    }

    @Test
    fun testFlightsInboundError() {
        AbacusTestUtils.bucketTestAndEnableFeature(context, AbacusUtils.EBAndroidAppPackagesMidApi, R.string.preference_packages_mid_api)

        val errorSubscriber = TestSubscriber<PackageApiError.Code>()
        sut.errorObservable.subscribe(errorSubscriber)

        val params = setUpParams()
        params.selectedLegId = "error"
        sut.flightParamsObservable.onNext(params)

        errorSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        errorSubscriber.assertNoTerminalEvent()
        errorSubscriber.assertNoErrors()

        assertEquals(PackageApiError.Code.mid_could_not_find_results, errorSubscriber.onNextEvents[0])
    }

    @Test
    fun testFlightsOutbound() {
        val resultsSubscriber = TestSubscriber<PackageSearchType>()
        sut.autoAdvanceObservable.subscribe(resultsSubscriber)
        val params = setUpParams()
        params.packagePIID = "happy_outbound_flight"
        params.currentFlights = arrayOf("legs")
        sut.flightParamsObservable.onNext(params)

        resultsSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        resultsSubscriber.assertNoTerminalEvent()
        resultsSubscriber.assertNoErrors()
        resultsSubscriber.assertValueCount(1)

        assertEquals(PackageSearchType.OUTBOUND_FLIGHT, resultsSubscriber.onNextEvents[0])
    }

    @Test
    fun testFlightsOutboundError() {
        AbacusTestUtils.bucketTestAndEnableFeature(context, AbacusUtils.EBAndroidAppPackagesMidApi, R.string.preference_packages_mid_api)

        val errorSubscriber = TestSubscriber<PackageApiError.Code>()
        sut.errorObservable.subscribe(errorSubscriber)

        val params = setUpParams()
        params.ratePlanCode = "error"
        sut.flightParamsObservable.onNext(params)

        errorSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        errorSubscriber.assertNoTerminalEvent()
        errorSubscriber.assertNoErrors()

        assertEquals(PackageApiError.Code.mid_could_not_find_results, errorSubscriber.onNextEvents[0])
    }

    @Test
    fun testStepTitle() {
        val stepOneTestSubscriber = TestSubscriber<String>()
        val stepTwoTestSubscriber = TestSubscriber<String>()
        val stepThreeTestSubscriber = TestSubscriber<String>()

        sut.stepOneTextObservable.subscribe(stepOneTestSubscriber)
        sut.stepTwoTextObservable.subscribe(stepTwoTestSubscriber)
        sut.stepThreeTextObservale.subscribe(stepThreeTestSubscriber)

        sut.searchParamsChangeObservable.onNext(Unit)

        stepOneTestSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(stepOneTestSubscriber.onNextEvents[0],"Step 1: Select hotel")

        stepTwoTestSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(stepTwoTestSubscriber.onNextEvents[0],"Step 2: Select flights")

        stepThreeTestSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(stepThreeTestSubscriber.onNextEvents[0],"")
    }

    @Test
    fun testStepTitleWithBreadcrumbs() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppPackagesBreadcrumbsForNav)

        val stepOneTestSubscriber = TestSubscriber<String>()
        val stepTwoTestSubscriber = TestSubscriber<String>()
        val stepThreeTestSubscriber = TestSubscriber<String>()

        sut.stepOneTextObservable.subscribe(stepOneTestSubscriber)
        sut.stepTwoTextObservable.subscribe(stepTwoTestSubscriber)
        sut.stepThreeTextObservale.subscribe(stepThreeTestSubscriber)

        sut.searchParamsChangeObservable.onNext(Unit)

        stepOneTestSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(stepOneTestSubscriber.onNextEvents[0],"Step 1: Select hotel")

        stepTwoTestSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(stepTwoTestSubscriber.onNextEvents[0],"Step 2: Select outbound flight")

        stepThreeTestSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(stepThreeTestSubscriber.onNextEvents[0],"Step 3: Select inbound flight")
    }

    @Test
    fun testStepTitleAfterCreateTrip(){
        val stepOneTestSubscriber = TestSubscriber<String>()
        val stepTwoTestSubscriber = TestSubscriber<String>()
        val stepThreeTestSubscriber = TestSubscriber<String>()

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
        assertEquals(stepOneTestSubscriber.onNextEvents[1],"Hotel in New York - 1 room, 1 night")

        stepTwoTestSubscriber.awaitValueCount(2, 1, TimeUnit.SECONDS)
        assertEquals(stepTwoTestSubscriber.onNextEvents[1],"Flights - JFK to LHR, round trip")

        stepThreeTestSubscriber.awaitValueCount(2, 1, TimeUnit.SECONDS)
        assertEquals(stepThreeTestSubscriber.onNextEvents[1],"")
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
