package com.expedia.bookings.data.trips

import android.content.Context
import com.expedia.bookings.data.AirAttach
import com.expedia.bookings.data.Db
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.itin.ItinButtonCard
import com.expedia.bookings.widget.itin.support.ItinCardDataFlightBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RuntimeEnvironment
import org.junit.Assert.assertEquals
import java.util.HashSet

@RunWith(RobolectricRunner::class)
class ItinCardDataAdapterTest {

    val context = RuntimeEnvironment.application

    lateinit private var sut: ItinCardDataAdapter

    @Before
    fun setup() {
        // make mock itinerary manager
        val mockItineraryManager = createMockItinManager()
        sut = TestItinCardDataAdapter(context, mockItineraryManager)
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

    class TestItinCardDataAdapter(context: Context, val mockedItineraryManager: ItineraryManager) : ItinCardDataAdapter(context) {

        override fun getItineraryManagerInstance(): ItineraryManager {
            return mockedItineraryManager
        }

        override fun getDismissedHotelAndFlightButtons(): HashSet<String> {
            return HashSet()
        }

        override fun getDismissedLXAttachButtons(): HashSet<String> {
            return HashSet()
        }
    }
}
