package com.expedia.bookings.vm

import android.content.Context
import com.expedia.bookings.utils.shouldShowTravelocityGnomeArModule

class ArGnomeBannerViewModel(context: Context) {
    val shouldShowGnomeAnimations = shouldShowTravelocityGnomeArModule(context)
}
