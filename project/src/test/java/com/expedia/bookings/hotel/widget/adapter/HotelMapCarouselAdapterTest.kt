package com.expedia.bookings.hotel.widget.adapter

import android.support.v7.widget.RecyclerView
import android.widget.LinearLayout
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.hotel.widget.HotelCarouselViewHolder
import com.expedia.bookings.hotel.widget.HotelMapCellViewHolder
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.testutils.JSONResourceReader
import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class HotelMapCarouselAdapterTest {

    private val context = RuntimeEnvironment.application
    private var adapter = HotelMapCarouselAdapter(emptyList(), false)
    private lateinit var recyclerView: RecyclerView
    private lateinit var parent: LinearLayout
    private var hotels: List<Hotel> = emptyList()
    private var viewHolders = ArrayList<RecyclerView.ViewHolder>()

    @Before
    fun before() {
        recyclerView = RecyclerView(context)
        parent = LinearLayout(context)
    }

    @Test
    fun testHotelSoldOut() {
        setUpTestForHotelMapCellViewHolder()

        val soldOutSubscriber = TestObserver<Boolean>()
        (viewHolders[0] as HotelMapCellViewHolder).viewModel.soldOut.subscribe(soldOutSubscriber)

        adapter.hotelSoldOut.onNext("0")

        assertTrue(hotels[0].isSoldOut)
        soldOutSubscriber.assertValue(true)
    }

    @Test
    fun testHotelSoldOutAfterRecycled() {
        setUpTestForHotelMapCellViewHolder()

        val soldOutSubscriber = TestObserver<Boolean>()
        (viewHolders[0] as HotelMapCellViewHolder).viewModel.soldOut.subscribe(soldOutSubscriber)

        adapter.onViewRecycled(viewHolders[0])

        adapter.hotelSoldOut.onNext("0")

        assertTrue(hotels[0].isSoldOut)
        soldOutSubscriber.assertEmpty()
    }

    @Test
    fun testHotelSoldOutIdNotInList() {
        setUpTestForHotelMapCellViewHolder()

        val soldOutSubscriber = TestObserver<Boolean>()
        (viewHolders[0] as HotelMapCellViewHolder).viewModel.soldOut.subscribe(soldOutSubscriber)

        adapter.hotelSoldOut.onNext("3")

        for (hotel in hotels) {
            assertFalse(hotel.isSoldOut)
        }
        soldOutSubscriber.assertEmpty()
    }

    @Test
    fun testSetItems() {
        assertTrue(adapter.hotels.isEmpty())
        assertEquals(0, adapter.itemCount)

        adapter.setItems(createHotels(1))
        assertEquals(1, adapter.hotels.size)
        assertEquals(1, adapter.itemCount)

        adapter.setItems(createHotels(5))
        assertEquals(5, adapter.hotels.size)
        assertEquals(5, adapter.itemCount)
    }

    @Test
    fun testOnBindHotelMapCellViewHolder() {
        setUpTestForHotelMapCellViewHolder(bindViewHolder = false)

        assertOnBindViewHolder()
    }

    @Test
    fun testOnBindHotelCarouselViewHolder() {
        setUpTestForHotelCarouselViewHolder(bindViewHolder = false)

        assertOnBindViewHolder()
    }

    @Test
    fun testOnCreateHotelMapCellViewHolder() {
        setUpTestForHotelMapCellViewHolder()

        val hotelSubscriber = TestObserver<Hotel>()
        adapter.hotelSubject.subscribe(hotelSubscriber)
        val favoriteAddedSubscriber = TestObserver<String>()
        adapter.favoriteAddedSubject.subscribe(favoriteAddedSubscriber)
        val favoriteRemovedSubscriber = TestObserver<String>()
        adapter.favoriteRemovedSubject.subscribe(favoriteRemovedSubscriber)

        val viewHolder = adapter.createViewHolder(parent, 0) as HotelMapCellViewHolder
        adapter.bindViewHolder(viewHolder, 0)
        adapter.notifyDataSetChanged()

        viewHolder.hotelClickedSubject.onNext(0)
        // cant set holder adapterPosition so hotelSubject not called
        hotelSubscriber.assertEmpty()

        viewHolder.favoriteAddedSubject.onNext("0")
        favoriteAddedSubscriber.assertValue("0")

        viewHolder.favoriteRemovedSubject.onNext("0")
        favoriteRemovedSubscriber.assertValue("0")
    }

    @Test
    fun testOnCreateHotelCarouselViewHolder() {
        setUpTestForHotelMapCellViewHolder()

        val hotelSubscriber = TestObserver<Hotel>()
        adapter.hotelSubject.subscribe(hotelSubscriber)

        assertFalse(adapter.isPackage)

        val viewHolder = adapter.createViewHolder(parent, 0) as HotelMapCellViewHolder

        assertFalse(viewHolder.isPackage)
    }

    @Test
    fun testOnCreateHotelCarouselViewHolderPackage() {
        setUpTestForPackageHotelMapCellViewHolder()

        val hotelSubscriber = TestObserver<Hotel>()
        adapter.hotelSubject.subscribe(hotelSubscriber)

        assertTrue(adapter.isPackage)

        val viewHolder = adapter.createViewHolder(parent, 0) as HotelMapCellViewHolder

        assertTrue(viewHolder.isPackage)
    }

    @Test
    fun testOnRecycledHotelMapCellViewHolder() {
        setUpTestForHotelMapCellViewHolder()

        val soldOutSubscriber = TestObserver<Boolean>()
        (viewHolders[1] as HotelMapCellViewHolder).viewModel.soldOut.subscribe(soldOutSubscriber)

        adapter.onViewRecycled(viewHolders[1])
        adapter.onViewRecycled(viewHolders[1])

        adapter.hotelSoldOut.onNext("1")

        soldOutSubscriber.assertEmpty()
    }

    @Test
    fun testOnRecycledHotelCarouselViewHolder() {
        setUpTestForHotelCarouselViewHolder()

        val soldOutSubscriber = TestObserver<Boolean>()
        (viewHolders[1] as HotelCarouselViewHolder).viewModel.soldOut.subscribe(soldOutSubscriber)

        adapter.onViewRecycled(viewHolders[1])
        adapter.onViewRecycled(viewHolders[1])

        adapter.hotelSoldOut.onNext("1")

        soldOutSubscriber.assertEmpty()
    }

    private fun assertOnBindViewHolder() {
        val subscribers = List(viewHolders.count(), { i ->
            val subscriber = TestObserver<Boolean>()
            if (viewHolders[i] is HotelMapCellViewHolder) {
                (viewHolders[i] as HotelMapCellViewHolder).viewModel.soldOut.subscribe(subscriber)
            } else if (viewHolders[i] is HotelCarouselViewHolder) {
                (viewHolders[i] as HotelCarouselViewHolder).viewModel.soldOut.subscribe(subscriber)
            }
            subscriber
        })

        viewHolders.forEach { viewHolder ->
            adapter.bindViewHolder(viewHolder, 0)
        }

        viewHolders.forEach { viewHolder ->
            if (viewHolder is HotelMapCellViewHolder) {
                assertEquals("0", viewHolder.viewModel.hotelId)
            } else if (viewHolder is HotelCarouselViewHolder) {
                assertEquals("0", viewHolder.viewModel.hotelId)
            }
        }

        adapter.hotelSoldOut.onNext("0")
        if (subscribers.isNotEmpty()) {
            subscribers[0].assertValue(true)
            subscribers.forEachIndexed { i, subscriber ->
                if (i != 0) {
                    subscriber.assertEmpty()
                }
            }
        }
    }

    private fun createHotels(count: Int): List<Hotel> {
        return List(count, { i ->
            val resourceReader = JSONResourceReader("src/test/resources/raw/hotel/the_talbott_hotel.json")
            val hotel = resourceReader.constructUsingGson(Hotel::class.java)
            hotel.hotelId = i.toString()
            hotel
        })
    }

    private fun setUpTestForHotelMapCellViewHolder(hotelCount: Int = 3, bindViewHolder: Boolean = true) {
        AbacusTestUtils.bucketTests(AbacusUtils.HotelResultsCellOnMapCarousel)
        setUpAdapterAndViewHolders(hotelCount, bindViewHolder, false)
    }

    private fun setUpTestForPackageHotelMapCellViewHolder(hotelCount: Int = 3, bindViewHolder: Boolean = true) {
        AbacusTestUtils.bucketTests(AbacusUtils.HotelResultsCellOnMapCarousel)
        setUpAdapterAndViewHolders(hotelCount, bindViewHolder, true)
    }

    private fun setUpTestForHotelCarouselViewHolder(hotelCount: Int = 3, bindViewHolder: Boolean = true) {
        AbacusTestUtils.unbucketTests(AbacusUtils.HotelResultsCellOnMapCarousel)
        setUpAdapterAndViewHolders(hotelCount, bindViewHolder, false)
    }

    private fun setUpAdapterAndViewHolders(hotelCount: Int, bindViewHolder: Boolean, isPackage: Boolean) {
        hotels = createHotels(hotelCount)
        adapter = HotelMapCarouselAdapter(hotels, isPackage)
        recyclerView.adapter = adapter
        for (i in 0 until hotelCount) {
            val viewHolder = adapter.createViewHolder(parent, 0)
            viewHolders.add(viewHolder)
            if (bindViewHolder) {
                adapter.bindViewHolder(viewHolder, i)
            }
        }
    }
}
