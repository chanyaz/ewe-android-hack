package com.expedia.bookings.presenter.lx

import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.data.LXState
import com.expedia.bookings.data.lx.LXSearchParams
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.Ui
import org.joda.time.LocalDate
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class LXDetailsPresenterTest {

    @Test
    fun testSetToolbarTitles() {
        Ui.getApplication(RuntimeEnvironment.application).defaultLXComponents()
        val lxDetailsPresenter = LayoutInflater.from(RuntimeEnvironment.application).inflate(R.layout.lx_details_presenter, null) as LXDetailsPresenter

        val searchParams = LXSearchParams()
        searchParams.startDate = LocalDate(2016, 4, 23)
        searchParams.endDate = LocalDate(2016, 4, 23).plusDays(2)

        lxDetailsPresenter.lxState = LXState()
        lxDetailsPresenter.lxState.searchParams = searchParams;
        lxDetailsPresenter.setToolbarTitles("San Fransisco")

        assertEquals(lxDetailsPresenter.toolBarDetailText.text, "San Fransisco")
        assertEquals(lxDetailsPresenter.toolBarSubtitleText.text, "Apr 23 - Apr 25")
        assertEquals(lxDetailsPresenter.toolBarSubtitleText.contentDescription, "Apr 23 to Apr 25")
    }
}