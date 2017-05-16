package com.expedia.bookings.test.robolectric

import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.vm.packages.BundleOverviewViewModel
import com.expedia.vm.packages.PackageSearchType
import com.mobiata.android.util.SettingUtils
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
        setRemoveBundleOverviewScreenTest()
        setUpParams()
        sut = BundleOverviewViewModel(context, serviceRule.services!!)
    }

    private fun setRemoveBundleOverviewScreenTest() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppPackagesRemoveBundleOverview)
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
    fun testFlightsOutbound() {
        val resultsSubscriber = TestSubscriber<PackageSearchType>()
        sut.autoAdvanceObservable.subscribe(resultsSubscriber)
        val params = setUpParams()
        params.packagePIID = "packagePIID"
        sut.flightParamsObservable.onNext(params)

        resultsSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        resultsSubscriber.assertNoTerminalEvent()
        resultsSubscriber.assertNoErrors()
        resultsSubscriber.assertValueCount(1)

        assertEquals(PackageSearchType.OUTBOUND_FLIGHT, resultsSubscriber.onNextEvents[0])
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
        suggestion.hierarchyInfo!!.airport!!.airportCode = ""
        suggestion.hierarchyInfo!!.airport!!.multicity = "happy"
        return suggestion
    }
}
