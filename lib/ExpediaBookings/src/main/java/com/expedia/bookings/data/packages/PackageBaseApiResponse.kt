package com.expedia.bookings.data.packages

open class PackageBaseApiResponse {
    var errors: List<PackageApiError.Code> = emptyList()

    fun hasErrors(): Boolean {
        return errors.isNotEmpty()
    }

    val firstError: PackageApiError.Code
        get() {
            if (!hasErrors()) {
                throw RuntimeException("No errors to get!")
            }
            return errors[0]
        }

}
