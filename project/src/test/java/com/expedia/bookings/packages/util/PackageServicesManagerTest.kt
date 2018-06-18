package com.expedia.bookings.packages.util

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.services.PackageProductSearchType
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.tracking.ApiCallFailing
import com.expedia.bookings.utils.PackageResponseUtils
import io.reactivex.subjects.PublishSubject
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricRunner::class)
class PackageServicesManagerTest {
    val context = RuntimeEnvironment.application
    var serviceRule = ServicesRule(PackageServices::class.java)
        @Rule get

    lateinit var sut: PackageServicesManager

    @Before
    fun setup() {
        setUpParams()
        sut = PackageServicesManager(context, serviceRule.services!!)
    }

    private fun setUpParams(): PackageSearchParams {
        val packageParams = PackageSearchParams.Builder(26, 329)
                .origin(getDummySuggestion())
                .destination(getDummySuggestion())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .build() as PackageSearchParams
        Db.setPackageParams(packageParams)
        return packageParams
    }

    private fun getDummySuggestion(): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = ""
        suggestion.regionNames.fullName = ""
        suggestion.regionNames.shortName = ""
        suggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
        suggestion.hierarchyInfo!!.airport = SuggestionV4.Airport()
        suggestion.hierarchyInfo!!.airport!!.airportCode = "happy"
        suggestion.hierarchyInfo!!.airport!!.multicity = "happy"
        return suggestion
    }

    @Test
    fun testPackageHotelSearch() {
        val testSuccessHandler = TestObserver<Pair<PackageProductSearchType, BundleSearchResponse>>()
        val testErrorHandler = TestObserver<Triple<PackageProductSearchType, PackageApiError.Code, ApiCallFailing>>()

        val successHandler = PublishSubject.create<Pair<PackageProductSearchType, BundleSearchResponse>>()
        val errorHandler = PublishSubject.create<Triple<PackageProductSearchType, PackageApiError.Code, ApiCallFailing>>()

        successHandler.subscribe(testSuccessHandler)
        errorHandler.subscribe(testErrorHandler)

        sut.doPackageSearch(Db.sharedInstance.packageParams, PackageProductSearchType.MultiItemHotels, successHandler, errorHandler)

        testSuccessHandler.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(1, testSuccessHandler.valueCount())
        assertEquals(PackageProductSearchType.MultiItemHotels, testSuccessHandler.values()[0].first)
        val expectedResponse = testSuccessHandler.values()[0].second

        assert(Db.getPackageResponse() == expectedResponse)

        assertNotNull(Db.sharedInstance.packageParams.currentFlights)
        assertEquals("ddef7598d8ebe9fb4aaa50edf7f724fe", Db.sharedInstance.packageParams.currentFlights[0])
        assertEquals("52eb781f016058f4a517e8e802331956", Db.sharedInstance.packageParams.currentFlights[1])

        assertNotNull(Db.sharedInstance.packageParams.defaultFlights)
        assertEquals("ddef7598d8ebe9fb4aaa50edf7f724fe", Db.sharedInstance.packageParams.defaultFlights[0])
        assertEquals("52eb781f016058f4a517e8e802331956", Db.sharedInstance.packageParams.defaultFlights[1])

        assert(Db.sharedInstance.packageParams.currentFlights != Db.sharedInstance.packageParams.defaultFlights)

        val actualPackageResponse = PackageResponseUtils.recentPackageHotelsResponse
        assertNotNull(actualPackageResponse)
        assert(expectedResponse == actualPackageResponse)
    }

    @Test
    fun testPackageFlightOutboundSearch() {
        val testSuccessHandler = TestObserver<Pair<PackageProductSearchType, BundleSearchResponse>>()
        val testErrorHandler = TestObserver<Triple<PackageProductSearchType, PackageApiError.Code, ApiCallFailing>>()

        val successHandler = PublishSubject.create<Pair<PackageProductSearchType, BundleSearchResponse>>()
        val errorHandler = PublishSubject.create<Triple<PackageProductSearchType, PackageApiError.Code, ApiCallFailing>>()

        successHandler.subscribe(testSuccessHandler)
        errorHandler.subscribe(testErrorHandler)

        val params = Db.sharedInstance.packageParams
        params.latestSelectedOfferInfo.hotelId = "hotelID"
        params.latestSelectedOfferInfo.ratePlanCode = "flight_outbound_happy"
        params.latestSelectedOfferInfo.roomTypeCode = "flight_outbound_happy"

        sut.doPackageSearch(params, PackageProductSearchType.MultiItemOutboundFlights, successHandler, errorHandler)

        testSuccessHandler.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(1, testSuccessHandler.valueCount())
        assertEquals(PackageProductSearchType.MultiItemOutboundFlights, testSuccessHandler.values()[0].first)
        val expectedResponse = testSuccessHandler.values()[0].second

        assert(Db.getPackageResponse() == expectedResponse)

        val actualPackageResponse = PackageResponseUtils.recentPackageOutboundFlightsResponse
        assertNotNull(actualPackageResponse)
        assert(expectedResponse == actualPackageResponse)
    }

    @Test
    fun testPackageFlightInboundSearch() {
        val testSuccessHandler = TestObserver<Pair<PackageProductSearchType, BundleSearchResponse>>()
        val testErrorHandler = TestObserver<Triple<PackageProductSearchType, PackageApiError.Code, ApiCallFailing>>()

        val successHandler = PublishSubject.create<Pair<PackageProductSearchType, BundleSearchResponse>>()
        val errorHandler = PublishSubject.create<Triple<PackageProductSearchType, PackageApiError.Code, ApiCallFailing>>()

        successHandler.subscribe(testSuccessHandler)
        errorHandler.subscribe(testErrorHandler)

        val params = Db.sharedInstance.packageParams
        params.latestSelectedOfferInfo.hotelId = "hotelID"
        params.latestSelectedOfferInfo.ratePlanCode = "flight_outbound_happy"
        params.latestSelectedOfferInfo.roomTypeCode = "flight_outbound_happy"
        params.selectedLegId = "flight_inbound_happy"

        sut.doPackageSearch(params, PackageProductSearchType.MultiItemInboundFlights, successHandler, errorHandler)

        testSuccessHandler.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(1, testSuccessHandler.valueCount())
        assertEquals(PackageProductSearchType.MultiItemInboundFlights, testSuccessHandler.values()[0].first)
        val expectedResponse = testSuccessHandler.values()[0].second

        assert(Db.getPackageResponse() == expectedResponse)

        val actualPackageResponse = PackageResponseUtils.recentPackageInboundFlightsResponse
        assertNotNull(actualPackageResponse)
        assert(expectedResponse == actualPackageResponse)
    }

    @Test
    fun testPackageSearchErrorWithHTTPException() {
        testSearchError("error", PackageApiError.Code.mid_could_not_find_results, "MIS_INVALID_REQUEST")
    }

    @Test
    fun testPackageSearchNoInternetError() {
        testSearchError("malformed", PackageApiError.Code.no_internet, "no_internet")
    }

    @Test
    fun testPackageSearchErrorWithoutHTTPException() {
        testSearchError("garbage", PackageApiError.Code.pkg_error_code_not_mapped, "pkg_error_code_not_mapped")
    }

    private fun testSearchError(selectedLegId: String, expectedErrorCode: PackageApiError.Code, expectedErrorKey: String) {
        val testSuccessHandler = TestObserver<Pair<PackageProductSearchType, BundleSearchResponse>>()
        val testErrorHandler = TestObserver<Triple<PackageProductSearchType, PackageApiError.Code, ApiCallFailing>>()

        val successHandler = PublishSubject.create<Pair<PackageProductSearchType, BundleSearchResponse>>()
        val errorHandler = PublishSubject.create<Triple<PackageProductSearchType, PackageApiError.Code, ApiCallFailing>>()

        successHandler.subscribe(testSuccessHandler)
        errorHandler.subscribe(testErrorHandler)

        val params = Db.sharedInstance.packageParams
        params.latestSelectedOfferInfo.hotelId = "hotelID"
        params.latestSelectedOfferInfo.ratePlanCode = "flight_outbound_happy"
        params.latestSelectedOfferInfo.roomTypeCode = "flight_outbound_happy"
        params.selectedLegId = selectedLegId

        sut.doPackageSearch(params, PackageProductSearchType.MultiItemInboundFlights, successHandler, errorHandler)

        testErrorHandler.awaitValueCount(1, 1, TimeUnit.SECONDS)
        assertEquals(PackageProductSearchType.MultiItemInboundFlights, testErrorHandler.values()[0].first)
        assertEquals(expectedErrorCode, testErrorHandler.values()[0].second)
        assert(testErrorHandler.values()[0].third is ApiCallFailing.PackageFlightInbound)
        assertEquals(expectedErrorKey, testErrorHandler.values()[0].third.errorCode)
    }
}
