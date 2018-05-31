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
        fun withoutProps(vararg evars: Int): Matcher<Map<String, Any>> =
                Matchers.allOf(evars.map { Matchers.not(Matchers.hasKey("&&c$it")) })

        @JvmStatic
        fun withProps(data: Map<Int, String>): Matcher<Map<String, Any>> =
                CustomMatchers.hasEntries(data.map { Pair("&&c" + it.key, it.value) }.toMap())

        @JvmStatic
        fun withCurrency(currency: String): Matcher<Map<String, Any>> =
                CustomMatchers.hasEntries(mapOf("&&cc" to currency))

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun withProductsString(products: String, shouldExactlyMatch: Boolean = true): Matcher<Map<String, Any>> =
                if (shouldExactlyMatch) Matchers.hasEntry("&&products", products) as Matcher<Map<String, Any>>
                else Matchers.hasEntry(Matchers.equalTo("&&products"), Matchers.containsString(products)) as Matcher<Map<String, Any>>

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun withEventsString(events: String): Matcher<Map<String, Any>> =
                Matchers.hasEntry("&&events", events) as Matcher<Map<String, Any>>

        @JvmStatic
        fun withAbacusTestControl(testId: Int): Matcher<Map<String, Any>> =
                withAbacusTestVariant(testId, 0)

        @JvmStatic
        fun withAbacusTestBucketed(testId: Int): Matcher<Map<String, Any>> =
                withAbacusTestVariant(testId, 1)

        @Suppress("MemberVisibilityCanPrivate")
        @JvmStatic
        fun withAbacusTestVariant(testId: Int, variant: Int): Matcher<Map<String, Any>> {
            val expectedTestString = "$testId.0.$variant"

            @Suppress("UNCHECKED_CAST")
            return Matchers.hasEntry(Matchers.equalTo("&&v34"), Matchers.containsString(expectedTestString)) as Matcher<Map<String, Any>>
        }
    }
}
