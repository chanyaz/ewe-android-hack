package com.expedia.bookings.widget.packages

import android.app.Activity
import android.content.Intent
import android.view.View
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.PackageBundleHotelWidget
import com.expedia.bookings.packages.activity.PackageHotelActivity
import com.expedia.bookings.packages.vm.BundleHotelViewModel
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.Shadows
import java.util.ArrayList
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class PackageHotelWidgetTest {
    lateinit var testHotelWidget: PackageBundleHotelWidget
    val activity = Robolectric.buildActivity(Activity::class.java).create().get()

    var testOrigin: SuggestionV4 by Delegates.notNull()
    var testDestination: SuggestionV4 by Delegates.notNull()
    val testRegionName = "Chicago"
    val testAirportCode = "ORD"

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
        testOrigin = buildMockOriginSuggestion()
        testDestination = buildMockDestinationSuggestion()
        setupParams()
    }

    private fun buildMockOriginSuggestion(): SuggestionV4 {
        val origin = Mockito.mock(SuggestionV4::class.java)
        val hierarchyInfo = SuggestionV4.HierarchyInfo()
        val regionNames = SuggestionV4.RegionNames()
        regionNames.displayName = testRegionName
        hierarchyInfo.airport = buildMockAirport()
        origin.hierarchyInfo = hierarchyInfo
        origin.regionNames = regionNames
        return origin
    }

    private fun buildMockDestinationSuggestion(): SuggestionV4 {
        val country = SuggestionV4.Country()
        country.name = "OR"
        val hierarchyInfo = SuggestionV4.HierarchyInfo()
        hierarchyInfo.country = country
        val destination = SuggestionV4()
        destination.hierarchyInfo = hierarchyInfo
        return destination
    }

    private fun buildMockAirport(): SuggestionV4.Airport {
        val airport = Mockito.mock(SuggestionV4.Airport::class.java)
        airport.airportCode = testAirportCode
        return airport
    }

    private fun setupParams() {
        val packageParams = PackageSearchParams.Builder(26, 329)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .origin(testOrigin)
                .destination(testDestination)
                .build() as PackageSearchParams
        Db.setPackageParams(packageParams)
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

        assertTrue(actualIntent.filterEquals(expectedIntent))
    }

    @Test
    fun testOnlyFreeCancellationOrNonRefundShow() {
        val hotel = getSelectedHotel()
        val room = getSelectedRoom()
        Db.setPackageSelectedHotel(hotel, room)

        testHotelWidget.canExpand = true
        testHotelWidget.isRowClickable = true
        testHotelWidget.mainContainer.visibility = Presenter.GONE
        testHotelWidget.rowContainer.performClick()

        testHotelWidget.viewModel.selectedHotelObservable.onNext(Unit)
        assertTrue(testHotelWidget.hotelFreeCancellation.visibility.equals(Presenter.VISIBLE))
        assertTrue(testHotelWidget.hotelNotRefundable.visibility.equals(Presenter.GONE))

        room.hasFreeCancellation = false
        testHotelWidget.viewModel.selectedHotelObservable.onNext(Unit)
        assertTrue(testHotelWidget.hotelNotRefundable.visibility.equals(Presenter.VISIBLE))
        assertTrue(testHotelWidget.hotelFreeCancellation.visibility.equals(Presenter.GONE))
    }

    @Test
    fun testHotelCityText() {
        val hotel = getSelectedHotel()
        val room = getSelectedRoom()
        Db.setPackageSelectedHotel(hotel, room)

        testHotelWidget.viewModel.selectedHotelObservable.onNext(Unit)
        assertTrue(testHotelWidget.hotelCity.visibility.equals(Presenter.VISIBLE))
        assertEquals("sfo, CA", testHotelWidget.hotelCity.text)
    }

    @Test
    fun testHotelCityTextWhenProvinceCodeIsNull() {
        val hotel = getSelectedHotel()
        hotel.stateProvinceCode = null
        val room = getSelectedRoom()
        Db.setPackageSelectedHotel(hotel, room)

        testHotelWidget.viewModel.selectedHotelObservable.onNext(Unit)
        assertTrue(testHotelWidget.hotelCity.visibility.equals(Presenter.VISIBLE))
        assertEquals("sfo, OR", testHotelWidget.hotelCity.text)
    }

    @Test
    fun testHotelCityTextWhenProvinceCodeIsEmpty() {
        val hotel = getSelectedHotel()
        hotel.stateProvinceCode = ""
        val room = getSelectedRoom()
        Db.setPackageSelectedHotel(hotel, room)

        testHotelWidget.viewModel.selectedHotelObservable.onNext(Unit)
        assertTrue(testHotelWidget.hotelCity.visibility.equals(Presenter.VISIBLE))
        assertEquals("sfo, OR", testHotelWidget.hotelCity.text)
    }

    @Test
    fun testHotelCityTextWhenProvinceCodeAndCountryAreNull() {
        val hotel = getSelectedHotel()
        hotel.stateProvinceCode = null
        val room = getSelectedRoom()
        Db.setPackageSelectedHotel(hotel, room)
        Db.sharedInstance.packageParams.destination?.hierarchyInfo?.country = null

        testHotelWidget.viewModel.selectedHotelObservable.onNext(Unit)
        assertTrue(testHotelWidget.hotelCity.visibility.equals(Presenter.VISIBLE))
        assertEquals("sfo, ", testHotelWidget.hotelCity.text)
    }

    @Test
    fun testHotelCityTextWhenProvinceCodeAndHierarchyInfoAreNull() {
        val hotel = getSelectedHotel()
        hotel.stateProvinceCode = null
        val room = getSelectedRoom()
        Db.setPackageSelectedHotel(hotel, room)
        Db.sharedInstance.packageParams.destination?.hierarchyInfo = null

        testHotelWidget.viewModel.selectedHotelObservable.onNext(Unit)
        assertTrue(testHotelWidget.hotelCity.visibility.equals(Presenter.VISIBLE))
        assertEquals("sfo, ", testHotelWidget.hotelCity.text)
    }

    private fun getSelectedHotel(): Hotel {
        val hotel = Hotel()
        hotel.localizedName = ""
        hotel.address = "address"
        hotel.city = "sfo"
        hotel.stateProvinceCode = "CA"
        return hotel
    }

    private fun getSelectedRoom(): HotelOffersResponse.HotelRoomResponse {
        val room = HotelOffersResponse.HotelRoomResponse()
        room.roomThumbnailUrl = ""
        room.hasFreeCancellation = true
        room.freeCancellationWindowDate = "2016-02-01 11:59"
        return room
    }
}
