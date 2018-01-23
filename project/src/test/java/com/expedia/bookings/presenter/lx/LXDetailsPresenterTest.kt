package com.expedia.bookings.presenter.lx

import android.app.Activity
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.LXState
import com.expedia.bookings.data.lx.LxSearchParams
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class LXDetailsPresenterTest {

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA, MultiBrand.ORBITZ, MultiBrand.CHEAPTICKETS, MultiBrand.TRAVELOCITY,
            MultiBrand.AIRASIAGO, MultiBrand.VOYAGES, MultiBrand.WOTIF, MultiBrand.LASTMINUTE, MultiBrand.EBOOKERS))
    fun testSetToolbarTitles() {
        Ui.getApplication(RuntimeEnvironment.application).defaultLXComponents()
        val activity = Robolectric.buildActivity(Activity::class.java).create().get()
        activity.setTheme(R.style.V2_Theme_LX)
        val lxDetailsPresenter = LayoutInflater.from(activity).inflate(R.layout.lx_details_presenter, null) as LXDetailsPresenter

        val startDate = LocalDate(2016, 4, 23)
        val endDate = LocalDate(2016, 4, 23).plusDays(2)

        val searchParams = LxSearchParams.Builder().location("San Fransisco").startDate(startDate).endDate(endDate).build() as LxSearchParams

        lxDetailsPresenter.lxState = LXState()
        lxDetailsPresenter.lxState.searchParams = searchParams
        lxDetailsPresenter.setToolbarTitles("San Fransisco")

        assertEquals(lxDetailsPresenter.toolBarDetailText.text, "San Fransisco")
        assertEquals(lxDetailsPresenter.toolBarSubtitleText.text, "Apr 23 - Apr 25")
        assertEquals(lxDetailsPresenter.toolBarSubtitleText.contentDescription, "Apr 23 to Apr 25")
    }
}
