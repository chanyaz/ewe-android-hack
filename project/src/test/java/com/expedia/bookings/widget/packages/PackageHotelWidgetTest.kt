package com.expedia.bookings.widget.packages

import android.app.Activity
import android.content.Intent
import android.view.View
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.PackageBundleHotelWidget
import com.expedia.ui.PackageHotelActivity
import com.expedia.vm.packages.BundleHotelViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.Shadows
import java.util.ArrayList
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackageHotelWidgetTest {
    lateinit var testHotelWidget: PackageBundleHotelWidget
    val activity = Robolectric.buildActivity(Activity::class.java).create().get()

    @Before
    fun setup() {
        val origin = Mockito.mock(SuggestionV4::class.java)
        val destination = Mockito.mock(SuggestionV4::class.java)
        val checkInDate = LocalDate()
        val checkOutDate = LocalDate()

        val params = PackageSearchParams(origin, destination, checkInDate, checkOutDate, 1, ArrayList<Int>(), false)
        Db.setPackageParams(params)

        testHotelWidget = PackageBundleHotelWidget(activity, null)
        testHotelWidget.viewModel = BundleHotelViewModel(activity)
    }

    @Test
    fun testCancel() {
        testHotelWidget.cancel()

        assertEquals(View.GONE, testHotelWidget.hotelLoadingBar.visibility)
        assertEquals(View.VISIBLE, testHotelWidget.hotelsDatesGuestInfoText.visibility)
    }

    @Test
    fun testToggleHotelWidget() {
        testHotelWidget.toggleHotelWidget(1f, true)
        assertTrue(testHotelWidget.rowContainer.isEnabled)
        assertTrue(testHotelWidget.hotelDetailsIcon.isEnabled)
        assertTrue(testHotelWidget.isEnabled)
        assertEquals(1f, testHotelWidget.hotelsText.alpha)
        assertEquals(1f, testHotelWidget.hotelsDatesGuestInfoText.alpha)
        assertEquals(1f, testHotelWidget.hotelLuggageIcon.alpha)
        assertEquals(1f, testHotelWidget.hotelDetailsIcon.alpha)

        testHotelWidget.toggleHotelWidget(1f, false)
        assertTrue (!testHotelWidget.rowContainer.isEnabled)
        assertTrue(!testHotelWidget.hotelDetailsIcon.isEnabled)
        assertTrue(!testHotelWidget.isEnabled)
    }

    @Test
    fun testRowContainerClickListenerExpand() {
        testHotelWidget.canExpand = true
        testHotelWidget.isRowClickable = true
        testHotelWidget.mainContainer.visibility = Presenter.GONE
        testHotelWidget.rowContainer.performClick()
        assertTrue(testHotelWidget.mainContainer.visibility.equals(Presenter.VISIBLE))
    }

    @Test
    fun testRowContainerClickListenerCollapse() {
        testHotelWidget.canExpand = true
        testHotelWidget.isRowClickable = true
        testHotelWidget.mainContainer.visibility = Presenter.VISIBLE
        testHotelWidget.rowContainer.performClick()
        assertTrue(testHotelWidget.mainContainer.visibility.equals(Presenter.GONE))
    }

    @Test
    fun testRowContainerClickListenerOpenHotels() {
        testHotelWidget.canExpand = false
        testHotelWidget.isRowClickable = true
        testHotelWidget.rowContainer.performClick()

        val expectedIntent = Intent(activity, PackageHotelActivity::class.java)
        val shadowActivity = Shadows.shadowOf(activity)
        val actualIntent = shadowActivity.nextStartedActivity

        assertTrue(actualIntent.filterEquals(expectedIntent));
    }
}
