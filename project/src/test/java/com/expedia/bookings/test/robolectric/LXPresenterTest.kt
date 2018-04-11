package com.expedia.bookings.test.robolectric

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.lx.Offer
import com.expedia.bookings.data.lx.Ticket
import com.expedia.bookings.otto.Events
import com.expedia.bookings.presenter.lx.LXPresenter
import com.expedia.bookings.utils.AbacusTestUtils
import com.expedia.bookings.utils.Ui
import com.google.gson.GsonBuilder
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class LXPresenterTest {
    lateinit var lxPresenter: LXPresenter
    lateinit var activity: Activity

    @Before
    fun setup() {
        Ui.getApplication(RuntimeEnvironment.application).defaultLXComponents()
        activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_LX)
    }

    @Test
    fun testDisplayOfLXWebCheckoutView() {
        AbacusTestUtils.bucketTestAndEnableRemoteFeature(activity, AbacusUtils.EBAndroidAppLxWebCheckoutView)
        lxPresenter = LayoutInflater.from(activity).inflate(R.layout.lx_base_layout, null) as LXPresenter
        Events.register(lxPresenter)
        lxPresenter.show(lxPresenter.detailsPresenter)
        lxPresenter.show(lxPresenter.webCheckoutView)
        Events.post(Events.LXOfferBooked(Offer(), listOf(adultTicket())))

        assertEquals(View.VISIBLE, lxPresenter.webCheckoutView.visibility)
    }

    fun adultTicket(): Ticket {
        val gson = GsonBuilder().create()
        val adultTicket = gson.fromJson(
                "{\"code\": \"Adult\",\"count\": \"3 \", \"ticketId\": \"90042\", \"name\": \"Adult\", \"restrictionText\": \"13+ years\", \"price\": \"$130\", \"originalPrice\": \"$145\", \"amount\": \"130\", \"originalAmount\": \"145\", \"displayName\": null, \"defaultTicketCount\": 2 }",
                Ticket::class.java)
        adultTicket.money = Money("100", "USD")
        return adultTicket
    }
}
