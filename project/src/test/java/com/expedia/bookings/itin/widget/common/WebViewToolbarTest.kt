package com.expedia.bookings.itin.widget.common

import android.content.Context
import android.view.LayoutInflater
import com.expedia.bookings.R
import com.expedia.bookings.itin.common.WebViewToolbar
import com.expedia.bookings.itin.common.WebViewToolbarViewModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertEquals
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class WebViewToolbarTest {

    lateinit var context: Context
    lateinit var sut: WebViewToolbar

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        sut = LayoutInflater.from(context).inflate(R.layout.test_webview_toolbar, null) as WebViewToolbar
        sut.viewModel = WebViewToolbarViewModel()
    }

    @Test
    fun testTitle() {
        sut.viewModel.toolbarTitleSubject.onNext("Las Vegas")
        assertEquals("Las Vegas", sut.toolbarTitleText.text.toString())
    }

    @Test
    fun testNavigationBackPressed() {
        sut.navigationIcon = null
        sut.navigationContentDescription = null
        assertNull(sut.navigationIcon)
        assertNull(sut.navigationContentDescription)
        sut.setNavigation()
        assertEquals(context.getDrawable(R.drawable.ic_close_white_24dp), sut.navigationIcon)
        assertEquals("Close", sut.navigationContentDescription)
    }
}
