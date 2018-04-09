package com.expedia.bookings.data.multiitem

import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.packages.PackageApiError

// TODO This needs to be removed eventually. Packages path should consider only one type of error code.
sealed class PackageErrorDetails(val errorKey: String) {
    class PackageAPIErrorDetails(val key: String, val errorCode: PackageApiError.Code) : PackageErrorDetails(key)
    class ApiErrorDetails(val key: String, val errorCode: ApiError.Code) : PackageErrorDetails(key)
}
