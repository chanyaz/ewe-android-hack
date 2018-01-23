package com.expedia.bookings.presenter.rail

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.rail.presenter.RailDetailsPresenter
import com.expedia.bookings.rail.presenter.RailInboundPresenter
import com.expedia.bookings.rail.presenter.RailOutboundPresenter
import com.expedia.bookings.rail.presenter.RailPresenter
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.rail.widget.RailSearchLegalInfoWebView
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import kotlin.properties.Delegates
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(RobolectricRunner::class)
@Config(sdk = intArrayOf(21))
class RailPresenterTests {

    var railPresenter by Delegates.notNull<RailPresenter>()
    @Before
    fun before() {
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.Theme_Rail)
        Ui.getApplication(activity).defaultRailComponents()
        Ui.getApplication(activity).defaultTravelerComponent()
        railPresenter = LayoutInflater.from(activity).inflate(R.layout.test_rail_presenter, null) as RailPresenter
    }

    //@Test
    fun testLegalInfoOnOutbound() {
        assertNotNull(railPresenter)
        railPresenter.show(railPresenter.outboundPresenter, Presenter.TEST_FLAG_FORCE_NEW_STATE)
        assertEquals(RailOutboundPresenter::class.java.name, railPresenter.currentState)
        railPresenter.outboundPresenter.legalBanner.performClick()
        assertEquals(RailSearchLegalInfoWebView::class.java.name, railPresenter.currentState)
    }

    //@Test
    fun testLegalInfoOnInbound() {
        assertNotNull(railPresenter)
        railPresenter.show(railPresenter.outboundPresenter, Presenter.TEST_FLAG_FORCE_NEW_STATE)
        assertEquals(RailOutboundPresenter::class.java.name, railPresenter.currentState)
        railPresenter.show(railPresenter.outboundDetailsPresenter, Presenter.TEST_FLAG_FORCE_NEW_STATE)
        assertEquals(RailDetailsPresenter::class.java.name, railPresenter.currentState)
        railPresenter.show(railPresenter.inboundPresenter, Presenter.TEST_FLAG_FORCE_NEW_STATE)
        assertEquals(RailInboundPresenter::class.java.name, railPresenter.currentState)
        railPresenter.inboundPresenter.legalBanner.performClick()
        assertEquals(RailSearchLegalInfoWebView::class.java.name, railPresenter.currentState)
    }
}
