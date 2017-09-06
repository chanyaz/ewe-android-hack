package com.expedia.bookings.data.user

import android.app.Activity
import com.mobiata.android.util.AndroidUtils

open class RestrictedProfileSource(val activity: Activity) {
    open fun isRestrictedProfile(): Boolean = AndroidUtils.isRestrictedProfile(activity)
}
