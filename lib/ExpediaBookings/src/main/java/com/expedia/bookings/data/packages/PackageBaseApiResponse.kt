package com.expedia.bookings.data.packages

import com.expedia.bookings.data.multiitem.PackageErrorDetails

open class PackageBaseApiResponse {
    var errors: List<PackageApiError.Code?> = emptyList()

    fun hasErrors(): Boolean {
        return errors.isNotEmpty()
    }

    val firstError: PackageErrorDetails.PackageAPIErrorDetails
        get() {
            if (!hasErrors()) {
                throw RuntimeException("No errors to get!")
            }

            val error = errors[0] ?: PackageApiError.Code.pkg_error_code_not_mapped
            return PackageErrorDetails.PackageAPIErrorDetails(error.name, error)
        }
}
