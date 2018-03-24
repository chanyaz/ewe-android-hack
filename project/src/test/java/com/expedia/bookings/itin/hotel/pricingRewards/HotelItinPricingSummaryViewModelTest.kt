package com.expedia.bookings.itin.hotel.pricingRewards

import android.content.Context
import com.expedia.bookings.data.trips.ItinCardData
import com.expedia.bookings.itin.helpers.MockLifecycleOwner
import com.expedia.bookings.itin.hotel.repositories.ItinHotelRepo
import com.expedia.bookings.itin.scopes.HotelItinPricingSummaryScope
import com.expedia.bookings.itin.tripstore.data.Itin
import com.expedia.bookings.itin.tripstore.data.ItinDetailsResponse
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.services.TestObserver
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.mobiata.mocke3.mockObject
import io.reactivex.Observable
import io.reactivex.Observer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricRunner::class)
class HotelItinPricingSummaryViewModelTest {
    val context: Context = RuntimeEnvironment.application
    lateinit var observer: TestObserver<List<HotelItinPricingSummary>>

    @Before
    fun setup() {
        observer = TestObserver()
    }

    @After
    fun tearDown() {
        observer.dispose()
    }

    @Test
    fun testSingleRoomOutputsSingleSummary() {
        val viewModel = viewModelWithSingleRoom()

        observer.assertEmpty()
        viewModel.observer.onChanged(viewModel.scope.itinHotelRepo.liveDataHotel.value)
        assertEquals(1, observer.values().firstOrNull()?.size)
    }

    @Test
    fun testMultipleRoomsOutputsMultipleSummaries() {
        val viewModel = viewModelWithMultipleRooms()

        observer.assertEmpty()
        viewModel.observer.onChanged(viewModel.scope.itinHotelRepo.liveDataHotel.value)
        assertEquals(3, observer.values().firstOrNull()?.size)
    }

    @Test
    fun testMultipleSummaryCounts() {
        val viewModel = viewModelWithMultipleRooms()

        observer.assertEmpty()
        viewModel.observer.onChanged(viewModel.scope.itinHotelRepo.liveDataHotel.value)
        val summaries = observer.values().firstOrNull()

        assertNotNull(summaries)
        assertEquals(3, summaries?.size)

        val expectedCounts = listOf(4, 4, 3)

        if (summaries != null) {
            for ((idx, summary) in summaries.withIndex()) {
                assertEquals(expectedCounts[idx], summary.perDayLineItems.size)
            }
        }
    }

    @Test
    fun testSummaryValues() {
        val viewModel = viewModelWithSingleRoom()

        observer.assertEmpty()
        viewModel.observer.onChanged(viewModel.scope.itinHotelRepo.liveDataHotel.value)
        val summary = observer.values().firstOrNull()?.firstOrNull()

        assertNotNull(summary)
        assertEquals("Room price", summary?.totalLineItem?.labelString)
        assertEquals("₹3,500.00", summary?.totalLineItem?.priceString)

        val lineItems = summary?.perDayLineItems

        assertNotNull(lineItems)
        assertEquals(4, lineItems?.size)

        val expectedLabels = listOf("Mon, Mar 12", "Tue, Mar 13", "Wed, Mar 14", "Thu, Mar 15")

        if (lineItems != null) {
            for ((idx, item) in lineItems.withIndex()) {
                assertEquals(expectedLabels[idx], item.labelString)
                assertEquals("₹875.00", item.priceString)
            }
        }
    }

    private fun viewModelWithSingleRoom(): HotelItinPricingSummaryViewModel<HotelItinPricingSummaryScope> {
        val viewModel = HotelItinPricingSummaryViewModel(getScope(true))

        viewModel.lineItemViewModelSubject.subscribe(observer)

        return viewModel
    }

    private fun viewModelWithMultipleRooms(): HotelItinPricingSummaryViewModel<HotelItinPricingSummaryScope> {
        val viewModel = HotelItinPricingSummaryViewModel(getScope())

        viewModel.lineItemViewModelSubject.subscribe(observer)

        return viewModel
    }

    private fun getScope(forSingleRoom: Boolean = false): HotelItinPricingSummaryScope {
        val itinId = if (forSingleRoom) "single" else ""
        val repo = ItinHotelRepo(itinId, MockReadJsonUtil, TestObservable)

        return HotelItinPricingSummaryScope(repo, MockStringProvider, MockLifecycleOwner())
    }

    object MockStringProvider : StringSource {
        override fun fetchWithPhrase(stringResource: Int, map: Map<String, String>): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun fetch(stringResource: Int): String {
            return RuntimeEnvironment.application.getString(stringResource)
        }
    }

    object MockReadJsonUtil : IJsonToItinUtil {
        override fun getItin(itinId: String?): Itin? {
            var mockName = "api/trips/hotel_trip_details_with_multiple_rooms_for_mocker.json"

            if (itinId == "single") {
                mockName = "api/trips/hotel_trip_details_for_mocker.json"
            }

            return mockObject(ItinDetailsResponse::class.java, mockName)?.itin
        }
    }

    object TestObservable : Observable<MutableList<ItinCardData>>() {
        override fun subscribeActual(observer: Observer<in MutableList<ItinCardData>>?) {}
    }
}
