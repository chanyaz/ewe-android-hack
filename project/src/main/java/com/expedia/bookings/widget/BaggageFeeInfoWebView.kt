package com.expedia.bookings.widget

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.webkit.WebView
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.widget.shared.BaseWebViewWidget

class BaggageFeeInfoWebView(context: Context, attrs: AttributeSet) : BaseWebViewWidget(context, attrs) {

    private val APP_DOWNLOAD_BANNER_ID = "mobile-app-banner-wrapper"
    private val isWebViewImprovementABTestEnabled = AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsBaggageWebViewHideAd)

    override fun onFinishInflate() {
        super.onFinishInflate()
        toolbar.setTitle(R.string.package_flight_overview_baggage_fees)
    }

    override fun onWebViewLoadResource(url: String) {
        super.onWebViewLoadResource(url)
        if (isWebViewImprovementABTestEnabled) {
            preventLoadingOfDivId(APP_DOWNLOAD_BANNER_ID)
        }
    }
}
