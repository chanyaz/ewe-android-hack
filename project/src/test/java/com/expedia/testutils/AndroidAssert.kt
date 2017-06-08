package com.expedia.testutils

import android.view.View
import com.expedia.bookings.widget.TextView
import kotlin.test.assertEquals

class AndroidAssert {
    companion object {
        fun assertVisible(view: View) {
            assertEquals(View.VISIBLE, view.visibility)
        }

        fun assertGone(view: View) {
            assertEquals(View.GONE, view.visibility)
        }

        fun assertViewTextEquals(expectedText: String, view: TextView) {
            assertEquals(expectedText, view.text)
        }

        fun assertViewContDescEquals(expectedText: String, view: View) {
            assertEquals(expectedText, view.contentDescription)
        }
    }
}