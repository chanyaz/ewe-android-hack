package com.expedia.bookings.test.robolectric

import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.packages.PackageApiError
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
import com.expedia.bookings.services.TestObserver
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
        val resultsSubscriber = TestObserver<PackageSearchType>()
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
        val resultsSubscriber = TestObserver<PackageSearchType>()
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

    private fun setUpParams(originAirportCode: String = ""): PackageSearchParams {
        val packageParams = PackageSearchParams.Builder(26, 329)
                .origin(getDummySuggestion(originAirportCode))
                .destination(getDummySuggestion())
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
}
