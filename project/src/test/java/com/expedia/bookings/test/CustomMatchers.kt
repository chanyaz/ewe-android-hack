package com.expedia.bookings.test

import org.hamcrest.BaseMatcher
import org.hamcrest.Description


class CustomMatchers {
    companion object {
        @JvmStatic
        fun matchesPattern(regex: String): RegexMatcher {
            return RegexMatcher(regex)
        }
    }

    class RegexMatcher(private val regex: String) : BaseMatcher<String>() {
        override fun matches(o: Any): Boolean {
            return (o as String).matches(regex.toRegex())

        }

        override fun describeTo(description: Description) {
            description.appendText("matches regex /" + regex + "/")
        }
    }
}