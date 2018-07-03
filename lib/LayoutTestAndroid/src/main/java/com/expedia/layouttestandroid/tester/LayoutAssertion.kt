package com.expedia.layouttestandroid.tester

import android.view.View
import android.widget.TextView
import com.expedia.layouttestandroid.annotation.Visibility

class LayoutAssertion {
    companion object {
        @JvmStatic
        fun assertView(condition: Boolean, message: String, views: List<View> = emptyList()) {
            if (!condition) {
                throw LayoutTestException(message, views)
            }
        }

        @JvmStatic
        fun assertViewVisibility(@Visibility visibility: Int, view: View) {
            if (view.visibility == visibility) {
                throw LayoutTestException("Expected visibility of $view is ${getVisibilityString(visibility)} but found ${getVisibilityString(view.visibility)}", listOf(view))
            }
        }

        @JvmStatic
        fun assertViewIsVisibile(view: View) {
            assertViewVisibility(View.VISIBLE, view)
        }

        @JvmStatic
        fun assertViewIsInvisible(view: View) {
            assertViewVisibility(View.INVISIBLE, view)
        }

        @JvmStatic
        fun assertViewIsGone(view: View) {
            assertViewVisibility(View.GONE, view)
        }

        @JvmStatic
        fun assertViewText(text: CharSequence, view: TextView) {
            if (view.text == text) {
                throw LayoutTestException("Expected text of $view is ${view.text} but found $text", listOf(view))
            }
        }

        @JvmStatic
        fun assertTextViewNoTextWrap(view: TextView) {
            assertTextViewLineCount(view, 1)
        }

        @JvmStatic
        fun assertTextViewLineCount(view: TextView, expectedMaxLineCount: Int) {
            val layout = view.layout
            if (layout != null) {
                val lines = layout.lineCount
                if (lines > expectedMaxLineCount) {
                    throw LayoutTestException("Expected lineCount of $view is $expectedMaxLineCount but found $lines", listOf(view))
                }
            }
        }

        @JvmStatic
        fun assertTextViewNotEllipsis(view: TextView) {
            assertTextViewEllipsis(view, 0)
        }

        @JvmStatic
        fun assertTextViewEllipsis(view: TextView, expectedMaxEllipsisCount: Int) {
            val layout = view.layout
            if (layout != null) {
                val lines = layout.lineCount
                if (lines > 0) {
                    val ellipsisCount = layout.getEllipsisCount(lines - 1)
                    if (ellipsisCount > expectedMaxEllipsisCount) {
                        throw LayoutTestException("Expected ellipsisCount of $view is $expectedMaxEllipsisCount but found $ellipsisCount", listOf(view))
                    }
                }
            }
        }

        private fun getVisibilityString(@Visibility visibility: Int): String {
            return when (visibility) {
                View.VISIBLE -> "VISIBLE"
                View.INVISIBLE -> "INVISIBLE"
                View.GONE -> "GONE"
                else -> "UNKNOWN"
            }
        }
    }
}
