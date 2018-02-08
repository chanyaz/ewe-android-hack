package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.utils.WebViewUtils
import com.expedia.bookings.widget.shared.BaseWebViewWidget
import com.mobiata.android.util.AndroidUtils

class BaggageFeeInfoWebView(context: Context, attrs: AttributeSet) : BaseWebViewWidget(context, attrs) {

    private val isWebViewImprovementABTestEnabled = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsBaggageWebViewHideAd)

    override fun onFinishInflate() {
        super.onFinishInflate()
        toolbar.setTitle(R.string.package_flight_overview_baggage_fees)
        if (isWebViewImprovementABTestEnabled) {
            setUserAgentString(AndroidUtils.isTablet(context))
        }
    }

    private fun setUserAgentString(isTabletDevice: Boolean) {
        val userAgentString = WebViewUtils.userAgentString
        webView.settings.userAgentString = WebViewUtils.generateUserAgentStringWithDeviceType(userAgentString, isTabletDevice)
    }
}
