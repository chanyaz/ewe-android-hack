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

    val APP_DOWNLOAD_BANNER_ID = "mobile-app-banner-wrapper"
    val isWebViewImprovementABTestEnabled = AbacusFeatureConfigManager.isUserBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsBaggageWebViewHideAd)

    private val progressDialog = ProgressDialog(context)

    override fun onFinishInflate() {
        super.onFinishInflate()
        toolbar.setTitle(R.string.package_flight_overview_baggage_fees)
    }

    override fun onWebPageStarted(view: WebView, url: String, favicon: Bitmap?) {
        super.onWebPageStarted(view, url, favicon)
        if (isWebViewImprovementABTestEnabled && !progressDialog.isShowing) {
            progressDialog.show()
            progressDialog.setCancelable(false)
            progressDialog.setContentView(R.layout.process_dialog_layout)
        }
    }

    override fun onWebViewLoadResource(url: String) {
        super.onWebViewLoadResource(url)
        if (isWebViewImprovementABTestEnabled) {
            preventLoadingOfDivId(APP_DOWNLOAD_BANNER_ID)
        }
    }

    override fun onPageFinished(url: String) {
        super.onPageFinished(url)
        if ( progressDialog.isShowing) {
            progressDialog.dismiss()
        }
    }
}
