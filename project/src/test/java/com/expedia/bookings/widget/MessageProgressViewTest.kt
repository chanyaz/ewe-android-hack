package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class MessageProgressViewTest {

    private val context = RuntimeEnvironment.application
    private lateinit var messageProgressView: TestMessageProgressView

    @Before
    fun setUp() {
        messageProgressView = TestMessageProgressView(context, null)
    }

    @Test
    fun testDontAnimateProgressBeforeOnSizeChanged() {
        // https://eiwork.mingle.thoughtworks.com/projects/ebapp/cards/3149
        messageProgressView.progress = 1.0f

        assertFalse(messageProgressView.drawLine1)
        assertFalse(messageProgressView.drawLine2)
        assertFalse(messageProgressView.drawLine3)
    }

    @Test
    fun testAnimateProgressAfterOnSizeChanged() {
        messageProgressView.progress = 1.0f

        assertFalse(messageProgressView.drawLine1)
        assertFalse(messageProgressView.drawLine2)
        assertFalse(messageProgressView.drawLine3)

        messageProgressView.forceOnSizeChanged()

        messageProgressView.progress = 1.0f

        assertTrue(messageProgressView.drawLine1)
        assertTrue(messageProgressView.drawLine2)
        assertTrue(messageProgressView.drawLine3)
    }

    private class TestMessageProgressView(context: Context, attrs: AttributeSet?) : MessageProgressView(context, attrs) {
        fun forceOnSizeChanged() {
            super.onSizeChanged(10, 10, 10, 10)
        }
    }
}
