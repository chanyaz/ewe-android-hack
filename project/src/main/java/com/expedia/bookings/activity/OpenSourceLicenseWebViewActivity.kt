package com.expedia.bookings.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.expedia.bookings.fragment.OpenSourceLicenseWebViewFragment
import com.expedia.bookings.fragment.WebViewFragment

class OpenSourceLicenseWebViewActivity : WebViewActivity() {

    companion object {
        @JvmStatic
        fun createIntent(context: Context): Intent {
            val intent = Intent(context, OpenSourceLicenseWebViewActivity::class.java)
            // provide empty extras as WebViewActivity requires non-null extras
            intent.putExtras(Bundle())
            return intent
        }
    }

    override fun createWebViewFragment(
        extras: Bundle?,
        enableLogin: Boolean,
        injectExpediaCookies: Boolean,
        allowMobileRedirects: Boolean,
        name: String?,
        handleBack: Boolean,
        retryOnError: Boolean,
        enableDomStorage: Boolean
    ): WebViewFragment {
        return OpenSourceLicenseWebViewFragment.newInstance()
    }
}
