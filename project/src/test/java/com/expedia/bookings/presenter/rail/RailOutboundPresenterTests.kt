package com.expedia.bookings.presenter.rail

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.rail.presenter.RailOutboundPresenter
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import com.expedia.bookings.services.TestObserver
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
class RailOutboundPresenterTests {

    var railOutboundPresenter by Delegates.notNull<RailOutboundPresenter>()

    @Before
    fun before() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        railOutboundPresenter = LayoutInflater.from(activity).inflate(R.layout.test_rail_outbound_presenter, null) as RailOutboundPresenter
    }

    @Test
    fun testLegalBannerClick() {
        val testSubscriber = TestObserver<Unit>()
        railOutboundPresenter.legalBannerClicked.subscribe(testSubscriber)

        railOutboundPresenter.legalBanner.performClick()
        testSubscriber.assertValueCount(1)
    }
}
