package com.expedia.bookings.test.robolectric

import android.text.SpannableStringBuilder
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.data.packages.PackageSelectedOfferInfo
import com.expedia.bookings.packages.util.PackageServicesManager
import com.expedia.bookings.packages.vm.BundleOverviewViewModel
import com.expedia.bookings.services.PackageProductSearchType
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.MockPackageServiceTestRule
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.tracking.ApiCallFailing
import com.expedia.bookings.utils.Constants
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class BundleOverviewViewModelTests {
    val context = RuntimeEnvironment.application
    var serviceRule = ServicesRule(PackageServices::class.java)
        @Rule get

    val mockPackageServiceRule: MockPackageServiceTestRule = MockPackageServiceTestRule()
        @Rule get

    lateinit var sut: BundleOverviewViewModel

    @Before
    fun setup() {
        setUpParams()
        sut = BundleOverviewViewModel(context, PackageServicesManager(context, serviceRule.services!!))
    }

    @Test
    fun testHotels() {
        val resultsSubscriber = TestObserver<PackageProductSearchType>()
        sut.autoAdvanceObservable.subscribe(resultsSubscriber)
        val params = setUpParams("happy")

        sut.hotelParamsObservable.onNext(params)

        resultsSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        resultsSubscriber.assertNotTerminated()
        resultsSubscriber.assertNoErrors()
        resultsSubscriber.assertValueCount(1)

        assertEquals(PackageProductSearchType.MultiItemHotels, resultsSubscriber.values()[0])
        assertNotNull(Db.getUnfilteredRespnse())
    }

    @Test
    fun testHotelSearchError() {
        val errorSubscriber = TestObserver<Pair<PackageApiError.Code, ApiCallFailing>>()
        sut.errorObservable.subscribe(errorSubscriber)

        var params = setUpParams("error")
        sut.hotelParamsObservable.onNext(params)

        errorSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        errorSubscriber.assertNotTerminated()
        errorSubscriber.assertNoErrors()

        var apiCallFailingDetails = errorSubscriber.values()[0].second

        assertEquals(PackageApiError.Code.mid_could_not_find_results, errorSubscriber.values()[0].first)
        assertEquals("MIS_INVALID_REQUEST", apiCallFailingDetails.errorCode)
        assertEquals("PACKAGE_HOTEL_SEARCH", apiCallFailingDetails.apiCall)

        // Change Hotel
        params.pageType = Constants.PACKAGE_CHANGE_FLIGHT
        sut.hotelParamsObservable.onNext(params)
        errorSubscriber.awaitValueCount(2, 1, TimeUnit.SECONDS)

        apiCallFailingDetails = errorSubscriber.values()[1].second

        assertEquals(PackageApiError.Code.mid_could_not_find_results, errorSubscriber.values()[1].first)
        assertEquals("MIS_INVALID_REQUEST", apiCallFailingDetails.errorCode)
        assertEquals("PACKAGE_HOTEL_SEARCH_CHANGE", apiCallFailingDetails.apiCall)
        assertNull( Db.getUnfilteredRespnse())
    }

    @Test
    fun testResponseWithNoOffers() {
        val errorSubscriber = TestObserver<Pair<PackageApiError.Code, ApiCallFailing>>()
        sut.errorObservable.subscribe(errorSubscriber)

        var params = setUpParams("no_offers")
        sut.hotelParamsObservable.onNext(params)

        errorSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)

        var apiCallFailingDetails = errorSubscriber.values()[0].second

        assertEquals(PackageApiError.Code.search_response_null, errorSubscriber.values()[0].first)
        assertEquals("search_response_null", apiCallFailingDetails.errorCode)
        assertEquals("PACKAGE_HOTEL_SEARCH", apiCallFailingDetails.apiCall)
        assertNull(Db.getUnfilteredRespnse())
    }

    @Test
    fun testUnknownErrorThrown() {
        val errorSubscriber = TestObserver<Pair<PackageApiError.Code, ApiCallFailing>>()
        sut.errorObservable.subscribe(errorSubscriber)

        var params = setUpParams("invalid_hotel_star_rating_for_unknown_error")
        sut.hotelParamsObservable.onNext(params)

        errorSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)

        var apiCallFailingDetails = errorSubscriber.values()[0].second

        assertEquals(PackageApiError.Code.pkg_error_code_not_mapped, errorSubscriber.values()[0].first)
        assertEquals("pkg_error_code_not_mapped", apiCallFailingDetails.errorCode)
        assertEquals("PACKAGE_HOTEL_SEARCH", apiCallFailingDetails.apiCall)
        assertNull(Db.getUnfilteredRespnse())
    }

    @Test
    fun testFlightsInbound() {
        val resultsSubscriber = TestObserver<PackageProductSearchType>()
        sut.autoAdvanceObservable.subscribe(resultsSubscriber)
        val params = setUpParams()
        params.selectedLegId = "flight_outbound_happy"
        sut.flightParamsObservable.onNext(params)

        resultsSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        resultsSubscriber.assertNotTerminated()
        resultsSubscriber.assertNoErrors()
        resultsSubscriber.assertValueCount(1)

        assertEquals(PackageProductSearchType.MultiItemInboundFlights, resultsSubscriber.values()[0])
        assertNull(Db.getUnfilteredRespnse())
    }

    @Test
    fun testFlightsInboundError() {
        val errorSubscriber = TestObserver<Pair<PackageApiError.Code, ApiCallFailing>>()
        sut.errorObservable.subscribe(errorSubscriber)

        val params = setUpParams()
        params.selectedLegId = "error"
        sut.flightParamsObservable.onNext(params)

        errorSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        errorSubscriber.assertNotTerminated()
        errorSubscriber.assertNoErrors()

        var apiCallFailingDetails = errorSubscriber.values()[0].second

        assertEquals(PackageApiError.Code.mid_could_not_find_results, errorSubscriber.values()[0].first)
        assertEquals("MIS_INVALID_REQUEST", apiCallFailingDetails.errorCode)
        assertEquals("PACKAGE_FLIGHT_INBOUND", apiCallFailingDetails.apiCall)

        // Change inbound flight
        params.pageType = Constants.PACKAGE_CHANGE_FLIGHT
        sut.flightParamsObservable.onNext(params)
        errorSubscriber.awaitValueCount(2, 1, TimeUnit.SECONDS)

        apiCallFailingDetails = errorSubscriber.values()[1].second

        assertEquals(PackageApiError.Code.mid_could_not_find_results, errorSubscriber.values()[1].first)
        assertEquals("MIS_INVALID_REQUEST", apiCallFailingDetails.errorCode)
        assertEquals("PACKAGE_FLIGHT_INBOUND_CHANGE", apiCallFailingDetails.apiCall)
    }

    @Test
    fun testFlightsOutbound() {
        val resultsSubscriber = TestObserver<PackageProductSearchType>()
        sut.autoAdvanceObservable.subscribe(resultsSubscriber)
        val params = setUpParams()
        params.latestSelectedOfferInfo.ratePlanCode = "flight_outbound_happy"
        sut.flightParamsObservable.onNext(params)

        resultsSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        resultsSubscriber.assertNotTerminated()
        resultsSubscriber.assertNoErrors()
        resultsSubscriber.assertValueCount(1)

        assertEquals(PackageProductSearchType.MultiItemOutboundFlights, resultsSubscriber.values()[0])
        assertNull(Db.getUnfilteredRespnse())
    }

    @Test
    fun testFlightsOutboundError() {
        val errorSubscriber = TestObserver<Pair<PackageApiError.Code, ApiCallFailing>>()
        sut.errorObservable.subscribe(errorSubscriber)

        val params = setUpParams()
        params.latestSelectedOfferInfo.ratePlanCode = "error"
        sut.flightParamsObservable.onNext(params)

        errorSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        errorSubscriber.assertNotTerminated()
        errorSubscriber.assertNoErrors()

        var apiCallFailingDetails = errorSubscriber.values()[0].second

        assertEquals(PackageApiError.Code.mid_could_not_find_results, errorSubscriber.values()[0].first)
        assertEquals("MIS_INVALID_REQUEST", apiCallFailingDetails.errorCode)
        assertEquals("PACKAGE_FLIGHT_OUTBOUND", apiCallFailingDetails.apiCall)

        // Change outbound flight
        params.pageType = Constants.PACKAGE_CHANGE_FLIGHT
        sut.flightParamsObservable.onNext(params)
        errorSubscriber.awaitValueCount(2, 1, TimeUnit.SECONDS)

        apiCallFailingDetails = errorSubscriber.values()[1].second

        assertEquals(PackageApiError.Code.mid_could_not_find_results, errorSubscriber.values()[1].first)
        assertEquals("MIS_INVALID_REQUEST", apiCallFailingDetails.errorCode)
        assertEquals("PACKAGE_FLIGHT_OUTBOUND_CHANGE", apiCallFailingDetails.apiCall)
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
        assertEquals(stepTwoTestSubscriber.values()[0], "Step 2: Select outbound flight")

        stepThreeTestSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(stepThreeTestSubscriber.values()[0], "Step 3: Select inbound flight")
    }

    @Test
    fun testStepTitleAfterCreateTripWithSplitTicketsTrue() {
        val splitTicketSubscriber = TestObserver<SpannableStringBuilder>()
        val showSplitTicketSubsciber = TestObserver<Boolean>()

        sut.splitTicketBaggageFeesLinksObservable.subscribe(splitTicketSubscriber)
        sut.showSplitTicketMessagingObservable.subscribe(showSplitTicketSubsciber)

        setHotelResponseAndPackageSelectedHotel()
        setUpParams("JFK", true)

        sut.getHotelNameAndDaysToSetUpTitle()

        assertEquals(1, splitTicketSubscriber.valueCount())
        assertEquals(true, showSplitTicketSubsciber.values()[0])
    }

    @Test
    fun testStepTitleAfterCreateTripWithSplitTicketsFalse() {
        val splitTicketSubscriber = TestObserver<SpannableStringBuilder>()
        val showSplitTicketSubsciber = TestObserver<Boolean>()

        sut.splitTicketBaggageFeesLinksObservable.subscribe(splitTicketSubscriber)
        sut.showSplitTicketMessagingObservable.subscribe(showSplitTicketSubsciber)

        setHotelResponseAndPackageSelectedHotel()
        setUpParams("JFK", false)

        sut.getHotelNameAndDaysToSetUpTitle()

        assertEquals(0, splitTicketSubscriber.valueCount())
        assertEquals(false, showSplitTicketSubsciber.values()[0])
    }

    @Test
    fun testStepTitleAfterCreateTripWithSplitTicketsTrueOnlyOutboundUrl() {
        val splitTicketSubscriber = TestObserver<SpannableStringBuilder>()
        val showSplitTicketSubsciber = TestObserver<Boolean>()

        sut.splitTicketBaggageFeesLinksObservable.subscribe(splitTicketSubscriber)
        sut.showSplitTicketMessagingObservable.subscribe(showSplitTicketSubsciber)

        setHotelResponseAndPackageSelectedHotel()
        setUpParams("JFK", true, true, false)

        sut.getHotelNameAndDaysToSetUpTitle()

        assertEquals(0, splitTicketSubscriber.valueCount())
        assertEquals(true, showSplitTicketSubsciber.values()[0])
    }

    @Test
    fun testStepTitleAfterCreateTripWithSplitTicketsTrueOnlyInboundUrl() {
        val splitTicketSubscriber = TestObserver<SpannableStringBuilder>()
        val showSplitTicketSubsciber = TestObserver<Boolean>()

        sut.splitTicketBaggageFeesLinksObservable.subscribe(splitTicketSubscriber)
        sut.showSplitTicketMessagingObservable.subscribe(showSplitTicketSubsciber)

        setHotelResponseAndPackageSelectedHotel()
        setUpParams("JFK", true, false, true)

        sut.getHotelNameAndDaysToSetUpTitle()

        assertEquals(0, splitTicketSubscriber.valueCount())
        assertEquals(true, showSplitTicketSubsciber.values()[0])
    }

    private fun setHotelResponseAndPackageSelectedHotel() {
        val hotelResponse = mockPackageServiceRule.getMIDHotelResponse()
        Db.setPackageResponse(hotelResponse)
        PackageTestUtil.setDbPackageSelectedHotel()
    }

    private fun setUpParams(originAirportCode: String = "", splitTicket: Boolean = false, outboundUrlPresent: Boolean = true, inboundUrlPresent: Boolean = true): PackageSearchParams {
        val packageParams = PackageSearchParams.Builder(26, 329)
                .origin(getDummySuggestion(originAirportCode))
                .destination(getDummySuggestion("LHR"))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build() as PackageSearchParams

        addLatestSelectedOffer(splitTicket, packageParams, outboundUrlPresent, inboundUrlPresent)

        Db.setPackageParams(packageParams)
        return packageParams
    }

    private fun addLatestSelectedOffer(isSplitTicket: Boolean, packageParams: PackageSearchParams, inboundUrlPresent: Boolean, outboundUrlPresent: Boolean): PackageSearchParams {
        val selectedOffer = PackageSelectedOfferInfo()
        selectedOffer.isSplitTicketFlights = isSplitTicket

        if (isSplitTicket) {
            if (outboundUrlPresent) {
                selectedOffer.outboundFlightBaggageFeesUrl = "outboundFlightBaggageFeesUrl"
            }

            if (inboundUrlPresent) {
                selectedOffer.inboundFlightBaggageFeesUrl = "inboundFlightBaggageFeesUrl"
            }
        }

        packageParams.latestSelectedOfferInfo = selectedOffer
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
}
