package com.expedia.bookings.packages.vm

import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.packages.util.PackageServicesManager
import com.expedia.bookings.services.PackageProductSearchType
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.testrule.ServicesRule
import org.junit.Before
import org.junit.Rule
import java.util.concurrent.TimeUnit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackageHotelResultsViewModelTest {

    val context = RuntimeEnvironment.application
    lateinit var checkInDate: LocalDate
    lateinit var checkOutDate: LocalDate

    var serviceRule = ServicesRule(PackageServices::class.java)
        @Rule get

    lateinit var sut: PackageHotelResultsViewModel

    @Before
    fun setup() {
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
        sut = PackageHotelResultsViewModel(activity, PackageServicesManager(context, serviceRule.services!!))
    }

    @Test
    fun testPackageSearchResponse() {
        Db.setPackageParams(setUpParams())

        val titleSubscriber = TestObserver<String>()
        val subtitleSubscriber = TestObserver<CharSequence>()
        sut.titleSubject.subscribe(titleSubscriber)
        sut.subtitleSubject.subscribe(subtitleSubscriber)
        sut.paramsSubject.onNext(makeHappyParams())
        assertEquals("DisplayName", titleSubscriber.values()[0])
        assert("Aug 18 - Aug 21, 1 guest".equals(subtitleSubscriber.values()[0].toString(), ignoreCase = true))
    }

    @Test
    fun testPackageHotelFilterResponse() {
        Db.setPackageParams(setUpParams())
        val testPackageResponseSubscriber = TestObserver<Pair<PackageProductSearchType, BundleSearchResponse>>()
        val testFilterResponseSubscriber = TestObserver<HotelSearchResponse>()
        sut.filterSearchSuccessResponseHandler.subscribe(testPackageResponseSubscriber)
        sut.filterResultsObservable.subscribe(testFilterResponseSubscriber)
        sut.filterChoicesSubject.onNext(buildUserFilterChoices())
        testPackageResponseSubscriber.awaitTerminalEvent(2, TimeUnit.SECONDS)
        assertTrue(sut.isFilteredResponse)
        assertTrue(testFilterResponseSubscriber.values()[0].isFilteredResponse)
        assertNotNull(testPackageResponseSubscriber.values()[0].second)
    }

    private fun setUpParams(): PackageSearchParams {
        val packageParams = PackageSearchParams.Builder(26, 329)
                .origin(getDummySuggestion())
                .destination(getDummySuggestion())
                .startDate(LocalDate.parse("2025-08-18"))
                .endDate(LocalDate.parse("2025-08-19"))
                .build() as PackageSearchParams
        Db.setPackageParams(packageParams)
        return packageParams
    }

    private fun buildUserFilterChoices(): UserFilterChoices {
        val filterChoices = UserFilterChoices()

        filterChoices.isVipOnlyAccess = true
        filterChoices.name = "Test_Hotel"

        return filterChoices
    }

    private fun getDummySuggestion(): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = ""
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = "happy"
        suggestion.regionNames.fullName = "happy"
        suggestion.regionNames.shortName = "happy"
        suggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
        suggestion.hierarchyInfo!!.airport = SuggestionV4.Airport()
        suggestion.hierarchyInfo!!.airport!!.airportCode = "happy"
        suggestion.hierarchyInfo!!.airport!!.multicity = "happy"
        return suggestion
    }

    private fun makeHappyParams(): HotelSearchParams {
        return makeParams("", "")
    }

    private fun makeParams(gaiaId: String, regionShortName: String): HotelSearchParams {
        checkInDate = LocalDate.parse("2025-08-18")
        checkOutDate = checkInDate.plusDays(3)
        val suggestion = makeSuggestion(gaiaId, regionShortName)
        val hotelSearchParams = HotelSearchParams.Builder(3, 500).destination(suggestion).startDate(checkInDate).endDate(checkOutDate).build() as HotelSearchParams

        return hotelSearchParams
    }

    private fun makeSuggestion(gaiaId: String, regionShortName: String): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = gaiaId
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = "DisplayName"
        suggestion.regionNames.fullName = ""
        suggestion.regionNames.shortName = regionShortName
        suggestion.coordinates = SuggestionV4.LatLng()

        return suggestion
    }
}
