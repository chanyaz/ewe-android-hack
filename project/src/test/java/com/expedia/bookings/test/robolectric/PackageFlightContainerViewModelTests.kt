package com.expedia.bookings.test.robolectric

import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.vm.packages.PackageFlightContainerViewModel
import com.expedia.vm.packages.PackageSearchType
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit

@RunWith(RobolectricRunner::class)
class PackageFlightContainerViewModelTests {

    val context = RuntimeEnvironment.application
    var serviceRule = ServicesRule(PackageServices::class.java)
        @Rule get

    lateinit var sut: PackageFlightContainerViewModel

    @Before
    fun setup() {
        setUpParams()
        sut = PackageFlightContainerViewModel(context, serviceRule.services!!)
    }

    @Test
    fun inboundFlightSearch() {
        val resultsSubscriber = TestSubscriber<BundleSearchResponse>()
        sut.flightSearchResponseObservable.subscribe(resultsSubscriber)
        sut.performFlightSearch.onNext(PackageSearchType.INBOUND_FLIGHT)

        resultsSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        resultsSubscriber.assertNoTerminalEvent()
        resultsSubscriber.assertNoErrors()
        resultsSubscriber.assertValueCount(1)
    }

    @Test
    fun outboundFlightSearch() {
        val resultsSubscriber = TestSubscriber<BundleSearchResponse>()
        sut.flightSearchResponseObservable.subscribe(resultsSubscriber)
        sut.performFlightSearch.onNext(PackageSearchType.OUTBOUND_FLIGHT)

        resultsSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        resultsSubscriber.assertNoTerminalEvent()
        resultsSubscriber.assertNoErrors()
        resultsSubscriber.assertValueCount(1)
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
