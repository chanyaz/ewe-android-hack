package com.expedia.bookings.mia.activity

import android.content.Intent
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import com.expedia.bookings.R
import com.expedia.bookings.data.sos.DealsDestination
import com.expedia.bookings.data.sos.MemberDealsRequest
import com.expedia.bookings.data.sos.MemberDealsResponse
import com.expedia.bookings.mia.arch.MemberDealsArchViewModel
import com.expedia.bookings.services.sos.ISmartOfferService
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.ui.HotelActivity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadows.ShadowApplication
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class MemberDealsActivityTest {

    private lateinit var activityController: ActivityController<TestMemberDealsActivity>
    private lateinit var activity: TestMemberDealsActivity

    @Before
    fun setUp() {
        activityController = Robolectric.buildActivity(TestMemberDealsActivity::class.java)
        activity = activityController.get()
        activity.setTheme(R.style.Theme_Deals)
        activityController.create().start().visible()
    }

    @Test
    fun testLiveDataIsBindToAdapterSubject() {
        val mockResponse = TestMemberDealsResponse()
        activity.getDealsViewModel().responseLiveData.value = mockResponse
        assertEquals(3, activity.getDealsAdapter().itemCount)
    }

    @Test
    fun testHotelDealSearchWasNavigatedTo() {
        val recyclerView = activity.findViewById<RecyclerView>(R.id.member_deal_recycler_view)
        recyclerView.findViewHolderForAdapterPosition(0).itemView.findViewById<CardView>(R.id.search_for_hotel_deals_card_view).performClick()
        val expectedIntent = Intent(activity, HotelActivity::class.java)
        val actual = ShadowApplication.getInstance().nextStartedActivity
        assertEquals(expectedIntent.component, actual.component)
    }

    class TestMemberDealsActivity : MemberDealsActivity() {
        fun getDealsAdapter() = adapter
        fun getDealsViewModel() = viewModel
        private val mockOfferService = Mockito.mock(ISmartOfferService::class.java)
        override val viewModel = MemberDealsArchViewModel(mockOfferService, MemberDealsRequest())
    }

    inner class TestMemberDealsResponse : MemberDealsResponse() {
        init {
            destinations = listOf(DealsDestination(), DealsDestination())
        }
    }
}
