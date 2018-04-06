package com.expedia.bookings.data.trips

import android.content.Context
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.analytics.OmnitureTestUtils.Companion.assertStateTracked
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.AirAttach
import com.expedia.bookings.data.Db
import com.expedia.bookings.itin.common.ItinPageUsableTracking
import com.expedia.bookings.test.OmnitureMatchers.Companion.withEventsString
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.stubbing.Answer
import org.robolectric.RuntimeEnvironment
import java.util.HashSet
import org.mockito.Mockito.`when` as whenever

@RunWith(RobolectricRunner::class)
class ItinCardDataAdapterTest {

    val context = RuntimeEnvironment.application

    private lateinit var sut: ItinCardDataAdapter
    private lateinit var mockedPageUsableTrackingDataModel: ItinPageUsableTracking
    private lateinit var mockedItineraryManager: ItineraryManager
    private lateinit var mockAnalyticsProvider: AnalyticsProvider

    @Before
    fun setup() {
        Db.getTripBucket().clearAirAttach()
        mockedItineraryManager = createMockItinManager()
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        mockedPageUsableTrackingDataModel = Mockito.spy(ItinPageUsableTracking())
        Mockito.doAnswer(Answer<String?> {
            if (it.callRealMethod() != null) {
                return@Answer "0.10"
            } else {
                return@Answer null
            }
        }).`when`(mockedPageUsableTrackingDataModel).getLoadTimeInSeconds()
        sut = TestItinCardDataAdapter(context, mockedItineraryManager, mockedPageUsableTrackingDataModel)
        // setup air attach bucket
        enableAirAttach()
    }

    @Test
    fun airAttachCardShowsUnderFlightCard() {
        sut.syncWithManager()
        val firstItem = sut.getItem(0)
        val secondItem = sut.getItem(1)

        assertEquals(ItinCardDataFlight::class.java, firstItem.javaClass)
        assertEquals(ItinCardDataAirAttach::class.java, secondItem.javaClass)
    }

    @Test
    fun trackItinLoginPageUsable() {
        givenPageUsableTrackingDataModelHasStartTime()
        sut.trackItinLoginPageUsable()

        assertPageUsableTracked()
    }

    @Test
    fun trackItinLoginPageUsableWithNoData() {
        givenPageUsableTrackingDataModelHasStartTime()
        givenNoItinDataAvailable()

        sut.trackItinLoginPageUsable()

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
    }

    @Test
    fun trackItinLoginPageUsableNoStartTime() {
        givenPageUsableTrackingDataModelNoStartTime()
        sut.trackItinLoginPageUsable()

        OmnitureTestUtils.assertNoTrackingHasOccurred(mockAnalyticsProvider)
    }

    @Test
    fun trackItinLoginPageUsableTwiceOnlyActuallyTracksOnce() {
        givenPageUsableTrackingDataModelHasStartTime()

        sut.trackItinLoginPageUsable()
        sut.trackItinLoginPageUsable()

        assertPageUsableTracked()
    }

    private fun givenPageUsableTrackingDataModelNoStartTime() {
        mockedPageUsableTrackingDataModel.resetStartTime()
    }

    private fun givenPageUsableTrackingDataModelHasStartTime() {
        mockedPageUsableTrackingDataModel.markSuccessfulStartTime(System.currentTimeMillis())
    }

    private fun givenNoItinDataAvailable() {
        whenever(mockedItineraryManager.itinCardData).thenReturn(emptyList())
    }

    private fun createMockItinManager(): ItineraryManager {
        val mockItineraryManager = Mockito.mock(ItineraryManager::class.java)
        val itinCardData = ItinCardDataFlightBuilder().build(airAttachEnabled = true)
        whenever(mockItineraryManager.itinCardData).thenReturn(listOf(itinCardData))
        return mockItineraryManager
    }

    private fun enableAirAttach() {
        val airAttach = Mockito.mock(AirAttach::class.java)
        whenever(airAttach.isAirAttachQualified).thenReturn(true)
        Db.getTripBucket().setAirAttach(airAttach)
    }

    private fun assertPageUsableTracked() {
        assertStateTracked("App.Itinerary", withEventsString("event63,event220,event221=0.10"), mockAnalyticsProvider)
    }

    class TestItinCardDataAdapter(context: Context, private val itinManager: ItineraryManager, private val putDataModel: ItinPageUsableTracking?) : ItinCardDataAdapter(context) {

        override fun getItineraryManager(): ItineraryManager = itinManager

        override fun getDismissedHotelAndFlightButtons(): HashSet<String> = HashSet()

        override fun getDismissedLXAttachButtons(): HashSet<String> = HashSet()

        override fun getItinPageUsableTracking(): ItinPageUsableTracking? = putDataModel
    }
}
