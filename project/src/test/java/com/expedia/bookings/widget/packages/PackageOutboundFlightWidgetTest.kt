package com.expedia.bookings.widget.packages

import android.app.Activity
import android.support.v4.content.ContextCompat
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.StrUtils
import com.expedia.vm.packages.BundleFlightViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.Shadows
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackageOutboundFlightWidgetTest {
    var testDestination: SuggestionV4 by Delegates.notNull()
    val testRegionName = "Chicago"
    val testAirportCode = "ORD"
    var testWidget: PackageOutboundFlightWidget by Delegates.notNull()
    var widgetVM: BundleFlightViewModel by Delegates.notNull()

    var expectedDisabledColor: Int by Delegates.notNull()
    private var activity: Activity by Delegates.notNull()

    @Before fun before() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_Packages)
        testDestination = buildMockDestination()
        setUpParams()
        testWidget = PackageOutboundFlightWidget(activity, null)
        widgetVM = BundleFlightViewModel(activity)
        testWidget.viewModel = widgetVM
        expectedDisabledColor = ContextCompat.getColor(activity, R.color.package_bundle_icon_color)
    }

    @Test
    fun testEnable() {
        testWidget.enable()
        assertTrue(testWidget.rowContainer.isEnabled)
        assertTrue(testWidget.flightDetailsIcon.isEnabled)

        assertEquals(View.GONE, testWidget.flightDetailsIcon.visibility)
        val shadowDrawable = Shadows.shadowOf(testWidget.flightIcon.drawable)
        assertEquals(R.drawable.packages_flight1_icon, shadowDrawable.createdFromResId)

        val expectedFlightText = activity.getString(R.string.select_flight_to, StrUtils.formatAirportCodeCityName(testDestination))
        assertEquals(expectedFlightText, testWidget.flightCardText.text)
        assertEquals(View.VISIBLE, testWidget.travelInfoText.visibility)
    }

    @Test
    fun testDisable() {
        testWidget.disable()
        assertFalse(testWidget.rowContainer.isEnabled)
        assertFalse(testWidget.flightDetailsIcon.isEnabled)

        assertEquals(expectedDisabledColor, testWidget.flightCardText.currentTextColor)
        assertEquals(expectedDisabledColor, testWidget.travelInfoText.currentTextColor)

        assertEquals(View.GONE, testWidget.forwardArrow.visibility)
    }

    @Test
    fun testHandleResultsLoaded() {
        testWidget.handleResultsLoaded()

        assertTrue(testWidget.rowContainer.isEnabled)
        assertTrue(testWidget.flightInfoContainer.isEnabled)

        assertEquals(View.GONE, testWidget.flightLoadingBar.visibility)

        val expectedFlightText = activity.getString(R.string.select_flight_to, StrUtils.formatAirportCodeCityName(testDestination))
        assertEquals(expectedFlightText, testWidget.flightCardText.text)
        assertEquals(View.VISIBLE, testWidget.travelInfoText.visibility)
    }

    private fun setUpParams() {
        // Can't mock PackageSearchParams because it's a 'data' class. So we have to build one.... #KotlinOP
        val packageParams = PackageSearchParams.Builder(26, 329)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .origin(SuggestionV4())
                .destination(testDestination)
                .build() as PackageSearchParams
        Db.setPackageParams(packageParams)
    }

    private fun buildMockDestination() : SuggestionV4 {
        val origin = Mockito.mock(SuggestionV4::class.java)
        val hierarchyInfo = SuggestionV4.HierarchyInfo()
        val regionNames = SuggestionV4.RegionNames()
        regionNames.displayName = testRegionName
        hierarchyInfo.airport = buildMockAirport()
        origin.hierarchyInfo = hierarchyInfo
        origin.regionNames = regionNames
        return origin
    }

    private fun buildMockAirport() : SuggestionV4.Airport {
        val airport = Mockito.mock(SuggestionV4.Airport::class.java)
        airport.airportCode = testAirportCode
        return airport
    }
}