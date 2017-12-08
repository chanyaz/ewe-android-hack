package com.expedia.bookings.mia.activity

import android.content.Intent
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.ui.HotelActivity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.shadows.ShadowApplication
import org.robolectric.shadows.ShadowLog
import org.robolectric.util.ActivityController
import kotlin.test.assertEquals


/**
 * Created by cplachta on 12/7/17.
 */

@RunWith(RobolectricRunner::class)
class MemberDealsActivityTest {
    private lateinit var activityController: ActivityController<MemberDealsActivity>

    @Before
    fun setUp() {
        ShadowLog.stream = System.out
        activityController = Robolectric.buildActivity(MemberDealsActivity::class.java)
        activityController.create().start().resume()
    }

    @Test
    fun testHotelDealSearchWasNavigatedTo() {
        val recyclerView = activityController.get().findViewById<RecyclerView>(R.id.member_deal_recycler_view)

        recyclerView.measure(0,0)
        recyclerView.layout(0,0,100,1000)

        recyclerView.findViewHolderForAdapterPosition(0).itemView.findViewById<CardView>(R.id.search_for_hotel_deals_card_view).performClick()

        val expectedIntent = Intent(activityController.get(), HotelActivity::class.java)
        val actual = ShadowApplication.getInstance().nextStartedActivity
        assertEquals(expectedIntent.component, actual.component)
    }
}