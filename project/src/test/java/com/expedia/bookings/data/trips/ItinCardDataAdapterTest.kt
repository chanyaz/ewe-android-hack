package com.expedia.bookings.data.trips

import android.content.Context
import com.expedia.bookings.data.AirAttach
import com.expedia.bookings.data.Db
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.tracking.ItinPageUsableTrackingData
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import java.util.HashSet

@RunWith(RobolectricRunner::class)
class ItinCardDataAdapterTest {

    val context = RuntimeEnvironment.application

    lateinit private var sut: ItinCardDataAdapter
    lateinit private var mockedPageUsableTrackingDataModel: ItinPageUsableTrackingData

    @Before
    fun setup() {
        Db.getTripBucket().clearAirAttach()
        // make mock itinerary manager
        val mockItineraryManager = createMockItinManager()
        mockedPageUsableTrackingDataModel = Mockito.mock(ItinPageUsableTrackingData::class.java)
        sut = TestItinCardDataAdapter(context, mockItineraryManager, mockedPageUsableTrackingDataModel)
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

        Mockito.verify(mockedPageUsableTrackingDataModel, Mockito.times(1)).hasStartTime()
        Mockito.verify(mockedPageUsableTrackingDataModel, Mockito.times(1)).markTripResultsUsable(Matchers.anyLong())
    }

    @Test
    fun trackItinLoginPageUsableNoStartTime() {
        givenPageUsableTrackingDataModelNoStartTime()
        sut.trackItinLoginPageUsable()

        Mockito.verify(mockedPageUsableTrackingDataModel, Mockito.times(1)).hasStartTime()
        Mockito.verify(mockedPageUsableTrackingDataModel, Mockito.never()).markTripResultsUsable(Matchers.anyLong())
    }

    private fun givenPageUsableTrackingDataModelNoStartTime() {
        Mockito.`when`(mockedPageUsableTrackingDataModel.hasStartTime()).thenReturn(false)
    }

    private fun givenPageUsableTrackingDataModelHasStartTime() {
        Mockito.`when`(mockedPageUsableTrackingDataModel.hasStartTime()).thenReturn(true)
    }

    private fun createMockItinManager(): ItineraryManager {
        val mockItineraryManager = Mockito.mock(ItineraryManager::class.java)
        val itinCardData = ItinCardDataFlightBuilder().build(airAttachEnabled = true)
        Mockito.`when`(mockItineraryManager.itinCardData).thenReturn(listOf(itinCardData))
        return mockItineraryManager
    }

    private fun enableAirAttach() {
        val airAttach = Mockito.mock(AirAttach::class.java)
        Mockito.`when`(airAttach.isAirAttachQualified).thenReturn(true)
        Db.getTripBucket().setAirAttach(airAttach)
    }

    class TestItinCardDataAdapter(context: Context, val mockedItineraryManager: ItineraryManager, val mockedPageUsableTrackingDataModel: ItinPageUsableTrackingData?) : ItinCardDataAdapter(context) {

        override fun getItineraryManagerInstance(): ItineraryManager {
            return mockedItineraryManager
        }

        override fun getDismissedHotelAndFlightButtons(): HashSet<String> {
            return HashSet()
        }

        override fun getDismissedLXAttachButtons(): HashSet<String> {
            return HashSet()
        }

        override fun getItinPageUsableTrackingDataModel(): ItinPageUsableTrackingData? {
            return mockedPageUsableTrackingDataModel
        }
    }
}
