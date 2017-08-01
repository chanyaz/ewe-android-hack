package com.expedia.bookings.hotel.vm

import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
<<<<<<< HEAD
import rx.observers.TestSubscriber
import kotlin.test.assertEquals
=======
import com.expedia.bookings.services.TestObserver
import java.util.concurrent.TimeUnit
>>>>>>> 5abc89409b... WIP

@RunWith(RobolectricRunner::class)
class PackageHotelResultsViewModelTest {

    val context = RuntimeEnvironment.application
    lateinit var checkInDate: LocalDate
    lateinit var checkOutDate: LocalDate

    @Test
    fun testPackageSearchResponse() {
        Db.setPackageParams(setUpParams())
        val activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()

<<<<<<< HEAD
        val titleSubscriber = TestSubscriber<String>()
        val subtitleSubscriber = TestSubscriber<CharSequence>()
=======
        val resultsSubscriber = TestObserver<HotelSearchResponse>()

        val viewModel = PackageHotelResultsViewModel(activity, packageServiceRule.services!!)
        viewModel.hotelResultsObservable.subscribe(resultsSubscriber)
        viewModel.paramsSubject.onNext(makeHappyParams())
>>>>>>> 5abc89409b... WIP

        val viewModel = PackageHotelResultsViewModel(activity)
        viewModel.titleSubject.subscribe(titleSubscriber)
        viewModel.subtitleSubject.subscribe(subtitleSubscriber)

        viewModel.paramsSubject.onNext(makeHappyParams())
        assertEquals("DisplayName", titleSubscriber.onNextEvents[0])
        assert("Aug 18 - Aug 21, 1 guest".equals(subtitleSubscriber.onNextEvents[0].toString(), ignoreCase = true))
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