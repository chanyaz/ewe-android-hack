package com.expedia.bookings.test.robolectric

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.os.LastMinuteDealsResponse
import com.expedia.bookings.data.sos.DealsDestination
import com.expedia.bookings.mia.DealsDestinationViewHolder
import com.expedia.bookings.mia.LastMinuteDealListAdapter
import com.expedia.bookings.mia.activity.LastMinuteDealActivity
import com.expedia.bookings.mia.vm.DealsDestinationViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadows.ShadowActivity

@RunWith(RobolectricRunner::class)
class LastMinuteDealListAdapterTest {
    private lateinit var context: Context
    private lateinit var adapterUnderTest: LastMinuteDealListAdapter
    private lateinit var mockAnalyticsProvider: AnalyticsProvider
    private lateinit var lastMinuteDealActivityController: ActivityController<LastMinuteDealActivity>
    private lateinit var lastMinuteDealShadowActivity: ShadowActivity
    private var dealResponseWithDeals: MockDealResponse = MockDealResponse(100.00)
    private var dealResponseWithOutDeals: MockDealResponse = MockDealResponse(0.00)

    @Before
    fun setUp() {
        lastMinuteDealActivityController = Robolectric.buildActivity(LastMinuteDealActivity::class.java)
        lastMinuteDealShadowActivity = shadowOf(lastMinuteDealActivityController.get())
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        context = lastMinuteDealActivityController.get()
        adapterUnderTest = LastMinuteDealListAdapter(context)
    }

    @Test
    fun tappingDestinationTracksRank() {
        lastMinuteDealActivityController.create().start().visible()
        adapterUnderTest.resultSubject.onNext(dealResponseWithDeals)

        val vm = DealsDestinationViewModel(context, DealsDestination().Hotel(), "")
        val recyclerView = lastMinuteDealShadowActivity.findViewById(R.id.last_minute_deal_recycler_view) as RecyclerView
        val layoutManager = LinearLayoutManager(context)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapterUnderTest

        clickOnViewHolderForAdapterPosition(recyclerView, vm, 0)
        OmnitureTestUtils.assertLinkTracked("Last Minute Deals", "App.LMD.Rank.0", mockAnalyticsProvider)

        clickOnViewHolderForAdapterPosition(recyclerView, vm, 1)
        OmnitureTestUtils.assertLinkTracked("Last Minute Deals", "App.LMD.Rank.1", mockAnalyticsProvider)
    }

    @Test
    fun subtitleShowsLocation_givenContextIsLastMinuteDeal() {
        lastMinuteDealActivityController.create().start().visible()
        adapterUnderTest.resultSubject.onNext(dealResponseWithDeals)

        val recyclerView = lastMinuteDealShadowActivity.findViewById(R.id.last_minute_deal_recycler_view) as RecyclerView
        val layoutManager = LinearLayoutManager(context)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapterUnderTest

        val dealsDestinationViewHolderInPositionOne = recyclerView.findViewHolderForAdapterPosition(0) as DealsDestinationViewHolder
        assertEquals("Some City", dealsDestinationViewHolderInPositionOne.dealsSubtitle.text)
    }

    @Test
    fun titleShowsHotelName_givenContextIsLastMinuteDeal() {
        lastMinuteDealActivityController.create().start().visible()
        adapterUnderTest.resultSubject.onNext(dealResponseWithDeals)

        val recyclerView = lastMinuteDealShadowActivity.findViewById(R.id.last_minute_deal_recycler_view) as RecyclerView
        val layoutManager = LinearLayoutManager(context)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapterUnderTest

        val dealsDestinationViewHolderInPositionOne = recyclerView.findViewHolderForAdapterPosition(0) as DealsDestinationViewHolder
        assertEquals("Some Hotel", dealsDestinationViewHolderInPositionOne.titleView.text)
    }

    @Test
    fun dealCardNotIncluded_givenNoDiscount() {
        lastMinuteDealActivityController.create().start().visible()
        adapterUnderTest.resultSubject.onNext(dealResponseWithOutDeals)

        val recyclerView = lastMinuteDealShadowActivity.findViewById(R.id.last_minute_deal_recycler_view) as RecyclerView
        val layoutManager = LinearLayoutManager(context)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapterUnderTest

        assertEquals(0, recyclerView.adapter.itemCount)
    }

    private fun clickOnViewHolderForAdapterPosition(recyclerView: RecyclerView, vm: DealsDestinationViewModel, position: Int) {
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position) as DealsDestinationViewHolder
        viewHolder.bind(vm)
        viewHolder.itemView.performClick()
    }

    class MockDealResponse(crossoutPriceValue: Double) : LastMinuteDealsResponse() {
        init {
            offers = Offers()
            offerInfo = OfferInfo()
            offers?.hotels = generateCells(2, crossoutPriceValue)
            offerInfo?.currency = "USD"
        }

        private fun generateCells(count: Int, crossoutPriceValue: Double): List<DealsDestination.Hotel> {
            val listLoading = ArrayList<DealsDestination.Hotel>()
            for (i in 1..count) {
                val lastMinuteDealsDestination = DealsDestination().Hotel()
                lastMinuteDealsDestination.offerMarkers = listOf("LEADIN_PRICE")
                lastMinuteDealsDestination.hotelPricingInfo = DealsDestination().Hotel().HotelPricingInfo()
                lastMinuteDealsDestination.hotelInfo = DealsDestination().Hotel().HotelInfo()
                lastMinuteDealsDestination.destination = DealsDestination().Hotel().Destination()
                lastMinuteDealsDestination.hotelPricingInfo!!.crossOutPriceValue = crossoutPriceValue
                lastMinuteDealsDestination.hotelPricingInfo!!.percentSavings = 20.00
                lastMinuteDealsDestination.hotelInfo!!.hotelProvince = "Some State"
                lastMinuteDealsDestination.hotelInfo!!.hotelName = "Some Hotel"
                lastMinuteDealsDestination.destination!!.shortName = "Some City"
                listLoading.add(lastMinuteDealsDestination)
            }
            return listLoading
        }
    }
}
