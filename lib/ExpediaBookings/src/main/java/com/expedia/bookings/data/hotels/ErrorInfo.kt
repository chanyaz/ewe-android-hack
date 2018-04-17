package com.expedia.bookings.data.hotels

import com.expedia.bookings.data.ApiError

class ErrorInfo {
    var message: String = ""
    var localizedMessage: String = ""
    var errors: List<String> = emptyList()

    fun convertToApiError(): ApiError {
        val apiError = ApiError()
        apiError.setMessage(if (message.isNotBlank()) message else localizedMessage)
        val errorInfo = ApiError.ErrorInfo()
        errorInfo.summary = errors.toString()
        apiError.errorInfo = errorInfo
        return apiError
    }
}
