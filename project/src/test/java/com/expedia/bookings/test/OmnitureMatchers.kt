package com.expedia.bookings.test

import org.hamcrest.Matcher
import org.hamcrest.Matchers

class OmnitureMatchers {
    companion object {
        @JvmStatic
        fun withEvars(data: Map<Int, String>): Matcher<Map<String, Any>> =
                CustomMatchers.hasEntries(data.map { Pair("&&v" + it.key, it.value) }.toMap())

        @JvmStatic
        fun withoutEvars(vararg evars: Int): Matcher<Map<String, Any>> =
                Matchers.allOf(evars.map { Matchers.not(Matchers.hasKey("&&v" + it)) })

        @JvmStatic
        fun withEventsString(events: String): Matcher<Map<String, Any>> =
                Matchers.hasEntry("&&events", events) as Matcher<Map<String, Any>>
    }
}