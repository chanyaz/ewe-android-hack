package com.expedia.bookings.hotel.vm

import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotel.UserFilterChoices
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.test.robolectric.RoboTestHelper
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.testrule.ServicesRule
import com.mobiata.android.util.SettingUtils
import org.joda.time.LocalDate
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit

@RunWith(RobolectricRunner::class)
class PackageHotelResultsViewModelTest {

    val context = RuntimeEnvironment.application
    lateinit var checkInDate: LocalDate
    lateinit var checkOutDate: LocalDate

    var packageServiceRule = ServicesRule(PackageServices::class.java)
        @Rule get

    @Test
    fun testPackageSearchResponse() {
        setRemoveBundleOverviewScreenTest()
        Db.setPackageParams(setUpParams())
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()

        val resultsSubscriber = TestSubscriber<HotelSearchResponse>()

        val viewModel = PackageHotelResultsViewModel(activity, packageServiceRule.services!!)
        viewModel.hotelResultsObservable.subscribe(resultsSubscriber)
        viewModel.paramsSubject.onNext(makeHappyParams())

        resultsSubscriber.awaitValueCount(1, 1, TimeUnit.SECONDS)
        resultsSubscriber.assertNoTerminalEvent()
        resultsSubscriber.assertNoErrors()
        resultsSubscriber.assertValueCount(1)
    }

    private fun setRemoveBundleOverviewScreenTest() {
        RoboTestHelper.bucketTests(AbacusUtils.EBAndroidAppPackagesRemoveBundleOverview)
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

    private fun makeHappyParams(): HotelSearchParams {
        return makeParams("", "")
    }

    private fun makeParams(gaiaId: String, regionShortName: String): HotelSearchParams {
        checkInDate = LocalDate.now()
        checkOutDate = checkInDate.plusDays(3)
        val suggestion = makeSuggestion(gaiaId, regionShortName)
        val hotelSearchParams = HotelSearchParams.Builder(3, 500).destination(suggestion).startDate(checkInDate).endDate(checkOutDate).build() as HotelSearchParams

        return hotelSearchParams
    }

    private fun makeSuggestion(gaiaId: String, regionShortName: String): SuggestionV4 {
        val suggestion = SuggestionV4()
        suggestion.gaiaId = gaiaId
        suggestion.regionNames = SuggestionV4.RegionNames()
        suggestion.regionNames.displayName = ""
        suggestion.regionNames.fullName = ""
        suggestion.regionNames.shortName = regionShortName
        suggestion.coordinates = SuggestionV4.LatLng()

        return suggestion
    }
}