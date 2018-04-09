package com.expedia.bookings.unit.data

import com.expedia.bookings.data.ApiError
import com.google.gson.Gson
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ApiErrorTest {
    @Test
    fun testMappedApiErrorHasBothKeyAndCode() {
        val errorKey = "UNKNOWN_ERROR"
        val apiError = Gson().fromJson(apiErrorJson(errorKey), ApiError::class.java)
        assertEquals(ApiError.Code.UNKNOWN_ERROR, apiError.errorCode)
        assertEquals(errorKey, apiError.errorKey)
    }

    @Test
    fun tesUnmappedApiErrorHasOnlyKey() {
        val errorKey = "NOT_MAPPED"
        val apiError = Gson().fromJson(apiErrorJson(errorKey), ApiError::class.java)
        assertEquals(errorKey, apiError.errorKey)
        assertNull(apiError.errorCode)
    }

    private fun apiErrorJson(key: String) = """{"errorCode": "$key","errorInfo": {"summary": "Unknown","field": ""},
                      "diagnosticId": 358317182,"diagnosticFullText": ""}"""
}
