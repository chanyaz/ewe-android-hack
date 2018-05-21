package com.expedia.bookings.itin.utils

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.utils.ClipboardUtils
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.shadows.ShadowToast
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class ToasterTest {
    private lateinit var context: Context
    private lateinit var sut: Toaster

    @Before
    fun setup() {
        context = RuntimeEnvironment.application
        sut = Toaster(context)
    }

    @Test
    fun testEmptyString() {
        assertFalse(ClipboardUtils.hasText(context))
        assertNull(ShadowToast.getTextOfLatestToast())
        sut.toastAndCopy("")
        assertNull(ShadowToast.getTextOfLatestToast())
        assertFalse(ClipboardUtils.hasText(context))
    }

    @Test
    fun happyToastTest() {
        val expectedTest = "mmmm toasty"
        assertFalse(ClipboardUtils.hasText(context))
        assertNull(ShadowToast.getTextOfLatestToast())
        sut.toastAndCopy("mmmm toasty")
        assertEquals(context.getString(R.string.toast_copied_to_clipboard), ShadowToast.getTextOfLatestToast())
        assertEquals(expectedTest, ClipboardUtils.getText(context))
    }
}
