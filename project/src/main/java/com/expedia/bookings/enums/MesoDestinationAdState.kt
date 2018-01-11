package com.expedia.bookings.enums

import android.support.annotation.StringRes
import com.expedia.bookings.R
import com.expedia.bookings.utils.Constants

// Mock data for smoke test only
enum class MesoDestinationAdState(@StringRes val titleID: Int, @StringRes val subtitleID: Int, val webviewUrl: String, val backgroundUrl: String) {
    LAS_VEGAS(R.string.meso_destination_name_las_vegas, R.string.meso_destination_description_las_vegas, Constants.MESO_LAS_VEGAS_WEBVIEW_URL, Constants.MESO_LAS_VEGAS_BG_URL),
    LOS_ANGELES(R.string.meso_destination_name_los_angeles, R.string.meso_destination_description_los_angeles, Constants.MESO_LOS_ANGELES_WEBVIEW_URL, Constants.MESO_LOS_ANGELES_BG_URL),
    MIAMI(R.string.meso_destination_name_miami, R.string.meso_destination_description_miami, Constants.MESO_MIAMI_WEBVIEW_URL, Constants.MESO_MIAMI_BG_URL),
    CANCUN(R.string.meso_destination_name_cancun, R.string.meso_destination_description_cancun, Constants.MESO_CANCUN_WEBVIEW_URL, Constants.MESO_CANCUN_BG_URL),
    SAN_DIEGO(R.string.meso_destination_name_san_diego, R.string.meso_destination_description_san_diego, Constants.MESO_SAN_DIEGO_WEBVIEW_URL, Constants.MESO_SAN_DIEGO_BG_URL)
}
