package com.expedia.bookings.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.expedia.bookings.data.ApiError
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.lx.LxSearchParams
import com.expedia.bookings.data.lx.SearchType
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.lob.lx.ui.activity.LXBaseActivity
import com.expedia.bookings.otto.Events
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.navigation.NavUtils

class LXNavUtils : NavUtils() {
    companion object {

        @JvmStatic fun goToActivities(context: Context, animOptions: Bundle?, expediaFlags: Int) {
            sendKillActivityBroadcast(context)
            val intent = Intent(context, LXBaseActivity::class.java)
            startActivity(context, intent, animOptions)
            finishIfFlagged(context, expediaFlags)
        }

        @JvmStatic fun goToActivities(context: Context, animOptions: Bundle?, searchParams: LxSearchParams?, expediaFlags: Int) {
            sendKillActivityBroadcast(context)
            val intent = Intent(context, LXBaseActivity::class.java)
            if (searchParams != null) {
                intent.putExtra("startDateStr", DateUtils.localDateToyyyyMMdd(searchParams.activityStartDate))
                intent.putExtra("endDateStr", DateUtils.localDateToyyyyMMdd(searchParams.activityEndDate))
                intent.putExtra("location", searchParams.location)
            }

            if (expediaFlags == FLAG_OPEN_SEARCH) {
                intent.putExtra(Codes.EXTRA_OPEN_SEARCH, true)
            }

            if (expediaFlags == FLAG_OPEN_RESULTS) {
                intent.putExtra(Codes.EXTRA_OPEN_RESULTS, true)
            }

            if (expediaFlags == FLAG_DEEPLINK) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                // If we don't have filters, open search box.
                if (Strings.isNotEmpty(searchParams!!.activityId)) {
                    intent.putExtra("activityId", searchParams.activityId)
                    intent.putExtra(Codes.FROM_DEEPLINK_TO_DETAILS, true)
                } else {
                    if (AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppLXNavigateToSRP) || !searchParams.filters.isEmpty()) {
                        intent.putExtra("filters", searchParams.filters)
                        intent.putExtra(Codes.FROM_DEEPLINK, true)
                    } else {
                        intent.putExtra(Codes.EXTRA_OPEN_SEARCH, true)
                    }
                }
            }

            startActivity(context, intent, animOptions)
            finishIfFlagged(context, expediaFlags)
        }

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
