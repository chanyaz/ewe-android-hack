package com.expedia.bookings.test.robolectric

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.analytics.OmnitureTestUtils
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.os.LastMinuteDealsResponse
import com.expedia.bookings.data.sos.DealsDestination
import com.expedia.bookings.hotel.deeplink.HotelExtras
import com.expedia.bookings.mia.DealsCardViewHolder
import com.expedia.bookings.mia.LastMinuteDealsListAdapter
import com.expedia.bookings.mia.activity.LastMinuteDealsActivity
import com.expedia.bookings.mia.vm.BaseDealsCardViewModel
import com.expedia.bookings.mia.vm.LastMinuteDealsCardViewModel
import com.expedia.bookings.utils.HotelsV2DataUtil
import com.expedia.testutils.JSONResourceReader
import org.joda.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadows.ShadowActivity
import kotlin.test.assertFalse

@RunWith(RobolectricRunner::class)
class LastMinuteDealsListAdapterTest {
    private lateinit var context: Context
    private lateinit var adapterUnderTest: LastMinuteDealsListAdapter
    private lateinit var mockAnalyticsProvider: AnalyticsProvider
    private lateinit var lastMinuteDealsActivityController: ActivityController<LastMinuteDealsActivity>
    private lateinit var lastMinuteDealShadowActivity: ShadowActivity
    private lateinit var recyclerView: RecyclerView
    private var dealResponseWithDeals: MockDealResponse = MockDealResponse(100.00)
    private var dealResponseWithOutDeals: MockDealResponse = MockDealResponse(0.00)

    @Before
    fun setUp() {
        lastMinuteDealsActivityController = Robolectric.buildActivity(LastMinuteDealsActivity::class.java)
        lastMinuteDealShadowActivity = shadowOf(lastMinuteDealsActivityController.get())
        mockAnalyticsProvider = OmnitureTestUtils.setMockAnalyticsProvider()
        context = lastMinuteDealsActivityController.get()
        adapterUnderTest = LastMinuteDealsListAdapter(context)
        lastMinuteDealsActivityController.create().start().visible()
        recyclerView = setupRecyclerView()
    }

    @Test
    fun tappingOnLastMinuteDeals_setsParamsCorrectlyForHotelSearch() {
        adapterUnderTest.responseObserver.onChanged(dealResponseWithDeals)
        val vm = LastMinuteDealsCardViewModel(context, dealResponseWithDeals.offers.hotels[1], "USD")

        clickOnViewHolderForAdapterPosition(recyclerView, vm, 0)

        val startedIntent = lastMinuteDealShadowActivity.nextStartedActivity
        val hotelSearchParamsString = startedIntent.extras.getString(HotelExtras.EXTRA_HOTEL_SEARCH_PARAMS)
        val hotelSearchParams = HotelsV2DataUtil.getHotelV2SearchParamsFromJSON(hotelSearchParamsString)

        assertEquals(hotelSearchParams?.adults, 2)
        assertEquals(hotelSearchParams?.suggestion?.hotelId, "12345")
        assertEquals(hotelSearchParams?.checkIn, LocalDate(2020, 2, 12))
        assertEquals(hotelSearchParams?.checkOut, LocalDate(2020, 2, 16))
    }

    @Test
    fun tappingDestinationTracksRank() {
        adapterUnderTest.responseObserver.onChanged(dealResponseWithDeals)

        val vm = LastMinuteDealsCardViewModel(context, DealsDestination.Hotel(), "")

        clickOnViewHolderForAdapterPosition(recyclerView, vm, 0)
        OmnitureTestUtils.assertLinkTracked("Last Minute Deals", "App.LMD.Rank.0", mockAnalyticsProvider)

        clickOnViewHolderForAdapterPosition(recyclerView, vm, 1)
        OmnitureTestUtils.assertLinkTracked("Last Minute Deals", "App.LMD.Rank.1", mockAnalyticsProvider)
    }

    @Test
    fun tappingOnLastMinuteDeals_disablesPayWithPoints() {
        adapterUnderTest.responseObserver.onChanged(dealResponseWithDeals)
        val vm = LastMinuteDealsCardViewModel(context, DealsDestination.Hotel(), "")

        clickOnViewHolderForAdapterPosition(recyclerView, vm, 0)

        val startedIntent = lastMinuteDealShadowActivity.nextStartedActivity
        val hotelSearchParamsString = startedIntent.extras.getString(HotelExtras.EXTRA_HOTEL_SEARCH_PARAMS)
        val hotelSearchParams = HotelsV2DataUtil.getHotelV2SearchParamsFromJSON(hotelSearchParamsString)

        assertFalse(hotelSearchParams!!.shopWithPoints)
    }

    @Test
    fun tappingOnLastMinuteDeals_sendsDealsCodeOnIntent() {
        adapterUnderTest.responseObserver.onChanged(dealResponseWithDeals)
        val vm = LastMinuteDealsCardViewModel(context, DealsDestination.Hotel(), "")

        clickOnViewHolderForAdapterPosition(recyclerView, vm, 0)

        val startedIntent = lastMinuteDealShadowActivity.nextStartedActivity
        val code = startedIntent.extras.get(Codes.DEALS)

        assertEquals(true, code)
    }

    @Test
    fun subtitleShowsLocation_givenContextIsLastMinuteDeal() {
        adapterUnderTest.responseObserver.onChanged(dealResponseWithDeals)

        val dealsDestinationViewHolderInPositionOne = recyclerView.findViewHolderForAdapterPosition(0) as DealsCardViewHolder
        assertEquals("Some City", dealsDestinationViewHolderInPositionOne.dealsSubtitle.text)
    }

    @Test
    fun strikeOutPriceIsEmpty_untilOfferServiceIsFixed() {
        adapterUnderTest.responseObserver.onChanged(dealResponseWithDeals)

        val dealsDestinationViewHolderInPositionOne = recyclerView.findViewHolderForAdapterPosition(0) as DealsCardViewHolder
        assertEquals("", dealsDestinationViewHolderInPositionOne.strikePriceView.text)
    }

    @Test
    fun titleShowsHotelName_givenContextIsLastMinuteDeal() {
        adapterUnderTest.responseObserver.onChanged(dealResponseWithDeals)
        val dealsDestinationViewHolderInPositionOne = recyclerView.findViewHolderForAdapterPosition(0) as DealsCardViewHolder
        assertEquals("Some Hotel", dealsDestinationViewHolderInPositionOne.titleView.text)
    }

    @Test
    fun dealCardNotIncluded_givenNoDiscount() {
        adapterUnderTest.responseObserver.onChanged(dealResponseWithOutDeals)
        assertEquals(0, recyclerView.adapter.itemCount)
    }

    @Test
    fun showsNoCells_WhenNoOffersInResponse() {

        adapterUnderTest.responseObserver.onChanged(createResponseWithEmptyOffersNode())
        assertEquals(0, recyclerView.adapter.itemCount)
    }

    private fun clickOnViewHolderForAdapterPosition(recyclerView: RecyclerView, vm: BaseDealsCardViewModel, position: Int) {
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position) as DealsCardViewHolder
        viewHolder.bind(vm)
        viewHolder.itemView.performClick()
    }

    class MockDealResponse(crossoutPriceValue: Double) : LastMinuteDealsResponse() {
        init {
            offerInfo = OfferInfo()
            offers.hotels = generateCells(2, crossoutPriceValue)
            offerInfo?.currency = "USD"
        }

        private fun generateCells(count: Int, crossoutPriceValue: Double): List<DealsDestination.Hotel> {
            val listLoading = ArrayList<DealsDestination.Hotel>()
            for (i in 1..count) {
                val lastMinuteDealsDestination = DealsDestination.Hotel()
                lastMinuteDealsDestination.offerMarkers = listOf("LEADIN_PRICE")
                lastMinuteDealsDestination.hotelPricingInfo = DealsDestination.Hotel.HotelPricingInfo()
                lastMinuteDealsDestination.hotelInfo = DealsDestination.Hotel.HotelInfo()
                lastMinuteDealsDestination.destination = DealsDestination.Hotel.Destination()
                lastMinuteDealsDestination.offerDateRange = DealsDestination.Hotel.OfferDateRange()
                lastMinuteDealsDestination.hotelPricingInfo!!.crossOutPriceValue = crossoutPriceValue
                lastMinuteDealsDestination.hotelPricingInfo!!.percentSavings = 20.00
                lastMinuteDealsDestination.hotelInfo!!.hotelProvince = "Some State"
                lastMinuteDealsDestination.hotelInfo!!.hotelName = "Some Hotel"
                lastMinuteDealsDestination.destination!!.shortName = "Some City"
                lastMinuteDealsDestination.hotelInfo!!.hotelId = "12345"
                lastMinuteDealsDestination.offerDateRange!!.travelStartDate = listOf(2020, 2, 12)
                lastMinuteDealsDestination.offerDateRange!!.travelEndDate = listOf(2020, 2, 16)
                listLoading.add(lastMinuteDealsDestination)
            }
            return listLoading
        }
    }

    private fun setupRecyclerView(): RecyclerView {
        val recyclerView = lastMinuteDealShadowActivity.findViewById(R.id.last_minute_deal_recycler_view) as RecyclerView
        val layoutManager = LinearLayoutManager(context)

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapterUnderTest
        return recyclerView
    }

    private fun createResponseWithEmptyOffersNode(): LastMinuteDealsResponse {
        val resourceReader = JSONResourceReader("../lib/mocked/templates/m/api/os/os_no_offers.json")
        return resourceReader.constructUsingGson(LastMinuteDealsResponse::class.java)
    }
}
