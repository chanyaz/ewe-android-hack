package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.KrazyglueResponse
import com.expedia.bookings.presenter.shared.KrazyglueHotelViewHolder
import com.expedia.bookings.presenter.shared.KrazyglueWidget
import com.expedia.bookings.data.flights.FlightSearchParams
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.widget.TextView
import com.mobiata.android.util.SettingUtils
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class KrazyglueWidgetTest {

    private lateinit var mockAnalyticsProvider: AnalyticsProvider
    private var activity: Activity by Delegates.notNull()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
    }

    @Test
    fun testVisibilityGONEWhenFeatureToggleONAndBucketingOFF() {
        SettingUtils.save(activity, R.string.preference_enable_krazy_glue_on_flights_confirmation, true)
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppFlightsKrazyglue)

        val krazyglueWidget = LayoutInflater.from(activity).inflate(R.layout.krazyglue_widget, null) as KrazyglueWidget

        assertEquals(View.GONE, krazyglueWidget.visibility)
        krazyglueWidget.viewModel.hotelsObservable.onNext(arrayListOf(KrazyglueResponse.KrazyglueHotel()))
        assertEquals(View.GONE, krazyglueWidget.visibility)
    }

    @Test
    fun testVisibilityGONEWhenFeatureToggleOFFAndBucketingOFF() {
        SettingUtils.save(activity, R.string.preference_enable_krazy_glue_on_flights_confirmation, false)
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppFlightsKrazyglue)

        val krazyglueWidget = LayoutInflater.from(activity).inflate(R.layout.krazyglue_widget, null) as KrazyglueWidget

        assertEquals(View.GONE, krazyglueWidget.visibility)
        krazyglueWidget.viewModel.hotelsObservable.onNext(arrayListOf(KrazyglueResponse.KrazyglueHotel()))
        assertEquals(View.GONE, krazyglueWidget.visibility)
    }

    @Test
    fun testVisibilityGONEWhenFeatureToggleOFFAndBucketingON() {
        SettingUtils.save(activity, R.string.preference_enable_krazy_glue_on_flights_confirmation, false)
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightsKrazyglue)

        val krazyglueWidget = LayoutInflater.from(activity).inflate(R.layout.krazyglue_widget, null) as KrazyglueWidget

        assertEquals(View.GONE, krazyglueWidget.visibility)
        krazyglueWidget.viewModel.hotelsObservable.onNext(arrayListOf(KrazyglueResponse.KrazyglueHotel()))
        assertEquals(View.GONE, krazyglueWidget.visibility)
    }

    @Test
    fun testVisibilityVISIBLEWhenFeatureToggleONAndBucketingON() {
        enableKrazyglueTest(activity)
        setDbFlightSearch()

        val krazyglueWidget = LayoutInflater.from(activity).inflate(R.layout.krazyglue_widget, null) as KrazyglueWidget

        assertEquals(View.VISIBLE, krazyglueWidget.visibility)
        krazyglueWidget.viewModel.hotelsObservable.onNext(getKrazyGlueHotels())
        assertEquals(View.VISIBLE, krazyglueWidget.visibility)
    }

    @Test
    fun testViewModelHeaderText() {
        enableKrazyglueTest(activity)

        val krazyglueWidget = LayoutInflater.from(activity).inflate(R.layout.krazyglue_widget, null) as KrazyglueWidget

        krazyglueWidget.viewModel.cityObservable.onNext("Paris")
        assertEquals("Because you booked a flight, save on select hotels in Paris", krazyglueWidget.headerText.text)
    }

    @Test
    fun testFourHotelsInRecyclerView() {
        enableKrazyglueTest(activity)
        setDbFlightSearch()
        val krazyglueWidget = LayoutInflater.from(activity).inflate(R.layout.krazyglue_widget, null) as KrazyglueWidget

        krazyglueWidget.viewModel.hotelsObservable.onNext(getKrazyGlueHotels())
        assertEquals(4, krazyglueWidget.hotelsRecyclerView.adapter.itemCount)
    }

    @Test
    fun testHotelBindsDataCorrectly() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val hotelView = LayoutInflater.from(activity).inflate(R.layout.krazyglue_hotel_view, null)

        val viewHolder = KrazyglueHotelViewHolder(hotelView)
        val hotel = getKrazyGlueHotel("21222", "San Francisco Hotel")
        viewHolder.viewModel.hotelObservable.onNext(hotel)

        assertEquals("San Francisco Hotel", hotelView.findViewById<TextView>(R.id.hotel_name_text_view).text)
        assertEquals("4.0", hotelView.findViewById<TextView>(R.id.hotel_guest_rating).text)
        assertEquals("330$", hotelView.findViewById<TextView>(R.id.hotel_strike_through_price).text)
        assertEquals("220$", hotelView.findViewById<TextView>(R.id.hotel_price_per_night).text)
        assertEquals(View.VISIBLE, hotelView.findViewById<TextView>(R.id.hotel_guest_rating).visibility)
    }

    @Test
    fun testKrazyGlueExposureOmnitureTracking() {
        enableKrazyglueTest(activity)
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        setDbFlightSearch()

        val expectedEvars = mapOf(43 to "mip.hot.kg.expedia.conf",
                28 to "mip.hot.kg.expedia.conf",
                2 to "D=c2",
                4 to "D=c4",
                65 to Constants.KRAZY_GLUE_PARTNER_ID)
        val expectedProps = mapOf(2 to "krazyglue",
                4 to "DTW",
                16 to "mip.hot.kg.expedia.conf")
        val expectedProducts = "Hotel:11111;;,Hotel:99999;;,Hotel:55555;;,Hotel:77777;;"

        FlightsV2Tracking.trackKrazyglueExposure(getKrazyGlueHotels())
        assertKrazyglueExposure(expectedEvars, expectedProducts, expectedProps)
    }

    @Test
    fun testKrazyGlueClickTracking() {
        enableKrazyglueTest(activity)
        setDbFlightSearch()
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        val krazyglueWidget = LayoutInflater.from(activity).inflate(R.layout.krazyglue_widget, null) as KrazyglueWidget

        krazyglueWidget.viewModel.hotelsObservable.onNext(getKrazyGlueHotels())
        krazyglueWidget.hotelsRecyclerView.measure(0, 0);
        krazyglueWidget.hotelsRecyclerView.layout(0, 0, 100, 10000);
        (krazyglueWidget.hotelsRecyclerView.findViewHolderForAdapterPosition(0) as KrazyglueHotelViewHolder).onClick(krazyglueWidget)
        val expectedEvars = mapOf(65 to "expedia-hot-mobile-conf")

        assertKrazyGlueClickTracking(expectedEvars)
    }

    private fun getKrazyGlueHotels(): List<KrazyglueResponse.KrazyglueHotel> {
        val firstKrazyHotel = getKrazyGlueHotel("11111", "Mariot")
        val secondKrazyHotel = getKrazyGlueHotel("99999", "Cosmopolitan")
        val thirdKrazyHotel = getKrazyGlueHotel("55555", "Holiday Inn")
        val fourthKrazyGlueHotel = getKrazyGlueHotel("77777", "Motel 8")

        return listOf(firstKrazyHotel, secondKrazyHotel, thirdKrazyHotel, fourthKrazyGlueHotel)
    }

    private fun getKrazyGlueHotel(hotelID: String, hoteName: String): KrazyglueResponse.KrazyglueHotel {
        val hotel = KrazyglueResponse.KrazyglueHotel()
        hotel.hotelId = hotelID
        hotel.hotelName = hoteName
        hotel.guestRating = "4.0"
        hotel.airAttachedPrice = "220$"
        hotel.standAlonePrice = "330$"
        hotel.hotelImage = "image"
        hotel.starRating = "2.5"
        return hotel
    }

    private fun setDbFlightSearch() {
        val departureAirport = SuggestionV4()
        departureAirport.hierarchyInfo = SuggestionV4.HierarchyInfo()
        val arrivalAirport = SuggestionV4()
        val airport = SuggestionV4.Airport()
        airport.airportCode = "DTW"
        val hierArchyInfo = SuggestionV4.HierarchyInfo()
        hierArchyInfo.airport = airport
        arrivalAirport.hierarchyInfo = hierArchyInfo

        val paramsBuilder = FlightSearchParams.Builder(26, 500)
                .origin(departureAirport)
                .destination(arrivalAirport)
                .startDate(LocalDate.now())
                .adults(1) as FlightSearchParams.Builder

        Db.setFlightSearchParams(paramsBuilder.build())
    }

    private fun assertKrazyglueExposure(expectedEvars: Map<Int, String>, expectedProducts: String, expectedProps: Map<Int, String>) {
        OmnitureTestUtils.assertStateTracked("App.Kg.expedia.conf", OmnitureMatchers.withEvars(expectedEvars), mockAnalyticsProvider)
        OmnitureTestUtils.assertStateTracked("App.Kg.expedia.conf", OmnitureMatchers.withProps(expectedProps), mockAnalyticsProvider)
        OmnitureTestUtils.assertStateTracked("App.Kg.expedia.conf", OmnitureMatchers.withProductsString(expectedProducts), mockAnalyticsProvider)
    }

    private fun assertKrazyGlueClickTracking(expectedEvars: Map<Int, String>) {
        OmnitureTestUtils.assertLinkTracked("Krazyglue Click", "mip.hot.app.kg.flight.conf.HSR.tile1", OmnitureMatchers.withEvars(expectedEvars), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked("Krazyglue Click", "mip.hot.app.kg.flight.conf.HSR.tile1", OmnitureMatchers.withEventsString("event83"), mockAnalyticsProvider)
    }

    private fun enableKrazyglueTest(activity: Activity?) {
        SettingUtils.save(activity, R.string.preference_enable_krazy_glue_on_flights_confirmation, true)
        AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppFlightsKrazyglue)
    }
}