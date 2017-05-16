package com.expedia.bookings.hotel.widget

import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.hotels.HotelSearchResponse
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.widget.LoadingViewHolder
import com.expedia.bookings.widget.hotel.HotelCellViewHolder
import com.expedia.bookings.widget.hotel.HotelListAdapter
import com.expedia.bookings.widget.shared.AbstractHotelCellViewHolder
import com.expedia.testutils.JSONResourceReader
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import rx.subjects.PublishSubject
import java.util.ArrayList
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelListAdapterTest {
    val testSelectedSubject = PublishSubject.create<Hotel>()
    val testAdapter = HotelListAdapter(testSelectedSubject, PublishSubject.create(), PublishSubject.create())

    val context = RuntimeEnvironment.application

    @Test
    fun testCreateLoadingViewHolder() {
        val parent = LinearLayout(context)
        val viewHolder = testAdapter.createViewHolder(parent, testAdapter.LOADING_VIEW)

        assertTrue(viewHolder is LoadingViewHolder)
    }

    @Test
    fun testCreateHotelCellViewHolder() {
        val parent = LinearLayout(context)
        val viewHolder = testAdapter.createViewHolder(parent, testAdapter.HOTEL_VIEW)
        assertTrue(viewHolder is HotelCellViewHolder)
    }

    @Test
    fun testBindHotelViewHolder_NoPin_FirstItem() {
        testAdapter.resultsSubject.onNext(getMockSearchResponse(1))
        val parent = LinearLayout(context)
        val viewHolder = testAdapter.createViewHolder(parent, testAdapter.HOTEL_VIEW) as AbstractHotelCellViewHolder

        testAdapter.bindViewHolder(viewHolder, 0 + testAdapter.numHeaderItemsInHotelsList())
        assertEquals(View.GONE, viewHolder.pinnedHotelTextView.visibility)
    }

    @Test
    fun testBindHotelViewHolder_Pinned_FirstItem() {
        val response = getMockSearchResponse(1)
        response.isPinnedSearch = true
        testAdapter.resultsSubject.onNext(response)

        val parent = LinearLayout(context)
        val viewHolder = testAdapter.createViewHolder(parent, testAdapter.HOTEL_VIEW) as AbstractHotelCellViewHolder

        testAdapter.bindViewHolder(viewHolder, 0 + testAdapter.numHeaderItemsInHotelsList())
        assertEquals(View.VISIBLE, viewHolder.pinnedHotelTextView.visibility)
    }

    @Test
    fun testBindHotelViewHolder_Pinned_SecondItem() {
        val response = getMockSearchResponse(2)
        response.isPinnedSearch = true
        testAdapter.resultsSubject.onNext(response)

        val parent = LinearLayout(context)
        val viewHolder = testAdapter.createViewHolder(parent, testAdapter.HOTEL_VIEW) as AbstractHotelCellViewHolder

        testAdapter.bindViewHolder(viewHolder, 1 + testAdapter.numHeaderItemsInHotelsList())
        assertEquals(View.GONE, viewHolder.pinnedHotelTextView.visibility, "FAIL: Only first item in pinned search should be pinned.")
    }

    fun getMockSearchResponse(hotelCount: Int) : HotelSearchResponse {
        val response = HotelSearchResponse()
        response.hotelList = ArrayList()
        for(i in 0..hotelCount) {
            response.hotelList.add(getHappyHotel())
        }
        return response
    }

    fun getHappyHotel() : Hotel {
        val resourceReader = JSONResourceReader("src/test/resources/raw/hotel/the_talbott_hotel.json")
        val searchResponse = resourceReader.constructUsingGson(Hotel::class.java)
        return searchResponse
    }
}
