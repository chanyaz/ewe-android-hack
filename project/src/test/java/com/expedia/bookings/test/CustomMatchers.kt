package com.expedia.bookings.test

import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers

class CustomMatchers {
    companion object {
        @JvmStatic
        fun matchesPattern(regex: String): RegexMatcher = RegexMatcher(regex)

        @JvmStatic
        fun <K, V> hasEntries(data: Map<K, V>): Matcher<Map<K, V>> =
                Matchers.allOf(data.map { Matchers.hasEntry(it.key, it.value) })
    }

    class RegexMatcher(private val regex: String) : BaseMatcher<String>() {
        override fun matches(o: Any): Boolean = (o as String).matches(regex.toRegex())

        override fun describeTo(description: Description) {
            description.appendText("matches regex /$regex/")
        }
    }
}
