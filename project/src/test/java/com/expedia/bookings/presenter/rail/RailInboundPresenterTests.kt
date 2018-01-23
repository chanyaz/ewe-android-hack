package com.expedia.bookings.presenter.rail

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.rail.presenter.RailInboundPresenter
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import com.expedia.bookings.services.TestObserver
import kotlin.properties.Delegates

@RunWith(RobolectricRunner::class)
class RailInboundPresenterTests {

    var railInboundPresenter by Delegates.notNull<RailInboundPresenter>()

    @Before
    fun before() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        railInboundPresenter = LayoutInflater.from(activity).inflate(R.layout.test_rail_inbound_presenter, null) as RailInboundPresenter
    }

    @Test
    fun testLegalBannerClick() {
        val testSubscriber = TestObserver<Unit>()
        railInboundPresenter.legalBannerClicked.subscribe(testSubscriber)

        railInboundPresenter.legalBanner.performClick()
        testSubscriber.assertValueCount(1)
    }
}
