package com.expedia.bookings.itin.utils

import android.content.Intent
import com.expedia.bookings.data.trips.ItinCardDataFlight
import com.expedia.bookings.data.trips.TripsShareUrlShortenResponse
import com.expedia.bookings.itin.activity.FlightItinDetailsActivity
import com.expedia.bookings.services.TripShareUrlShortenServiceInterface
import com.expedia.bookings.subscribeObserver
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.ShareUtils
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows
import kotlin.test.assertEquals

@RunForBrands(brands = [(MultiBrand.EXPEDIA)])
@RunWith(RobolectricRunner::class)
class ShareTripHelperTest {

    lateinit var activity: FlightItinDetailsActivity
    lateinit var itinCardData: ItinCardDataFlight

    @Before
    fun setup() {
        val intent = Intent()
        intent.putExtra("FLIGHT_ITIN_ID", "FLIGHT_ITIN_ID")
        activity = Robolectric.buildActivity(FlightItinDetailsActivity::class.java, intent).create().get()
        itinCardData = ItinCardDataFlightBuilder().build()
    }

    @Test
    fun testTripComponentShort() {
        //case 1 - trip component has short url
        itinCardData.flightLeg.shareInfo.shortSharableDetailsUrl = "testtest"
        val sut = ShareTripHelper(activity, itinCardData)
        sut.shortenShareUtil.tripShareUrlShortenService = TestShortenUrlService()
        sut.fetchShortShareUrlShowShareDialog()
        val shadowActivity = Shadows.shadowOf(activity)
        val shareIntent = shadowActivity.peekNextStartedActivityForResult().intent
        assertEquals(ShareUtils(activity).getFlightShareTextShort(itinCardData.flightLeg, "testtest", false, "ninaricci"), shareIntent.getStringExtra(Intent.EXTRA_TEXT))
    }

    @Test
    fun testTripComponentDetails() {
        //case 2 - trip component does not have short url but has details url
        itinCardData.flightLeg.shareInfo.shortSharableDetailsUrl = null
        itinCardData.flightLeg.shareInfo.sharableDetailsUrl = "https://expedia.com"
        val sut = ShareTripHelper(activity, itinCardData)
        sut.shortenShareUtil.tripShareUrlShortenService = TestShortenUrlService()
        sut.fetchShortShareUrlShowShareDialog()
        val shadowActivity = Shadows.shadowOf(activity)
        val shareIntent = shadowActivity.peekNextStartedActivityForResult().intent
        assertEquals(ShareUtils(activity).getFlightShareTextShort(itinCardData.flightLeg, "https://e.xp.co", false, "ninaricci"), shareIntent.getStringExtra(Intent.EXTRA_TEXT))
    }

    @Test
    fun testTripShort() {
        //case 3 - trip has short url
        itinCardData.flightLeg.shareInfo.shortSharableDetailsUrl = null
        itinCardData.flightLeg.shareInfo.sharableDetailsUrl = null
        itinCardData.tripComponent.parentTrip.shareInfo.shortSharableDetailsUrl = "asdasd"
        val sut = ShareTripHelper(activity, itinCardData)
        sut.shortenShareUtil.tripShareUrlShortenService = TestShortenUrlService()
        sut.fetchShortShareUrlShowShareDialog()
        val shadowActivity = Shadows.shadowOf(activity)
        val shareIntent = shadowActivity.peekNextStartedActivityForResult().intent
        assertEquals(ShareUtils(activity).getFlightShareTextShort(itinCardData.flightLeg, "asdasd", false, "ninaricci"), shareIntent.getStringExtra(Intent.EXTRA_TEXT))
    }

    @Test
    fun testTripDetails() {
        //case 4 - trip does not have short url but has details url
        itinCardData.flightLeg.shareInfo.shortSharableDetailsUrl = null
        itinCardData.flightLeg.shareInfo.sharableDetailsUrl = null
        itinCardData.tripComponent.parentTrip.shareInfo.shortSharableDetailsUrl = null
        itinCardData.tripComponent.parentTrip.shareInfo.sharableDetailsUrl = "https://expedia.com"
        val sut = ShareTripHelper(activity, itinCardData)
        sut.shortenShareUtil.tripShareUrlShortenService = TestShortenUrlService()
        sut.fetchShortShareUrlShowShareDialog()
        val shadowActivity = Shadows.shadowOf(activity)
        val shareIntent = shadowActivity.peekNextStartedActivityForResult().intent
        assertEquals(ShareUtils(activity).getFlightShareTextShort(itinCardData.flightLeg, "https://e.xp.co", false, "ninaricci"), shareIntent.getStringExtra(Intent.EXTRA_TEXT))
    }

    private class TestShortenUrlService : TripShareUrlShortenServiceInterface {
        override fun getShortenedShareUrl(url: String, observer: Observer<TripsShareUrlShortenResponse>): Disposable =
                Observable.just(TripsShareUrlShortenResponse("https://expedia.com", "https://e.xp.co")).subscribeObserver(observer)
    }
}
