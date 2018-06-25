package com.expedia.bookings.tracking.hotel

import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils.Companion.assertLinkTracked
import com.expedia.bookings.analytics.OmnitureTestUtils.Companion.assertStateTracked
import com.expedia.bookings.data.abacus.ABTest
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.HotelOffersResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.pos.PointOfSaleId
import com.expedia.bookings.test.ExcludeForBrands
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.OmnitureMatchers.Companion.withEvars
import com.expedia.bookings.test.OmnitureMatchers.Companion.withEventsString
import com.expedia.bookings.test.OmnitureMatchers.Companion.withProps
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.mobiata.android.util.SettingUtils
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricRunner::class)
class HotelTrackingTest {

    private lateinit var mockAnalyticsProvider: AnalyticsProvider
    private fun getContext() = RuntimeEnvironment.application

    @Before
    fun before() {
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
    }

    @Test
    fun testTrackInfositeChangeDate() {
        HotelTracking.trackInfositeChangeDateClick()

        var tpid = PointOfSale.getPointOfSale().tpid.toString()
        if (PointOfSale.getPointOfSale().eapid != PointOfSale.INVALID_EAPID) {
            tpid += "-" + PointOfSale.getPointOfSale().eapid
        }
        val evar = mapOf(61 to tpid)
        val prop = mapOf(7 to tpid)

        assertLinkTracked("Infosite Change Dates", "App.Hotels.IS.ChangeDates",
                Matchers.allOf(withEvars(evar), withProps(prop)),
                mockAnalyticsProvider)
    }

    @Test
    @ExcludeForBrands(brands = [MultiBrand.ORBITZ])
    fun testTrackingInRoomBookClickForIndiaPOS() {
        setPOS(PointOfSaleId.INDIA.id.toString())
        performHotelRoomClickTracking()
        val evar = mapOf(61 to "27", 52 to "Non Etp")
        val prop = mapOf(7 to "27")

        assertLinkTracked("Hotel Infosite", "App.Hotels.IS.BookNow",
                Matchers.allOf(withEvars(evar), withProps(prop)),
                mockAnalyticsProvider)
    }

    @Test
    @ExcludeForBrands(brands = [MultiBrand.ORBITZ])
    fun testTrackingInRoomBookClickForNonIndiaPOSWithABTestBucketed() {
        assertTrackingRoomBookClickForNonIndiaPOS(AbacusUtils.HotelsWebCheckout1, true)
    }

    @Test
    @ExcludeForBrands(brands = [MultiBrand.ORBITZ])
    fun testTrackingInRoomBookClickForNonIndiaPOSWithABTestInControl() {
        assertTrackingRoomBookClickForNonIndiaPOS(AbacusUtils.HotelsWebCheckout1, false)
    }

    @Test
    @ExcludeForBrands(brands = [MultiBrand.ORBITZ])
    fun testTrackingInRoomBookClickForNonIndiaPOSWithSecondABTestBucketed() {
        assertTrackingRoomBookClickForNonIndiaPOS(AbacusUtils.HotelsWebCheckout2, true)
    }

    @Test
    @ExcludeForBrands(brands = [MultiBrand.ORBITZ])
    fun testTrackingInRoomBookClickForNonIndiaPOSWithSecondABTestInControl() {
        assertTrackingRoomBookClickForNonIndiaPOS(AbacusUtils.HotelsWebCheckout2, false)
    }

    @Test
    fun testTrackHotelMapShown() {
        AbacusTestUtils.unbucketTests(AbacusUtils.HotelMapSmallSoldOutPins, AbacusUtils.HotelResultsCellOnMapCarousel)
        HotelTracking.trackHotelMapLoad(false)

        assertTrackHotelMapShown(null, "25410.0.0|26455.0.0")
    }

    @Test
    fun testTrackHotelMapShownSwp() {
        AbacusTestUtils.unbucketTests(AbacusUtils.HotelMapSmallSoldOutPins, AbacusUtils.HotelResultsCellOnMapCarousel)
        HotelTracking.trackHotelMapLoad(true)

        assertTrackHotelMapShown("event118", "25410.0.0|26455.0.0")
    }

    @Test
    fun testTrackHotelMapShownSwpTestsBucketed() {
        AbacusTestUtils.bucketTests(AbacusUtils.HotelMapSmallSoldOutPins, AbacusUtils.HotelResultsCellOnMapCarousel)
        HotelTracking.trackHotelMapLoad(false)

        assertTrackHotelMapShown(null, "25410.0.1|26455.0.1")
    }

    private fun assertTrackingRoomBookClickForNonIndiaPOS(test: ABTest, bucket: Boolean) {
        val propString: String
        if (bucket) {
            AbacusTestUtils.bucketTests(test)
            propString = "${test.key}.0.1"
        } else {
            AbacusTestUtils.unbucketTests(test)
            propString = "${test.key}.0.0"
        }
        setPOS(PointOfSaleId.UNITED_STATES.id.toString())
        performHotelRoomClickTracking()

        val evar = mapOf(61 to "1", 52 to "Non Etp")
        val prop = mapOf(7 to "1", 34 to propString)

        assertLinkTracked("Hotel Infosite", "App.Hotels.IS.BookNow",
                Matchers.allOf(withEvars(evar), withProps(prop)),
                mockAnalyticsProvider)
    }

    private fun assertTrackHotelMapShown(eventsString: String?, thirtyFourTracking: String) {
        val linkName = "App.Hotels.Search.Map"

        val evar = mapOf(18 to linkName, 2 to "D=c2", 34 to thirtyFourTracking)
        val prop = mapOf(2 to "hotels", 34 to thirtyFourTracking)

        if (eventsString.isNullOrBlank()) {
            assertStateTracked(linkName,
                    Matchers.allOf(withEvars(evar),
                            withProps(prop)),
                    mockAnalyticsProvider)
        } else {
            assertStateTracked(linkName,
                    Matchers.allOf(withEventsString(eventsString!!),
                            withEvars(evar),
                            withProps(prop)),
                    mockAnalyticsProvider)
        }
    }

    private fun setPOS(id: String) {
        SettingUtils.save(getContext(), "point_of_sale_key", id)
        PointOfSale.onPointOfSaleChanged(getContext())
    }

    private fun performHotelRoomClickTracking() {
        val roomResponse = HotelOffersResponse.HotelRoomResponse()
        roomResponse.isPayLater = false
        HotelTracking.trackLinkHotelRoomBookClick(HotelOffersResponse.HotelRoomResponse(), false)
    }
}
