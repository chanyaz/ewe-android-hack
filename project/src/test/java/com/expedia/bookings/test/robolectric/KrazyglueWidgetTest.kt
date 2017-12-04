package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.KrazyglueResponse
import com.expedia.bookings.presenter.shared.KrazyglueHotelViewHolder
import com.expedia.bookings.presenter.shared.KrazyglueWidget
import com.expedia.bookings.data.hotels.HotelSearchParams
import com.expedia.bookings.presenter.shared.KrazyglueHotelsListAdapter
import com.expedia.bookings.presenter.shared.KrazyglueSeeMoreViewHolder
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.widget.TextView
import com.expedia.vm.KrazyglueHotelSeeMoreHolderViewModel
import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import rx.subjects.BehaviorSubject
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class KrazyglueWidgetTest {

    private lateinit var mockAnalyticsProvider: AnalyticsProvider
    private var activity: AppCompatActivity by Delegates.notNull()

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
    }

    @Test
    fun testVisibilityGONEWhenFeatureToggleONAndBucketingOFF() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppFlightsKrazyglue)

        val krazyglueWidget = LayoutInflater.from(activity).inflate(R.layout.krazyglue_widget, null) as KrazyglueWidget

        assertEquals(View.GONE, krazyglueWidget.visibility)
        krazyglueWidget.viewModel.hotelsObservable.onNext(arrayListOf(KrazyglueResponse.KrazyglueHotel()))
        assertEquals(View.GONE, krazyglueWidget.visibility)
    }

    @Test
    fun testVisibilityGONEWhenFeatureToggleOFFAndBucketingOFF() {
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppFlightsKrazyglue)

        val krazyglueWidget = LayoutInflater.from(activity).inflate(R.layout.krazyglue_widget, null) as KrazyglueWidget

        assertEquals(View.GONE, krazyglueWidget.visibility)
        krazyglueWidget.viewModel.hotelsObservable.onNext(arrayListOf(KrazyglueResponse.KrazyglueHotel()))
        assertEquals(View.GONE, krazyglueWidget.visibility)
    }

    @Test
    fun testVisibilityGONEWhenFeatureToggleOFFAndBucketingON() {
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
    fun testThreeHotelsPlusSeeMoreCountInRecyclerView() {
        enableKrazyglueTest(activity)
        setDbFlightSearch()
        val krazyglueWidget = LayoutInflater.from(activity).inflate(R.layout.krazyglue_widget, null) as KrazyglueWidget

        krazyglueWidget.viewModel.hotelsObservable.onNext(getKrazyGlueHotels())
        assertEquals(4, krazyglueWidget.hotelsRecyclerView.adapter.itemCount)
    }

    @Test
    fun testSeeMoreHotelDisplayPositionWithABTestVariantFront() {
        enableKrazyglueTest(activity)
        setDbFlightSearch()
        val krazyglueWidget = LayoutInflater.from(activity).inflate(R.layout.krazyglue_widget, null) as KrazyglueWidget

        krazyglueWidget.viewModel.hotelsObservable.onNext(getKrazyGlueHotels())
        val krazyglueAdapter = krazyglueWidget.hotelsRecyclerView.adapter as KrazyglueHotelsListAdapter
        
        assertEquals(KrazyglueHotelsListAdapter.KrazyglueViewHolderType.SEE_MORE_VIEW_HOLDER, krazyglueAdapter.getKrazyGlueViewHolderTypeFromInt(krazyglueWidget.hotelsRecyclerView.adapter.getItemViewType(0)))
        assertEquals(KrazyglueHotelsListAdapter.KrazyglueViewHolderType.HOTEL_VIEW_HOLDER,krazyglueAdapter.getKrazyGlueViewHolderTypeFromInt(krazyglueWidget.hotelsRecyclerView.adapter.getItemViewType(4)))
    }

    @Test
    fun testSeeMoreHotelDisplayPositionWithABTestVariantEnd() {
        enableKrazyglueTest(activity, false)
        setDbFlightSearch()
        val krazyglueWidget = LayoutInflater.from(activity).inflate(R.layout.krazyglue_widget, null) as KrazyglueWidget

        krazyglueWidget.viewModel.hotelsObservable.onNext(getKrazyGlueHotels())
        val krazyglueAdapter = krazyglueWidget.hotelsRecyclerView.adapter as KrazyglueHotelsListAdapter

        assertEquals(KrazyglueHotelsListAdapter.KrazyglueViewHolderType.HOTEL_VIEW_HOLDER, krazyglueAdapter.getKrazyGlueViewHolderTypeFromInt(krazyglueWidget.hotelsRecyclerView.adapter.getItemViewType(0)))
        assertEquals(KrazyglueHotelsListAdapter.KrazyglueViewHolderType.SEE_MORE_VIEW_HOLDER,krazyglueAdapter.getKrazyGlueViewHolderTypeFromInt(krazyglueWidget.hotelsRecyclerView.adapter.getItemViewType(3)))
    }

    @Test
    fun testOfferValidFunctionalityInSeeMoreHotel() {
        var departureDate = DateTime().plusDays(1)
        var seeMoreHotelViewModel = KrazyglueHotelSeeMoreHolderViewModel(activity, departureDate)

        assertEquals("Offer expires in 1 day", seeMoreHotelViewModel.getOfferValidDate())

        departureDate = DateTime().plusDays(8)
        seeMoreHotelViewModel = KrazyglueHotelSeeMoreHolderViewModel(activity, departureDate)

        assertEquals("Offer expires in 7 days", seeMoreHotelViewModel.getOfferValidDate())
    }

    @Test
    fun testSeeMoreHotelDataBinding() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val seeMoreHotel = LayoutInflater.from(activity).inflate(R.layout.krazyglue_see_more_hotel_view, null)
        val hotelSearchParamsObservable = BehaviorSubject.create<HotelSearchParams>(HotelPresenterTestUtil.getDummyHotelSearchParams(activity))
        val regionIdObservable = BehaviorSubject.create<String>("12345")
        KrazyglueSeeMoreViewHolder(seeMoreHotel, activity, hotelSearchParamsObservable, regionIdObservable)

        assertEquals("Offer expires in 1 day", seeMoreHotel.findViewById<TextView>(R.id.hotel_offer_expire).text)
    }

    @Test
    fun testHotelBindsDataCorrectly() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        val hotelView = LayoutInflater.from(activity).inflate(R.layout.krazyglue_hotel_view, null)
        val hotelSearchObservable = BehaviorSubject.create<HotelSearchParams>(HotelPresenterTestUtil.getDummyHotelSearchParams(activity))
        val regionIdObservable = BehaviorSubject.create<String>("12345")
        val viewHolder = KrazyglueHotelViewHolder(hotelView, hotelSearchObservable, regionIdObservable)
        val hotel = FlightPresenterTestUtil.getKrazyglueHotel(hotelID = "21222", hoteName = "San Francisco Hotel")
        viewHolder.viewModel.hotelObservable.onNext(hotel)

        assertEquals("San Francisco Hotel", hotelView.findViewById<TextView>(R.id.hotel_name_text_view).text)
        assertEquals("4.0", hotelView.findViewById<TextView>(R.id.hotel_guest_rating).text)
        assertEquals("330$", hotelView.findViewById<TextView>(R.id.hotel_strike_through_price).text)
        assertEquals("220$", hotelView.findViewById<TextView>(R.id.hotel_price_per_night).text)
        assertEquals("21222", viewHolder.viewModel.hotelId)
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
        val expectedProducts = "Hotel:11111;;,Hotel:99999;;,Hotel:55555;;"

        FlightsV2Tracking.trackKrazyglueExposure(getKrazyGlueHotels())
        assertKrazyglueExposure(expectedEvars = expectedEvars, expectedProducts = expectedProducts, expectedProps = expectedProps)
    }

    @Test
    fun testKrazyglueClickTrackingOnFirstHotelVariantEnd() {
        enableKrazyglueTest(activity)
        setDbFlightSearch()
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        val krazyglueWidget = LayoutInflater.from(activity).inflate(R.layout.krazyglue_widget, null) as KrazyglueWidget
        setupKrazyglueRecycler(krazyglueWidget)
        (krazyglueWidget.hotelsRecyclerView.findViewHolderForAdapterPosition(1) as KrazyglueHotelViewHolder).itemView.performClick()
        val expectedEvars = mapOf(65 to "expedia-hot-mobile-conf")

        assertKrazyglueClickTracking(expectedEvars = expectedEvars, expectedSuffix =  "tile1")
    }

    @Test
    fun testKrazyglueClickTrackingOnFirstHotelVariantFront() {
        enableKrazyglueTest(activity, false)
        setDbFlightSearch()
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        val krazyglueWidget = LayoutInflater.from(activity).inflate(R.layout.krazyglue_widget, null) as KrazyglueWidget
        setupKrazyglueRecycler(krazyglueWidget)
        (krazyglueWidget.hotelsRecyclerView.findViewHolderForAdapterPosition(0) as KrazyglueHotelViewHolder).itemView.performClick()
        val expectedEvars = mapOf(65 to "expedia-hot-mobile-conf")

        assertKrazyglueClickTracking(expectedEvars = expectedEvars, expectedSuffix = "tile1")
    }

    @Test
    fun testKrazyglueClickTrackingOnLastHotelVariantEnd() {
        enableKrazyglueTest(activity)
        setDbFlightSearch()
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        val krazyglueWidget = LayoutInflater.from(activity).inflate(R.layout.krazyglue_widget, null) as KrazyglueWidget
        setupKrazyglueRecycler(krazyglueWidget)
        (krazyglueWidget.hotelsRecyclerView.findViewHolderForAdapterPosition(3) as KrazyglueHotelViewHolder).itemView.performClick()
        val expectedEvars = mapOf(65 to "expedia-hot-mobile-conf")

        assertKrazyglueClickTracking(expectedEvars = expectedEvars, expectedSuffix = "tile3")
    }

    @Test
    fun testKrazyglueClickTrackingOnLastHotelVariantFront() {
        enableKrazyglueTest(activity, false)
        setDbFlightSearch()
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        val krazyglueWidget = LayoutInflater.from(activity).inflate(R.layout.krazyglue_widget, null) as KrazyglueWidget
        setupKrazyglueRecycler(krazyglueWidget)
        (krazyglueWidget.hotelsRecyclerView.findViewHolderForAdapterPosition(2) as KrazyglueHotelViewHolder).itemView.performClick()
        val expectedEvars = mapOf(65 to "expedia-hot-mobile-conf")

        assertKrazyglueClickTracking(expectedEvars = expectedEvars, expectedSuffix =  "tile3")
    }

    @Test
    fun testKrazyglueSeeMoreClickTracking() {
        enableKrazyglueTest(activity)
        setDbFlightSearch()
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        val krazyglueWidget = LayoutInflater.from(activity).inflate(R.layout.krazyglue_widget, null) as KrazyglueWidget
        setupKrazyglueRecycler(krazyglueWidget)
        (krazyglueWidget.hotelsRecyclerView.findViewHolderForAdapterPosition(0) as KrazyglueSeeMoreViewHolder).itemView.performClick()
        val expectedEvars = mapOf(65 to "expedia-hot-mobile-conf")

        assertKrazyglueClickTracking(expectedEvars = expectedEvars, expectedSuffix =  "see_more")
    }

    @Test
    fun testKrazyglueVisibilityGoneWithEmptyHotelList() {
        enableKrazyglueTest(activity)
        setDbFlightSearch()
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        val krazyglueWidget = LayoutInflater.from(activity).inflate(R.layout.krazyglue_widget, null) as KrazyglueWidget
        krazyglueWidget.viewModel.hotelsObservable.onNext(emptyList())

        assertEquals(View.GONE, krazyglueWidget.visibility)
        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
    }

    private fun getKrazyGlueHotels(): List<KrazyglueResponse.KrazyglueHotel> {
        return FlightPresenterTestUtil.getKrazyGlueHotels()
    }

    private fun setDbFlightSearch() {
        Db.setFlightSearchParams(FlightPresenterTestUtil.getFlightSearchParams(isRoundTrip = false))
    }

    private fun setupKrazyglueRecycler(krazyglueWidget: KrazyglueWidget) {
        krazyglueWidget.viewModel.hotelSearchParamsObservable.onNext(HotelPresenterTestUtil.getDummyHotelSearchParams(activity))
        krazyglueWidget.viewModel.hotelsObservable.onNext(getKrazyGlueHotels())
        krazyglueWidget.hotelsRecyclerView.measure(2000, 2000)
        krazyglueWidget.hotelsRecyclerView.layout(100, 100, 100, 100)
    }

    private fun assertKrazyglueExposure(expectedEvars: Map<Int, String>, expectedProducts: String, expectedProps: Map<Int, String>) {
        OmnitureTestUtils.assertStateTracked("App.Kg.expedia.conf", OmnitureMatchers.withEvars(expectedEvars), mockAnalyticsProvider)
        OmnitureTestUtils.assertStateTracked("App.Kg.expedia.conf", OmnitureMatchers.withProps(expectedProps), mockAnalyticsProvider)
        OmnitureTestUtils.assertStateTracked("App.Kg.expedia.conf", OmnitureMatchers.withProductsString(expectedProducts), mockAnalyticsProvider)
    }

    private fun assertKrazyglueClickTracking(expectedEvars: Map<Int, String>, expectedSuffix: String) {
        OmnitureTestUtils.assertLinkTracked("Krazyglue Click", "mip.hot.app.kg.flight.conf.HSR.$expectedSuffix", OmnitureMatchers.withEvars(expectedEvars), mockAnalyticsProvider)
        OmnitureTestUtils.assertLinkTracked("Krazyglue Click", "mip.hot.app.kg.flight.conf.HSR.$expectedSuffix", OmnitureMatchers.withEventsString("event83"), mockAnalyticsProvider)
    }
    
    private fun enableKrazyglueTest(context: Context, displaySeeMoreFront: Boolean = true) {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(context, AbacusUtils.EBAndroidAppFlightsKrazyglue, if (displaySeeMoreFront) 1 else 2 )
    }
}