package com.expedia.bookings.itin

import android.view.View
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.activity.WebViewActivity
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.itin.activity.HotelItinDetailsActivity
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.itin.widget.HotelItinBookingDetails
import com.expedia.bookings.itin.widget.HotelItinCheckInCheckOutDetails
import com.expedia.bookings.itin.widget.HotelItinImage
import com.expedia.bookings.itin.widget.HotelItinRoomDetails
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.OmnitureMatchers
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.widget.itin.support.ItinCardDataHotelBuilder
import com.mobiata.android.util.SettingUtils
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelItinDetailsActivityTest {
    lateinit private var activity: HotelItinDetailsActivity
    lateinit private var itinCardDataHotel: ItinCardDataHotel
    lateinit private var intentBuilder: WebViewActivity.IntentBuilder
    lateinit private var mockAnalyticsProvider: AnalyticsProvider


    @Before
    fun before() {
        activity = Robolectric.buildActivity(HotelItinDetailsActivity::class.java).create().get()
        itinCardDataHotel = ItinCardDataHotelBuilder().build()
        intentBuilder = WebViewActivity.IntentBuilder(RuntimeEnvironment.application)
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @After
    fun tearDown() {
        SettingUtils.save(activity, R.string.preference_enable_trips_hotel_messaging, false)
        AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppTripsMessageHotel)
    }

    @Test
    fun sharedItinView() {
        val sharedItin = ItinCardDataHotelBuilder().isSharedItin(true).build()
        activity.itinCardDataHotel = sharedItin
        activity.setUpWidgets()

        val roomDetailsView: HotelItinRoomDetails = activity.roomDetailsView
        assertEquals(View.GONE, roomDetailsView.visibility)

        val hotelImageView: HotelItinImage = activity.hotelImageView
        assertEquals(View.VISIBLE, hotelImageView.visibility)

        val hotelCheckinCheckout: HotelItinCheckInCheckOutDetails = activity.checkinCheckoutView
        assertEquals(View.VISIBLE, hotelCheckinCheckout.visibility)

        val bookingDetailsView: HotelItinBookingDetails = activity.hotelBookingDetailsView
        assertEquals(View.GONE, bookingDetailsView.visibility)

        val roomDetailsHeader: View = activity.roomDetailsHeader
        assertEquals(View.GONE, roomDetailsHeader.visibility)

        val shareIcon: View = activity.findViewById(R.id.itin_share_button)
        assertEquals(View.GONE, shareIcon.visibility)
    }

    @Test
    fun testRoomDetailsExpansion() {
        val itinCardDataHotelMock = Mockito.spy(itinCardDataHotel)
        activity.itinCardDataHotel = itinCardDataHotelMock
        Mockito.`when`(activity.itinCardDataHotel.lastHotelRoom).thenReturn(null)
        activity.setUpWidgets()
        assertEquals(View.GONE, activity.roomDetailsView.expandedRoomDetails.visibility)
        assertEquals(View.GONE, activity.roomDetailsChevron.visibility)
        assertEquals(false, activity.roomDetailsView.isRowClickable)
        assertEquals(View.GONE, activity.roomDetailsView.changeCancelRulesContainer.visibility)

        activity.itinCardDataHotel = itinCardDataHotel
        activity.setUpWidgets()
        assertEquals(View.GONE, activity.roomDetailsView.expandedRoomDetails.visibility)
        assertEquals(View.VISIBLE, activity.roomDetailsChevron.visibility)
        assertEquals(true, activity.roomDetailsView.isRowClickable)
        assertEquals(View.GONE, activity.roomDetailsView.changeCancelRulesContainer.visibility)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelHasMessagingTestsOnWithURL() {
        SettingUtils.save(activity, R.string.preference_enable_trips_hotel_messaging, true)
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppTripsMessageHotel, 1)

        val itinCardDataHotelWithMessaging = ItinCardDataHotelBuilder().build()
        itinCardDataHotelWithMessaging.property.epcConversationUrl = "https://google.com"
        activity.itinCardDataHotel = itinCardDataHotelWithMessaging
        activity.setUpWidgets()

        OmnitureTestUtils.assertStateTracked(OmnitureMatchers.withAbacusTestBucketed(AbacusUtils.EBAndroidAppTripsMessageHotel.key), mockAnalyticsProvider)
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testHotelHasMessagingTestsOnNoURL() {
        SettingUtils.save(activity, R.string.preference_enable_trips_hotel_messaging, true)
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppTripsMessageHotel, 1)

        val itinCardDataHotelNoMessaging = ItinCardDataHotelBuilder().build()
        activity.itinCardDataHotel = itinCardDataHotelNoMessaging
        activity.setUpWidgets()

        OmnitureTestUtils.assertStateNotTracked(OmnitureMatchers.withAbacusTestBucketed(AbacusUtils.EBAndroidAppTripsMessageHotel.key), mockAnalyticsProvider)
    }

    @Test
    fun testHotelHasMessagingTestsOffWithURL() {
        val itinCardDataHotelWithMessaging = ItinCardDataHotelBuilder().build()
        itinCardDataHotelWithMessaging.property.epcConversationUrl = "https://google.com"
        activity.itinCardDataHotel = itinCardDataHotelWithMessaging
        activity.setUpWidgets()

        OmnitureTestUtils.assertStateNotTracked(OmnitureMatchers.withAbacusTestBucketed(AbacusUtils.EBAndroidAppTripsMessageHotel.key), mockAnalyticsProvider)
    }

    @Test
    fun testHotelHasMessagingTestsOffNoURL() {
        val itinCardDataHotelNoMessaging = ItinCardDataHotelBuilder().build()
        activity.itinCardDataHotel = itinCardDataHotelNoMessaging
        activity.setUpWidgets()

        OmnitureTestUtils.assertStateNotTracked(OmnitureMatchers.withAbacusTestBucketed(AbacusUtils.EBAndroidAppTripsMessageHotel.key), mockAnalyticsProvider)
    }

}
