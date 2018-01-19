package com.expedia.bookings.test.robolectric

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.expedia.bookings.OmnitureTestUtils
import com.expedia.bookings.R
import com.expedia.bookings.analytics.AnalyticsProvider
import com.expedia.bookings.data.sos.DealsDestination
import com.expedia.bookings.data.sos.DealsResponse
import com.expedia.bookings.mia.DealsDestinationViewHolder
import com.expedia.bookings.mia.LastMinuteDealListAdapter
import com.expedia.bookings.mia.activity.LastMinuteDealActivity
import com.expedia.bookings.mia.vm.DealsDestinationViewModel
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowActivity
import org.robolectric.android.controller.ActivityController
import java.util.ArrayList

@RunWith(RobolectricRunner::class)
class LastMinuteDealListAdapterTest {
    private lateinit var context: Context
    private lateinit var adapterUnderTest: LastMinuteDealListAdapter
    private lateinit var mockAnalyticsProvider: AnalyticsProvider
    private lateinit var lastMinuteDealActivityController: ActivityController<LastMinuteDealActivity>
    private lateinit var lastMinuteDealShadowActivity: ShadowActivity
    private var dealResponse: MockDealResponse = MockDealResponse()


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
        adapterUnderTest.resultSubject.onNext(dealResponse)

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

    private fun clickOnViewHolderForAdapterPosition(recyclerView: RecyclerView, vm: DealsDestinationViewModel, position: Int) {
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position) as DealsDestinationViewHolder
        viewHolder.bind(vm)
        viewHolder.itemView.performClick()
    }


    class MockDealResponse : DealsResponse() {
        init {
            destinations = generateCells(2)
        }

        private fun generateCells(count: Int): List<DealsDestination> {
            val listLoading = ArrayList<DealsDestination>()
            for (i in 1..count) {
                listLoading.add(DealsDestination())
            }
            return listLoading
        }
    }
}
