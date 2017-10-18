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
        fun withProps(data: Map<Int, String>): Matcher<Map<String, Any>> =
                CustomMatchers.hasEntries(data.map { Pair("&&c" + it.key, it.value) }.toMap())

        @JvmStatic
        fun withProductsString(products: String): Matcher<Map<String, Any>> =
                Matchers.hasEntry("&&products", products) as Matcher<Map<String, Any>>

        @JvmStatic
        fun withEventsString(events: String): Matcher<Map<String, Any>> =
                Matchers.hasEntry("&&events", events) as Matcher<Map<String, Any>>

        @JvmStatic
        fun withAbacusTestControl(testId: Int): Matcher<Map<String, Any>> =
                withAbacusTestVariant(testId, 0)

        @JvmStatic
        fun withAbacusTestBucketed(testId: Int): Matcher<Map<String, Any>> =
                withAbacusTestVariant(testId, 1)

        @JvmStatic
        fun withAbacusTestVariant(testId: Int, variant: Int): Matcher<Map<String, Any>> {
            val expectedTestString = "$testId.0.$variant"
            return Matchers.hasEntry(Matchers.equalTo("&&v34"), Matchers.containsString(expectedTestString)) as Matcher<Map<String, Any>>
        }
    }
}