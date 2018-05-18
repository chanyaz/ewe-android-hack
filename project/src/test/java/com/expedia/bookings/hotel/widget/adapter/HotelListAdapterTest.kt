package com.expedia.bookings.hotel.widget.adapter

import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.hotel.data.HotelAdapterItem
import com.expedia.bookings.hotel.widget.HotelCellViewHolder
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.LoadingViewHolder
import com.expedia.bookings.widget.shared.AbstractHotelCellViewHolder
import com.expedia.testutils.JSONResourceReader
import io.reactivex.subjects.PublishSubject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import java.util.ArrayList
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelListAdapterTest {
    val testSelectedSubject = PublishSubject.create<Hotel>()
    val testAdapter = HotelListAdapter(testSelectedSubject, PublishSubject.create(), PublishSubject.create())

    val context = RuntimeEnvironment.application

    val EXPECTED_URGENCY_LOCATION = 6

    @Test
    fun testCreateLoadingViewHolder() {
        val parent = LinearLayout(context)
        val viewHolder = testAdapter.createViewHolder(parent, HotelAdapterItem.LOADING)

        assertTrue(viewHolder is LoadingViewHolder)
    }

    @Test
    fun testCreateHotelCellViewHolder() {
        val parent = LinearLayout(context)
        val viewHolder = testAdapter.createViewHolder(parent, HotelAdapterItem.HOTEL)
        assertTrue(viewHolder is HotelCellViewHolder)
    }

    @Test
    fun testBindHotelViewHolder_NullPin_FirstItem() {
        testAdapter.resultsSubject.onNext(getMockSearchResponse(1, null))
        val parent = LinearLayout(context)
        val viewHolder = testAdapter.createViewHolder(parent, HotelAdapterItem.HOTEL) as AbstractHotelCellViewHolder

        testAdapter.bindViewHolder(viewHolder, 0 + testAdapter.firstHotelIndex)
        assertEquals(View.GONE, viewHolder.pinnedHotelTextView.visibility)
    }

    @Test
    fun testBindHotelViewHolder_NoPin_FirstItem() {
        val response = getMockSearchResponse(1, false)
        testAdapter.resultsSubject.onNext(response)
        val parent = LinearLayout(context)
        val viewHolder = testAdapter.createViewHolder(parent, HotelAdapterItem.HOTEL) as AbstractHotelCellViewHolder

        testAdapter.bindViewHolder(viewHolder, 0 + testAdapter.firstHotelIndex)
        assertEquals(View.GONE, viewHolder.pinnedHotelTextView.visibility)
    }

    @Test
    fun testBindHotelViewHolder_Pinned_FirstItem() {
        val response = getMockSearchResponse(1, true)
        testAdapter.resultsSubject.onNext(response)

        val parent = LinearLayout(context)
        val viewHolder = testAdapter.createViewHolder(parent, HotelAdapterItem.HOTEL) as AbstractHotelCellViewHolder

        testAdapter.bindViewHolder(viewHolder, 0 + testAdapter.firstHotelIndex)
        assertEquals(View.VISIBLE, viewHolder.pinnedHotelTextView.visibility)
    }

    @Test
    fun testBindHotelViewHolder_Pinned_SecondItem() {
        val response = getMockSearchResponse(2, true)
        testAdapter.resultsSubject.onNext(response)

        val parent = LinearLayout(context)
        val viewHolder = testAdapter.createViewHolder(parent, HotelAdapterItem.HOTEL) as AbstractHotelCellViewHolder

        testAdapter.bindViewHolder(viewHolder, 1 + testAdapter.firstHotelIndex)
        assertEquals(View.GONE, viewHolder.pinnedHotelTextView.visibility, "FAIL: Only first item in pinned search should be pinned.")
    }

    @Test
    fun testAddUrgencyHappy() {
        val response = getMockSearchResponse(8, true)
        testAdapter.resultsSubject.onNext(response)
        testAdapter.addUrgency("URGENT")

        assertEquals(HotelAdapterItem.URGENCY, testAdapter.getItemViewType(position = EXPECTED_URGENCY_LOCATION),
                "FAILURE: Expected urgency message to be placed after the 4th hotel result")
    }

    @Test
    fun testAddUrgencyOneResult() {
        val response = getMockSearchResponse(1, true)
        testAdapter.resultsSubject.onNext(response)
        testAdapter.addUrgency("URGENT")

        assertEquals(HotelAdapterItem.URGENCY, testAdapter.getItemViewType(position = 4),
                "FAILURE: if less than 4 results, urgency should appear at the end of the hotel results.")
    }

    @Test
    fun testClearUrgency() {
        val response = getMockSearchResponse(8, true)
        testAdapter.resultsSubject.onNext(response)
        testAdapter.addUrgency("URGENT")
        assertEquals(HotelAdapterItem.URGENCY, testAdapter.getItemViewType(position = EXPECTED_URGENCY_LOCATION))

        testAdapter.clearUrgency()
        assertNotEquals(HotelAdapterItem.URGENCY, testAdapter.getItemViewType(position = EXPECTED_URGENCY_LOCATION),
                "FAILURE: Expected urgency message")
    }

    @Test
    fun test2xMessaging() {
        testAdapter.loading = true
        assertNotEquals(testAdapter.getItemViewType(2), HotelAdapterItem.EARN_2X)

        val response = getMockSearchResponse(2, false)
        testAdapter.canShow2xMessaging = true
        testAdapter.loading = false
        testAdapter.resultsSubject.onNext(response)
        assertEquals(testAdapter.getItemViewType(2), HotelAdapterItem.EARN_2X)

        testAdapter.loading = false
        testAdapter.canShow2xMessaging = false
        testAdapter.resultsSubject.onNext(response)
        assertNotEquals(testAdapter.getItemViewType(2), HotelAdapterItem.EARN_2X)
    }

    @Test
    fun testGenericAttachedOneResult() {
        val response = getMockSearchResponse(1, false)
        testAdapter.resultsSubject.onNext(response)
        assertNotEquals(testAdapter.getItemViewType(3), HotelAdapterItem.GENERIC_ATTACHED)
        testAdapter.insertGenericAttach()
        assertEquals(testAdapter.getItemViewType(3), HotelAdapterItem.GENERIC_ATTACHED)
    }

    @Test
    fun testGenericAttachedtwoResults() {
        val response = getMockSearchResponse(2, false)
        assertNotEquals(testAdapter.getItemViewType(3), HotelAdapterItem.GENERIC_ATTACHED)
        testAdapter.resultsSubject.onNext(response)
        testAdapter.insertGenericAttach()
        assertEquals(testAdapter.getItemViewType(3), HotelAdapterItem.GENERIC_ATTACHED)
    }

    @Test
    fun testGenericAttachedManyResults() {
        val response = getMockSearchResponse(10, false)
        assertNotEquals(testAdapter.getItemViewType(3), HotelAdapterItem.GENERIC_ATTACHED)
        testAdapter.resultsSubject.onNext(response)
        testAdapter.insertGenericAttach()
        assertEquals(testAdapter.getItemViewType(3), HotelAdapterItem.GENERIC_ATTACHED)
    }

    fun getMockSearchResponse(hotelCount: Int, pinned: Boolean?): HotelSearchResponse {
        val response: HotelSearchResponse
        if (pinned == null) {
            response = getHotelSearchResponse("src/test/resources/raw/hotel/hotel_happy_search_response.json")
        } else {
            if (pinned) {
                response = getHotelSearchResponse("src/test/resources/raw/hotel/hotel_happy_search_response_pinned.json")
            } else {
                response = getHotelSearchResponse("src/test/resources/raw/hotel/hotel_happy_search_response_no_pinned.json")
            }
        }

        response.hotelList = ArrayList()
        for (i in 0..hotelCount) {
            response.hotelList.add(getHappyHotel())
        }
        return response
    }

    fun getHappyHotel(): Hotel {
        val resourceReader = JSONResourceReader("src/test/resources/raw/hotel/the_talbott_hotel.json")
        val searchResponse = resourceReader.constructUsingGson(Hotel::class.java)
        return searchResponse
    }

    private fun getHotelSearchResponse(filePath: String): HotelSearchResponse {
        val resourceReader = JSONResourceReader(filePath)
        val searchResponse = resourceReader.constructUsingGson(HotelSearchResponse::class.java)
        return searchResponse
    }
}
