package com.expedia.bookings.unit.hotels

import com.expedia.bookings.data.hotels.ErrorInfo
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class ErrorInfoTest {

    private lateinit var errorInfo: ErrorInfo

    @Before
    fun before() {
        errorInfo = NewHotelSearchResponseTestUtils.createErrorInfo()
    }

    @Test
    fun testConvertToApiErrorMessage() {
        val apiError = errorInfo.convertToApiError()
        assertEquals("message0", apiError.apiErrorMessage)
    }

    @Test
    fun testConvertToApiErrorLocalizedMessage() {
        errorInfo.message = ""
        val apiError = errorInfo.convertToApiError()
        assertEquals("localizedMessage0", apiError.apiErrorMessage)
    }

    @Test
    fun testConvertToApiErrorNoMessageNoLocalizedMessage() {
        errorInfo.message = ""
        errorInfo.localizedMessage = ""
        val apiError = errorInfo.convertToApiError()
        assertEquals("", apiError.apiErrorMessage)
    }

    @Test
    fun testConvertToApiErrorErrors() {
        val apiError = errorInfo.convertToApiError()
        assertEquals("[errors1, errors2]", apiError.errorInfo.summary)
    }

    @Test
    fun testConvertToApiErrorEmptyErrors() {
        errorInfo.errors = emptyList()
        var apiError = errorInfo.convertToApiError()
        assertEquals("[]", apiError.errorInfo.summary)
    }
}
