package com.expedia.bookings.test

import org.hamcrest.Matcher
import org.mockito.hamcrest.MockitoHamcrest

/**
 * The default implementations in MockitoHamcrest return null for non-primitive types, which breaks
 * Kotlin code that expects non-null parameters. The methods here provide non-null return values in
 * order to get around this problem.
 */
object NullSafeMockitoHamcrest {
    fun mapThat(matcher: Matcher<Map<String, Any>>): Map<String, Any> {
        MockitoHamcrest.argThat(matcher)
        return emptyMap()
    }
}