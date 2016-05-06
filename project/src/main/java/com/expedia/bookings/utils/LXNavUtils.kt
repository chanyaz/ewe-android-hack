package com.expedia.bookings.utils

import com.expedia.bookings.data.cars.ApiError
import com.expedia.bookings.data.lx.SearchType
import com.expedia.bookings.otto.Events
import com.expedia.bookings.tracking.OmnitureTracking

class LXNavUtils {
    companion object {
        /**
         * If searchType is default take the user to the search form, Default ApiError is SUGGESTIONS_NO_RESULTS
         * If searchType is explicit show user the appropriate error message, Bucket all other errors as
         * UNKNOWN_ERROR to give some feedback to the user.
         */
        @JvmStatic fun handleLXSearchFailure(e: Throwable?, searchType: SearchType, isGroundTransport: Boolean) {
            var apiError: ApiError = ApiError()
            if (searchType == SearchType.DEFAULT_SEARCH) {
                apiError = if (e == null || e !is ApiError) ApiError(ApiError.Code.SUGGESTIONS_NO_RESULTS) else e
                Events.post(Events.LXShowSearchWidget())
            } else if (searchType == SearchType.EXPLICIT_SEARCH) {
                if (e != null && e is ApiError) {
                    apiError = e
                }
                Events.post(Events.LXShowSearchError(apiError, searchType))
            }
            OmnitureTracking.trackAppLXNoSearchResults(apiError, isGroundTransport)
        }
    }
}