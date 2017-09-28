package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.presenter.packages.PackageHotelPresenter
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.services.ProductSearchType
import com.expedia.bookings.utils.Ui
import com.mobiata.mocke3.ExpediaDispatcher
import com.mobiata.mocke3.FileSystemOpener
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.observers.TestSubscriber
import java.io.File
import java.util.concurrent.TimeUnit
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.test.OmnitureMatchers.Companion.withEvars
import com.expedia.bookings.testrule.ServicesRule
import com.expedia.bookings.utils.AbacusTestUtils
import org.junit.Rule
import org.robolectric.RuntimeEnvironment


@RunWith(RobolectricRunner::class)
class PackageHotelPresenterTest {
    lateinit private var widget: PackageHotelPresenter
    lateinit private var activity: Activity
    private lateinit var mockAnalyticsProvider: AnalyticsProvider
    lateinit var params: PackageSearchParams
    val context = RuntimeEnvironment.application

    val packageServiceRule = ServicesRule(PackageServices::class.java)
        @Rule get

    var observer = TestSubscriber<BundleSearchResponse>()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        Ui.getApplication(activity).defaultPackageComponents()
        Ui.getApplication(activity).defaultTravelerComponent()
    }

    @Test
    fun testPackageSearchParamsTracked() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp)

        buildPackagesSearchParams()
        searchPackages()

        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter

        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()

        widget.dataAvailableSubject.onNext(observer.onNextEvents[0])
        widget.trackEventSubject.onNext(Unit)

        val expectedEvars = mapOf(
                47 to "PKG|1R|RT|A1|C3|L1"
        )
        OmnitureTestUtils.assertStateTracked(withEvars(expectedEvars), mockAnalyticsProvider)

    }

    @Test
    fun testPackageSearchParamsTrackedWithNewTravelerForm() {
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightTravelerFormRevamp)

        buildPackagesSearchParams()
        searchPackages()

        widget = LayoutInflater.from(activity).inflate(R.layout.test_package_hotel_presenter,
                null) as PackageHotelPresenter

        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()

        widget.dataAvailableSubject.onNext(observer.onNextEvents[0])
        widget.trackEventSubject.onNext(Unit)

        val expectedEvars = mapOf(
                47 to "PKG|1R|RT|A1|C1|YTH1|IL1|IS0"
        )
        OmnitureTestUtils.assertStateTracked(withEvars(expectedEvars), mockAnalyticsProvider)
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

    private fun buildPackagesSearchParams() {
        params = PackageSearchParams.Builder(26, 329)
                .infantSeatingInLap(true)
                .origin(getDummySuggestion())
                .destination(getDummySuggestion())
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .adults(1)
                .children(listOf(16, 10, 1))
                .build() as PackageSearchParams

        Db.setPackageParams(params)
    }

    private fun searchPackages() {
        val root = File("../lib/mocked/templates").canonicalPath
        val opener = FileSystemOpener(root)
        packageServiceRule.server.setDispatcher(ExpediaDispatcher(opener))

        packageServiceRule.services!!.packageSearch(params, ProductSearchType.OldPackageSearch).subscribe(observer)
        observer.awaitTerminalEvent(10, TimeUnit.SECONDS)
    }
}